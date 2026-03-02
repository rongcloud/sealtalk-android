package cn.rongcloud.im.newdesign.search;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.rong.common.rlog.RLog;
import io.rong.imkit.base.BaseViewModel;
import io.rong.imkit.usermanage.handler.FriendInfoHandler;
import io.rong.imkit.usermanage.handler.GroupJoinedSearchPagedHandler;
import io.rong.imlib.model.FriendInfo;
import io.rong.imlib.model.GroupInfo;
import io.rong.imlib.model.SearchConversationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索 ViewModel
 *
 * <p>直接使用 Lib 的接口和数据类型，不依赖 Task
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchViewModel extends BaseViewModel {

    private static final String TAG = SearchViewModel.class.getSimpleName();
    private static final String ARG_SEARCH_TYPES = "search_types";

    // 每种类型独立的 LiveData，直接使用 Lib 的数据类型
    private final MutableLiveData<List<FriendInfo>> friendResultData = new MutableLiveData<>();
    private final MutableLiveData<List<GroupInfo>> groupResultData = new MutableLiveData<>();
    private final MutableLiveData<List<SearchConversationResult>> conversationResultData =
            new MutableLiveData<>();

    private FriendInfoHandler friendInfoHandler;
    private GroupJoinedSearchPagedHandler groupJoinedSearchPagedHandler;
    private SearchConversationHandler searchConversationHandler;

    @SearchType.Type private int searchTypes;
    private String currentQuery = "";

    public SearchViewModel(@NonNull Bundle arguments) {
        super(arguments);
        searchTypes = arguments.getInt(ARG_SEARCH_TYPES, SearchType.ALL);
        initHandlers();
    }

    /** 初始化各个 Handler */
    private void initHandlers() {
        // 初始化好友搜索 Handler
        if (SearchType.contains(searchTypes, SearchType.FRIEND)) {
            friendInfoHandler = new FriendInfoHandler();
            friendInfoHandler.addDataChangeListener(
                    FriendInfoHandler.KEY_SEARCH_FRIENDS,
                    friendInfos -> {
                        RLog.d(
                                TAG,
                                "Friend search result: "
                                        + (friendInfos != null ? friendInfos.size() : 0));
                        // 直接传递 Lib 的数据类型
                        friendResultData.postValue(
                                friendInfos != null ? friendInfos : new ArrayList<>());
                    });
        }

        // 初始化群组搜索 Handler
        if (SearchType.contains(searchTypes, SearchType.GROUP)) {
            groupJoinedSearchPagedHandler = new GroupJoinedSearchPagedHandler();
            groupJoinedSearchPagedHandler.addDataChangeListener(
                    GroupJoinedSearchPagedHandler.KEY_SEARCH_JOINED_GROUPS,
                    groupInfos -> {
                        RLog.d(
                                TAG,
                                "Group search result: "
                                        + (groupInfos != null ? groupInfos.size() : 0));
                        // 直接传递 Lib 的数据类型
                        groupResultData.postValue(
                                groupInfos != null ? groupInfos : new ArrayList<>());
                    });
        }

        // 初始化会话搜索 Handler
        if (SearchType.contains(searchTypes, SearchType.CONVERSATION)) {
            searchConversationHandler = new SearchConversationHandler();
            searchConversationHandler.addDataChangeListener(
                    SearchConversationHandler.KEY_SEARCH_CONVERSATIONS,
                    results -> {
                        RLog.d(
                                TAG,
                                "Conversation search result: "
                                        + (results != null ? results.size() : 0));
                        // 直接传递 Lib 的数据类型
                        conversationResultData.postValue(
                                results != null ? results : new ArrayList<>());
                    });
        }
    }

    /**
     * 搜索
     *
     * @param query 搜索关键字
     */
    public void search(@NonNull String query) {
        if (TextUtils.isEmpty(query)) {
            clearResults();
            return;
        }

        currentQuery = query;

        // 根据配置的搜索类型执行搜索
        if (SearchType.contains(searchTypes, SearchType.FRIEND)) {
            friendInfoHandler.searchFriendsInfo(query);
        }

        if (SearchType.contains(searchTypes, SearchType.GROUP)) {
            groupJoinedSearchPagedHandler.searchJoinedGroups(query);
        }

        if (SearchType.contains(searchTypes, SearchType.CONVERSATION)) {
            searchConversationHandler.searchConversations(query);
        }
    }

    /** 清除搜索结果 */
    private void clearResults() {
        if (SearchType.contains(searchTypes, SearchType.FRIEND)) {
            friendResultData.setValue(new ArrayList<>());
        }
        if (SearchType.contains(searchTypes, SearchType.GROUP)) {
            groupResultData.setValue(new ArrayList<>());
        }
        if (SearchType.contains(searchTypes, SearchType.CONVERSATION)) {
            conversationResultData.setValue(new ArrayList<>());
        }
    }

    // LiveData Getters - 返回 Lib 的原生数据类型
    public LiveData<List<FriendInfo>> getFriendResultData() {
        return friendResultData;
    }

    public LiveData<List<GroupInfo>> getGroupResultData() {
        return groupResultData;
    }

    public LiveData<List<SearchConversationResult>> getConversationResultData() {
        return conversationResultData;
    }

    @SearchType.Type
    public int getSearchTypes() {
        return searchTypes;
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (friendInfoHandler != null) {
            friendInfoHandler.stop();
        }
        if (groupJoinedSearchPagedHandler != null) {
            groupJoinedSearchPagedHandler.stop();
        }
        if (searchConversationHandler != null) {
            searchConversationHandler.stop();
        }
    }
}
