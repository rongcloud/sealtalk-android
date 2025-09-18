package cn.rongcloud.im.im.provider;

import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_ADD;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_CREATE;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_DISMISS;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_JOIN;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_KICKED;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_MANAGER_REMOVE_DISPLAY;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_MANAGER_SET;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_MEMBER_PROTECTION_CLOSE;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_MEMBER_PROTECTION_OPEN;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_QUIT;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_RENAME;
import static io.rong.message.GroupNotificationMessage.GROUP_OPERATION_TRANSFER;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.messgelist.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SealGroupNotificationMessageItemProvider extends GroupNotificationMessageItemProvider {

    String targetId;

    @Override
    protected void bindMessageContentViewHolder(
            ViewHolder holder,
            ViewHolder parentHolder,
            GroupNotificationMessage content,
            UiMessage message,
            int position,
            List<UiMessage> list,
            IViewProviderListener<UiMessage> listener) {
        if (content != null && message != null) {
            targetId = message.getTargetId();
            if (content.getData() == null) {
                return;
            }
            if (TextUtils.isEmpty(content.getOperation())) {
                super.bindMessageContentViewHolder(
                        holder, parentHolder, content, message, position, list, listener);
                return;
            }
            String operationContent = getOperationContent(holder.getContext(), targetId, content);
            if (!TextUtils.isEmpty(operationContent)) {
                holder.setText(R.id.rc_msg, operationContent);
            }
        }
    }

    private String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    private String getString(Context context, int resId, String str) {
        return context.getResources().getString(resId, str);
    }

    private String getOperationContent(
            Context context, String targetId, GroupNotificationMessage content) {
        GroupNotificationMessageData msgData;
        try {
            msgData = jsonToBean(content.getData());
        } catch (Exception e) {
            return "";
        }
        String operatorUserId = content.getOperatorUserId();
        String currentUserId = RongIM.getInstance().getCurrentUserId();
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(operatorUserId);
        String operatorNickname =
                RongUserInfoManager.getInstance()
                        .getUserDisplayName(userInfo, msgData.getOperatorNickname());
        if (TextUtils.isEmpty(operatorNickname)) {
            operatorNickname = content.getOperatorUserId();
        }
        List<String> memberDisplayNameList = msgData.getTargetUserDisplayNames();
        List<String> memberIdList = msgData.getTargetUserIds();
        Collections.sort(memberIdList);
        String memberName = null;
        String memberUserId = null;
        if (memberIdList != null && memberIdList.size() == 1) {
            memberUserId = memberIdList.get(0);
        }
        if (memberDisplayNameList != null && !memberDisplayNameList.isEmpty()) {
            if (memberDisplayNameList.size() == 1) {
                memberName = memberDisplayNameList.get(0);
            } else if (memberIdList != null && memberIdList.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (String s : memberDisplayNameList) {
                    sb.append(s);
                    sb.append(getString(context, io.rong.imkit.R.string.rc_item_divided_string));
                }
                String str = sb.toString();
                memberName = str.substring(0, str.length() - 1);
            }
        }
        switch (content.getOperation()) {
            case GROUP_OPERATION_CREATE:
                if (!operatorUserId.equals(currentUserId)) {
                    return operatorNickname
                            + getString(context, io.rong.imkit.R.string.rc_item_created_group);
                } else {
                    return getString(context, io.rong.imkit.R.string.rc_item_you_created_group);
                }
            case GROUP_OPERATION_ADD:
                String joined = getString(context, io.rong.imkit.R.string.rc_item_join_group);
                String invitation = getString(context, io.rong.imkit.R.string.rc_item_invitation);
                String youInvitation =
                        getString(context, io.rong.imkit.R.string.rc_item_you_invitation);
                String showName =
                        getShowName(
                                context,
                                content,
                                currentUserId,
                                operatorUserId,
                                operatorNickname,
                                memberIdList,
                                memberName);
                if (operatorUserId.equals(memberUserId)) {
                    return showName + joined;
                } else {
                    String invitedName = showName;
                    if (memberUserId != null && memberUserId.equals(currentUserId)) {
                        invitedName = getString(context, io.rong.imkit.R.string.rc_item_you);
                    }
                    if (!operatorUserId.equals(RongIM.getInstance().getCurrentUserId())) {
                        return operatorNickname + invitation + " " + invitedName + " " + joined;
                    } else {
                        return youInvitation + " " + invitedName + " " + joined;
                    }
                }
            case GROUP_OPERATION_JOIN:
                return memberName + getString(context, io.rong.imkit.R.string.rc_item_join_group);
            case GROUP_OPERATION_QUIT:
                return memberName + getString(context, io.rong.imkit.R.string.rc_item_quit_groups);
            case GROUP_OPERATION_KICKED:
                if (memberIdList == null) {
                    return "";
                }
                String self = getString(context, io.rong.imkit.R.string.rc_item_you_remove_self);
                String member =
                        getString(context, io.rong.imkit.R.string.rc_item_remove_group_member);
                String remove = getString(context, io.rong.imkit.R.string.rc_item_remove);
                for (String userId : memberIdList) {
                    if (currentUserId.equals(userId)) {
                        return self + " " + operatorNickname + " " + remove;
                    } else {
                        String removeMsg;
                        if (!operatorUserId.equals(currentUserId)) {
                            removeMsg = operatorNickname + member + " " + memberName + " " + remove;
                        } else {
                            removeMsg = member + " " + memberName + " " + remove;
                        }
                        return removeMsg;
                    }
                }
            case GROUP_OPERATION_RENAME:
                String groupName = msgData.getTargetGroupName();
                String rename =
                        getString(context, io.rong.imkit.R.string.rc_item_change_group_name);
                if (!operatorUserId.equals(currentUserId)) {
                    return operatorNickname + rename + "\"" + groupName + "\"";
                } else {
                    return rename + "\"" + groupName + "\"";
                }
            case GROUP_OPERATION_DISMISS:
                return getString(context, io.rong.imkit.R.string.rc_item_dismiss_groups);
            case GROUP_OPERATION_TRANSFER:
                String transferName =
                        getShowName(
                                context,
                                content,
                                currentUserId,
                                operatorUserId,
                                operatorNickname,
                                memberIdList,
                                memberName);
                return getString(
                        context, R.string.seal_group_action_transfer_group_owner, transferName);
            case GROUP_OPERATION_MANAGER_SET:
                String setManagerName =
                        getShowName(
                                context,
                                content,
                                currentUserId,
                                operatorUserId,
                                operatorNickname,
                                memberIdList,
                                memberName);
                return getString(context, R.string.seal_group_action_set_manager, setManagerName);
            case GROUP_OPERATION_MANAGER_REMOVE_DISPLAY:
                String removeManagerName =
                        getShowName(
                                context,
                                content,
                                currentUserId,
                                operatorUserId,
                                operatorNickname,
                                memberIdList,
                                memberName);
                return getString(
                        context, R.string.seal_group_action_remove_manager, removeManagerName);
            case GROUP_OPERATION_MEMBER_PROTECTION_OPEN:
                return getString(context, R.string.seal_group_member_protection_open);
            case GROUP_OPERATION_MEMBER_PROTECTION_CLOSE:
                return getString(context, R.string.seal_group_member_protection_close);
            default:
                break;
        }
        return "";
    }

    private String getShowName(
            Context context,
            GroupNotificationMessage content,
            String currentUserId,
            String operatorUserId,
            String operatorNickname,
            List<String> memberIdList,
            String memberName) {
        try {
            JSONObject jsonObject = new JSONObject(content.getData());
            JSONArray targetUserIdsArray = jsonObject.optJSONArray("targetUserIds");
            List<String> targetUserIds = new ArrayList<>();
            if (targetUserIdsArray != null) {
                for (int i = 0; i < targetUserIdsArray.length(); i++) {
                    targetUserIds.add(targetUserIdsArray.getString(i));
                }
                Collections.sort(targetUserIds);
            }
            if (targetUserIds.equals(memberIdList) && !TextUtils.isEmpty(memberName)) {
                return memberName;
            }
            StringBuilder sb = new StringBuilder();
            for (String userId : targetUserIds) {
                String name = null;
                if (TextUtils.equals(userId, currentUserId)) {
                    name = getString(context, io.rong.imkit.R.string.rc_item_you);
                } else if (TextUtils.equals(userId, operatorUserId)) {
                    name = operatorNickname;
                } else {
                    UserInfo user = RongUserInfoManager.getInstance().getUserInfo(userId);
                    GroupUserInfo groupUser = null;
                    if (!TextUtils.isEmpty(targetId)) {
                        groupUser =
                                RongUserInfoManager.getInstance()
                                        .getGroupUserInfo(targetId, userId);
                    }
                    if (user != null && !TextUtils.isEmpty(user.getAlias())) {
                        name = user.getAlias();
                    } else if (groupUser != null && !TextUtils.isEmpty(groupUser.getNickname())) {
                        name = groupUser.getNickname();
                    } else if (user != null) {
                        name = user.getName();
                    }
                }
                if (TextUtils.isEmpty(name)) {
                    name = "name" + userId;
                }
                sb.append(name).append("、");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Spannable getSummarySpannable(Context context, GroupNotificationMessage message) {
        if (message != null && message.getData() == null) {
            return null;
        }
        if (message == null || TextUtils.isEmpty(message.getOperation())) {
            return new SpannableString("");
        }
        String operationContent = getOperationContent(context, targetId, message);
        if (!TextUtils.isEmpty(operationContent)) {
            return new SpannableString(operationContent);
        }
        return super.getSummarySpannable(context, message);
    }
}
