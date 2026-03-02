package cn.rongcloud.im.newdesign.qrcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.ui.BaseActivity;

/**
 * 加入群聊 Activity
 *
 * <p>显示加入群聊界面，允许用户加入指定的群组
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class JoinGroupActivity extends BaseActivity {

    private static final String EXTRA_GROUP_ID = "group_id";

    private Fragment fragment;

    /**
     * 创建加入群聊的 Intent
     *
     * @param context 上下文
     * @param groupId 群组 ID
     * @return Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String groupId) {
        Intent intent = new Intent(context, JoinGroupActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
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
        Bundle bundle = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        JoinGroupFragment fragment = new JoinGroupFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
