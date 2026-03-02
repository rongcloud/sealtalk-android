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

/** 修改未成年人模式密码页面 */
public class MinorModeModifyPasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private ImageView ivToggleOldPassword;
    private ImageView ivToggleNewPassword;
    private ImageView ivToggleConfirmNewPassword;
    private Button btnConfirm;

    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;

    private MinorModeCache minorModeCache;
    private String currentUserId;

    public static Intent newIntent(Context context) {
        return new Intent(context, MinorModeModifyPasswordActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor_mode_modify_password);
        initView();
        initData();
    }

    private void initView() {
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        ivToggleOldPassword = findViewById(R.id.iv_toggle_old_password);
        ivToggleNewPassword = findViewById(R.id.iv_toggle_new_password);
        ivToggleConfirmNewPassword = findViewById(R.id.iv_toggle_confirm_new_password);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 默认使用星号隐藏密码
        etOldPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());
        etNewPassword.setTransformationMethod(StarPasswordTransformationMethod.getInstance());
        etConfirmNewPassword.setTransformationMethod(
                StarPasswordTransformationMethod.getInstance());

        ivToggleOldPassword.setOnClickListener(this);
        ivToggleNewPassword.setOnClickListener(this);
        ivToggleConfirmNewPassword.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initData() {
        minorModeCache = MinorModeCache.getInstance();
        currentUserId = IMManager.getInstance().getCurrentId();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_toggle_old_password) {
            togglePasswordVisibility(etOldPassword, ivToggleOldPassword, isOldPasswordVisible);
            isOldPasswordVisible = !isOldPasswordVisible;
        } else if (id == R.id.iv_toggle_new_password) {
            togglePasswordVisibility(etNewPassword, ivToggleNewPassword, isNewPasswordVisible);
            isNewPasswordVisible = !isNewPasswordVisible;
        } else if (id == R.id.iv_toggle_confirm_new_password) {
            togglePasswordVisibility(
                    etConfirmNewPassword, ivToggleConfirmNewPassword, isConfirmNewPasswordVisible);
            isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible;
        } else if (id == R.id.btn_confirm) {
            confirmModifyPassword();
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
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(
                    IMKitThemeManager.getAttrResId(this, R.attr.rc_minor_eye_open));
        }
        editText.setSelection(editText.getText().length());
    }

    private void confirmModifyPassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // 验证原密码是否正确
        if (minorModeCache.verifyPasswordInvalid(currentUserId, oldPassword)) {
            showToast(R.string.seal_minor_mode_old_password_error);
            return;
        }

        // 验证新密码格式
        if (MinorModeCache.isInvalidPasswordFormat(newPassword)) {
            showToast(R.string.seal_minor_mode_password_format_error);
            return;
        }

        // 验证新密码与原密码不同
        if (oldPassword.equals(newPassword)) {
            showToast(R.string.seal_minor_mode_new_password_same_as_old);
            return;
        }

        // 验证两次新密码是否一致
        if (!newPassword.equals(confirmNewPassword)) {
            showToast(R.string.seal_minor_mode_new_password_not_match);
            return;
        }

        // 保存新密码
        minorModeCache.setMinorPassword(currentUserId, newPassword);
        showToast(R.string.seal_minor_mode_modify_success);
        finish();
    }
}
