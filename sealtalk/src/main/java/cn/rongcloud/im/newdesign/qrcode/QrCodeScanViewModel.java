package cn.rongcloud.im.newdesign.qrcode;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.model.qrcode.QRCodeResult;
import cn.rongcloud.im.model.qrcode.QRCodeType;
import cn.rongcloud.im.qrcode.QRCodeManager;
import cn.rongcloud.im.task.GroupTask;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.GroupInfo;
import io.rong.imlib.model.GroupMemberRole;
import java.util.ArrayList;
import java.util.List;

/**
 * 二维码扫描 ViewModel
 *
 * <p>处理二维码扫描结果，解析并查询相关信息
 *
 * @author rongcloud
 * @since 5.12.0
 */
public class QrCodeScanViewModel extends BaseViewModel {

    /** 扫描结果类型 */
    public enum ScanResultType {
        /** 用户 */
        USER,
        /** 群组（未加入） */
        GROUP,
        /** 群组（已加入） */
        GROUP_ALREADY_IN,
        /** 错误 */
        ERROR
    }

    /** 扫描结果数据 */
    public static class ScanResult {
        private ScanResultType type;
        private String targetId; // 用户 ID 或群组 ID
        private String fromUserId; // 群组分享来源用户 ID（可选）
        private String groupName; // 群组名称（可选）

        public ScanResult(ScanResultType type, String targetId) {
            this.type = type;
            this.targetId = targetId;
        }

        public ScanResult(ScanResultType type, String targetId, String fromUserId) {
            this.type = type;
            this.targetId = targetId;
            this.fromUserId = fromUserId;
        }

        public ScanResult(
                ScanResultType type, String targetId, String fromUserId, String groupName) {
            this.type = type;
            this.targetId = targetId;
            this.fromUserId = fromUserId;
            this.groupName = groupName;
        }

        public ScanResultType getType() {
            return type;
        }

        public String getTargetId() {
            return targetId;
        }

        public String getFromUserId() {
            return fromUserId;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    private final MutableLiveData<ScanResult> scanResultData = new MutableLiveData<>();
    private final MutableLiveData<String> errorData = new MutableLiveData<>();

    private QRCodeManager qrCodeManager;
    private GroupTask groupTask;

    public QrCodeScanViewModel(@NonNull Bundle arguments) {
        super(arguments);
    }

    /**
     * 处理二维码
     *
     * @param qrCodeText 二维码文本
     */
    public void handleQrCode(String qrCodeText) {
        if (TextUtils.isEmpty(qrCodeText)) {
            errorData.postValue(
                    SealApp.getApplication().getString(R.string.seal_qrcode_scan_content_empty));
            return;
        }

        // 初始化管理器
        if (qrCodeManager == null) {
            qrCodeManager = new QRCodeManager(SealApp.getApplication());
        }
        if (groupTask == null) {
            groupTask = new GroupTask(SealApp.getApplication());
        }

        // 解析二维码
        QRCodeResult qrCodeResult = qrCodeManager.getQRCodeType(qrCodeText);
        QRCodeType type = qrCodeResult.getType();

        switch (type) {
            case USER_INFO:
                handleUserInfo(qrCodeResult);
                break;
            case GROUP_INFO:
                handleGroupInfo(qrCodeResult);
                break;
            default:
                errorData.postValue(
                        SealApp.getApplication().getString(R.string.seal_qrcode_scan_unrecognized));
                break;
        }
    }

    /**
     * 处理用户信息
     *
     * @param qrCodeResult 二维码结果
     */
    private void handleUserInfo(QRCodeResult qrCodeResult) {
        String userId = qrCodeResult.getUserInfoResult().getUserId();
        if (TextUtils.isEmpty(userId)) {
            errorData.postValue(
                    SealApp.getApplication().getString(R.string.seal_qrcode_scan_user_id_empty));
            return;
        }

        // 直接返回用户结果
        ScanResult result = new ScanResult(ScanResultType.USER, userId);
        scanResultData.postValue(result);
    }

    /**
     * 处理群组信息
     *
     * @param qrCodeResult 二维码结果
     */
    private void handleGroupInfo(QRCodeResult qrCodeResult) {
        String groupId = qrCodeResult.getGroupInfoResult().getGroupId();
        String sharedUserId = qrCodeResult.getGroupInfoResult().getSharedUserId();

        if (TextUtils.isEmpty(groupId)) {
            errorData.postValue(
                    SealApp.getApplication().getString(R.string.seal_qrcode_scan_group_id_empty));
            return;
        }

        // 先获取群组信息
        ArrayList<String> groupIds = new ArrayList<>();
        groupIds.add(groupId);
        RongCoreClient.getInstance()
                .getGroupsInfo(
                        groupIds,
                        new IRongCoreCallback.ResultCallback<List<GroupInfo>>() {
                            @Override
                            public void onSuccess(List<GroupInfo> groupInfos) {
                                if (groupInfos != null && !groupInfos.isEmpty()) {
                                    GroupInfo groupInfo = groupInfos.get(0);
                                    // 群组存在，检查是否已加入
                                    // 判断 GroupInfo 为空或 role 为空/Undefined
                                    if (groupInfo.getRole() == GroupMemberRole.Undef) {
                                        // 未加入群组，返回加群结果
                                        ScanResult result =
                                                new ScanResult(
                                                        ScanResultType.GROUP,
                                                        groupId,
                                                        sharedUserId);
                                        scanResultData.postValue(result);
                                    } else {
                                        // 已加入群组，返回群聊结果
                                        ScanResult result =
                                                new ScanResult(
                                                        ScanResultType.GROUP_ALREADY_IN,
                                                        groupId,
                                                        sharedUserId,
                                                        groupInfo.getGroupName());
                                        scanResultData.postValue(result);
                                    }
                                } else {
                                    errorData.postValue(
                                            SealApp.getApplication()
                                                    .getString(
                                                            R.string
                                                                    .seal_qrcode_scan_group_not_exist));
                                }
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                errorData.postValue(
                                        SealApp.getApplication()
                                                .getString(
                                                        R.string.seal_qrcode_scan_group_not_exist));
                            }
                        });
    }

    // Getters for LiveData

    public LiveData<ScanResult> getScanResultData() {
        return scanResultData;
    }

    public LiveData<String> getErrorData() {
        return errorData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
        qrCodeManager = null;
        groupTask = null;
    }
}
