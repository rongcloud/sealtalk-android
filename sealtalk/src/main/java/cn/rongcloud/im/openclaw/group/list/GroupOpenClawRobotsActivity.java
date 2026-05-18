package cn.rongcloud.im.openclaw.group.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;

public class GroupOpenClawRobotsActivity extends BaseActivity {
    public static final String EXTRA_GROUP_ID = "extra_group_id";
    public static final String EXTRA_CAN_MANAGE = "extra_can_manage";

    public static Intent newIntent(Context context, String groupId, boolean canManage) {
        Intent intent = new Intent(context, GroupOpenClawRobotsActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_CAN_MANAGE, canManage);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            finish();
            return;
        }
        setContentView(io.rong.imkit.R.layout.rc_activity);
        Fragment fragment = new GroupOpenClawRobotsFragment();
        fragment.setArguments(getIntent().getExtras());
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }
}
