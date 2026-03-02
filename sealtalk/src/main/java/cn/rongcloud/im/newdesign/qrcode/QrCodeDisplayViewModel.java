package cn.rongcloud.im.newdesign.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.qrcode.QRCodeManager;
import cn.rongcloud.im.utils.qrcode.RongQrCodeGenerator;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imkit.usermanage.handler.GroupInfoHandler;
import io.rong.imkit.usermanage.handler.UserProfileHandler;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupInfo;
import io.rong.imlib.model.UserProfile;

/**
 * 二维码展示 ViewModel
 *
 * <p>负责获取用户/群组信息，生成二维码
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeDisplayViewModel extends BaseViewModel {

    /** 二维码展示类型 */
    public enum DisplayType {
        /** 个人二维码 */
        PRIVATE,
        /** 群组二维码 */
        GROUP
    }

    private static final String ARG_DISPLAY_TYPE = "display_type";
    private static final String ARG_TARGET_ID = "target_id";
    private static final String ARG_FROM_ID = "from_id";
    private static final String ARG_CONTEXT = "context";

    private final DisplayType displayType;
    private final String targetId; // 用户ID或群组ID
    private final String fromId;

    private final MutableLiveData<UserProfile> userProfileData = new MutableLiveData<>();
    private final MutableLiveData<GroupInfo> groupInfoData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> qrCodeBitmapData = new MutableLiveData<>();
    private final MutableLiveData<IRongCoreEnum.CoreErrorCode> errorData = new MutableLiveData<>();

    private UserProfileHandler userProfileHandler;
    private GroupInfoHandler groupInfoHandler;
    private final QRCodeManager qrCodeManager;

    // 保存二维码尺寸，用于在数据就绪后生成二维码
    private int qrCodeWidth = 0;
    private int qrCodeHeight = 0;

    public QrCodeDisplayViewModel(@NonNull Bundle arguments) {
        super(arguments);

        // 从 Bundle 中读取参数
        String typeStr = arguments.getString(ARG_DISPLAY_TYPE);
        this.displayType =
                DisplayType.valueOf(typeStr != null ? typeStr : DisplayType.PRIVATE.name());
        this.targetId = arguments.getString(ARG_TARGET_ID);
        this.fromId = arguments.getString(ARG_FROM_ID);

        qrCodeManager = new QRCodeManager(SealApp.getApplication());

        // 初始化对应的 Handler
        if (displayType == DisplayType.PRIVATE) {
            initUserProfileHandler();
        } else if (displayType == DisplayType.GROUP) {
            initGroupInfoHandler();
        }
    }

    /** 初始化用户信息处理器 */
    private void initUserProfileHandler() {
        userProfileHandler = new UserProfileHandler();

        // 监听用户信息变化
        userProfileHandler.addDataChangeListener(
                UserProfileHandler.KEY_GET_USER_PROFILE,
                (data) -> {
                    if (data != null) {
                        userProfileData.postValue(data);
                        // 用户信息加载完成，如果已经有二维码尺寸，立即生成二维码
                        if (qrCodeWidth > 0 && qrCodeHeight > 0) {
                            generateQRCode(qrCodeWidth, qrCodeHeight);
                        }
                    }
                });
    }

    /** 初始化群组信息处理器 */
    private void initGroupInfoHandler() {
        if (!TextUtils.isEmpty(targetId)) {
            ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(targetId);
            groupInfoHandler = new GroupInfoHandler(identifier);

            // 监听群组信息变化
            groupInfoHandler.addDataChangeListener(
                    GroupInfoHandler.KEY_GROUP_INFO,
                    (data) -> {
                        if (data != null) {
                            groupInfoData.postValue(data);
                        }
                    });
        }
    }

    /** 加载信息 */
    public void loadInfo() {
        if (displayType == DisplayType.PRIVATE) {
            loadUserProfile();
        } else if (displayType == DisplayType.GROUP) {
            loadGroupInfo();
        }
    }

    /** 加载用户信息 */
    private void loadUserProfile() {
        if (userProfileHandler != null && !TextUtils.isEmpty(targetId)) {
            userProfileHandler.getUserProfile(targetId);
        }
    }

    /** 加载群组信息 */
    private void loadGroupInfo() {
        if (groupInfoHandler != null) {
            groupInfoHandler.getGroupsInfo();
        }
    }

    /**
     * 设置二维码尺寸并在数据就绪时生成二维码
     *
     * @param width 二维码宽度
     * @param height 二维码高度
     */
    public void setQrCodeSize(int width, int height) {
        this.qrCodeWidth = width;
        this.qrCodeHeight = height;

        // 如果数据已经就绪，立即生成二维码
        if (isDataReady()) {
            generateQRCode(width, height);
        }
    }

    /**
     * 检查数据是否就绪可以生成二维码
     *
     * @return true 如果数据就绪
     */
    private boolean isDataReady() {
        if (displayType == DisplayType.PRIVATE) {
            // 个人二维码需要等待 targetId (userId)
            return !TextUtils.isEmpty(targetId);
        } else if (displayType == DisplayType.GROUP) {
            // 群组二维码需要等待 targetId (groupId) 和 fromId
            return !TextUtils.isEmpty(targetId);
        }
        return false;
    }

    /**
     * 生成二维码
     *
     * @param width 二维码宽度
     * @param height 二维码高度
     */
    private void generateQRCode(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        String qrContent = buildQRCodeContent();
        if (TextUtils.isEmpty(qrContent)) {
            return;
        }

        // 在后台线程生成二维码
        final String content = qrContent;
        new Thread(
                        () -> {
                            Bitmap bitmap =
                                    RongQrCodeGenerator.getInstance()
                                            .generateQRCode(content, width, height);
                            if (bitmap != null) {
                                qrCodeBitmapData.postValue(bitmap);
                            }
                        })
                .start();
    }

    /**
     * 构建二维码内容
     *
     * @return 二维码内容字符串
     */
    private String buildQRCodeContent() {
        if (displayType == DisplayType.PRIVATE) {
            return buildPrivateQRCodeContent();
        } else if (displayType == DisplayType.GROUP) {
            return buildGroupQRCodeContent();
        }
        return null;
    }

    /** 构建个人二维码内容 */
    private String buildPrivateQRCodeContent() {
        // 个人二维码使用传入的 userId
        if (TextUtils.isEmpty(targetId)) {
            return null;
        }

        // 直接调用 QRCodeManager 的方法
        return qrCodeManager.generateUserQRCodeContent(targetId);
    }

    /** 构建群组二维码内容 */
    private String buildGroupQRCodeContent() {
        if (TextUtils.isEmpty(targetId)) {
            return null;
        }

        // 直接调用 QRCodeManager 的方法，groupId 和 fromId 都是必传的
        return qrCodeManager.generateGroupQRCodeContent(targetId, fromId);
    }

    // Getters for LiveData
    public LiveData<UserProfile> getUserProfileData() {
        return userProfileData;
    }

    public LiveData<GroupInfo> getGroupInfoData() {
        return groupInfoData;
    }

    public LiveData<Bitmap> getQrCodeBitmapData() {
        return qrCodeBitmapData;
    }

    public LiveData<IRongCoreEnum.CoreErrorCode> getErrorData() {
        return errorData;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userProfileHandler != null) {
            userProfileHandler.stop();
        }
        if (groupInfoHandler != null) {
            groupInfoHandler.stop();
        }
    }
}
