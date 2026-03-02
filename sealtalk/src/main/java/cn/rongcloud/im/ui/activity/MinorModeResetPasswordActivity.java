package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.ImageCodeResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.viewmodel.LoginViewModel;

/** 重置未成年人模式密码页面 通过验证手机号和短信验证码来重置密码 */
public class MinorModeResetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvCountryCode;
    private EditText etPhone;
    private EditText etImageCode;
    private ImageView ivImageCode;
    private EditText etSmsCode;
    private TextView tvGetCode;
    private Button btnReset;

    private MinorModeCache minorModeCache;
    private String currentUserId;
    private LoginViewModel loginViewModel;
    private UserTask userTask;
    private ImageCodeResult imageCodeResult;
    private boolean isRequestingCode = false;

    public static Intent newIntent(Context context) {
        return new Intent(context, MinorModeResetPasswordActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor_mode_reset_password);
        initView();
        initData();
        initViewModel();
    }

    private void initView() {
        tvCountryCode = findViewById(R.id.tv_country_code);
        etPhone = findViewById(R.id.et_phone);
        etImageCode = findViewById(R.id.et_image_code);
        ivImageCode = findViewById(R.id.iv_image_code);
        etSmsCode = findViewById(R.id.et_sms_code);
        tvGetCode = findViewById(R.id.tv_get_code);
        btnReset = findViewById(R.id.btn_reset);

        ivImageCode.setOnClickListener(this);
        tvGetCode.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    private void initData() {
        minorModeCache = MinorModeCache.getInstance();
        currentUserId = IMManager.getInstance().getCurrentId();
        userTask = new UserTask(getApplication());
    }

    private void initViewModel() {
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        // 监听图片验证码结果
        loginViewModel
                .getImageCodeResult()
                .observe(
                        this,
                        new Observer<Resource<ImageCodeResult>>() {
                            @Override
                            public void onChanged(Resource<ImageCodeResult> resource) {
                                if (resource.status == Status.SUCCESS && resource.data != null) {
                                    imageCodeResult = resource.data;
                                    // 显示图片验证码
                                    displayImageCode(resource.data.getPicCode());
                                } else if (resource.status == Status.ERROR) {
                                    showToast(resource.message);
                                }
                            }
                        });

        // 监听发送验证码结果
        loginViewModel
                .getSendCodeState()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                if (resource.status == Status.SUCCESS) {
                                    showToast(R.string.seal_login_toast_send_code_success);
                                    isRequestingCode = true;
                                } else if (resource.status == Status.ERROR) {
                                    showToast(resource.message);
                                    tvGetCode.setEnabled(true);
                                    isRequestingCode = false;
                                }
                            }
                        });

        // 监听验证码倒计时
        loginViewModel
                .getCodeCountDown()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer seconds) {
                                if (seconds > 0) {
                                    tvGetCode.setText(seconds + "s");
                                    tvGetCode.setEnabled(false);
                                } else {
                                    tvGetCode.setText(R.string.seal_minor_mode_get_sms_code);
                                    tvGetCode.setEnabled(true);
                                }
                            }
                        });

        // 监听验证结果（使用 registerAndLogin 接口验证）
        loginViewModel
                .getLoginResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                if (resource.status == Status.SUCCESS) {
                                    // 验证成功，重置密码为默认值
                                    minorModeCache.resetPasswordToDefault(currentUserId);
                                    showToast(R.string.seal_minor_mode_reset_success);
                                    setResult(RESULT_OK);
                                    finish();
                                } else if (resource.status == Status.ERROR) {
                                    dismissLoadingDialog();
                                    showToast(R.string.seal_minor_mode_verify_failed);
                                } else if (resource.status == Status.LOADING) {
                                    showLoadingDialog("");
                                }
                            }
                        });

        // 获取图片验证码
        loginViewModel.getImageCode();
    }

    private void displayImageCode(String base64Code) {
        if (TextUtils.isEmpty(base64Code)) {
            return;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64Code, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            ivImageCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_image_code) {
            // 刷新图片验证码
            loginViewModel.getImageCode();
        } else if (id == R.id.tv_get_code) {
            // 获取短信验证码
            requestSmsCode();
        } else if (id == R.id.btn_reset) {
            // 重置密码
            resetPassword();
        }
    }

    private void requestSmsCode() {
        String phone = etPhone.getText().toString().trim();
        String imageCode = etImageCode.getText().toString().trim();
        String countryCode = tvCountryCode.getText().toString().replace("+", "");

        if (TextUtils.isEmpty(phone)) {
            showToast(R.string.seal_login_toast_phone_number_is_null);
            return;
        }

        // 验证手机号是否与当前登录用户一致
        UserCacheInfo userCache = userTask.getUserCache();
        if (userCache != null) {
            String cachedPhone = userCache.getPhoneNumber();
            String cachedRegion = userCache.getRegion();
            if (!phone.equals(cachedPhone) || !countryCode.equals(cachedRegion)) {
                showToast(R.string.seal_minor_mode_phone_not_match);
                return;
            }
        }

        if (TextUtils.isEmpty(imageCode)) {
            showToast(R.string.image_verification_code_is_null);
            return;
        }

        if (imageCodeResult == null) {
            showToast(R.string.image_verification_code_is_expired);
            return;
        }

        tvGetCode.setEnabled(false);
        loginViewModel.sendCode(countryCode, phone, imageCode, imageCodeResult.getPicCodeId());
    }

    private void resetPassword() {
        String phone = etPhone.getText().toString().trim();
        String smsCode = etSmsCode.getText().toString().trim();
        String countryCode = tvCountryCode.getText().toString().replace("+", "");

        if (TextUtils.isEmpty(phone)) {
            showToast(R.string.seal_login_toast_phone_number_is_null);
            return;
        }

        // 验证手机号是否与当前登录用户一致
        UserCacheInfo userCache = userTask.getUserCache();
        if (userCache != null) {
            String cachedPhone = userCache.getPhoneNumber();
            String cachedRegion = userCache.getRegion();
            if (!phone.equals(cachedPhone) || !countryCode.equals(cachedRegion)) {
                showToast(R.string.seal_minor_mode_phone_not_match);
                return;
            }
        }

        if (TextUtils.isEmpty(smsCode)) {
            showToast(R.string.seal_login_toast_code_is_null);
            return;
        }

        // 使用 registerAndLogin 接口验证手机号和验证码
        loginViewModel.verifyCode(countryCode, phone, smsCode);
    }
}
