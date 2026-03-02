package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.StarPasswordTransformationMethod;
import io.rong.imkit.config.IMKitThemeManager;

/** 设置未成年人模式密码页面 */
public class MinorModeSetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText etPassword;
    private EditText etConfirmPassword;
    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirmPassword;
    private Button btnConfirm;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private MinorModeCache minorModeCache;
    private String currentUserId;

    public static Intent newIntent(Context context) {
        return new Intent(context, MinorModeSetPasswordActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor_mode_set_password);
        initView();
        initData();
    }

    private void initView() {
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 默认使用星号隐藏密码
        etPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());
        etConfirmPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());

        ivTogglePassword.setOnClickListener(this);
        ivToggleConfirmPassword.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initData() {
        minorModeCache = MinorModeCache.getInstance();
        currentUserId = IMManager.getInstance().getCurrentId();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_toggle_password) {
            togglePasswordVisibility(etPassword, ivTogglePassword, isPasswordVisible);
            isPasswordVisible = !isPasswordVisible;
        } else if (id == R.id.iv_toggle_confirm_password) {
            togglePasswordVisibility(
                    etConfirmPassword, ivToggleConfirmPassword, isConfirmPasswordVisible);
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        } else if (id == R.id.btn_confirm) {
            confirmSetPassword();
        }
    }

    private void togglePasswordVisibility(
            EditText editText, ImageView imageView, boolean isVisible) {
        if (isVisible) {
            // 隐藏密码 - 使用星号显示
            editText.setTransformationMethod(StarPasswordTransformationMethod.getInstance());
            imageView.setImageResource(
                    IMKitThemeManager.getAttrResId(this, R.attr.rc_minor_eye_close));
        } else {
            // 显示密码
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(
                    IMKitThemeManager.getAttrResId(this, R.attr.rc_minor_eye_open));
        }
        // 将光标移动到末尾
        editText.setSelection(editText.getText().length());
    }

    private void confirmSetPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证密码格式
        if (MinorModeCache.isInvalidPasswordFormat(password)) {
            showToast(R.string.seal_minor_mode_password_format_error);
            return;
        }

        // 验证两次密码是否一致
        if (!password.equals(confirmPassword)) {
            showToast(R.string.seal_minor_mode_password_not_match);
            return;
        }

        // 保存密码
        minorModeCache.setMinorPassword(currentUserId, password);
        showToast(R.string.seal_minor_mode_set_success);

        setResult(RESULT_OK);
        finish();
    }
}
