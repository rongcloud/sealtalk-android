package cn.rongcloud.im.newdesign.search.processor;

import android.text.TextUtils;
import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.newdesign.search.SearchType;
import cn.rongcloud.im.ui.adapter.models.SearchGroupModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.adapter.models.SearchShowMorModel;
import cn.rongcloud.im.ui.adapter.models.SearchTitleModel;
import cn.rongcloud.im.utils.SearchUtils;
import io.rong.imlib.model.GroupInfo;

/**
 * 群组搜索结果处理器
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class GroupSearchResultProcessor extends BaseSearchResultProcessor<GroupInfo> {

    @Override
    public int getSearchType() {
        return SearchType.GROUP;
    }

    @Override
    protected void observeLiveData() {
        if (viewModel != null && lifecycleOwner != null) {
            viewModel.getGroupResultData().observe(lifecycleOwner, this::updateResults);
        }
    }

    @Override
    protected SearchModel convertToSearchModel(GroupInfo groupInfo, String query) {
        // 构建 GroupEntity（仅用于展示）
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setId(groupInfo.getGroupId());
        groupEntity.setName(groupInfo.getGroupName());
        groupEntity.setPortraitUri(
                groupInfo.getPortraitUri() != null ? groupInfo.getPortraitUri().toString() : "");
        groupEntity.setMemberCount(groupInfo.getMembersCount());

        // 计算高亮位置
        String groupName = groupInfo.getGroupName();
        int start = -1;
        int end = -1;

        if (!TextUtils.isEmpty(groupName)) {
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(groupName, query);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
        }

        return new SearchGroupModel(
                groupEntity, R.layout.serach_fragment_recycler_group_item, start, end, null);
    }

    @Override
    protected SearchModel createTitleModel() {
        return new SearchTitleModel(
                R.string.seal_ac_search_group,
                R.layout.search_fragment_recycler_title_layout,
                SearchModel.SHOW_PRIORITY_GROUP);
    }

    @Override
    protected SearchModel createShowMoreModel() {
        return new SearchShowMorModel(
                R.string.seal_search_more_group,
                R.layout.search_frament_show_more_item,
                SearchModel.SHOW_PRIORITY_GROUP);
    }
}
