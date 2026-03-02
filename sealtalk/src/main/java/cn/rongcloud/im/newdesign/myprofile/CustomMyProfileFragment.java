package cn.rongcloud.im.newdesign.myprofile;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import io.rong.imkit.picture.config.PictureConfig;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileFragment;
import java.lang.ref.WeakReference;

/**
 * 自定义我的资料页面
 *
 * <p>优化点：
 *
 * <ul>
 *   <li>1. 继承 IMKit 的 MyProfileFragment，保留原有功能
 *   <li>2. 使用单独的七牛上传逻辑
 *   <li>3. 使用 updateMyUserProfileExamine 更新用户信息
 *   <li>4. 使用自定义 ViewModel 扩展头像上传功能
 * </ul>
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class CustomMyProfileFragment extends MyProfileFragment {
    private SelectPictureBottomDialog mDialog;
    private CustomMyProfileViewModel customViewModel;

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mDialog != null) {
                    mDialog.takePicture();
                }
            }
        } else if (requestCode == PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mDialog != null) {
                    mDialog.selectPicture();
                }
            }
        }
    }

    @Override
    protected void onViewReady(
            @NonNull io.rong.imkit.usermanage.friend.my.profile.MyProfileViewModel viewModel) {
        super.onViewReady(viewModel);
        initCustomViewModel();
    }

    /**
     * 初始化自定义 ViewModel
     *
     * <p>CustomMyProfileViewModel 继承自 MyProfileViewModel， 所以可以直接获取到 Fragment 的 ViewModel 实例
     */
    private void initCustomViewModel() {
        // 直接将 getViewModel() 转换为 CustomMyProfileViewModel
        customViewModel = (CustomMyProfileViewModel) getViewModel();

        // 监听 Toast 消息（统一提示）
        customViewModel
                .getToastMessageLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        message -> {
                            if (message != null && getContext() != null) {
                                android.widget.Toast.makeText(
                                                getContext(),
                                                message,
                                                android.widget.Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

        // 监听上传成功，刷新 UI
        customViewModel
                .getUploadSuccessLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        success -> {
                            if (success != null && success) {
                                // 重新加载用户信息
                                customViewModel.loadMyUserProfile();
                            }
                        });
    }

    @Override
    protected void onUserHeaderClick(View view) {
        showSelectPictureDialog();
    }

    /** 显示选择图片的 dialog */
    private void showSelectPictureDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new MySelectPictureListener(this));
        mDialog = builder.build();
        mDialog.show(getChildFragmentManager(), "select_picture_dialog");
    }

    /** 图片选择监听器 */
    public static class MySelectPictureListener
            implements SelectPictureBottomDialog.OnSelectPictureListener {
        WeakReference<CustomMyProfileFragment> weakFragment;

        public MySelectPictureListener(CustomMyProfileFragment fragment) {
            weakFragment = new WeakReference<>(fragment);
        }

        @Override
        public void onSelectPicture(Uri uri) {
            CustomMyProfileFragment fragment = weakFragment.get();
            if (fragment != null) {
                // 上传图片
                fragment.uploadPortrait(uri);
            }
        }
    }

    /** 上传头像 */
    private void uploadPortrait(Uri uri) {
        if (customViewModel != null) {
            customViewModel.uploadPortrait(uri);
        }
    }

    @NonNull
    @Override
    protected io.rong.imkit.usermanage.friend.my.profile.MyProfileViewModel onCreateViewModel(
            android.os.Bundle bundle) {
        // 创建自定义的 ViewModel（继承自 MyProfileViewModel）
        return new ViewModelProvider(this, new io.rong.imkit.usermanage.ViewModelFactory(bundle))
                .get(CustomMyProfileViewModel.class);
    }
}
