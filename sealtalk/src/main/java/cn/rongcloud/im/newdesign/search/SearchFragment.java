package cn.rongcloud.im.newdesign.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.newdesign.search.processor.ConversationSearchResultProcessor;
import cn.rongcloud.im.newdesign.search.processor.FriendSearchResultProcessor;
import cn.rongcloud.im.newdesign.search.processor.GroupSearchResultProcessor;
import cn.rongcloud.im.newdesign.search.processor.ISearchResultProcessor;
import cn.rongcloud.im.newdesign.searchmsg.SearchMessageActivity;
import cn.rongcloud.im.ui.adapter.SearchAdapter;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnChatItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.ui.interfaces.OnShowMoreClickListener;
import cn.rongcloud.im.viewmodel.SearchMessageModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.component.SearchComponent;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索 Fragment（重构版）
 *
 * <p>新版搜索页面，使用 Processor 模式实现搜索逻辑的完全解耦
 *
 * <p>每种搜索类型（好友、群组、会话、消息）都有独立的 Processor
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchFragment extends BaseViewModelFragment<SearchViewModel>
        implements OnContactItemClickListener,
                OnGroupItemClickListener,
                OnChatItemClickListener,
                OnShowMoreClickListener,
                OnMessageRecordClickListener {

    private static final String ARG_SEARCH_TYPES = "search_types";
    private static final String ARG_INIT_QUERY = "init_query";

    private HeadComponent headComponent;
    private SearchComponent searchComponent;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmpty;
    private TextView tvEmptyHint;

    private SearchAdapter searchAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    /** 搜索结果处理器列表（核心：可自由组合的 Processors） */
    private final List<ISearchResultProcessor> processors = new ArrayList<>();

    private String initQuery; // 初始搜索关键字

    @NonNull
    @Override
    protected SearchViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle)).get(SearchViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull Context context,
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle args) {
        View view = inflater.inflate(R.layout.qrcode_fragment_search, container, false);
        initViews(view);
        return view;
    }

    /** 初始化视图 */
    private void initViews(View view) {
        headComponent = view.findViewById(R.id.head_component);
        searchComponent = view.findViewById(R.id.search_component);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        llEmpty = view.findViewById(R.id.ll_empty);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);

        // 设置标题
        headComponent.setTitleText(R.string.seal_ac_search);
        headComponent.setLeftClickListener(v -> finishActivity());

        // 设置搜索组件
        searchComponent.setSearchHint(R.string.seal_search_hint);
        searchComponent.setSearchQueryListener(
                new SearchComponent.OnSearchQueryListener() {
                    @Override
                    public void onSearch(String query) {
                        performSearch(query);
                    }
                });

        // 设置 RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(this, this, this, this, this);
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void onViewReady(@NonNull SearchViewModel viewModel) {
        // 获取初始搜索关键字
        Bundle args = getArguments();
        if (args != null) {
            initQuery = args.getString(ARG_INIT_QUERY);
        }

        // 根据搜索类型动态创建 Processors（自由组合）
        initProcessors(viewModel);

        // 如果有初始搜索关键字，自动执行搜索
        if (!TextUtils.isEmpty(initQuery)) {
            searchComponent.setSearchContent(initQuery);
            searchHandler.postDelayed(
                    () -> {
                        performSearch(initQuery);
                    },
                    300);
        }
    }

    /**
     * 初始化搜索结果处理器（根据搜索类型自由组合）
     *
     * @param viewModel SearchViewModel
     */
    private void initProcessors(@NonNull SearchViewModel viewModel) {
        int searchTypes = viewModel.getSearchTypes();

        // 根据搜索类型动态添加 Processors
        if (SearchType.contains(searchTypes, SearchType.FRIEND)) {
            processors.add(new FriendSearchResultProcessor());
        }

        if (SearchType.contains(searchTypes, SearchType.GROUP)) {
            processors.add(new GroupSearchResultProcessor());
        }

        if (SearchType.contains(searchTypes, SearchType.CONVERSATION)) {
            processors.add(new ConversationSearchResultProcessor());
        }

        // 初始化所有 Processors
        for (ISearchResultProcessor processor : processors) {
            processor.initialize(viewModel, this::mergeAndUpdateResults);
            processor.setLifecycleOwner(this);
        }
    }

    /** 执行搜索 */
    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            clearResults();
            return;
        }
        getViewModel().search(query);
    }

    /**
     * 合并并更新搜索结果（核心方法：超级简洁）
     *
     * <p>所有复杂的数据转换逻辑都由各个 Processor 处理
     *
     * <p>Fragment 只负责合并结果并展示
     */
    private void mergeAndUpdateResults() {
        List<SearchModel> allResults = new ArrayList<>();

        // 检查是否只搜索单一类型（用于判断是否需要限制显示条数）
        boolean isSingleType = isSingleSearchType();

        // 遍历所有 Processors，生成展示数据
        for (ISearchResultProcessor processor : processors) {
            if (processor.hasResults()) {
                allResults.addAll(processor.generateDisplayModels(isSingleType));
            }
        }

        // 更新显示
        if (allResults.isEmpty()) {
            showEmptyView();
        } else {
            showResultsView();
            searchAdapter.updateData(allResults);
        }
    }

    /** 检查是否只搜索单一类型 */
    private boolean isSingleSearchType() {
        return processors.size() == 1;
    }

    /** 显示空状态视图 */
    private void showEmptyView() {
        rvSearchResults.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
        tvEmptyHint.setText(R.string.seal_search_hint);
    }

    /** 显示结果视图 */
    private void showResultsView() {
        llEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    /** 清除搜索结果 */
    private void clearResults() {
        for (ISearchResultProcessor processor : processors) {
            processor.clearResults();
        }
        searchAdapter.updateData(new ArrayList<>());
        showEmptyView();
    }

    // ========== 实现接口方法（点击事件处理） ==========

    @Override
    public void onItemContactClick(FriendShipInfo friendShipInfo) {
        // 延迟跳转，等待键盘隐藏
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            String displayName = friendShipInfo.getDisplayName();
                            if (TextUtils.isEmpty(displayName)) {
                                displayName = friendShipInfo.getUser().getNickname();
                            }
                            RongIM.getInstance()
                                    .startPrivateChat(
                                            getContext(),
                                            friendShipInfo.getUser().getId(),
                                            displayName);
                        },
                        100);
    }

    @Override
    public void onGroupClicked(GroupEntity groupEntity) {
        // 延迟跳转，等待键盘隐藏
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            RongIM.getInstance()
                                    .startGroupChat(
                                            getContext(),
                                            groupEntity.getId(),
                                            groupEntity.getName());
                        },
                        100);
    }

    @Override
    public void OnChatItemClicked(SearchConversationModel searchConversationModel) {
        // 延迟跳转，等待键盘隐藏
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            SearchConversationResult result = searchConversationModel.getBean();
                            if (result.getMatchCount() == 1) {
                                RongIM.getInstance()
                                        .startConversation(
                                                getContext(),
                                                ConversationIdentifier.obtain(
                                                        result.getConversation()),
                                                searchConversationModel.getName(),
                                                result.getConversation().getSentTime());
                            } else {
                                startActivity(
                                        SearchMessageActivity.newIntent(
                                                getContext(),
                                                ConversationIdentifier.obtain(
                                                        result.getConversation()),
                                                getViewModel().getCurrentQuery()));
                            }
                        },
                        100);
    }

    @Override
    public void onSearchShowMoreClicked(int type) {
        // 获取当前搜索关键字
        String query = getViewModel().getCurrentQuery();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        // 根据类型跳转到新的搜索页面，只显示该类型的完整结果
        if (type == R.string.seal_search_more_friend) {
            startActivity(SearchActivity.newIntent(getContext(), SearchType.FRIEND, query));
        } else if (type == R.string.seal_search_more_group) {
            startActivity(SearchActivity.newIntent(getContext(), SearchType.GROUP, query));
        } else if (type == R.string.seal_search_more_chatting_records) {
            startActivity(SearchActivity.newIntent(getContext(), SearchType.CONVERSATION, query));
        }
    }

    @Override
    public void onMessageRecordClick(SearchMessageModel searchMessageModel) {
        // 延迟跳转，等待键盘隐藏
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            Message message = searchMessageModel.getBean();
                            RongIM.getInstance()
                                    .startConversation(
                                            getContext(),
                                            ConversationIdentifier.obtain(message),
                                            searchMessageModel.getName(),
                                            message.getSentTime() + 2);
                        },
                        100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 释放所有 Processors
        for (ISearchResultProcessor processor : processors) {
            processor.destroy();
        }
        processors.clear();

        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}
