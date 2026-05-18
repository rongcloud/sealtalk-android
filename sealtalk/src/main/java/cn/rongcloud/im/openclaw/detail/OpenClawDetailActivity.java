package cn.rongcloud.im.openclaw.detail;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import cn.rongcloud.im.openclaw.component.OpenClawBaseActivity;
import cn.rongcloud.im.openclaw.model.OpenClawRobotInfo;

public class OpenClawDetailActivity extends OpenClawBaseActivity {
    public static final String EXTRA_ROBOT = "extra_robot";
    public static final String EXTRA_TOKEN = "extra_token";
    public static final String EXTRA_FROM_CREATE = "extra_from_create";

    public static Intent newIntent(Context context, OpenClawRobotInfo robot, String token) {
        Intent intent = new Intent(context, OpenClawDetailActivity.class);
        intent.putExtra(EXTRA_ROBOT, robot);
        intent.putExtra(EXTRA_TOKEN, token);
        return intent;
    }

    public static Intent newCreateResultIntent(
            Context context, OpenClawRobotInfo robot, String token) {
        Intent intent = newIntent(context, robot, token);
        intent.putExtra(EXTRA_FROM_CREATE, true);
        return intent;
    }

    @Override
    protected String getPageTitle() {
        boolean fromCreate =
                getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_CREATE, false);
        return getString(
                fromCreate ? R.string.openclaw_add_ai_robot : R.string.openclaw_detail_title);
    }

    @Override
    protected Fragment createFragment() {
        OpenClawDetailFragment fragment = new OpenClawDetailFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
