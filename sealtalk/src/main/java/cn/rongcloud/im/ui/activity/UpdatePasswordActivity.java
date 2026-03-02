package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;

/** 修改密码 */
public class UpdatePasswordActivity extends TitleBaseActivity {
    private EditText oldPasswordEt;
    private EditText newPasswordEt;
    private EditText confirmPasswordEt;
    private Button updateBtn;
    private UserInfoViewModel userViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        initView();
        initViewModel();
    }

    /** 初始化 view */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_set_account_change_password);

        oldPasswordEt = findViewById(R.id.et_old_password);
        newPasswordEt = findViewById(R.id.et_new_password);
        confirmPasswordEt = findViewById(R.id.et_confirm_password);
        updateBtn = findViewById(R.id.btn_update);
        updateBtn.setOnClickListener(
                v -> {
                    String oldPassword = oldPasswordEt.getText().toString();
                    String newPassword = newPasswordEt.getText().toString();
                    String confirmPassword = confirmPasswordEt.getText().toString();

                    if (oldPassword.equals(newPassword)) {
                        showToast(R.string.seal_update_password_toast_password_old_equal_new);
                        return;
                    }

                    if (!confirmPassword.equals(newPassword)) {
                        showToast(R.string.seal_update_password_toast_password_not_equal);
                        return;
                    }
                    changePassword(oldPassword, newPassword);
                });

        TextWatcher passwordWatcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        setConformButtonState();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                };

        oldPasswordEt.addTextChangedListener(passwordWatcher);
        newPasswordEt.addTextChangedListener(passwordWatcher);
        confirmPasswordEt.addTextChangedListener(passwordWatcher);
    }

    /** 检测是否满足要求 */
    private void setConformButtonState() {
        String oldPassword = oldPasswordEt.getText().toString().trim();
        String newPassword = newPasswordEt.getText().toString().trim();
        String confirmPassword = confirmPasswordEt.getText().toString().trim();
        if (!TextUtils.isEmpty(oldPassword)
                && !TextUtils.isEmpty(newPassword)
                && newPassword.length() >= 6
                && newPassword.length() <= 16
                && !TextUtils.isEmpty(confirmPassword)
                && confirmPassword.length() >= 6
                && confirmPassword.length() <= 16) {
            updateBtn.setEnabled(true);
        } else {
            updateBtn.setEnabled(false);
        }
    }

    /** 初始化 ViewModel */
    private void initViewModel() {
        userViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        // 修改密码结果
        userViewModel
                .getChangePasswordResult()
                .observe(
                        this,
                        resultResource -> {
                            if (resultResource.status == Status.SUCCESS) {
                                finish();
                            } else if (resultResource.status == Status.ERROR) {
                                showToast(resultResource.message);
                            }
                        });
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     */
    private void changePassword(String oldPassword, String newPassword) {
        if (userViewModel != null) {
            userViewModel.changePassword(oldPassword, newPassword);
        }
    }
}
