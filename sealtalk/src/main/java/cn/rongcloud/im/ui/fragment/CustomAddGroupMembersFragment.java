package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.add.AddGroupMembersFragment;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomAddGroupMembersFragment extends AddGroupMembersFragment {

    @Override
    protected void onInviteUsersToGroupResult(
            String groupId, List<String> selectedIdList, IRongCoreEnum.CoreErrorCode errorCode) {
        super.onInviteUsersToGroupResult(groupId, selectedIdList, errorCode);
        if (errorCode == IRongCoreEnum.CoreErrorCode.SUCCESS) {
            // 邀请用户加入成功
            JSONObject dataObj = new JSONObject();
            try {
                JSONArray targetUserIds = new JSONArray();
                for (String userId : selectedIdList) {
                    targetUserIds.put(userId);
                }
                dataObj.put("targetUserIds", targetUserIds);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SealGroupNotificationMessage message =
                    SealGroupNotificationMessage.obtain(
                            RongCoreClient.getInstance().getCurrentUserId(),
                            GroupNotificationMessage.GROUP_OPERATION_ADD,
                            dataObj.toString());
            ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
            IMCenter.getInstance()
                    .sendMessage(Message.obtain(identifier, message), null, null, null);
        }
    }
}
