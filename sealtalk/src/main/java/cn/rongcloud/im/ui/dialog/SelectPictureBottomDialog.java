package cn.rongcloud.im.ui.dialog;

import static cn.rongcloud.im.ui.activity.QrCodeDisplayActivity.REQUEST_CODE_ASK_PERMISSIONS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.PhotoUtils;
import io.rong.imkit.picture.config.PictureConfig;
import io.rong.imkit.picture.permissions.PermissionChecker;
import io.rong.imkit.utils.PermissionCheckUtil;

public class SelectPictureBottomDialog extends BaseBottomDialog {

    private PhotoUtils photoUtils;
    private static OnSelectPictureListener listener;
    private int mType;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_picture, null);
        view.findViewById(R.id.btn_take_picture)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PermissionChecker.checkSelfPermission(
                                        getContext(), Manifest.permission.CAMERA)) {
                                    photoUtils.takePicture(SelectPictureBottomDialog.this);
                                } else {
                                    Activity activity = (Activity) getContext();
                                    PermissionChecker.requestPermissions(
                                            activity,
                                            new String[] {Manifest.permission.CAMERA},
                                            PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE);
                                }
                            }
                        });
        view.findViewById(R.id.btn_album)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
                                if (!checkPremission()) {
                                    return;
                                }
                                photoUtils.selectPicture(SelectPictureBottomDialog.this);
                            }
                        });
        view.findViewById(R.id.btn_cancel)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dismiss();
                            }
                        });

        photoUtils =
                new PhotoUtils(
                        new PhotoUtils.OnPhotoResultListener() {
                            @Override
                            public void onPhotoResult(Uri uri) {
                                if (listener != null) {
                                    listener.onSelectPicture(uri);
                                }
                                dismiss();
                            }

                            @Override
                            public void onPhotoCancel() {
                                dismiss();
                            }
                        },
                        mType);
        return view;
    }

    private boolean checkPremission() {
        if (Build.VERSION.SDK_INT >= 23) {
            Context context = getContext();
            if (context == null) {
                return false;
            }
            String[] permissions = PermissionCheckUtil.getMediaStoragePermissions(context);
            boolean checkPermission = PermissionCheckUtil.checkPermissions(context, permissions);
            if (!checkPermission) {
                requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(this, requestCode, resultCode, data);
                break;
        }
    }

    public static class Builder {

        public void setOnSelectPictureListener(OnSelectPictureListener l) {
            listener = l;
        }

        public SelectPictureBottomDialog build() {
            SelectPictureBottomDialog dialog = getCurrentDialog();
            return dialog;
        }

        protected SelectPictureBottomDialog getCurrentDialog() {
            return new SelectPictureBottomDialog();
        }
    }

    /** 选择图片回调结果 */
    public interface OnSelectPictureListener {
        void onSelectPicture(Uri uri);
    }

    /**
     * 设置是否需要裁剪的类型
     *
     * @param type
     */
    public void setType(int type) {
        mType = type;
    }
}
