package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.newdesign.search.SearchActivity;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.SealSearchActivity;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.ui.dialog.MorePopWindow;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.usermanage.component.HeadComponent;
import io.rong.imkit.usermanage.component.SearchComponent;

/**
 * 聊天列表包装Fragment 因为 ConversationListFragment 是 Kit 层代码不能直接修改， 所以创建这个包装类来添加 HeadComponent 和
 * SearchComponent
 */
public class SealConversationListFragment extends Fragment
        implements MorePopWindow.OnPopWindowItemClickListener {
    private HeadComponent headComponent;
    private SearchComponent searchComponent;
    private ConversationListFragment conversationListFragment;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.seal_fragment_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headComponent = view.findViewById(R.id.head_component);
        searchComponent = view.findViewById(R.id.search_component);
        headComponent.setBackground(null);
        searchComponent.setBackground(null);

        // 配置标题组件
        setupHeadComponent();

        // 配置搜索组件
        setupSearchComponent();

        // 嵌入 ConversationListFragment
        if (savedInstanceState == null) {
            conversationListFragment = new ConversationListFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.conversation_list_container, conversationListFragment);
            transaction.commit();
        }
    }

    /** 配置标题组件 */
    private void setupHeadComponent() {
        // 设置标题
        headComponent.setTitleText(R.string.seal_main_chat_title);

        // 默认不显示左侧返回按钮
        headComponent.getLeftTextView().setVisibility(View.GONE);

        // 设置右侧按钮
        headComponent.setRightTextDrawable(
                IMKitThemeManager.dynamicResource(
                        getContext(), R.attr.rc_seal_main_add, R.drawable.seal_ic_main_more));
        headComponent.getRightTextView().setVisibility(View.VISIBLE);
        headComponent.setRightClickListener(
                v -> {
                    if (getActivity() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        MorePopWindow morePopWindow = new MorePopWindow(mainActivity, this);
                        morePopWindow.showPopupWindow(
                                headComponent.getRightTextView(), 0.8f, -getXOffset(), 0);
                    }
                });
    }

    /** 配置搜索组件 */
    private void setupSearchComponent() {
        searchComponent.setSearchHint(R.string.seal_search);
        searchComponent.setVisibility(View.VISIBLE);
        searchComponent.setSearchClickListener(
                v -> {
                    if (SealTalkDebugTestActivity.isUserManagementEnabled(
                            SealApp.getApplication())) {
                        startActivity(SearchActivity.newIntent(getActivity()));
                    } else {
                        Intent intent = new Intent(getActivity(), SealSearchActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private int getXOffset() {
        if (getResources() != null
                && headComponent != null
                && headComponent.getRightTextView() != null) {
            float popSelfXOffset =
                    getResources().getDimension(R.dimen.seal_main_title_popup_width)
                            - headComponent.getRightTextView().getWidth();
            return (int) (popSelfXOffset);
        }
        return 0;
    }

    @Override
    public void onStartChartClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onStartChartClick();
        }
    }

    @Override
    public void onCreateGroupClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onCreateGroupClick();
        }
    }

    @Override
    public void onAddFriendClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onAddFriendClick();
        }
    }

    @Override
    public void onScanClick() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onScanClick();
        }
    }
}
