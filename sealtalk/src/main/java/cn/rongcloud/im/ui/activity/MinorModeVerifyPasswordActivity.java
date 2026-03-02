package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.StarPasswordTransformationMethod;
import io.rong.imkit.config.IMKitThemeManager;

/** 验证未成年人模式密码页面 用于关闭未成年人模式或退出登录时的验证 */
public class MinorModeVerifyPasswordActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_TYPE = "extra_type";
    public static final int TYPE_CLOSE_MODE = 1;
    public static final int TYPE_LOGOUT = 2;
    public static final int TYPE_DELETE_ACCOUNT = 3;

    private EditText etPassword;
    private ImageView ivTogglePassword;
    private TextView tvForgetPassword;
    private Button btnConfirm;

    private boolean isPasswordVisible = false;

    private MinorModeCache minorModeCache;
    private String currentUserId;
    private int verifyType;

    private final ActivityResultLauncher<Intent> resetPasswordLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // 重置密码成功
                            showToast(R.string.seal_minor_mode_reset_success);
                        }
                    });

    public static Intent newIntent(Context context, int type) {
        Intent intent = new Intent(context, MinorModeVerifyPasswordActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor_mode_verify_password);
        initView();
        initData();
    }

    private void initView() {
        etPassword = findViewById(R.id.et_password);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        tvForgetPassword = findViewById(R.id.tv_forget_password);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 默认使用星号隐藏密码
        etPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());

        ivTogglePassword.setOnClickListener(this);
        tvForgetPassword.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initData() {
        minorModeCache = MinorModeCache.getInstance();
        currentUserId = IMManager.getInstance().getCurrentId();
        verifyType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_CLOSE_MODE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_toggle_password) {
            togglePasswordVisibility();
        } else if (id == R.id.tv_forget_password) {
            // 忘记密码，进入重置密码页面
            Intent intent = MinorModeResetPasswordActivity.newIntent(this);
            resetPasswordLauncher.launch(intent);
        } else if (id == R.id.btn_confirm) {
            confirmVerifyPassword();
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 隐藏密码 - 使用星号显示
            etPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(
                    IMKitThemeManager.getAttrResId(this, R.attr.rc_minor_eye_close));
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(
                    IMKitThemeManager.getAttrResId(this, R.attr.rc_minor_eye_open));
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void confirmVerifyPassword() {
        String password = etPassword.getText().toString().trim();

        // 验证密码
        if (minorModeCache.verifyPasswordInvalid(currentUserId, password)) {
            showToast(R.string.seal_minor_mode_password_error);
            return;
        }

        // 验证成功
        if (verifyType == TYPE_CLOSE_MODE) {
            // 关闭未成年人模式
            minorModeCache.clearMinorPassword(currentUserId);
            showToast(R.string.seal_minor_mode_close_success);
        }

        setResult(RESULT_OK);
        finish();
    }
}
