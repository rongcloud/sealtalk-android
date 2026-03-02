package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.PhotoUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** 聊天背景设置页面 支持从相册选择和预置图片选择 */
public class ChatBackgroundSelectActivity extends TitleBaseActivity
        implements View.OnClickListener {

    private SettingItemView sivSelectFromAlbum;
    private SettingItemView sivPresetBackgrounds;
    private PhotoUtils photoUtils;
    private UserConfigCache userConfig;
    public static final int REQUEST_PRESET_BG = 0x1210;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_background_select);
        userConfig = new UserConfigCache(this);
        initView();
        initPhotoUtil();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_select_chat_bg_title);

        sivSelectFromAlbum = findViewById(R.id.siv_select_from_album);
        sivPresetBackgrounds = findViewById(R.id.siv_preset_backgrounds);

        sivSelectFromAlbum.setOnClickListener(this);
        sivPresetBackgrounds.setOnClickListener(this);
    }

    private void initPhotoUtil() {
        photoUtils =
                new PhotoUtils(
                        new PhotoUtils.OnPhotoResultListener() {
                            @Override
                            public void onPhotoResult(Uri uri) {}

                            @Override
                            public void onPhotoCancel() {}
                        },
                        PhotoUtils.NO_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PRESET_BG:
                // 从预置图片页面返回
                if (resultCode == RESULT_OK) {
                    ToastUtils.showToast(getString(R.string.seal_select_chat_bg_set_success));
                    finish();
                }
                break;
            case PhotoUtils.INTENT_SELECT:
                // 从相册中选择图片，复制到应用私有目录后应用
                if (data != null && data.getData() != null) {
                    Uri sourceUri = data.getData();

                    // 清理旧的背景图片
                    deleteOldChatBackground();

                    // 将图片复制到应用私有目录，避免权限问题
                    Uri savedUri = copyImageToAppStorage(sourceUri);
                    if (savedUri != null) {
                        userConfig.setChatbgUri(savedUri.toString());
                        ToastUtils.showToast(getString(R.string.seal_select_chat_bg_set_success));
                        finish();
                    } else {
                        ToastUtils.showToast("保存图片失败，请重试");
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.siv_select_from_album) {
            // 从相册选择
            photoUtils.selectPicture(this);
        } else if (id == R.id.siv_preset_backgrounds) {
            // 打开预置图片页面
            Intent intent = new Intent(this, ChatBackgroundListActivity.class);
            startActivityForResult(intent, REQUEST_PRESET_BG);
        }
    }

    /**
     * 将选择的图片复制到应用私有目录，避免权限问题
     *
     * @param sourceUri 原始图片URI
     * @return 复制后的文件URI，失败返回null
     */
    private Uri copyImageToAppStorage(Uri sourceUri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 创建聊天背景专用目录
            File bgDir =
                    new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ChatBackground");
            if (!bgDir.exists()) {
                bgDir.mkdirs();
            }

            // 生成文件名
            String fileName = "chat_bg_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(bgDir, fileName);

            // 复制文件内容
            inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                SLog.e("SelectChatBg", "Failed to open input stream from selected image");
                return null;
            }

            outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            SLog.d("SelectChatBg", "Image copied to: " + destFile.getAbsolutePath());

            // 返回文件URI（使用 file:// 格式，因为是应用私有目录）
            return Uri.fromFile(destFile);

        } catch (Exception e) {
            SLog.e("SelectChatBg", "Error copying image to app storage", e);
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 删除旧的聊天背景图片，避免占用过多存储空间 */
    private void deleteOldChatBackground() {
        try {
            String oldBgUri = userConfig.getChatbgUri();
            if (oldBgUri != null && oldBgUri.startsWith("file://")) {
                // 只删除我们保存在应用目录中的文件
                Uri uri = Uri.parse(oldBgUri);
                File oldFile = new File(uri.getPath());
                if (oldFile.exists() && oldFile.getAbsolutePath().contains("ChatBackground")) {
                    boolean deleted = oldFile.delete();
                    SLog.d(
                            "SelectChatBg",
                            "Old background deleted: "
                                    + deleted
                                    + ", path: "
                                    + oldFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            SLog.e("SelectChatBg", "Error deleting old chat background", e);
            e.printStackTrace();
        }
    }
}
