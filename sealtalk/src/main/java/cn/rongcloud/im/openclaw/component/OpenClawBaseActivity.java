package cn.rongcloud.im.openclaw.component;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;

public abstract class OpenClawBaseActivity extends TitleBaseActivity {
    private int containerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            finish();
            return;
        }
        getTitleBar().setTitle(getPageTitle());
        FrameLayout container = new FrameLayout(this);
        containerId = View.generateViewId();
        container.setId(containerId);
        setContentView(container);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(containerId, createFragment())
                    .commit();
        }
    }

    protected abstract String getPageTitle();

    protected abstract Fragment createFragment();
}
