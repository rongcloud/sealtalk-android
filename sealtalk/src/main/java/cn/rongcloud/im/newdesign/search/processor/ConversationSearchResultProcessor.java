package cn.rongcloud.im.newdesign.search.processor;

import cn.rongcloud.im.R;
import cn.rongcloud.im.newdesign.search.SearchType;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.adapter.models.SearchShowMorModel;
import cn.rongcloud.im.ui.adapter.models.SearchTitleModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.SearchConversationResult;
import io.rong.imlib.model.UserInfo;

/**
 * 会话搜索结果处理器
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class ConversationSearchResultProcessor
        extends BaseSearchResultProcessor<SearchConversationResult> {

    @Override
    public int getSearchType() {
        return SearchType.CONVERSATION;
    }

    @Override
    protected void observeLiveData() {
        if (viewModel != null && lifecycleOwner != null) {
            viewModel.getConversationResultData().observe(lifecycleOwner, this::updateResults);
        }
    }

    @Override
    protected SearchModel convertToSearchModel(SearchConversationResult result, String query) {
        String name = "";
        String portraitUrl = "";

        Conversation.ConversationType type = result.getConversation().getConversationType();
        String targetId = result.getConversation().getTargetId();

        if (type == Conversation.ConversationType.PRIVATE) {
            // 获取私聊用户信息
            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
            if (userInfo != null) {
                name = userInfo.getName();
                portraitUrl =
                        userInfo.getPortraitUri() != null
                                ? userInfo.getPortraitUri().toString()
                                : "";
            }
        } else if (type == Conversation.ConversationType.GROUP) {
            // 获取群组信息
            Group group = RongUserInfoManager.getInstance().getGroupInfo(targetId);
            if (group != null) {
                name = group.getName();
                portraitUrl =
                        group.getPortraitUri() != null ? group.getPortraitUri().toString() : "";
            }
        }

        return new SearchConversationModel(
                result,
                R.layout.serach_fragment_recycler_conversation_item,
                query,
                name,
                portraitUrl);
    }

    @Override
    protected SearchModel createTitleModel() {
        return new SearchTitleModel(
                R.string.seal_ac_search_chatting_records,
                R.layout.search_fragment_recycler_title_layout,
                SearchModel.SHOW_PRIORITY_CONVERSATION);
    }

    @Override
    protected SearchModel createShowMoreModel() {
        return new SearchShowMorModel(
                R.string.seal_search_more_chatting_records,
                R.layout.search_frament_show_more_item,
                SearchModel.SHOW_PRIORITY_CONVERSATION);
    }
}
