package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.PrivacyResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.dialog.ClearCacheDialog;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.NewMessageViewModel;
import cn.rongcloud.im.viewmodel.PrivacyViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.imkit.feature.resend.ResendManager;
import io.rong.imkit.widget.SettingItemView;

/** 账号设置页面 包含：新消息通知设置、隐私设置、通用设置、账号操作 */
public class AccountSettingActivity extends TitleBaseActivity implements View.OnClickListener {

    // ViewModel
    private UserInfoViewModel userInfoViewModel;
    private NewMessageViewModel newMessageViewModel;
    private PrivacyViewModel privacyViewModel;

    // 未成年人模式相关
    private ActivityResultLauncher<Intent> verifyPasswordLauncher;

    // 新消息通知设置控件
    private SettingItemView sivNewMessageNotification;
    private SettingItemView sivShowNotificationDetail;
    private SettingItemView sivDoNotDisturb;
    private SettingItemView sivReceivePokeMessage;

    // 隐私设置控件
    private SettingItemView sivSearchByPhone;
    private SettingItemView sivSearchByStAccount;
    private SettingItemView sivFriendVerification;
    private SettingItemView sivGroupVerification;

    // 隐私开关状态标记（防止初始化时触发事件）
    private boolean hasPhoneSwitchTouched = false;
    private boolean hasStAccountSwitchTouched = false;
    private boolean hasFriendVerifySwitchTouched = false;
    private boolean hasGroupVerifySwitchTouched = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        initMinorMode();
        initView();
        initViewModel();
    }

    /** 初始化未成年人模式相关 */
    private void initMinorMode() {
        // 注册验证密码的结果回调
        verifyPasswordLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                // 验证成功，执行注销账号
                                performDeleteAccount();
                            }
                        });
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_main_mine_set_account);

        // 初始化新消息通知控件
        initNewMessageViews();

        // 初始化隐私设置控件
        initPrivacyViews();

        // 初始化通用设置和账号操作控件
        findViewById(R.id.siv_chat_background).setOnClickListener(this);
        findViewById(R.id.siv_clear_cache).setOnClickListener(this);
        findViewById(R.id.siv_clear_chat_history).setOnClickListener(this);
        findViewById(R.id.btn_delete_account).setOnClickListener(this);
    }

    /** 初始化新消息通知设置控件 */
    private void initNewMessageViews() {
        sivNewMessageNotification = findViewById(R.id.siv_new_message_notification);
        sivShowNotificationDetail = findViewById(R.id.siv_show_notification_detail);
        sivDoNotDisturb = findViewById(R.id.siv_do_not_disturb);
        sivReceivePokeMessage = findViewById(R.id.siv_receive_poke_message);

        // 接收新消息通知开关
        sivNewMessageNotification.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    sivDoNotDisturb.setEnabled(isChecked);
                    setNewMessageNotificationStatus(isChecked);
                });

        // 推送消息显示详情开关
        sivShowNotificationDetail.setSwitchCheckListener(
                (buttonView, isChecked) -> setNotificationDetailStatus(isChecked));

        // 消息免打扰
        sivDoNotDisturb.setOnClickListener(
                v -> startActivity(new Intent(this, MessageDonotDisturbSettingActivity.class)));

        // 接收戳一下消息开关
        sivReceivePokeMessage.setSwitchCheckListener(
                (buttonView, isChecked) -> setReceivePokeMessageStatus(isChecked));
    }

    /** 初始化隐私设置控件 */
    private void initPrivacyViews() {
        sivSearchByPhone = findViewById(R.id.siv_search_by_phone);
        sivSearchByStAccount = findViewById(R.id.siv_search_by_st_account);
        sivFriendVerification = findViewById(R.id.siv_friend_verification);
        sivGroupVerification = findViewById(R.id.siv_group_verification);

        // 通过手机号搜索
        sivSearchByPhone.setSwitchTouchListener(
                (v, event) -> {
                    hasPhoneSwitchTouched = true;
                    return false;
                });
        sivSearchByPhone.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    if (!hasPhoneSwitchTouched) return;
                    updatePrivacySetting(isChecked ? 1 : 0, -1, -1, -1);
                });

        // 通过融云IM号搜索
        sivSearchByStAccount.setSwitchTouchListener(
                (v, event) -> {
                    hasStAccountSwitchTouched = true;
                    return false;
                });
        sivSearchByStAccount.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    if (!hasStAccountSwitchTouched) return;
                    updatePrivacySetting(-1, isChecked ? 1 : 0, -1, -1);
                });

        // 加好友验证
        sivFriendVerification.setSwitchTouchListener(
                (v, event) -> {
                    hasFriendVerifySwitchTouched = true;
                    return false;
                });
        sivFriendVerification.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    if (!hasFriendVerifySwitchTouched) return;
                    updatePrivacySetting(-1, -1, isChecked ? 1 : 0, -1);
                });

        // 加群验证
        sivGroupVerification.setSwitchTouchListener(
                (v, event) -> {
                    hasGroupVerifySwitchTouched = true;
                    return false;
                });
        sivGroupVerification.setSwitchCheckListener(
                (buttonView, isChecked) -> {
                    if (!hasGroupVerifySwitchTouched) return;
                    updatePrivacySetting(-1, -1, -1, isChecked ? 1 : 0);
                });

        // 黑名单
        findViewById(R.id.siv_blacklist)
                .setOnClickListener(v -> startActivity(new Intent(this, BlackListActivity.class)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.siv_chat_background) {
            // 聊天背景
            startActivity(new Intent(this, ChatBackgroundSelectActivity.class));
        } else if (id == R.id.siv_clear_cache) {
            // 清理缓存
            showClearCacheDialog();
        } else if (id == R.id.siv_clear_chat_history) {
            // 清理聊天记录
            startActivity(new Intent(this, ClearChatMessageActivity.class));
        } else if (id == R.id.btn_delete_account) {
            // 删除账号
            showDeleteAccountDialog();
        }
    }

    /** 显示清理缓存对话框 */
    private void showClearCacheDialog() {
        ClearCacheDialog.Builder builder = new ClearCacheDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_set_account_dialog_clear_cache_message));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "clear_cache");
    }

    /** 显示删除账号对话框 */
    private void showDeleteAccountDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_mine_set_account_dialog_delete_message));
        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        // 检查是否开启了未成年人模式
                        String currentUserId = IMManager.getInstance().getCurrentId();
                        if (MinorModeCache.getInstance().isMinorModeEnabled(currentUserId)) {
                            // 需要验证密码
                            Intent intent =
                                    MinorModeVerifyPasswordActivity.newIntent(
                                            AccountSettingActivity.this,
                                            MinorModeVerifyPasswordActivity.TYPE_DELETE_ACCOUNT);
                            verifyPasswordLauncher.launch(intent);
                        } else {
                            // 直接注销
                            performDeleteAccount();
                        }
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {}
                });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "delete_account_dialog");
    }

    /** 执行注销账号操作 */
    private void performDeleteAccount() {
        userInfoViewModel.deleteAccount();
    }

    private void initViewModel() {
        // 初始化用户信息 ViewModel
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel
                .getDeleteAccountResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                showToast(R.string.common_delete_successful);
                                performLogout(true);
                            } else if (resource.status == Status.ERROR) {
                                showToast(
                                        R.string
                                                .seal_mine_set_account_dialog_delete_failed_message);
                            }
                        });

        // 初始化新消息通知 ViewModel
        initNewMessageViewModel();

        // 初始化隐私设置 ViewModel
        initPrivacyViewModel();
    }

    /** 初始化新消息通知 ViewModel */
    private void initNewMessageViewModel() {
        newMessageViewModel = ViewModelProviders.of(this).get(NewMessageViewModel.class);

        // 监听新消息通知状态
        newMessageViewModel
                .getRemindStatus()
                .observe(
                        this,
                        status -> {
                            sivNewMessageNotification.setChecked(status);
                            sivDoNotDisturb.setEnabled(status);
                        });

        // 监听推送消息详情状态
        newMessageViewModel
                .getPushMsgDetailStatus()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                sivShowNotificationDetail.setCheckedImmediatelyWithOutEvent(
                                        resource.data);
                            } else if (resource.status == Status.ERROR) {
                                sivShowNotificationDetail.setCheckedImmediatelyWithOutEvent(
                                        !sivShowNotificationDetail.isChecked());
                                showToast(resource.message);
                            }
                        });

        // 监听接收戳一下消息状态
        newMessageViewModel
                .getReceivePokeMsgStatusResult()
                .observe(
                        this,
                        resultResource -> {
                            if (resultResource.status == Status.SUCCESS) {
                                GetPokeResult data = resultResource.data;
                                if (data != null) {
                                    sivReceivePokeMessage.setCheckedImmediatelyWithOutEvent(
                                            data.isReceivePokeMessage());
                                }
                            } else if (resultResource.status == Status.LOADING) {
                                sivReceivePokeMessage.setCheckedImmediatelyWithOutEvent(
                                        IMManager.getInstance().getReceivePokeMessageStatus());
                            }
                        });

        // 监听设置戳一下消息状态结果
        newMessageViewModel
                .getSetReceivePokeMessageStatusResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.ERROR) {
                                sivReceivePokeMessage.setCheckedImmediatelyWithOutEvent(
                                        !sivReceivePokeMessage.isChecked());
                                showToast(resource.message);
                            }
                        });

        // 请求戳一下消息状态
        newMessageViewModel.requestReceivePokeMessageStatus();
    }

    /** 初始化隐私设置 ViewModel */
    private void initPrivacyViewModel() {
        privacyViewModel = ViewModelProviders.of(this).get(PrivacyViewModel.class);

        privacyViewModel.getPrivacyState().observe(this, this::updatePrivacyView);

        privacyViewModel
                .getSetPrivacyResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                ToastUtils.showToast(
                                        getString(R.string.seal_set_clean_time_success));
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(getString(R.string.seal_set_clean_time_fail));
                            }
                        });
    }

    /** 更新隐私设置视图 */
    private void updatePrivacyView(Resource<PrivacyResult> resource) {
        if (resource.status == Status.SUCCESS && resource.data != null) {
            PrivacyResult data = resource.data;
            sivSearchByPhone.setCheckedImmediately(
                    data.phoneVerify == PrivacyResult.State.ALLOW.getValue());
            sivSearchByStAccount.setCheckedImmediately(
                    data.stSearchVerify == PrivacyResult.State.ALLOW.getValue());
            sivFriendVerification.setCheckedImmediately(
                    data.friVerify == PrivacyResult.State.ALLOW.getValue());
            sivGroupVerification.setCheckedImmediately(
                    data.groupVerify == PrivacyResult.State.ALLOW.getValue());
        }
    }

    /** 设置新消息通知状态 */
    private void setNewMessageNotificationStatus(boolean enabled) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setRemindStatus(enabled);
        }
    }

    /** 设置通知详情显示状态 */
    private void setNotificationDetailStatus(boolean enabled) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setPushMsgDetailStatus(enabled);
        }
    }

    /** 设置接收戳一下消息状态 */
    private void setReceivePokeMessageStatus(boolean enabled) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setReceivePokeMessageStatus(enabled);
        }
    }

    /** 更新隐私设置 */
    private void updatePrivacySetting(
            int phoneVerify, int stSearchVerify, int friendVerify, int groupVerify) {
        if (privacyViewModel != null) {
            privacyViewModel.setPrivacy(phoneVerify, stSearchVerify, friendVerify, groupVerify);
        }
    }

    /** 执行退出登录 */
    private void performLogout(boolean isAccountDeleted) {
        ResendManager.getInstance().removeAllResendMessage();

        if (userInfoViewModel != null) {
            if (isAccountDeleted) {
                userInfoViewModel.accountDelete();
            } else {
                userInfoViewModel.logout();
            }
        }

        // 通知退出
        sendLogoutNotify();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
