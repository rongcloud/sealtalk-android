package cn.rongcloud.im.newdesign.search.processor;

import android.text.TextUtils;
import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.newdesign.search.SearchType;
import cn.rongcloud.im.ui.adapter.models.SearchFriendModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.adapter.models.SearchShowMorModel;
import cn.rongcloud.im.ui.adapter.models.SearchTitleModel;
import cn.rongcloud.im.utils.SearchUtils;
import io.rong.imlib.model.FriendInfo;

/**
 * 好友搜索结果处理器
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class FriendSearchResultProcessor extends BaseSearchResultProcessor<FriendInfo> {

    @Override
    public int getSearchType() {
        return SearchType.FRIEND;
    }

    @Override
    protected void observeLiveData() {
        if (viewModel != null && lifecycleOwner != null) {
            viewModel.getFriendResultData().observe(lifecycleOwner, this::updateResults);
        }
    }

    @Override
    protected SearchModel convertToSearchModel(FriendInfo friendInfo, String query) {
        // 构建 FriendShipInfo（仅用于展示）
        FriendShipInfo friendShipInfo = new FriendShipInfo();
        FriendDetailInfo detailInfo = new FriendDetailInfo();
        detailInfo.setId(friendInfo.getUserId());
        detailInfo.setNickname(
                TextUtils.isEmpty(friendInfo.getRemark())
                        ? friendInfo.getName()
                        : friendInfo.getRemark());
        detailInfo.setPortraitUri(friendInfo.getPortraitUri());
        friendShipInfo.setUser(detailInfo);
        friendShipInfo.setDisplayName(friendInfo.getRemark());

        // 计算高亮位置
        String remark = friendInfo.getRemark();
        String name = friendInfo.getName();

        int remarkIndex = -1;
        int remarkIndexEnd = -1;
        int nameIndex = -1;
        int nameIndexEnd = -1;

        if (!TextUtils.isEmpty(remark)) {
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(remark, query);
            if (range != null) {
                remarkIndex = range.getStart();
                remarkIndexEnd = range.getEnd() + 1;
            }
        }

        if (!TextUtils.isEmpty(name)) {
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(name, query);
            if (range != null) {
                nameIndex = range.getStart();
                nameIndexEnd = range.getEnd() + 1;
            }
        }

        return new SearchFriendModel(
                friendShipInfo,
                R.layout.serach_fragment_recycler_friend_item,
                nameIndex,
                nameIndexEnd,
                remarkIndex,
                remarkIndexEnd);
    }

    @Override
    protected SearchModel createTitleModel() {
        return new SearchTitleModel(
                R.string.seal_ac_search_friend,
                R.layout.search_fragment_recycler_title_layout,
                SearchModel.SHOW_PRIORITY_FRIEND);
    }

    @Override
    protected SearchModel createShowMoreModel() {
        return new SearchShowMorModel(
                R.string.seal_search_more_friend,
                R.layout.search_frament_show_more_item,
                SearchModel.SHOW_PRIORITY_FRIEND);
    }
}
