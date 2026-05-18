package cn.rongcloud.im.openclaw.group.select;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.openclaw.group.list.GroupOpenClawRobotsActivity;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;

public class SelectOpenClawRobotActivity extends BaseActivity {
    public static Intent newIntent(Context context, String groupId) {
        Intent intent = new Intent(context, SelectOpenClawRobotActivity.class);
        intent.putExtra(GroupOpenClawRobotsActivity.EXTRA_GROUP_ID, groupId);
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
        Fragment fragment = new SelectOpenClawRobotFragment();
        fragment.setArguments(getIntent().getExtras());
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(io.rong.imkit.R.id.fl_fragment_container, fragment)
                .commit();
    }
}
