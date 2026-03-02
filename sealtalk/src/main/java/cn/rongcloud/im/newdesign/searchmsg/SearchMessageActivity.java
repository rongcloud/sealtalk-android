package cn.rongcloud.im.newdesign.searchmsg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.ui.BaseActivity;
import io.rong.imlib.model.ConversationIdentifier;

/**
 * 消息搜索 Activity
 *
 * <p>独立的消息搜索页面
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchMessageActivity extends BaseActivity {

    private static final String EXTRA_INIT_QUERY = "init_query";
    private static final String EXTRA_CONVERSATION_IDENTIFIER = "conversation_identifier";
    private Fragment fragment;

    /**
     * 创建 SearchMessageActivity 的 Intent
     *
     * @param context 上下文
     * @param conversationIdentifier 目标会话
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(
            @NonNull Context context, @NonNull ConversationIdentifier conversationIdentifier) {
        return newIntent(context, conversationIdentifier, null);
    }

    /**
     * 创建 SearchMessageActivity 的 Intent（带初始搜索关键字）
     *
     * @param context 上下文
     * @param conversationIdentifier 目标会话
     * @param initQuery 初始搜索关键字（可为空）
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull ConversationIdentifier conversationIdentifier,
            String initQuery) {
        Intent intent = new Intent(context, SearchMessageActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_IDENTIFIER, conversationIdentifier);
        if (initQuery != null) {
            intent.putExtra(EXTRA_INIT_QUERY, initQuery);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.rong.imkit.R.layout.rc_activity);

        fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }

    /**
     * 创建 Fragment
     *
     * @return Fragment 实例
     */
    @NonNull
    protected Fragment createFragment() {
        Bundle bundle = new Bundle();

        // 从 Intent 中获取初始搜索关键字
        String initQuery = getIntent().getStringExtra(EXTRA_INIT_QUERY);
        if (initQuery != null) {
            bundle.putString(SearchMessageFragment.ARG_INIT_QUERY, initQuery);
        }

        ConversationIdentifier conversationIdentifier =
                getIntent().getParcelableExtra(EXTRA_CONVERSATION_IDENTIFIER);
        if (conversationIdentifier != null) {
            bundle.putParcelable(
                    SearchMessageFragment.ARG_CONVERSATION_IDENTIFIER, conversationIdentifier);
        }

        SearchMessageFragment fragment = new SearchMessageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
