package cn.rongcloud.im.file;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UploadTokenResult;
import cn.rongcloud.im.net.proxy.RetrofitProxyServiceCreator;
import cn.rongcloud.im.net.service.AppService;
import cn.rongcloud.im.net.service.UserService;
import cn.rongcloud.im.utils.FileUtils;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import cn.rongcloud.im.utils.log.SLog;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import io.rong.message.utils.BitmapUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileManager {
    private Context context;
    private UserService userService;
    private AppService appService;

    public FileManager(Context context) {
        this.context = context.getApplicationContext();
        userService = RetrofitProxyServiceCreator.getRetrofitService(context, UserService.class);
        appService = RetrofitProxyServiceCreator.getRetrofitService(context, AppService.class);
    }

    /**
     * 保存图片至公共下载下载中
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap, String fileName) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance()
                .runOnWorkThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                String path =
                                        FileUtils.saveBitmapToPublicPictures(bitmap, fileName);
                                result.postValue(Resource.success(path));
                            }
                        });
        return result;
    }

    /**
     * 保存图片至缓存文件中
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToCache(Bitmap bitmap, String fileName) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance()
                .runOnWorkThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                String path = FileUtils.saveBitmapToCache(bitmap, fileName);
                                result.postValue(Resource.success(path));
                            }
                        });
        return result;
    }

    /**
     * 保存图片至公共下载下载中,使用时间作为文件名
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".png";
        return saveBitmapToPictures(bitmap, fileName);
    }

    /**
     * 保存图片至缓存文件中,使用时间作为文件名
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToCache(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".png";
        return saveBitmapToCache(bitmap, fileName);
    }

    /**
     * 上传图片
     *
     * @param imageUri
     * @return Resource 中 data 为上传成功后的 url
     */
    public LiveData<Resource<String>> uploadImage(Uri imageUri) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        LiveData<Resource<UploadTokenResult>> imageUploadTokenResource = getUploadToken();
        result.addSource(
                imageUploadTokenResource,
                tokenResultResource -> {
                    // 当有结果时移除数据源
                    if (tokenResultResource.status != Status.LOADING) {
                        result.removeSource(imageUploadTokenResource);
                    }

                    // 获取 token 失败时返回错误
                    if (tokenResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(tokenResultResource.code, null));
                        return;
                    }

                    if (tokenResultResource.status == Status.SUCCESS) {
                        UploadTokenResult tokenResult = tokenResultResource.data;
                        // 当获取 token 成功时上传服务器至七牛，目前没有其他云服务器所以不做类型判断
                        LiveData<Resource<String>> uploadResource =
                                uploadFileByQiNiu(imageUri, tokenResult.getToken());
                        result.addSource(
                                uploadResource,
                                uploadResultResource -> {
                                    // 当有结果时移除数据源
                                    if (uploadResultResource.status != Status.LOADING) {
                                        result.removeSource(uploadResource);
                                    }

                                    // 获取上传失败时返回错误
                                    if (uploadResultResource.status == Status.ERROR) {
                                        result.setValue(
                                                Resource.error(uploadResultResource.code, null));
                                        return;
                                    }

                                    if (uploadResultResource.status == Status.SUCCESS) {
                                        // 返回上传后结果 url
                                        String resultUrl =
                                                "http://"
                                                        + tokenResult.getDomain()
                                                        + "/"
                                                        + uploadResultResource.data;
                                        result.setValue(Resource.success(resultUrl));
                                    }
                                });
                    }
                });
        return result;
    }

    /**
     * 获取上传文件 token
     *
     * @return
     */
    private LiveData<Resource<UploadTokenResult>> getUploadToken() {
        // 请求服务器获取上传 token
        return new NetworkOnlyResource<UploadTokenResult, Result<UploadTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<UploadTokenResult>> createCall() {
                return userService.getImageUploadToken();
            }
        }.asLiveData();
    }

    /**
     * 使用七牛上传文件
     *
     * @param fileUri
     * @param uploadToken
     * @return
     */
    private LiveData<Resource<String>> uploadFileByQiNiu(Uri fileUri, String uploadToken) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        String realFilePath = saveFileToCacheDir(fileUri);
        if (TextUtils.isEmpty(realFilePath)) {
            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
            return result;
        }

        UploadManager uploadManager = new UploadManager();
        final File imageCacheFile = new File(realFilePath);
        uploadManager.put(
                imageCacheFile,
                null,
                uploadToken,
                new UpCompletionHandler() {
                    @Override
                    public void complete(
                            String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                        boolean delete = imageCacheFile.delete();
                        if (!delete) {
                            Log.e(LogTag.API, "qiniu upload success,but cannot delete cache file");
                        }
                        if (responseInfo.isOK()) {
                            try {
                                String key = (String) jsonObject.get("key");
                                result.postValue(Resource.success(key));
                            } catch (JSONException e) {
                                SLog.e(LogTag.API, "qiniu upload success,but cannot get key");
                                result.postValue(
                                        Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                            }
                        } else {
                            int statusCode = responseInfo.statusCode;
                            SLog.e(LogTag.API, "qiniu upload failed, status code:" + statusCode);
                            result.postValue(
                                    Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                        }
                    }
                },
                null);

        return result;
    }

    private String saveFileToCacheDir(Uri fileUri) {
        String path;
        if ("content".equals(fileUri.getScheme())) {
            path = getFilePathFromUri(context, fileUri);
        } else {
            path = fileUri.getPath();
        }

        if (TextUtils.isEmpty(path)) {
            return null;
        }
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        String fileName = System.currentTimeMillis() + path.substring(index);
        Context context = SealApp.getApplication();
        String cachePath =
                context.getExternalCacheDir() + File.separator + "image" + File.separator;
        boolean result =
                io.rong.common.FileUtils.copyFileToInternal(
                        SealApp.getApplication(), fileUri, cachePath, fileName);
        return result ? cachePath + fileName : null;
    }

    private String getFilePathFromUri(Context context, Uri uri) {
        // 对于 Android 10 及以上版本，_data 列已被废弃，需要采用新的方式处理
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return copyUriContentToTempFile(context, uri);
        }

        // Android 10 以下版本的处理方式
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                if (column_index >= 0) {
                    String filePath = cursor.getString(column_index);
                    return filePath;
                }
            }
        } catch (Exception e) {
            SLog.e(LogTag.API, "Error getting file path from uri: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // 如果无法获取文件路径，则复制文件到临时目录
        return copyUriContentToTempFile(context, uri);
    }

    /** 将 Uri 内容复制到临时文件 适用于 Android 10 及以上版本或无法获取真实路径的情况 */
    private String copyUriContentToTempFile(Context context, Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // 创建临时文件
            String fileName = "temp_" + System.currentTimeMillis();
            String fileExtension = getFileExtensionFromUri(context, uri);
            if (!TextUtils.isEmpty(fileExtension)) {
                fileName += "." + fileExtension;
            }

            File tempDir = new File(context.getCacheDir(), "temp_files");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File tempFile = new File(tempDir, fileName);
            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            return tempFile.getAbsolutePath();

        } catch (IOException e) {
            SLog.e(LogTag.API, "Error copying uri content to temp file: " + e.getMessage());
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                SLog.e(LogTag.API, "Error closing streams: " + e.getMessage());
            }
        }
    }

    /** 从 Uri 获取文件扩展名 */
    private String getFileExtensionFromUri(Context context, Uri uri) {
        String extension = null;

        // 首先尝试从 URI 的路径中获取扩展名
        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
                extension = path.substring(lastDotIndex + 1);
            }
        }

        // 如果无法从路径获取，尝试通过 ContentResolver 获取
        if (TextUtils.isEmpty(extension)) {
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        String displayName = cursor.getString(nameIndex);
                        if (!TextUtils.isEmpty(displayName)) {
                            int lastDotIndex = displayName.lastIndexOf('.');
                            if (lastDotIndex > 0 && lastDotIndex < displayName.length() - 1) {
                                extension = displayName.substring(lastDotIndex + 1);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                SLog.e(LogTag.API, "Error getting file extension: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return extension;
    }

    /**
     * 获取本地文件真实 uri
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromUri(Uri contentUri) {
        // 使用统一的方法来获取文件路径，兼容 Android 10 及以上版本
        return getFilePathFromUri(context, contentUri);
    }

    private static int COMPRESSED_SIZE = 1080;
    private static final String IMAGE_LOCAL_PATH = "/image/local/seal/";
    private static int MAX_ORIGINAL_IMAGE_SIZE = 500;
    private static int COMPRESSED_FULL_QUALITY = 100;
    private static int COMPRESSED_QUALITY = 70;

    public LiveData<Resource<String>> uploadCompressImage(Uri contentUri) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();

        // 添加空值检查
        if (contentUri == null) {
            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
            return result;
        }

        String localPath = "";
        Uri uri = Uri.parse(getSavePath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        String scheme = contentUri.getScheme();
        if (scheme == null) {
            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
            return result;
        }

        if (scheme.equals("file")) {
            localPath = contentUri.toString().substring(5);
        } else if (scheme.equals("content")) {
            localPath = getFilePathFromUri(context, contentUri);
            if (TextUtils.isEmpty(localPath)) {
                // 如果无法获取路径，则尝试直接使用 InputStream 处理
                try {
                    InputStream inputStream =
                            context.getContentResolver().openInputStream(contentUri);
                    if (inputStream != null) {
                        // 创建临时文件来处理图片压缩
                        String fileName = "temp_compress_" + System.currentTimeMillis() + ".jpg";
                        File tempDir = new File(context.getCacheDir(), "temp_compress");
                        if (!tempDir.exists()) {
                            tempDir.mkdirs();
                        }
                        File tempFile = new File(tempDir, fileName);

                        FileOutputStream outputStream = new FileOutputStream(tempFile);
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        inputStream.close();
                        outputStream.close();

                        localPath = tempFile.getAbsolutePath();
                    }
                } catch (IOException e) {
                    SLog.e(
                            LogTag.API,
                            "Error creating temp file for compression: " + e.getMessage());
                    return result; // 返回空结果
                }
            }
        }

        // 检查是否成功获取到文件路径
        if (TextUtils.isEmpty(localPath)) {
            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
            return result;
        }

        BitmapFactory.decodeFile(localPath, options);
        File file = new File(localPath);

        // 检查文件是否存在
        if (!file.exists()) {
            result.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
            return result;
        }

        long fileSize = file.length() / 1024;
        Bitmap bitmap = null;
        try {
            Log.e("uploadCompressImage", "localPath***" + localPath);
            bitmap =
                    BitmapUtil.getNewResizedBitmap(
                            context, Uri.parse("file://" + localPath), COMPRESSED_SIZE);
            if (bitmap != null) {
                String dir = uri.toString() + IMAGE_LOCAL_PATH;
                Log.e("uploadCompressImage", "dir***" + dir);
                file = new File(dir);
                if (!file.exists()) file.mkdirs();
                file = new File(dir + file.getName());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                boolean success;
                int quality;
                if (fileSize > MAX_ORIGINAL_IMAGE_SIZE) {
                    quality = COMPRESSED_QUALITY;
                } else {
                    quality = COMPRESSED_FULL_QUALITY;
                }
                success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                // 在部分机型调用系统压缩转换png会有异常情况，修改后先进行判断是否压缩成功，如果压缩不成功则使用png方式进行二次压缩
                if (!success) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
                }
                bos.close();
                //                model.setLocalUri(Uri.parse("file://" + dir + name));
                Log.e("uploadCompressImage", "file://" + dir + file.getName());
                if (!bitmap.isRecycled()) bitmap.recycle();
                return uploadImage(Uri.parse("file://" + dir + file.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSavePath() {
        File saveFileDirectory = SealApp.getApplication().getExternalCacheDir();
        if (saveFileDirectory == null) {
            saveFileDirectory = SealApp.getApplication().getCacheDir();
        }
        if (!saveFileDirectory.exists()) {
            saveFileDirectory.mkdirs();
        }

        return saveFileDirectory.getAbsolutePath();
    }

    /**
     * 下载文件
     *
     * @param downloadFilePath
     * @param saveFilePath
     * @return
     */
    public LiveData<Resource<String>> downloadFile(String downloadFilePath, String saveFilePath) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        appService
                .downloadFile(downloadFilePath)
                .enqueue(
                        new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(
                                    Call<ResponseBody> call, Response<ResponseBody> response) {
                                InputStream inputStream = response.body().byteStream();
                                File saveFile = new File(saveFilePath);

                                // TODO input 写进 file
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {}
                        });
        return result;
    }
}
