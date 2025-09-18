package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.managerlist.GroupManagerListFragment;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupMemberInfo;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomGroupManagerListFragment extends GroupManagerListFragment {

    @Override
    protected void onGroupManagerRemovalResult(
            String groupId, GroupMemberInfo groupMemberInfo, boolean isSuccess) {
        super.onGroupManagerRemovalResult(groupId, groupMemberInfo, isSuccess);
        if (isSuccess) {
            // 管理员移除成功
            sendMessage(groupId, groupMemberInfo);
        }
    }

    private void sendMessage(String groupId, GroupMemberInfo memberInfo) {
        if (memberInfo == null) {
            return;
        }
        JSONObject dataObj = new JSONObject();
        try {
            JSONArray targetUserIds = new JSONArray();
            targetUserIds.put(memberInfo.getUserId());
            dataObj.put("targetUserIds", targetUserIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SealGroupNotificationMessage message =
                SealGroupNotificationMessage.obtain(
                        RongCoreClient.getInstance().getCurrentUserId(),
                        GroupNotificationMessage.GROUP_OPERATION_MANAGER_REMOVE_DISPLAY,
                        dataObj.toString());
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
        IMCenter.getInstance().sendMessage(Message.obtain(identifier, message), null, null, null);
    }
}
