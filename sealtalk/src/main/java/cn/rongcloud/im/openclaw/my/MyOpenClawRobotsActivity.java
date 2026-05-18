package cn.rongcloud.im.openclaw.my;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import cn.rongcloud.im.openclaw.component.OpenClawBaseActivity;

public class MyOpenClawRobotsActivity extends OpenClawBaseActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, MyOpenClawRobotsActivity.class);
    }

    @Override
    protected String getPageTitle() {
        return getString(R.string.openclaw_my_ai_robots);
    }

    @Override
    protected Fragment createFragment() {
        return new MyOpenClawRobotsFragment();
    }
}
