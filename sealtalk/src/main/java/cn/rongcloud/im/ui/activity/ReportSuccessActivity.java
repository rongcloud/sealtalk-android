package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cn.rongcloud.im.R;
import io.rong.imkit.activity.RongBaseActivity;

/**
 * 举报成功页面
 *
 * @author rongcloud
 */
public class ReportSuccessActivity extends RongBaseActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, ReportSuccessActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_success);

        if (mTitleBar != null) {
            mTitleBar.setTitle(getString(R.string.report_title));
            mTitleBar.setRightVisible(false);
        }

        Button closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(
                v -> {
                    finishAllPreviousActivities();
                });
    }

    private void finishAllPreviousActivities() {
        // 假设你使用了ActivityManager或其他关闭所有前置活动的逻辑
        Intent intent = new Intent("CLOSE_ALL");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish(); // 最后关闭当前的 ReportSuccessActivity
    }
}
