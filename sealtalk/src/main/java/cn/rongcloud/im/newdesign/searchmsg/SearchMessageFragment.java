package cn.rongcloud.im.newdesign.searchmsg;

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
import cn.rongcloud.im.ui.adapter.SearchAdapter;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.viewmodel.SearchMessageModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.base.BaseViewModelFragment;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.usermanage.ViewModelFactory;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.component.SearchComponent;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息搜索 Fragment
 *
 * <p>独立的消息搜索页面，显示所有搜索结果
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchMessageFragment extends BaseViewModelFragment<SearchMessageViewModel>
        implements OnMessageRecordClickListener {

    static final String ARG_INIT_QUERY = "init_query";
    static final String ARG_CONVERSATION_IDENTIFIER = "conversation_identifier";

    private HeadComponent headComponent;
    private SearchComponent searchComponent;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmpty;
    private TextView tvEmptyHint;

    private SearchAdapter searchAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    private String initQuery; // 初始搜索关键字

    @NonNull
    @Override
    protected SearchMessageViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new ViewModelProvider(this, new ViewModelFactory(bundle))
                .get(SearchMessageViewModel.class);
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
        headComponent.setTitleText(R.string.seal_search_message_title);
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
        searchAdapter = new SearchAdapter(null, null, null, null, this);
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void onViewReady(@NonNull SearchMessageViewModel viewModel) {
        // 获取初始搜索关键字
        Bundle args = getArguments();
        if (args != null) {
            initQuery = args.getString(ARG_INIT_QUERY);
        }

        // 观察搜索结果
        viewModel.getMessageResultData().observe(this, this::updateSearchResults);

        // 如果有初始搜索关键字，自动执行搜索
        if (!TextUtils.isEmpty(initQuery)) {
            searchHandler.postDelayed(
                    () -> {
                        performSearch(initQuery);
                    },
                    300);
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

    /** 更新搜索结果 */
    private void updateSearchResults(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            showEmptyView();
            return;
        }

        String query = getViewModel().getCurrentQuery();
        List<SearchModel> displayModels = convertToSearchModels(messages, query);

        showResultsView();
        searchAdapter.updateData(displayModels);
    }

    /**
     * 将 Message 列表转换为 SearchModel 列表
     *
     * @param messages 消息列表
     * @param query 搜索关键字
     * @return SearchModel 列表
     */
    private List<SearchModel> convertToSearchModels(List<Message> messages, String query) {
        List<SearchModel> result = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            return result;
        }

        for (Message message : messages) {
            String name = "";
            String portraitUrl = "";

            // 获取发送者信息
            UserInfo userInfo =
                    RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (userInfo != null) {
                name = userInfo.getName();
                portraitUrl =
                        userInfo.getPortraitUri() != null
                                ? userInfo.getPortraitUri().toString()
                                : "";
            }

            if (TextUtils.isEmpty(name)) {
                name = message.getSenderUserId();
            }

            SearchMessageModel model =
                    new SearchMessageModel(
                            message,
                            R.layout.search_fragment_recycler_chatting_records_list,
                            name,
                            portraitUrl,
                            query);
            result.add(model);
        }

        return result;
    }

    /** 显示空状态视图 */
    private void showEmptyView() {
        rvSearchResults.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
        tvEmptyHint.setText(R.string.seal_search_message_empty);
    }

    /** 显示结果视图 */
    private void showResultsView() {
        llEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    /** 清除搜索结果 */
    private void clearResults() {
        searchAdapter.updateData(new ArrayList<>());
        showEmptyView();
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
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}
