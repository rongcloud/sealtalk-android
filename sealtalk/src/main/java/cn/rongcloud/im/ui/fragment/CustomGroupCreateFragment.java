package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.create.GroupCreateFragment;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;
import java.util.List;

/**
 * 功能描述:
 *
 * <p>创建时间: 2024/12/2
 *
 * @author haogaohui
 * @since 1.0
 */
public class CustomGroupCreateFragment extends GroupCreateFragment {

    @Override
    protected void onCreateGroupResult(
            String groupId,
            List<String> inviteeUserIds,
            IRongCoreEnum.CoreErrorCode coreErrorCode) {
        super.onCreateGroupResult(groupId, inviteeUserIds, coreErrorCode);
        if (coreErrorCode == IRongCoreEnum.CoreErrorCode.RC_GROUP_NEED_INVITEE_ACCEPT
                || coreErrorCode == IRongCoreEnum.CoreErrorCode.SUCCESS) {
            // 创建成功
            GroupNotificationMessage message =
                    SealGroupNotificationMessage.obtain(
                            RongCoreClient.getInstance().getCurrentUserId(),
                            GroupNotificationMessage.GROUP_OPERATION_CREATE);
            ConversationIdentifier identifier = ConversationIdentifier.obtainGroup(groupId);
            IMCenter.getInstance()
                    .sendMessage(Message.obtain(identifier, message), null, null, null);
        }
    }
}
