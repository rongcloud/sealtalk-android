package cn.rongcloud.im.ui.activity;

import static java.lang.System.exit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.ui.dialog.DownloadAppDialog;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.BuildVariantUtils;
import cn.rongcloud.im.utils.DialogWithYesOrNoUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongCoreClientImpl;
import io.rong.imlib.common.DeviceUtils;
import java.io.File;
import java.util.Objects;

/** 关于 SealTalk 的界面 */
public class AboutSealTalkActivity extends TitleBaseActivity implements View.OnClickListener {
    private SettingItemView sealtalkVersionSiv;
    private SettingItemView sdkVersionSiv;
    private String url;
    private SettingItemView debufModeSiv;
    private SettingItemView debugEnvSiv;
    private UserInfoViewModel userInfoViewModel;
    long[] mHits = new long[5];
    private SettingItemView sealtalkDebugSettingSiv;
    private SettingItemView sivDeviceId;

    static final String LAST_DEVICE_ID = "LAST_DEVICE_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_sealtalk);
        url = getIntent().getStringExtra(IntentExtra.URL);
        initView();
        initViewModel();
    }

    /** 初始化布局 */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_main_mine_about);

        findViewById(R.id.siv_update_log).setOnClickListener(this);
        findViewById(R.id.siv_func_introduce).setOnClickListener(this);
        findViewById(R.id.siv_rongcloud_web).setOnClickListener(this);
        sealtalkDebugSettingSiv = findViewById(R.id.siv_debug_go);
        sealtalkVersionSiv = findViewById(R.id.siv_sealtalk_version);
        sealtalkVersionSiv.setOnClickListener(this);
        sdkVersionSiv = findViewById(R.id.siv_sdk_version);
        sdkVersionSiv.setOnClickListener(this);
        debufModeSiv = findViewById(R.id.siv_close_debug_mode);
        debufModeSiv.setOnClickListener(this);
        sealtalkVersionSiv.setClickable(false);
        sealtalkDebugSettingSiv.setOnClickListener(this);

        // 设备ID相关逻辑 - 统一处理变量作用域
        SharedPreferences sharedPreferences =
                getSharedPreferences(SealTalkDebugTestActivity.ULTRA_DEBUG_CONFIG, MODE_PRIVATE);
        String lastDeviceId = sharedPreferences.getString(LAST_DEVICE_ID, "");
        String deviceId = DeviceUtils.getDeviceId(this);

        // 根据构建变体控制设备ID显示功能 - Develop版本启用，PublishStore版本禁用
        if (!BuildVariantUtils.isPublishStoreBuild()) {
            // Develop版本：查找并设置设备ID显示控件
            try {
                sivDeviceId =
                        findViewById(
                                getResources()
                                        .getIdentifier("siv_device_id", "id", getPackageName()));
                if (sivDeviceId != null) {
                    sivDeviceId.setContent("old: " + lastDeviceId + "\nnew:" + deviceId);
                }
            } catch (Exception e) {
                // 如果布局中没有siv_device_id控件，设为null
                sivDeviceId = null;
            }
        } else {
            // PublishStore版本隐藏设备ID显示功能
            sivDeviceId = null;
        }

        if (!Objects.equals(lastDeviceId, deviceId)) {
            sharedPreferences.edit().putString(LAST_DEVICE_ID, deviceId).commit();
        }

        debugEnvSiv = findViewById(R.id.siv_debug_env);
        String cloud = RongCoreClientImpl.isPrivateSDK() ? "私有云" : "公有云";
        debugEnvSiv.setContent("Debug 显示：SDK:" + cloud + "; " + stringForDbEncrypted());
    }

    /** 初始化 ViewModel */
    private void initViewModel() {
        AppViewModel appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        // 是否有新版本
        appViewModel
                .getHasNewVersion()
                .observe(
                        this,
                        new Observer<Resource<VersionInfo.AndroidVersion>>() {
                            @Override
                            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                                if (resource.data != null) {
                                    sealtalkVersionSiv.setClickable(true);
                                    sealtalkVersionSiv.setTagImageVisibility(View.VISIBLE);
                                }
                            }
                        });

        // sdk 版本
        appViewModel
                .getSDKVersion()
                .observe(
                        this,
                        new Observer<String>() {
                            @Override
                            public void onChanged(String version) {
                                sdkVersionSiv.setValue(version);
                            }
                        });

        // sealtalk 版本
        appViewModel
                .getSealTalkVersion()
                .observe(
                        this,
                        new Observer<String>() {
                            @Override
                            public void onChanged(String version) {
                                sealtalkVersionSiv.setValue(version);
                            }
                        });

        appViewModel
                .getDebugMode()
                .observe(
                        this,
                        new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean result) {
                                if (result) {
                                    sdkVersionSiv.setClickable(false);
                                    debufModeSiv.setVisibility(View.VISIBLE);
                                    debugEnvSiv.setVisibility(View.VISIBLE);
                                    sealtalkDebugSettingSiv.setVisibility(View.VISIBLE);
                                    // 根据构建变体控制设备ID显示 - Develop版本启用，PublishStore版本禁用
                                    if (!BuildVariantUtils.isPublishStoreBuild()
                                            && sivDeviceId != null) {
                                        sivDeviceId.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    sdkVersionSiv.setClickable(true);
                                    debufModeSiv.setVisibility(View.GONE);
                                    debugEnvSiv.setVisibility(View.GONE);
                                    sealtalkDebugSettingSiv.setVisibility(View.GONE);
                                    // 根据构建变体控制设备ID隐藏 - Develop版本启用，PublishStore版本禁用
                                    if (!BuildVariantUtils.isPublishStoreBuild()
                                            && sivDeviceId != null) {
                                        sivDeviceId.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.siv_update_log) {
            toWeb(
                    getString(R.string.seal_mine_about_update_log),
                    "http://www.rongcloud.cn/changelog");
        } else if (id == R.id.siv_func_introduce) {
            toWeb(
                    getString(R.string.seal_mine_about_function_introduce),
                    "http://rongcloud.cn/features");
        } else if (id == R.id.siv_rongcloud_web) {
            toWeb(getString(R.string.seal_mine_about_rongcloud_web), "http://rongcloud.cn/");
        } else if (id == R.id.siv_sealtalk_version) {
            showDownloadDialog(url);
        } else if (id == R.id.siv_sdk_version) {
            // TODO 开启 debug 模式规则: 上线 PublishStore 不能开启
            if (!BuildVariantUtils.isPublishStoreBuild()) {
                showStartDebugDialog();
            }
        } else if (id == R.id.siv_close_debug_mode) {
            // TODO 关闭 debug 模式
            sdkVersionSiv.setClickable(true);
            showCloseDialog();
        } else if (id == R.id.siv_debug_go) {
            toSetting();
        }
    }

    private void toSetting() {
        Intent intent = new Intent(this, SealTalkDebugTestActivity.class);
        startActivity(intent);
    }

    private void toWeb(String title, String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.PARAMS_TITLE, title);
        intent.putExtra(WebViewActivity.PARAMS_URL, url);
        startActivity(intent);
    }

    /** debug 提示 dialog */
    private void showStartDebugDialog() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] > SystemClock.uptimeMillis() - 10000) {
            if (getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                ToastUtils.showToast(this.getString(R.string.debug_mode_is_open));
            } else {
                DialogWithYesOrNoUtils.getInstance()
                        .showDialog(
                                this,
                                getString(R.string.setting_open_debug_prompt),
                                new DialogWithYesOrNoUtils.DialogCallBack() {
                                    @Override
                                    public void executeEvent() {
                                        SharedPreferences.Editor editor =
                                                getSharedPreferences("config", MODE_PRIVATE).edit();
                                        editor.putBoolean("isDebug", true);
                                        editor.commit();
                                        exit(0);
                                    }

                                    @Override
                                    public void executeEditEvent(String editText) {}

                                    @Override
                                    public void updatePassword(
                                            String oldPassword, String newPassword) {}
                                });
            }
        }
    }

    private void showCloseDialog() {
        DialogWithYesOrNoUtils.getInstance()
                .showDialog(
                        this,
                        getString(R.string.setting_close_debug_promt),
                        new DialogWithYesOrNoUtils.DialogCallBack() {
                            @Override
                            public void executeEvent() {
                                SharedPreferences.Editor editor =
                                        getSharedPreferences("config", MODE_PRIVATE).edit();
                                editor.putBoolean("isDebug", false);
                                editor.commit();
                                exit(0);
                            }

                            @Override
                            public void executeEditEvent(String editText) {}

                            @Override
                            public void updatePassword(String oldPassword, String newPassword) {}
                        });
    }

    /** 提示下载 */
    private void showDownloadDialog(String url) {
        DownloadAppDialog dialog = new DownloadAppDialog();
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.URL, url);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "download_dialog");
    }

    /** 退出 */
    public void logout() {
        userInfoViewModel.logout();
    }

    /**
     * 消息数据库是否加密的描述字符串
     *
     * @note storage.db 存在说明 db 加密
     */
    private String stringForDbEncrypted() {
        String appKey = BuildConfig.SEALTALK_APP_KEY;
        String userId = RongIM.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(appKey) || TextUtils.isEmpty(userId)) {
            return "db异常：Appkey：" + appKey + "，UserId：" + userId;
        }
        File file = getApplicationContext().getFilesDir();
        if (!file.exists()) {
            return "db异常：数据库文件夹不存在";
        }
        String dbEncryptedPath =
                file.getAbsolutePath() + "/" + appKey + "/" + userId + "/storage.db";
        File dbEncryptedFile = new File(dbEncryptedPath);
        if (dbEncryptedFile.exists()) {
            return "db:加密";
        }
        String dbNormalPath = file.getAbsolutePath() + "/" + appKey + "/" + userId + "/storage";
        File dbNormalFile = new File(dbNormalPath);
        if (dbNormalFile.exists()) {
            return "db:未加密";
        }
        return "db异常：数据库文件不存在";
    }
}
