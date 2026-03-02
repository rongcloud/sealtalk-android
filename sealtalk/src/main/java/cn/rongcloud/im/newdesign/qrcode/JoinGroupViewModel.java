package cn.rongcloud.im.newdesign.qrcode;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imkit.usermanage.handler.GroupInfoHandler;
import io.rong.imkit.usermanage.handler.GroupOperationsHandler;
import io.rong.imkit.usermanage.interfaces.OnDataChangeListener;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupInfo;

/**
 * 加入群聊 ViewModel
 *
 * <p>负责获取群组信息、处理加入群组操作
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class JoinGroupViewModel extends BaseViewModel {

    private static final String ARG_GROUP_ID = "group_id";

    private String groupId;

    private final MutableLiveData<GroupInfo> groupInfoData = new MutableLiveData<>();
    private final MutableLiveData<IRongCoreEnum.CoreErrorCode> errorData = new MutableLiveData<>();

    private GroupInfoHandler groupInfoHandler;
    private GroupOperationsHandler groupOperationsHandler;

    public JoinGroupViewModel(@NonNull Bundle arguments) {
        super(arguments);

        // 从 Bundle 中读取参数
        this.groupId = arguments.getString(ARG_GROUP_ID);

        // 初始化处理器
        if (!TextUtils.isEmpty(groupId)) {
            initHandlers();
        }
    }

    /** 初始化处理器 */
    private void initHandlers() {
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);

        // 初始化群组信息处理器
        groupInfoHandler = new GroupInfoHandler(identifier);
        groupInfoHandler.addDataChangeListener(
                GroupInfoHandler.KEY_GROUP_INFO,
                (data) -> {
                    if (data != null) {
                        groupInfoData.postValue(data);
                    }
                });

        // 初始化群组操作处理器
        groupOperationsHandler = new GroupOperationsHandler(identifier);
    }

    /** 加载群组信息 */
    public void loadGroupInfo() {
        if (groupInfoHandler != null) {
            groupInfoHandler.getGroupsInfo();
        }
    }

    /** 加入群组 */
    public void joinGroup(
            @NonNull OnDataChangeListener<IRongCoreEnum.CoreErrorCode> onDataChangeListener) {
        if (groupOperationsHandler != null) {
            groupOperationsHandler.replaceDataChangeListener(
                    GroupOperationsHandler.KEY_JOIN_GROUP, onDataChangeListener);
            groupOperationsHandler.joinGroup();
        }
    }

    // Getters for LiveData
    public LiveData<GroupInfo> getGroupInfoData() {
        return groupInfoData;
    }

    public LiveData<IRongCoreEnum.CoreErrorCode> getErrorData() {
        return errorData;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (groupInfoHandler != null) {
            groupInfoHandler.stop();
        }
        if (groupOperationsHandler != null) {
            groupOperationsHandler.stop();
        }
    }
}
