package cn.rongcloud.im.newdesign.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.ui.BaseActivity;

/**
 * 搜索 Activity
 *
 * <p>新版搜索页面，支持搜索好友、群组、会话、消息
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchActivity extends BaseActivity {

    private static final String EXTRA_SEARCH_TYPES = "search_types";
    private static final String EXTRA_INIT_QUERY = "init_query";
    private Fragment fragment;

    /**
     * 创建 SearchActivity 的 Intent（搜索全部）
     *
     * @param context 上下文
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return newIntent(context, SearchType.ALL, null);
    }

    /**
     * 创建 SearchActivity 的 Intent（指定搜索类型）
     *
     * @param context 上下文
     * @param searchTypes 搜索类型，可以使用 | 组合多个类型，例如：SearchType.FRIEND | SearchType.GROUP
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @SearchType.Type int searchTypes) {
        return newIntent(context, searchTypes, null);
    }

    /**
     * 创建 SearchActivity 的 Intent（指定搜索类型和初始搜索关键字）
     *
     * @param context 上下文
     * @param searchTypes 搜索类型
     * @param initQuery 初始搜索关键字（可为空）
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(
            @NonNull Context context, @SearchType.Type int searchTypes, String initQuery) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_TYPES, searchTypes);
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

        // 从 Intent 中获取搜索类型和初始搜索关键字
        int searchTypes = getIntent().getIntExtra(EXTRA_SEARCH_TYPES, SearchType.ALL);
        String initQuery = getIntent().getStringExtra(EXTRA_INIT_QUERY);

        bundle.putInt("search_types", searchTypes);
        if (initQuery != null) {
            bundle.putString("init_query", initQuery);
        }

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
