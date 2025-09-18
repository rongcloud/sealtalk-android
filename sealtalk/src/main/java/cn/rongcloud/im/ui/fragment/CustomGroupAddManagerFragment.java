package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.memberselect.impl.GroupAddManagerFragment;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupMemberInfo;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomGroupAddManagerFragment extends GroupAddManagerFragment {
    @Override
    protected void onAddGroupManagersResult(
            String groupId, List<GroupMemberInfo> selectGroupMemberInfoList, boolean isSuccess) {
        super.onAddGroupManagersResult(groupId, selectGroupMemberInfoList, isSuccess);
        if (isSuccess) {
            // 管理员添加成功
            sendMessage(groupId, selectGroupMemberInfoList);
        }
    }

    private void sendMessage(String groupId, List<GroupMemberInfo> selectGroupMemberInfoList) {
        if (selectGroupMemberInfoList == null || selectGroupMemberInfoList.isEmpty()) {
            return;
        }
        JSONObject dataObj = new JSONObject();
        try {
            JSONArray targetUserIds = new JSONArray();
            for (GroupMemberInfo info : selectGroupMemberInfoList) {
                targetUserIds.put(info.getUserId());
            }
            dataObj.put("targetUserIds", targetUserIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SealGroupNotificationMessage message =
                SealGroupNotificationMessage.obtain(
                        RongCoreClient.getInstance().getCurrentUserId(),
                        GroupNotificationMessage.GROUP_OPERATION_MANAGER_SET,
                        dataObj.toString());
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
        IMCenter.getInstance().sendMessage(Message.obtain(identifier, message), null, null, null);
    }
}
