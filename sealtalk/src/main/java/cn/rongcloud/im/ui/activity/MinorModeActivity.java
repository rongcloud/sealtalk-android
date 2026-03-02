package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.BaseActivity;

/** 未成年人模式状态页面 根据是否已设置密码来显示开启或关闭状态 */
public class MinorModeActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvStatusTitle;
    private Button btnToggleMode;
    private TextView tvModifyPassword;

    private MinorModeCache minorModeCache;
    private String currentUserId;

    private final ActivityResultLauncher<Intent> setPasswordLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // 设置密码成功，刷新页面状态
                            updateUI();
                        }
                    });

    private final ActivityResultLauncher<Intent> verifyPasswordLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // 关闭成功，刷新页面状态
                            updateUI();
                        }
                    });

    public static Intent newIntent(Context context) {
        return new Intent(context, MinorModeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor_mode);
        initView();
        initData();
    }

    private void initView() {
        tvStatusTitle = findViewById(R.id.tv_status_title);
        btnToggleMode = findViewById(R.id.btn_toggle_mode);
        tvModifyPassword = findViewById(R.id.tv_modify_password);

        btnToggleMode.setOnClickListener(this);
        tvModifyPassword.setOnClickListener(this);
    }

    private void initData() {
        minorModeCache = MinorModeCache.getInstance();
        currentUserId = IMManager.getInstance().getCurrentId();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        boolean isEnabled = minorModeCache.isMinorModeEnabled(currentUserId);
        if (isEnabled) {
            // 已开启状态
            tvStatusTitle.setText(R.string.seal_minor_mode_opened);
            btnToggleMode.setText(R.string.seal_minor_mode_disable);
            tvModifyPassword.setVisibility(View.VISIBLE);
        } else {
            // 已关闭状态
            tvStatusTitle.setText(R.string.seal_minor_mode_closed);
            btnToggleMode.setText(R.string.seal_minor_mode_enable);
            tvModifyPassword.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_toggle_mode) {
            boolean isEnabled = minorModeCache.isMinorModeEnabled(currentUserId);
            if (isEnabled) {
                // 关闭未成年人模式，需要验证密码
                Intent intent =
                        MinorModeVerifyPasswordActivity.newIntent(
                                this, MinorModeVerifyPasswordActivity.TYPE_CLOSE_MODE);
                verifyPasswordLauncher.launch(intent);
            } else {
                // 开启未成年人模式，需要设置密码
                Intent intent = MinorModeSetPasswordActivity.newIntent(this);
                setPasswordLauncher.launch(intent);
            }
        } else if (id == R.id.tv_modify_password) {
            // 修改密码
            startActivity(MinorModeModifyPasswordActivity.newIntent(this));
        }
    }
}
