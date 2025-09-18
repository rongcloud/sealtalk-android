package cn.rongcloud.im.ui.fragment;

import android.widget.Toast;
import androidx.annotation.NonNull;
import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.application.GroupApplicationsFragment;
import io.rong.imkit.usermanage.interfaces.OnActionClickListener;
import io.rong.imkit.utils.ToastUtils;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.GroupApplicationDirection;
import io.rong.imlib.model.GroupApplicationInfo;
import io.rong.imlib.model.GroupApplicationStatus;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomGroupApplicationsFragment extends GroupApplicationsFragment {

    @Override
    protected void onApplicationAccept(
            GroupApplicationInfo groupApplicationInfo,
            @NonNull
                    OnActionClickListener.OnConfirmClickListener<IRongCoreEnum.CoreErrorCode>
                            listener) {
        GroupApplicationDirection direction = groupApplicationInfo.getDirection();
        GroupApplicationStatus status = groupApplicationInfo.getStatus();
        if (status == GroupApplicationStatus.InviteeUnHandled
                || status == GroupApplicationStatus.ManagerUnHandled) {
            if (direction == GroupApplicationDirection.InvitationReceived) {
                getViewModel()
                        .acceptGroupInvite(
                                groupApplicationInfo.getGroupId(),
                                groupApplicationInfo.getInviterInfo().getUserId(),
                                isSuccess -> {
                                    if (!isSuccess) {
                                        ToastUtils.show(
                                                getContext(),
                                                getString(
                                                        io.rong.imkit.R.string
                                                                .rc_invite_confirm_failed),
                                                Toast.LENGTH_SHORT);
                                    }
                                    listener.onActionClick(IRongCoreEnum.CoreErrorCode.SUCCESS);
                                    sendMessage(
                                            groupApplicationInfo.getGroupId(),
                                            groupApplicationInfo.getInviterInfo().getUserId(),
                                            groupApplicationInfo.getJoinMemberInfo().getUserId());
                                });
            } else if (direction == GroupApplicationDirection.ApplicationReceived) {
                getViewModel()
                        .acceptGroupApplication(
                                groupApplicationInfo.getGroupId(),
                                groupApplicationInfo.getInviterInfo().getUserId(),
                                groupApplicationInfo.getJoinMemberInfo().getUserId(),
                                coreErrorCode -> {
                                    boolean isSuccess =
                                            coreErrorCode == IRongCoreEnum.CoreErrorCode.SUCCESS
                                                    || coreErrorCode
                                                            == IRongCoreEnum.CoreErrorCode
                                                                    .RC_GROUP_NEED_INVITEE_ACCEPT;
                                    if (!isSuccess) {
                                        ToastUtils.show(
                                                getContext(),
                                                getString(
                                                        io.rong.imkit.R.string
                                                                .rc_invite_confirm_failed),
                                                Toast.LENGTH_SHORT);
                                    }
                                    if (isSuccess) {
                                        listener.onActionClick(coreErrorCode);
                                    }
                                    sendMessage(
                                            groupApplicationInfo.getGroupId(),
                                            groupApplicationInfo.getInviterInfo().getUserId(),
                                            groupApplicationInfo.getJoinMemberInfo().getUserId());
                                });
            }
        }
    }

    private void sendMessage(String groupId, String inviteUserId, String joinUserId) {
        // 邀请用户加入成功
        JSONObject dataObj = new JSONObject();
        try {
            JSONArray targetUserIds = new JSONArray();
            targetUserIds.put(joinUserId);
            dataObj.put("targetUserIds", targetUserIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SealGroupNotificationMessage message =
                SealGroupNotificationMessage.obtain(
                        inviteUserId,
                        GroupNotificationMessage.GROUP_OPERATION_ADD,
                        dataObj.toString());
        ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
        IMCenter.getInstance().sendMessage(Message.obtain(identifier, message), null, null, null);
    }
}
