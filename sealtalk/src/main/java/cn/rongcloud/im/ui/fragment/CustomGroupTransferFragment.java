package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.transfer.GroupTransferFragment;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupMemberInfo;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomGroupTransferFragment extends GroupTransferFragment {
    @Override
    protected void onGroupOwnerTransferResult(
            String groupId, GroupMemberInfo groupMemberInfo, boolean isSuccess) {
        super.onGroupOwnerTransferResult(groupId, groupMemberInfo, isSuccess);
        if (isSuccess) {
            // 群主转移成功
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
                        GroupNotificationMessage.GROUP_OPERATION_TRANSFER,
                        dataObj.toString());
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
        IMCenter.getInstance().sendMessage(Message.obtain(identifier, message), null, null, null);
    }
}
