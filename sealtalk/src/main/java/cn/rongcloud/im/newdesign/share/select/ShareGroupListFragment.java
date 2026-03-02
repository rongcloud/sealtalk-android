package cn.rongcloud.im.newdesign.share.select;

import android.view.View;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import io.rong.imkit.usermanage.group.grouplist.GroupListFragment;
import io.rong.imlib.model.Conversation;

/** 分享/转发群组列表 Fragment 支持图片分享和消息转发两种模式 */
public class ShareGroupListFragment extends GroupListFragment {

    @Override
    protected void onViewReady(
            @NonNull io.rong.imkit.usermanage.group.grouplist.GroupListViewModel viewModel) {
        super.onViewReady(viewModel);
        if (searchComponent != null) {
            searchComponent.setVisibility(View.GONE);
        }
        if (headComponent != null) {
            headComponent.setTitleText(R.string.seal_select_chat_choose_group);
            headComponent.setLeftClickListener(v -> finishActivity());
        }
        groupListAdapter.setOnItemClickListener(
                groupInfo -> {
                    if (!(getActivity() instanceof ShareSendHandler)) {
                        return;
                    }
                    if (groupInfo != null) {
                        ((ShareSendHandler) getActivity())
                                .sendToConversation(
                                        Conversation.ConversationType.GROUP,
                                        groupInfo.getGroupId(),
                                        groupInfo.getGroupName(),
                                        groupInfo.getPortraitUri() != null
                                                ? groupInfo.getPortraitUri().toString()
                                                : null);
                    }
                });
    }
}
