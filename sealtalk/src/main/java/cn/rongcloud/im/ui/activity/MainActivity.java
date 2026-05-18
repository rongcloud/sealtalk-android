package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.newdesign.qrcode.QrCodeScanActivity;
import cn.rongcloud.im.newdesign.startchat.StartChatActivity;
import cn.rongcloud.im.openclaw.guide.OpenClawGuideActivity;
import cn.rongcloud.im.security.SMSDKUtils;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.AuthorityPrivacyDialog;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.dialog.FraudTipsDialog;
import cn.rongcloud.im.ui.dialog.MorePopWindow;
import cn.rongcloud.im.ui.fragment.MainContactsListFragment;
import cn.rongcloud.im.ui.fragment.MainDiscoveryFragment;
import cn.rongcloud.im.ui.fragment.MainMeFragment;
import cn.rongcloud.im.ui.fragment.SealConversationListFragment;
import cn.rongcloud.im.ui.fragment.SealFriendListFragment;
import cn.rongcloud.im.ui.fragment.UltraConversationListFragment;
import cn.rongcloud.im.ui.view.MainBottomTabGroupView;
import cn.rongcloud.im.ui.view.MainBottomTabItem;
import cn.rongcloud.im.ui.widget.DragPointView;
import cn.rongcloud.im.ui.widget.TabGroupView;
import cn.rongcloud.im.ui.widget.TabItem;
import cn.rongcloud.im.utils.BuildVariantUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.MainViewModel;
import cn.rongcloud.im.viewmodel.SecurityViewModel;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;
import com.tencent.bugly.crashreport.CrashReport;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imkit.usermanage.friend.add.AddFriendListActivity;
import io.rong.imkit.usermanage.friend.select.FriendSelectActivity;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.utils.ToastUtils;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity
        implements MorePopWindow.OnPopWindowItemClickListener {
    public static final String PARAMS_TAB_INDEX = "tab_index";
    private static final int REQUEST_START_CHAT = 0;
    private static final int REQUEST_START_GROUP = 1;
    private static final String TAG = "MainActivity";
    public static final String CHAT = "chat";
    public static final String ULTRA = "ultra";
    public static final String CONTACTS = "contacts";
    public static final String FIND = "find";
    public static final String ME = "me";

    private ViewPager vpFragmentContainer;
    private MainBottomTabGroupView tabGroupView;
    public MainViewModel mainViewModel;
    private SecurityViewModel securityViewModel;
    private UltraGroupViewModel mConversationListViewModel;
    private LinkedHashMap<String, Integer> tabsMap = new LinkedHashMap<>();
    private String[] tabNameList; // tab 显示名称数组

    /** 各个 Fragment 界面 */
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_main);
        initViewModel();
        initTabData();
        initView();
        // 根据构建变体控制Bugly的启用 - Develop版本启用，PublishStore版本禁用
        if (!BuildVariantUtils.isPublishStoreBuild()) {
            CrashReport.initCrashReport(getApplicationContext(), "cb8ebab203", true);
        }
        clearBadgeStatu();
        showFraudTipsDialog();
        initAMapPrivacy();
        // 根据构建变体控制隐私协议对话框 - PublishStore版本显示隐私协议
        if (BuildVariantUtils.isPublishStoreBuild()) {
            initRongPrivacy();
        }
        if (appViewModel.isUltraGroupDebugMode()) {
            initOtherPrivacy();
        }
        if (Build.VERSION.SDK_INT >= 33) {
            askNotificationPermission();
        }
        //        toastInterceptor();
    }

    private void toastInterceptor() {
        ToastUtils.setInterceptor(
                new ToastUtils.ToastInterceptor() {
                    @Override
                    public boolean willToast(
                            @NonNull Context context, @NonNull CharSequence text, int duration) {
                        String s = "被 sealtalk 拦截的 toast：" + text;
                        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
    }

    private void initOtherPrivacy() {
        // 根据构建变体控制友盟统计的启用 - Develop版本启用，PublishStore版本禁用
        if (!BuildVariantUtils.isPublishStoreBuild()) {
            try {
                // 使用反射调用UMConfigure，避免在PublishStore版本中引入依赖
                Class<?> umConfigureClass = Class.forName("com.umeng.commonsdk.UMConfigure");
                umConfigureClass
                        .getMethod(
                                "init",
                                android.content.Context.class,
                                String.class,
                                String.class,
                                int.class,
                                String.class)
                        .invoke(null, this, BuildConfig.SEALTALK_UMENG_APPKEY, null, 1, null);
            } catch (Exception e) {
                // Develop版本可能没有友盟依赖，忽略错误
                SLog.d("MainActivity", "UMConfigure not available: " + e.getMessage());
            }
        }
    }

    private void initAMapPrivacy() {
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);
    }

    private void showFraudTipsDialog() {
        if (!BuildConfig.DEBUG) {
            new FraudTipsDialog(this).show();
        }
    }

    // 设置Activity对应的顶部状态栏的颜色
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 清除华为的角标
    private void clearBadgeStatu() {
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            try {
                String packageName = getPackageName();
                String launchClassName =
                        getPackageManager()
                                .getLaunchIntentForPackage(packageName)
                                .getComponent()
                                .getClassName();
                Bundle bundle = new Bundle(); // 需要存储的数据
                bundle.putString("package", packageName); // 包名
                bundle.putString("class", launchClassName); // 启动的Activity完整名称
                bundle.putInt("badgenumber", 0); // 未读信息条数清空
                getContentResolver()
                        .call(
                                Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                                "change_badge",
                                null,
                                bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 初始化布局 */
    private void initView() {
        tabGroupView = findViewById(R.id.tg_bottom_tabs);
        vpFragmentContainer = findViewById(R.id.vp_main_container);

        int tabIndex = resolveTabIndex(getIntent());

        // 初始化底部 tabs
        initTabs();
        // 初始化 fragment 的 viewpager
        initFragmentViewPager();

        // 设置当前的选项为聊天界面
        tabGroupView.setSelected(tabIndex);
    }

    /** 初始化 Tabs */
    private void initTabs() {
        // 初始化 tab
        List<TabItem> items = new ArrayList<>();
        if (appViewModel.isUltraGroupDebugMode()) {
            tabNameList = getResources().getStringArray(R.array.tab_names_ultra);
        } else {
            tabNameList = getResources().getStringArray(R.array.tab_names_nomal);
        }

        // 初始化 tab 的图片 - 使用主题属性动态获取
        List<TabItem.AnimationDrawableBean> animationDrawableList = new ArrayList<>();

        // 聊天 Tab
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_chat_unselected, R.drawable.tab_chat_0),
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_chat_selected,
                                R.drawable.tab_chat_animation_list)));

        // 超级群 Tab (仅在 Ultra 模式下)
        if (appViewModel.isUltraGroupDebugMode()) {
            animationDrawableList.add(
                    new TabItem.AnimationDrawableBean(
                            getThemeDrawableResId(
                                    R.attr.rc_lively_tab_ultra_unselected, R.drawable.rc_ultra_0),
                            getThemeDrawableResId(
                                    R.attr.rc_lively_tab_ultra_selected,
                                    R.drawable.tab_ultra_animation_list)));
        }

        // 发现/聊天室 Tab
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_chatroom_unselected,
                                R.drawable.tab_chatroom_0),
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_chatroom_selected,
                                R.drawable.tab_chatroom_animation_list)));

        // 好友 Tab (原联系人)
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_contacts_unselected,
                                R.drawable.tab_contacts_0),
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_contacts_selected,
                                R.drawable.tab_contacts_animation_list)));

        // 我的 Tab
        animationDrawableList.add(
                new TabItem.AnimationDrawableBean(
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_me_unselected, R.drawable.tab_me_0),
                        getThemeDrawableResId(
                                R.attr.rc_lively_tab_me_selected,
                                R.drawable.tab_me_animation_list)));

        for (Map.Entry<String, Integer> entry : tabsMap.entrySet()) {
            TabItem tabItem = new TabItem();
            tabItem.id = entry.getValue();
            tabItem.text = tabNameList[entry.getValue()];
            tabItem.animationDrawable = animationDrawableList.get(entry.getValue());
            items.add(tabItem);
        }

        tabGroupView.initView(
                items,
                new TabGroupView.OnTabSelectedListener() {
                    @Override
                    public void onSelected(View view, TabItem item) {
                        // 当点击 tab 的后， 也要切换到正确的 fragment 页面
                        int currentItem = vpFragmentContainer.getCurrentItem();
                        if (currentItem != item.id) {
                            // 切换布局
                            vpFragmentContainer.setCurrentItem(item.id);
                        } else if (appViewModel.isUltraGroupDebugMode()
                                && item.id == tabsMap.get(ULTRA)) {
                            mConversationListViewModel.getUltraGroupMemberList();
                        } else if (item.id == tabsMap.get(ME)) {
                            // 如果是我的页面， 则隐藏红点
                            ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(ME)))
                                    .setRedVisibility(View.GONE);
                        }
                    }
                });

        tabGroupView.setOnTabDoubleClickListener(
                new MainBottomTabGroupView.OnTabDoubleClickListener() {
                    @Override
                    public void onDoubleClick(TabItem item, View view) {
                        // 双击定位到某一个未读消息位置
                        if (item.id == tabsMap.get(CHAT)) {
                            // todo
                            //                    MainConversationListFragment fragment =
                            // (MainConversationListFragment) fragments.get(Tab.CHAT.getValue());
                            //                    fragment.focusUnreadItem();
                        }
                    }
                });

        // 未读数拖拽
        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                .setTabUnReadNumDragListener(
                        new DragPointView.OnDragListencer() {

                            @Override
                            public void onDragOut() {
                                ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                                        .setNumVisibility(View.GONE);
                                showToast(getString(R.string.seal_main_toast_unread_clear_success));
                                clearUnreadStatus();
                            }
                        });
        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT)))
                .setNumVisibility(View.VISIBLE);
    }

    private void initTabData() {
        if (appViewModel.isUltraGroupDebugMode()) {
            tabsMap.put(CHAT, 0);
            tabsMap.put(ULTRA, 1);
            tabsMap.put(FIND, 2);
            tabsMap.put(CONTACTS, 3);
            tabsMap.put(ME, 4);
        } else {
            tabsMap.put(CHAT, 0);
            tabsMap.put(FIND, 1);
            tabsMap.put(CONTACTS, 2);
            tabsMap.put(ME, 3);
        }
    }

    /** 从 Intent 解析需要展示的 Tab，兼容字符串和整型传值 */
    private int resolveTabIndex(Intent intent) {
        if (intent == null) {
            return tabsMap.get(CHAT);
        }

        int tabIndex = intent.getIntExtra(PARAMS_TAB_INDEX, -1);
        if (tabIndex != -1) {
            return tabIndex;
        }

        String tabKey = intent.getStringExtra(PARAMS_TAB_INDEX);
        if (!TextUtils.isEmpty(tabKey)) {
            Integer targetIndex = tabsMap.get(tabKey);
            if (targetIndex != null) {
                return targetIndex;
            }
        }

        return tabsMap.get(CHAT);
    }

    /**
     * 从主题属性中获取 Drawable 资源 ID
     *
     * @param attrId 主题属性 ID
     * @param defaultResId 默认资源 ID (当主题属性未定义时使用)
     * @return Drawable 资源 ID
     */
    private int getThemeDrawableResId(int attrId, int defaultResId) {
        // 使用 IMKitThemeManager 的标准方法获取主题属性资源
        int resId = IMKitThemeManager.getAttrResId(this, attrId);
        // 如果获取失败（返回 0），则使用默认资源
        return resId != 0 ? resId : defaultResId;
    }

    /** 初始化 initFragmentViewPager */
    private void initFragmentViewPager() {
        // 使用包装的 ConversationListFragment
        fragments.add(new SealConversationListFragment());
        if (appViewModel.isUltraGroupDebugMode()) {
            fragments.add(new UltraConversationListFragment());
        }
        // 发现/聊天室 Fragment
        fragments.add(new MainDiscoveryFragment());
        // 根据用户托管开关决定使用哪个联系人Fragment (好友)
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            // 使用新的用户管理功能
            fragments.add(new SealFriendListFragment());
        } else {
            // 使用原来的实现
            fragments.add(new MainContactsListFragment());
        }
        // 我的 Fragment
        fragments.add(new MainMeFragment());

        //        FragmentTransaction fragmentTransaction =
        // getSupportFragmentManager().beginTransaction();
        //        for (Fragment item : fragments) {
        //            fragmentTransaction.add(R.id.vp_main_container, item).hide(item);
        //        }
        //        fragmentTransaction.show(fragments.get(0)).commit();

        // ViewPager 的 Adpater
        FragmentPagerAdapter fragmentPagerAdapter =
                new FragmentPagerAdapter(
                        getSupportFragmentManager(),
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                    @Override
                    public Fragment getItem(int position) {
                        return fragments.get(position);
                    }

                    @Override
                    public int getCount() {
                        return fragments.size();
                    }
                };

        vpFragmentContainer.setAdapter(fragmentPagerAdapter);
        vpFragmentContainer.setOffscreenPageLimit(fragments.size());
        // 设置页面切换监听
        vpFragmentContainer.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(
                            int position, float positionOffset, int positionOffsetPixels) {}

                    @Override
                    public void onPageSelected(int position) {
                        // 当页面切换完成之后， 同时也要把 tab 设置到正确的位置
                        tabGroupView.setSelected(position);
                        if (appViewModel.isUltraGroupDebugMode()) {
                            if (position == 0) {
                                RouteUtils.registerActivity(
                                        RouteUtils.RongActivityType.ConversationActivity,
                                        ConversationActivity.class);
                            } else if (position == 1) {
                                RouteUtils.registerActivity(
                                        RouteUtils.RongActivityType.ConversationActivity,
                                        UltraConversationActivity.class);
                            }
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });
    }

    /** 初始化ViewModel */
    private void initViewModel() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        securityViewModel = ViewModelProviders.of(this).get(SecurityViewModel.class);
        if (appViewModel.isUltraGroupDebugMode()) {
            mConversationListViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
        }
        appViewModel
                .getHasNewVersion()
                .observe(
                        this,
                        new Observer<Resource<VersionInfo.AndroidVersion>>() {
                            @Override
                            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                                if (resource.status == Status.SUCCESS && resource.data != null) {
                                    if (tabGroupView.getSelectedItemId() != tabsMap.get(ME)) {
                                        ((MainBottomTabItem) tabGroupView.getView(tabsMap.get(ME)))
                                                .setRedVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });

        // 未读消息
        mainViewModel
                .getUnReadNum()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer count) {
                                MainBottomTabItem chatTab =
                                        (MainBottomTabItem) tabGroupView.getView(tabsMap.get(CHAT));
                                if (count == 0) {
                                    chatTab.setNumVisibility(View.GONE);
                                } else if (count > 0 && count < 100) {
                                    chatTab.setNumVisibility(View.VISIBLE);
                                    chatTab.setNum(String.valueOf(count));
                                } else {
                                    chatTab.setVisibility(View.VISIBLE);
                                    chatTab.setNum(
                                            getString(
                                                    R.string.seal_main_chat_tab_more_read_message));
                                }
                            }
                        });

        // 新朋友数量
        mainViewModel
                .getNewFriendNum()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer count) {
                                MainBottomTabItem chatTab =
                                        tabGroupView.getView(tabsMap.get(CONTACTS));
                                if (count > 0) {

                                    chatTab.setRedVisibility(View.VISIBLE);
                                } else {
                                    chatTab.setRedVisibility(View.GONE);
                                }
                            }
                        });

        mainViewModel
                .getPrivateChatLiveData()
                .observe(
                        this,
                        new Observer<FriendShipInfo>() {
                            @Override
                            public void onChanged(FriendShipInfo friendShipInfo) {
                                Bundle bundle = new Bundle();
                                bundle.putString(
                                        "title",
                                        TextUtils.isEmpty(friendShipInfo.getDisplayName())
                                                ? friendShipInfo.getUser().getNickname()
                                                : friendShipInfo.getDisplayName());
                                RouteUtils.routeToConversationActivity(
                                        MainActivity.this,
                                        ConversationIdentifier.obtainPrivate(
                                                friendShipInfo.getUser().getId()),
                                        bundle);
                            }
                        });
        securityViewModel
                .getSecurityVerify()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                if (resource.data.isKickOut()) {
                                    logoutBySecurity();
                                }
                            }
                        });
        securityViewModel
                .getSecurityStatus()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS && resource.data != null) {
                                boolean openEnable = resource.data.openEnable;
                                if (!BuildConfig.DEBUG && openEnable) {
                                    SMSDKUtils.init(
                                            MainActivity.this.getApplicationContext(),
                                            new SMSDKUtils.Callback() {
                                                @Override
                                                public void onSuccess(String id) {
                                                    securityViewModel.doSecurityVerify(id);
                                                }
                                            });
                                }
                            }
                        });
    }

    /** 清理未读消息状态 */
    private void clearUnreadStatus() {
        if (mainViewModel != null) {
            mainViewModel.clearMessageUnreadStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_START_CHAT:
                    mainViewModel.startPrivateChat(data.getStringExtra(IntentExtra.STR_TARGET_ID));
                    break;
                case REQUEST_START_GROUP:
                    ArrayList<String> memberList =
                            data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
                    SLog.i(TAG, "memberList.size = " + memberList.size());
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    intent.putExtra(IntentExtra.LIST_STR_ID_LIST, memberList);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    /** 发起单聊 */
    @Override
    public void onStartChartClick() {
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            startActivity(StartChatActivity.newIntent(this));
        } else {
            Intent intent = new Intent(this, SelectSingleFriendActivity.class);
            startActivityForResult(intent, REQUEST_START_CHAT);
        }
    }

    /** 创建群组 */
    @Override
    public void onCreateGroupClick() {
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            startActivity(FriendSelectActivity.newIntent(this));
        } else {
            Intent intent = new Intent(this, SelectCreateGroupActivity.class);
            startActivityForResult(intent, REQUEST_START_GROUP);
        }
    }

    /** 添加好友 */
    @Override
    public void onAddFriendClick() {
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            startActivity(AddFriendListActivity.newIntent(this));
        } else {
            Intent intent = new Intent(this, AddFriendActivity.class);
            startActivity(intent);
        }
    }

    /** 扫一扫 */
    @Override
    public void onScanClick() {
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            startActivity(QrCodeScanActivity.newIntent(this));
        } else {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onAiAssistantClick() {
        if (SealTalkDebugTestActivity.isUserManagementEnabled(this)) {
            startActivity(OpenClawGuideActivity.newIntent(this));
        }
    }

    @RequiresApi(api = 33)
    private void askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.GET_PERMISSIONS) {

        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // FCM SDK (and your app) can post notifications.
                        } else {

                        }
                    });

    private void initRongPrivacy() {
        /** 显示同意隐私协议对话框 */
        AuthorityPrivacyDialog.Builder builder = new AuthorityPrivacyDialog.Builder();
        //                        .setButtonText(R.string.privacy_agree, R.string.privacy_disagree);
        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {}

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                        System.exit(0);
                    }
                });
        builder.build().show(getSupportFragmentManager(), null);
    }
}
