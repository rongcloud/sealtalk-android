package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.net.SealTalkUrl;
import cn.rongcloud.im.newdesign.qrcode.QrCodeDisplayActivity;
import cn.rongcloud.im.sp.MinorModeCache;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.activity.AboutSealTalkActivity;
import cn.rongcloud.im.ui.activity.AccountSettingActivity;
import cn.rongcloud.im.ui.activity.ChangeLanguageActivity;
import cn.rongcloud.im.ui.activity.LoginActivity;
import cn.rongcloud.im.ui.activity.MinorModeActivity;
import cn.rongcloud.im.ui.activity.MinorModeVerifyPasswordActivity;
import cn.rongcloud.im.ui.activity.MyAccountActivity;
import cn.rongcloud.im.ui.activity.QrCodeDisplayOldActivity;
import cn.rongcloud.im.ui.activity.SealTalkDebugTestActivity;
import cn.rongcloud.im.ui.activity.ThemeSettingActivity;
import cn.rongcloud.im.ui.activity.TranslationSettingActivity;
import cn.rongcloud.im.ui.activity.WebViewActivity;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.feature.resend.ResendManager;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.usermanage.friend.my.profile.MyProfileActivity;
import io.rong.imkit.usermanage.handler.UserProfileHandler;
import io.rong.imkit.usermanage.interfaces.OnDataChangeListener;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.utils.language.LangUtils;
import io.rong.imkit.widget.SettingItemView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.model.CSCustomServiceInfo;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserProfile;

/** 个人中心页面 提供用户个人信息展示和各项设置入口 */
public class MainMeFragment extends BaseFragment {

    private static final String TAG = "MainMeFragment";

    // UI 组件
    private TextView tvTitle;
    private ImageView ivQrcode;
    private ImageView ivUserPortrait;
    private TextView tvUserName;
    private TextView tvUserAccount;
    private View rlUserInfo;
    private SettingItemView sivAbout;
    private SettingItemView sivPrivacy;
    private SettingItemView sivLanguage;
    private SettingItemView sivThemeSetting;

    // ViewModel
    private AppViewModel appViewModel;
    private UserInfoViewModel userInfoViewModel;
    private UserProfileHandler userProfileHandler;

    // 未成年人模式相关
    private MinorModeCache minorModeCache;
    private ActivityResultLauncher<Intent> verifyPasswordLauncher;

