package cn.rongcloud.im.openclaw.guide;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import cn.rongcloud.im.openclaw.component.OpenClawBaseActivity;

public class OpenClawGuideActivity extends OpenClawBaseActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, OpenClawGuideActivity.class);
    }

    @Override
    protected String getPageTitle() {
        return getString(R.string.openclaw_add_ai_robot);
    }

    @Override
    protected Fragment createFragment() {
        return new OpenClawGuideFragment();
    }
}
