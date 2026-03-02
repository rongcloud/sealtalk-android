package cn.rongcloud.im.newdesign.share.select;

import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import io.rong.imkit.model.ContactModel;
import io.rong.imkit.model.OnlineStatusFriendInfo;
import io.rong.imkit.usermanage.friend.friendlist.FriendListFragment;
import io.rong.imlib.model.FriendInfo;

/** 分享/转发好友列表 Fragment 支持图片分享和消息转发两种模式 */
public class ShareFriendListFragment extends FriendListFragment {

    @Override
    protected void onViewReady(
            @NonNull io.rong.imkit.usermanage.friend.friendlist.FriendListViewModel viewModel) {
        super.onViewReady(viewModel);
        if (searchComponent != null) {
            searchComponent.setVisibility(View.GONE);
        }
        if (headComponent != null) {
            headComponent.setTitleText(R.string.seal_select_chat_choose_friend);
            headComponent.getRightTextView().setVisibility(View.GONE);
            headComponent.setLeftClickListener(v -> finishActivity());
        }
        contactListComponent.setOnItemClickListener(
                contactModel -> {
                    if (!(getActivity() instanceof ShareSendHandler)) {
                        return;
                    }
                    FriendInfo friendInfo = extractFriendInfo(contactModel);
                    if (friendInfo != null) {
                        String name =
                                !TextUtils.isEmpty(friendInfo.getRemark())
                                        ? friendInfo.getRemark()
                                        : friendInfo.getName();
                        ((ShareSendHandler) getActivity())
                                .sendToConversation(
                                        io.rong.imlib.model.Conversation.ConversationType.PRIVATE,
                                        friendInfo.getUserId(),
                                        name,
                                        friendInfo.getPortraitUri() != null
                                                ? friendInfo.getPortraitUri().toString()
                                                : null);
                    }
                });
    }

    /** 从 ContactModel 中提取 FriendInfo */
    private FriendInfo extractFriendInfo(ContactModel contactModel) {
        if (contactModel == null) {
            return null;
        }

        if (contactModel.getBean() instanceof FriendInfo) {
            return (FriendInfo) contactModel.getBean();
        } else if (contactModel.getBean() instanceof OnlineStatusFriendInfo) {
            return ((OnlineStatusFriendInfo) contactModel.getBean()).getFriendInfo();
        }

        return null;
    }
}
