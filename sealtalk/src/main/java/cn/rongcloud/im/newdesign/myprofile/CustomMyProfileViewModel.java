package cn.rongcloud.im.newdesign.myprofile;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.UserTask;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileViewModel;
import io.rong.imkit.usermanage.handler.UserProfileOperationsHandler;
import io.rong.imlib.model.UserProfile;

/**
 * 自定义我的资料 ViewModel（继承 IMKit 的 MyProfileViewModel）
 *
 * <p>负责处理用户头像上传和用户信息更新逻辑
 *
 * <p>优化点：
 *
 * <ul>
 *   <li>1. 继承 IMKit 的 MyProfileViewModel，保留原有功能
 *   <li>2. 使用单独的七牛上传逻辑 {@link UserTask#uploadPortraitImage}
 *   <li>3. 使用增强版的用户信息更新方法 {@link UserProfileOperationsHandler#updateMyUserProfileExamine}
 *   <li>4. 简化的状态管理，统一使用 Toast 提示
 * </ul>
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class CustomMyProfileViewModel extends MyProfileViewModel {

    private final UserTask userTask;
    private final UserProfileOperationsHandler uploadOperationsHandler;

    // LiveData - 简化版本
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> uploadSuccessLiveData = new MutableLiveData<>();

    // MediatorLiveData 用于管理上传流程
    private final MediatorLiveData<Resource<String>> uploadMediatorLiveData =
            new MediatorLiveData<>();

    // 保存 Observer 引用用于清理
    private final androidx.lifecycle.Observer<Resource<String>> uploadObserver =
            resource -> {
                // 这个观察者确保 MediatorLiveData 始终处于活跃状态
                // 实际的业务逻辑在 addSource 的回调中处理
            };

    public CustomMyProfileViewModel(@NonNull Bundle bundle) {
        super(bundle);
        this.userTask = new UserTask(IMCenter.getInstance().getContext());
        uploadOperationsHandler = new UserProfileOperationsHandler();

        // 监听更新成功
        uploadOperationsHandler.addDataChangeListener(
                UserProfileOperationsHandler.KEY_UPDATE_MY_USER_PROFILE_EXAMINE,
                (success) -> {
                    if (success != null && success) {
                        toastMessageLiveData.postValue(
                                SealApp.getApplication()
                                        .getString(R.string.seal_profile_avatar_update_success));
                        uploadSuccessLiveData.postValue(true);
                    }
                });

        uploadMediatorLiveData.observeForever(uploadObserver);
    }

    /**
     * 上传头像（简化版）
     *
     * <p>步骤：上传图片到七牛 → 更新用户信息
     *
     * @param imageUri 图片 URI
     */
    public void uploadPortrait(Uri imageUri) {
        if (imageUri == null) {
            toastMessageLiveData.setValue("图片无效");
            return;
        }

        // 步骤1：上传图片到七牛
        LiveData<Resource<String>> uploadResource = userTask.uploadPortraitImage(imageUri);

        uploadMediatorLiveData.addSource(
                uploadResource,
                resource -> {
                    if (resource == null) {
                        return;
                    }

                    if (resource.status == Status.ERROR) {
                        uploadMediatorLiveData.removeSource(uploadResource);
                        toastMessageLiveData.setValue("上传失败");
                    } else if (resource.status == Status.SUCCESS) {
                        uploadMediatorLiveData.removeSource(uploadResource);
                        // 步骤2：更新用户信息
                        updateUserProfile(resource.data);
                    }
                });
    }

    /**
     * 更新用户信息
     *
     * @param portraitUrl 头像 URL
     */
    private void updateUserProfile(String portraitUrl) {
        String currentUserId = IMManager.getInstance().getCurrentId();
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(currentUserId);
        userProfile.setPortraitUri(portraitUrl);
        uploadOperationsHandler.updateMyUserProfileExamine(userProfile);
    }

    // Getters for LiveData

    /** 获取 Toast 消息 LiveData */
    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    /** 获取上传成功状态 LiveData */
    public LiveData<Boolean> getUploadSuccessLiveData() {
        return uploadSuccessLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
        uploadOperationsHandler.stop();
        // 移除 observeForever 的观察者，防止内存泄漏
        uploadMediatorLiveData.removeObserver(uploadObserver);
    }
}
