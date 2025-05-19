package cn.rongcloud.im.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.util.List;

/**
 * [从本地选择图片以及拍照工具类，完美适配2.0-5.0版本]
 *
 * @author huxinwu
 * @version 1.0
 * @date 2015-1-7
 */
public class PhotoUtils {

    private final String tag = PhotoUtils.class.getSimpleName();

    /** 裁剪图片成功后返回 */
    public static final int INTENT_CROP = 2;

    /** 拍照成功后返回 */
    public static final int INTENT_TAKE = 3;

    /** 拍照成功后返回 */
    public static final int INTENT_SELECT = 4;

    public static final String CROP_FILE_NAME = "crop_file.jpg";

    // 不需要裁剪图片
    public static final int NO_CROP = 0x1772;
    private int mType;
    // R以及以上版本通过MediaStore保存Uri来处理裁剪后路径
    private static Uri lastCropUriForR;

    /** PhotoUtils对象 */
    private OnPhotoResultListener onPhotoResultListener;

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener, int type) {
        this.onPhotoResultListener = onPhotoResultListener;
        mType = type;
    }

    /**
     * 拍照
     *
     * @param
     * @return
     */
    public void takePicture(Activity activity) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = buildUri(activity);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // 添加必要的权限标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_TAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     *
     * @param
     * @return
     */
    public void takePicture(Fragment fragment) {
        try {
            Activity activity = fragment.getActivity();
            if (activity == null) {
                return;
            }

            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = buildUri(activity);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // 添加必要的权限标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            fragment.startActivityForResult(intent, INTENT_TAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择一张图片 图片类型，这里是image/*，当然也可以设置限制 如：image/jpeg等
     *
     * @param activity Activity
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Activity activity) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            // 添加必要的权限标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_SELECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择一张图片 图片类型，这里是image/*，当然也可以设置限制 如：image/jpeg等
     *
     * @param fragment Fragment
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Fragment fragment) {
        try {
            Activity activity = fragment.getActivity();
            if (activity == null) {
                return;
            }

            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            // 添加必要的权限标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            fragment.startActivityForResult(intent, INTENT_SELECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建uri
     *
     * @param activity
     * @return
     */
    private Uri buildUri(Activity activity) {
        // 统一为所有 Android 版本创建应用专属目录下的文件
        File pictureDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (pictureDir == null) {
            // 创建失败时尝试使用内部存储
            pictureDir = new File(activity.getFilesDir(), "Pictures");
            if (!pictureDir.exists()) {
                pictureDir.mkdirs();
            }
        }

        File file = new File(pictureDir, CROP_FILE_NAME);

        // 根据不同系统版本返回适当的 Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0 及以上版本需要使用 FileProvider
            return FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName()
                            + activity.getResources()
                                    .getString(io.rong.imkit.R.string.rc_authorities_fileprovider),
                    file);
        } else {
            // Android 7.0 以下版本可以直接使用 file://
            return Uri.fromFile(file);
        }
    }

    /**
     * 构建本地文件uri，保持与 buildUri 一致
     *
     * @return
     */
    private Uri buildLocalFileUri(Activity activity) {
        // 使用与 buildUri 相同的逻辑
        return buildUri(activity);
    }

    /**
     * @param intent
     * @return
     */
    protected boolean isIntentAvailable(Activity activity, Intent intent) {
        if (activity == null || intent == null) {
            return false;
        }
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    // 检查裁剪功能是否可用
    private boolean isCropAvailable(Activity activity, Uri uri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            return intent.resolveActivity(activity.getPackageManager()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    // 检查是否为谷歌 Pixel 设备
    private boolean isGooglePixelDevice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        return (manufacturer.contains("google") && model.contains("pixel"))
                || model.contains("google");
    }

    private boolean corp(Activity activity, Uri uri) {
        if (activity == null) {
            return false;
        }

        // 谷歌手机上直接跳过裁剪
        if (isGooglePixelDevice()
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && !isCropAvailable(activity, uri)) {
            // 直接返回原图
            Log.i(tag, "Google Pixel device detected or crop not available, skipping crop");
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }

        Intent cropIntent = buildCorpIntent(activity, uri);
        if (!isIntentAvailable(activity, cropIntent)) {
            // 裁剪不可用，直接返回原图
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }
        try {
            activity.startActivityForResult(cropIntent, INTENT_CROP);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常时尝试直接返回原图
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }
    }

    private boolean corp(Fragment fragment, Uri uri) {
        if (fragment.getActivity() == null) {
            return false;
        }

        Activity activity = fragment.getActivity();
        // 谷歌手机上直接跳过裁剪
        if (isGooglePixelDevice()
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && !isCropAvailable(activity, uri)) {
            // 直接返回原图
            Log.i(tag, "Google Pixel device detected or crop not available, skipping crop");
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }

        Intent cropIntent = buildCorpIntent(activity, uri);
        if (!isIntentAvailable(activity, cropIntent)) {
            // 裁剪不可用，直接返回原图
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }
        try {
            fragment.startActivityForResult(cropIntent, INTENT_CROP);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常时尝试直接返回原图
            onPhotoResultListener.onPhotoResult(uri);
            return true;
        }
    }

    private Intent buildCorpIntent(Activity activity, Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");

        // 添加必要的权限标志，解决高版本 Android 系统上的问题
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 200);
        cropIntent.putExtra("outputY", 200);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        // 创建临时文件用于保存裁剪后的图片
        File outputFile =
                new File(
                        activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "crop_temp_" + System.currentTimeMillis() + ".jpg");
        Uri outputUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outputUri =
                    FileProvider.getUriForFile(
                            activity,
                            activity.getPackageName()
                                    + activity.getResources()
                                            .getString(
                                                    io.rong.imkit.R.string
                                                            .rc_authorities_fileprovider),
                            outputFile);
            lastCropUriForR = outputUri;
        } else {
            outputUri = Uri.fromFile(outputFile);
        }

        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        return cropIntent;
    }

    private Uri buildCropUri(Context context) {
        if (context == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lastCropUriForR != null) {
                return lastCropUriForR;
            }
            return null;
        } else {
            File cropFile =
                    new File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            CROP_FILE_NAME);
            if (cropFile.exists()) {
                return Uri.fromFile(cropFile);
            }
            return null;
        }
    }

    private String getCropFilePath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + File.separator
                + CROP_FILE_NAME;
    }

    // R以及以上版本，先创建文件再插入到MediaStore
    private Uri createCropImageUriForR(Context context) {
        if (context == null) {
            return null;
        }
        try {
            File imgFile = new File(getCropFilePath(context));
            // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, CROP_FILE_NAME);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            return context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            return null;
        }
    }

    private Uri createCropImageUri(Context context) {
        return context != null ? Uri.fromFile(new File(getCropFilePath(context))) : null;
    }

    // R以及以上版本通过MediaStore查询裁剪后的文件
    private File queryCropFileForR(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(columnIndex);
                return new File(path);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /** 确保URI是有效的图片URI，如果不是，尝试修复 */
    private Uri ensureValidImageUri(Context context, Uri uri) {
        if (uri == null) return null;

        try {
            // 检查URI是否可以访问
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                Log.w(tag, "Invalid mime type for URI: " + uri + ", mime: " + mimeType);
                return copySelectedImageToAppStorage(context, uri); // 尝试复制来修复
            }

            // 测试是否可以打开流
            try (java.io.InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is == null) {
                    Log.w(tag, "Cannot open stream for URI: " + uri);
                    return copySelectedImageToAppStorage(context, uri); // 尝试复制来修复
                }
            }

            // 对于 content:// URI，始终复制到应用私有空间
            if ("content".equals(uri.getScheme())) {
                return copySelectedImageToAppStorage(context, uri);
            }

            return uri; // URI 是有效的
        } catch (Exception e) {
            Log.e(tag, "Error validating URI: " + uri, e);
            // 出错时尝试复制到应用私有目录
            return copySelectedImageToAppStorage(context, uri);
        }
    }

    /**
     * 返回结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (onPhotoResultListener == null) {
            Log.e(tag, "onPhotoResultListener is not null");
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
            switch (requestCode) {
                    // 拍照
                case INTENT_TAKE:
                    Uri photoUri = buildUri(activity);
                    if (photoUri != null) {
                        // 确保URI有效
                        photoUri = ensureValidImageUri(activity, photoUri);

                        if (mType == NO_CROP || isGooglePixelDevice()) {
                            // 不需要裁剪或是谷歌设备
                            onPhotoResultListener.onPhotoResult(photoUri);
                            return;
                        }
                        // 添加权限检查
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            activity.grantUriPermission(
                                    activity.getPackageName(),
                                    photoUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        if (corp(activity, photoUri)) {
                            return;
                        }
                    }
                    onPhotoResultListener.onPhotoCancel();
                    break;
                    // 选择图片
                case INTENT_SELECT:
                    if (data != null && data.getData() != null) {
                        // 复制选择的图片到应用私有目录并确保URI有效
                        Uri sourceUri = data.getData();
                        Uri localUri = copySelectedImageToAppStorage(activity, sourceUri);
                        localUri = ensureValidImageUri(activity, localUri);

                        Log.i(tag, "Selected image URI: " + sourceUri + ", local URI: " + localUri);

                        // 不需要裁剪或是谷歌设备
                        if (mType == NO_CROP || isGooglePixelDevice()) {
                            onPhotoResultListener.onPhotoResult(localUri);
                            return;
                        }
                        // 添加权限检查
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            activity.grantUriPermission(
                                    activity.getPackageName(),
                                    localUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        if (corp(activity, localUri)) {
                            return;
                        }
                    }
                    onPhotoResultListener.onPhotoCancel();
                    break;
                    // 裁剪图片
                case INTENT_CROP:
                    Uri cropUri = buildCropUri(activity);
                    if (cropUri != null) {
                        // 确保URI有效
                        cropUri = ensureValidImageUri(activity, cropUri);
                        onPhotoResultListener.onPhotoResult(cropUri);
                    } else {
                        onPhotoResultListener.onPhotoCancel();
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(tag, "Error in onActivityResult", e);
            onPhotoResultListener.onPhotoCancel();
        }
    }

    /**
     * 返回结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        if (onPhotoResultListener == null) {
            Log.e(tag, "onPhotoResultListener is not null");
            return;
        }
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
            switch (requestCode) {
                    // 拍照
                case INTENT_TAKE:
                    Uri photoUri = buildUri(activity);
                    if (photoUri != null) {
                        // 确保URI有效
                        photoUri = ensureValidImageUri(activity, photoUri);

                        // 不需要裁剪或是谷歌设备
                        if (mType == NO_CROP || isGooglePixelDevice()) {
                            onPhotoResultListener.onPhotoResult(photoUri);
                            return;
                        }
                        // 添加权限检查
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            activity.grantUriPermission(
                                    activity.getPackageName(),
                                    photoUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        if (corp(fragment, photoUri)) {
                            return;
                        }
                    }
                    onPhotoResultListener.onPhotoCancel();
                    break;
                    // 选择图片
                case INTENT_SELECT:
                    if (data != null && data.getData() != null) {
                        // 复制选择的图片到应用私有目录并确保URI有效
                        Uri sourceUri = data.getData();
                        Uri localUri = copySelectedImageToAppStorage(activity, sourceUri);
                        localUri = ensureValidImageUri(activity, localUri);

                        Log.i(tag, "Selected image URI: " + sourceUri + ", local URI: " + localUri);

                        // 不需要裁剪或是谷歌设备
                        if (mType == NO_CROP || isGooglePixelDevice()) {
                            onPhotoResultListener.onPhotoResult(localUri);
                            return;
                        }
                        // 添加权限检查
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            activity.grantUriPermission(
                                    activity.getPackageName(),
                                    localUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        if (corp(fragment, localUri)) {
                            return;
                        }
                    }
                    onPhotoResultListener.onPhotoCancel();
                    break;
                    // 裁剪图片
                case INTENT_CROP:
                    Uri cropUri = buildCropUri(activity);
                    if (cropUri != null) {
                        // 确保URI有效
                        cropUri = ensureValidImageUri(activity, cropUri);
                        onPhotoResultListener.onPhotoResult(cropUri);
                    } else {
                        onPhotoResultListener.onPhotoCancel();
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(tag, "Error in onActivityResult", e);
            onPhotoResultListener.onPhotoCancel();
        }
    }

    /**
     * 删除文件
     *
     * @param uri
     * @return
     */
    public boolean clearCropFile(Uri uri) {
        if (uri == null) {
            return false;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                Log.i(tag, "Cached crop file cleared.");
            } else {
                Log.e(tag, "Failed to clear cached crop file.");
            }
            return result;
        } else {
            Log.w(tag, "Trying to clear cached crop file but it does not exist.");
        }

        return false;
    }

    // 处理资源清理回收等操作
    private void onCleared(Activity activity) {
        clearCropFile(buildUri(activity));
        clearCropFile(buildLocalFileUri(activity));
        clearCropFile(buildCropUri(activity));
    }

    /**
     * [回调监听类]
     *
     * @author huxinwu
     * @version 1.0
     * @date 2015-1-7
     */
    public interface OnPhotoResultListener {
        void onPhotoResult(Uri uri);

        void onPhotoCancel();
    }

    public OnPhotoResultListener getOnPhotoResultListener() {
        return onPhotoResultListener;
    }

    public void setOnPhotoResultListener(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    /** 复制选择的图片到应用私有目录，避免权限问题 */
    private Uri copySelectedImageToAppStorage(Context context, Uri sourceUri) {
        try {
            // 创建目标文件
            File pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (pictureDir == null) {
                pictureDir = new File(context.getFilesDir(), "Pictures");
                if (!pictureDir.exists()) {
                    pictureDir.mkdirs();
                }
            }

            String fileName = "selected_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(pictureDir, fileName);

            // 复制文件内容
            java.io.InputStream is = context.getContentResolver().openInputStream(sourceUri);
            if (is == null) {
                Log.e(tag, "Failed to open input stream from selected image");
                return sourceUri; // 如果失败，返回原始URI
            }

            java.io.OutputStream os = new java.io.FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            is.close();
            os.close();

            // 创建新的URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return FileProvider.getUriForFile(
                        context,
                        context.getPackageName()
                                + context.getResources()
                                        .getString(
                                                io.rong.imkit.R.string.rc_authorities_fileprovider),
                        destFile);
            } else {
                return Uri.fromFile(destFile);
            }
        } catch (Exception e) {
            Log.e(tag, "Error copying selected image: " + e.getMessage());
            e.printStackTrace();
            return sourceUri; // 如果失败，返回原始URI
        }
    }
}
