package cn.rongcloud.im.openclaw.create;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import cn.rongcloud.im.openclaw.component.OpenClawBaseActivity;

public class OpenClawCreateActivity extends OpenClawBaseActivity {
    private OpenClawCreateFragment fragment;

    public static Intent newIntent(Context context) {
        return new Intent(context, OpenClawCreateActivity.class);
    }

    @Override
    protected String getPageTitle() {
        return getString(R.string.openclaw_add_ai_robot);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected Fragment createFragment() {
        fragment = new OpenClawCreateFragment();
        return fragment;
    }
}