    @Override
    protected int getLayoutResId() {
        return R.layout.main_fragment_me;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        minorModeCache = MinorModeCache.getInstance();

        // 注册验证密码的结果回调
        verifyPasswordLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == getActivity().RESULT_OK) {
                                // 验证成功，执行退出登录
                                performLogout();
                            }
                        });
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        // 初始化标题栏
        tvTitle = findView(R.id.tv_title);
        ivQrcode = findView(R.id.iv_qrcode, true);
        setupTitleBar();

        // 用户信息区域
        rlUserInfo = findView(R.id.rl_user_info, true);
        ivUserPortrait = findView(R.id.iv_user_portrait);
        tvUserName = findView(R.id.tv_user_name);
        tvUserAccount = findView(R.id.tv_user_account);

        // 功能项
        findView(R.id.siv_account_setting, true);
        findView(R.id.siv_theme_setting, true);
        sivThemeSetting = findView(R.id.siv_theme_setting, true);
        sivLanguage = findView(R.id.siv_language, true);
        findView(R.id.siv_translation, true);
        findView(R.id.siv_minor_mode, true);
        findView(R.id.siv_feedback, true);
        sivAbout = findView(R.id.siv_about, true);
        sivPrivacy = findView(R.id.siv_privacy, true);
        findView(R.id.btn_logout, true);
    }

    /** 配置标题栏 */
    private void setupTitleBar() {
        if (tvTitle == null) return;

        // 设置标题
        if (appViewModel != null && appViewModel.isUltraGroupDebugMode()) {
            tvTitle.setText(getResources().getStringArray(R.array.tab_names_ultra)[4]);
        } else {
            tvTitle.setText(getResources().getStringArray(R.array.tab_names_nomal)[3]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新用户信息
        refreshUserInfo();
        if (IMKitThemeManager.LIVELY_THEME.equals(IMKitThemeManager.getCurrentThemeName())) {
            sivThemeSetting.setValue(R.string.seal_theme_lively);
        } else if (IMKitThemeManager.TRADITION_THEME.equals(
                IMKitThemeManager.getCurrentThemeName())) {
            sivThemeSetting.setValue(R.string.seal_theme_origin_light);
        } else if (ThemeSettingActivity.CUSTOM_ORANGE_THEME.equals(
                IMKitThemeManager.getCurrentThemeName())) {
            sivThemeSetting.setValue(R.string.seal_theme_custom_orange);
        }
    }

    @Override
    protected void onInitViewModel() {
        appViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        RongUserInfoManager.getInstance().addUserDataObserver(mUserDataObserver);

        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel
                .getUserInfo()
                .observe(
                        getActivity(),
                        new Observer<Resource<UserInfo>>() {
                            @Override
                            public void onChanged(Resource<UserInfo> resource) {
                                if (resource.data != null) {
                                    updateUserInfo(resource.data, resource.status);
                                }
                            }
                        });

        // 初始化 UserProfileHandler
        userProfileHandler = new UserProfileHandler();
        userProfileHandler.addDataChangeListener(
                UserProfileHandler.KEY_GET_MY_USER_PROFILE,
                new OnDataChangeListener<UserProfile>() {
                    @Override
                    public void onDataChange(UserProfile userProfile) {
                        if (userProfile != null) {
                            displayUserProfile(userProfile);
                        }
                    }
                });

        appViewModel
                .getHasNewVersion()
                .observe(
                        this,
                        new Observer<Resource<VersionInfo.AndroidVersion>>() {
                            @Override
                            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                                if (resource.status == Status.SUCCESS && resource.data != null) {
                                    sivAbout.setTagImageVisibility(View.VISIBLE);
                                }
                            }
                        });

        appViewModel
                .getLanguageLocal()
                .observe(
                        this,
                        new Observer<LangUtils.RCLocale>() {
                            @Override
                            public void onChanged(LangUtils.RCLocale rcLocale) {
                                if (rcLocale == LangUtils.RCLocale.LOCALE_US) {
                                    sivLanguage.setValue(R.string.lang_english);
                                } else if (rcLocale == LangUtils.RCLocale.LOCALE_CHINA) {
                                    sivLanguage.setValue(R.string.lang_chs);
                                } else if (rcLocale == LangUtils.RCLocale.LOCALE_ARAB) {
                                    sivLanguage.setValue(R.string.lang_arab);
                                }
                            }
                        });
    }

    @Override
    protected void onClick(View v, int id) {
        if (id == R.id.iv_qrcode) {
            if (SealTalkDebugTestActivity.isUserManagementEnabled(getActivity())) {
                startActivity(
                        QrCodeDisplayActivity.newIntentForPrivate(
                                getActivity(), RongIM.getInstance().getCurrentUserId()));
            } else {
                // 二维码按钮
                Intent qrCodeIntent = new Intent(getActivity(), QrCodeDisplayOldActivity.class);
                qrCodeIntent.putExtra(
                        IntentExtra.STR_TARGET_ID, RongIM.getInstance().getCurrentUserId());
                qrCodeIntent.putExtra(
                        IntentExtra.SERIA_QRCODE_DISPLAY_TYPE, QrCodeDisplayType.PRIVATE);
                startActivity(qrCodeIntent);
            }
        } else if (id == R.id.rl_user_info) {
            // 用户信息 - 根据用户托管开关决定使用哪种实现
            if (SealTalkDebugTestActivity.isUserManagementEnabled(getActivity())) {
                // 使用新的用户管理功能
                startActivity(MyProfileActivity.newIntent(getActivity()));
            } else {
                // 使用原来的实现
                Intent intentUserInfo = new Intent(getActivity(), MyAccountActivity.class);
                startActivity(intentUserInfo);
            }
        } else if (id == R.id.siv_account_setting) {
            // 账号设置
            startActivity(new Intent(getActivity(), AccountSettingActivity.class));
        } else if (id == R.id.siv_theme_setting) {
            // 切换主题
            startActivity(new Intent(getActivity(), ThemeSettingActivity.class));
        } else if (id == R.id.siv_language) {
            // 多语言
            startActivity(new Intent(getActivity(), ChangeLanguageActivity.class));
        } else if (id == R.id.siv_feedback) {
            // 意见反馈
            CSCustomServiceInfo.Builder builder = new CSCustomServiceInfo.Builder();
            builder.province(getString(R.string.beijing));
            builder.city(getString(R.string.beijing));
            io.rong.imlib.model.UserInfo info =
                    RongUserInfoManager.getInstance()
                            .getUserInfo(RongIM.getInstance().getCurrentUserId());
            if (info != null && !TextUtils.isEmpty(info.getName())) {
                builder.name(info.getName());
            }
            // 佳信客服配置
            builder.referrer("10001");
            Bundle bundle = new Bundle();
            bundle.putString(
                    RouteUtils.TITLE, getString(R.string.seal_main_mine_online_custom_service));
            bundle.putParcelable(RouteUtils.CUSTOM_SERVICE_INFO, builder.build());
            RouteUtils.routeToConversationActivity(
                    getContext(), ConversationIdentifier.obtainCustomer("service"), bundle);
        } else if (id == R.id.siv_about) {
            // 关于融云 IM
            Intent intent = new Intent(getActivity(), AboutSealTalkActivity.class);
            Resource<VersionInfo.AndroidVersion> resource =
                    appViewModel.getHasNewVersion().getValue();
            if (resource != null
                    && resource.data != null
                    && !TextUtils.isEmpty(resource.data.getUrl())) {
                intent.putExtra(IntentExtra.URL, resource.data.getUrl());
            }
            startActivity(intent);
        } else if (id == R.id.siv_privacy) {
            // 隐私政策
            final String privacyPolicyTitle = getString(R.string.seal_talk_privacy_policy_title);
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.PARAMS_TITLE, privacyPolicyTitle);
            intent.putExtra(WebViewActivity.PARAMS_URL, SealTalkUrl.getPrivacyPolicy());
            startActivity(intent);
        } else if (id == R.id.siv_translation) {
            // 翻译设置
            Intent intent = new Intent(getActivity(), TranslationSettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.siv_minor_mode) {
            // 未成年人模式
            startActivity(MinorModeActivity.newIntent(getActivity()));
        } else if (id == R.id.btn_logout) {
            // 退出登录
            showLogoutDialog();
        }
    }

    /** 刷新用户信息 */
    private void refreshUserInfo() {
        if (!TextUtils.isEmpty(IMManager.getInstance().getCurrentId())) {
            // 使用 UserProfileHandler 获取用户资料
            if (userProfileHandler != null) {
                userProfileHandler.getMyUserProfile();
            }

            // 同时加载基本用户信息用于头像和昵称显示
            io.rong.imlib.model.UserInfo userInfo =
                    RongUserInfoManager.getInstance()
                            .getUserInfo(IMManager.getInstance().getCurrentId());
            if (userInfo == null) {
                userInfoViewModel.requestUserInfo(IMManager.getInstance().getCurrentId());
            } else {
                // 直接显示缓存的用户信息
                displayUserInfo(userInfo);
            }
        }
    }

    /**
     * 更新用户信息
     *
     * @param userInfo 用户信息
     * @param status 状态
     */
    private void updateUserInfo(UserInfo userInfo, Status status) {
        if (userInfo != null) {
            // 设置用户名
            tvUserName.setText(userInfo.getName());

            // 设置用户账号
            String userId = IMManager.getInstance().getCurrentId();
            if (!TextUtils.isEmpty(userId)) {
                tvUserAccount.setText(
                        getString(R.string.seal_main_me_app_account_label) + ": " + userId);
            }

            // 加载头像
            if (status == Status.SUCCESS || status == Status.ERROR) {
                if (!TextUtils.isEmpty(userInfo.getPortraitUri()) && getActivity() != null) {
                    RongConfigCenter.featureConfig()
                            .getKitImageEngine()
                            .loadUserPortrait(
                                    getActivity(),
                                    userInfo.getPortraitUri().toString(),
                                    ivUserPortrait);
                }
            }
        }
    }

    /**
     * 显示用户信息（从 RongUserInfoManager 获取）
     *
     * @param userInfo IMLib 用户信息
     */
    private void displayUserInfo(io.rong.imlib.model.UserInfo userInfo) {
        if (userInfo != null && getActivity() != null) {
            // 设置用户名
            tvUserName.setText(userInfo.getName());

            // 设置用户账号
            String userId = userInfo.getUserId();
            if (!TextUtils.isEmpty(userId)) {
                tvUserAccount.setText(getString(R.string.seal_main_me_app_account_label) + userId);
            }

            // 加载头像
            if (userInfo.getPortraitUri() != null) {
                RongConfigCenter.featureConfig()
                        .getKitImageEngine()
                        .loadUserPortrait(
                                getActivity(),
                                userInfo.getPortraitUri().toString(),
                                ivUserPortrait);
            }
        }
    }

    /**
     * 显示用户资料（从 UserProfile 获取）
     *
     * @param userProfile 用户资料
     */
    private void displayUserProfile(UserProfile userProfile) {
        if (userProfile != null && getActivity() != null) {
            // 使用 uniqueId 显示账号信息
            String uniqueId = userProfile.getUniqueId();
            if (!TextUtils.isEmpty(uniqueId)) {
                tvUserAccount.setText(
                        getString(R.string.seal_main_me_app_account_label) + uniqueId);
            }
        }
    }

    /** 显示退出登录确认对话框 */
    private void showLogoutDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_mine_set_account_dialog_logout_message));
        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        // 检查是否开启了未成年人模式
                        String currentUserId = IMManager.getInstance().getCurrentId();
                        if (minorModeCache != null
                                && minorModeCache.isMinorModeEnabled(currentUserId)) {
                            // 需要验证密码
                            Intent intent =
                                    MinorModeVerifyPasswordActivity.newIntent(
                                            getActivity(),
                                            MinorModeVerifyPasswordActivity.TYPE_LOGOUT);
                            verifyPasswordLauncher.launch(intent);
                        } else {
                            // 直接退出
                            performLogout();
                        }
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                        // 用户取消，不做任何操作
                    }
                });
        CommonDialog dialog = builder.build();
        dialog.show(getParentFragmentManager(), "logout_dialog");
    }

    /** 执行退出登录操作 参考 AccountSettingActivity 的实现 */
    private void performLogout() {
        // 清除所有待重发消息
        ResendManager.getInstance().removeAllResendMessage();

        // 执行登出
        if (userInfoViewModel != null) {
            userInfoViewModel.logout();
        }

        // 发送登出通知
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).sendLogoutNotify();
        }

        // 跳转到登录页面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 结束当前Activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RongUserInfoManager.getInstance().removeUserDataObserver(mUserDataObserver);
        if (userProfileHandler != null) {
            userProfileHandler.stop();
        }
    }

    /** 用户数据观察者 监听用户信息变化并更新UI */
    private RongUserInfoManager.UserDataObserver mUserDataObserver =
            new RongUserInfoManager.UserDataObserver() {
                @Override
                public void onUserUpdate(io.rong.imlib.model.UserInfo userInfo) {
                    if (userInfo != null
                            && getActivity() != null
                            && userInfo.getUserId()
                                    .equals(RongIMClient.getInstance().getCurrentUserId())) {
                        ThreadManager.getInstance()
                                .runOnUIThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                if (getActivity() == null) {
                                                    return;
                                                }
                                                displayUserInfo(userInfo);
                                            }
                                        });
                    }
                }

                @Override
                public void onGroupUpdate(Group group) {}

                @Override
                public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {}
            };
}
