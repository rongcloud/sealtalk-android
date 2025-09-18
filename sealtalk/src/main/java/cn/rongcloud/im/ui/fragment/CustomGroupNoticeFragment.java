package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.R;
import io.rong.imkit.IMCenter;
import io.rong.imkit.usermanage.group.notice.GroupNoticeFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.message.TextMessage;

public class CustomGroupNoticeFragment extends GroupNoticeFragment {

    @Override
    protected void onGroupNoticeUpdateResult(String groupId, String notice, boolean isSuccess) {
        super.onGroupNoticeUpdateResult(groupId, notice, isSuccess);
        if (isSuccess) {
            // 群公告更新成功
            String text = "@" + getString(R.string.seal_member_mention_all_member) + " " + notice;
            TextMessage msg = TextMessage.obtain(text);
            MentionedInfo mentionedInfo = new MentionedInfo();
            mentionedInfo.setType(MentionedInfo.MentionedType.ALL);
            msg.setMentionedInfo(mentionedInfo);
            IMCenter.getInstance()
                    .sendMessage(
                            Conversation.ConversationType.GROUP, groupId, msg, null, null, null);
        }
    }
}
