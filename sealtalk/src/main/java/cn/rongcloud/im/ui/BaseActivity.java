package cn.rongcloud.im.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.LoginActivity;
import cn.rongcloud.im.ui.activity.SplashActivity;
import cn.rongcloud.im.ui.dialog.LoadingDialog;
import cn.rongcloud.im.utils.StatusBarUtil;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.AppViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.language.RongConfigurationManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    protected AppViewModel appViewModel;
    private boolean mEnableListenKeyboardState = false;
    private LoadingDialog dialog;
    private final Handler handler = new Handler();
    private long lastClickTime;

    private final BroadcastReceiver loginExpirationReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (appViewModel != null) {
                        appViewModel.loginExpiration();
                    }
                    sendLogoutNotify();
                    Intent in = new Intent(BaseActivity.this, LoginActivity.class);
                    in.putExtra(IntentExtra.BOOLEAN_LOGIN_EXPIRATION, true);
                    startActivity(in);
                    finish();
                }
            };

    private final BroadcastReceiver logoutReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    RongIM.getInstance().logout();
                    finish();
                }
            };

    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            Context context =
                    RongConfigurationManager.getInstance().getConfigurationContext(newBase);
            super.attachBaseContext(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /*
         * 修复部分 Android 8.0 手机在TargetSDK 大于 26 时，在透明主题时指定 Activity 方向时崩溃的问题
         */
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation();
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        if (isFullScreen()) {
            // 隐藏Activity顶部的状态栏
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // 监听退出
        if (isObserveLogout()) {
            registerLoginExpiration();
            registerLogoutBoardcast();
            IMManager.getInstance()
                    .getKickedOffline()
                    .observe(
                            this,
                            new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean isLogout) {
                                    /*
                                     * 只有当前显示的 Activity 会走此段逻辑
                                     */
                                    if (isLogout) {
                                        SLog.d(BaseActivity.class.getCanonicalName(), "Log out.");
                                        IMManager.getInstance().resetKickedOfflineState();
                                        logout(true, false);
                                    }
                                }
                            });

            IMManager.getInstance()
                    .getUserIsAbandon()
                    .observe(
                            this,
                            new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean isLogout) {
                                    /*
                                     * 只有当前显示的 Activity 会走此段逻辑
                                     */
                                    if (isLogout) {
                                        SLog.d(
                                                BaseActivity.class.getCanonicalName(),
                                                "user Log out.");
                                        IMManager.getInstance().resetUserLogoutState();
                                        if (IMManager.getInstance().userIsLoginState()) {
                                            IMManager.getInstance().logoutAndClear();
                                            logout(false, true);
                                        }
                                    }
                                }
                            });

            IMManager.getInstance()
                    .getUserIsBlocked()
                    .observe(
                            this,
                            new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean isLogout) {
                                    /*
                                     * 只有当前显示的 Activity 会走此段逻辑
                                     */
                                    if (isLogout) {
                                        SLog.d(
                                                BaseActivity.class.getCanonicalName(),
                                                "user blocked.");
                                        IMManager.getInstance().resetUserBlockedState();
                                        if (IMManager.getInstance().userIsLoginState()) {
                                            IMManager.getInstance().logoutAndClear();
                                            logout(false, false);
                                        }
                                    }
                                }
                            });
        }

        // 清除已存在的 Fragment 防止因没有复用导致叠加显示
        clearAllFragmentExistBeforeCreate();
    }

    private void logout(boolean isKick, boolean isLogout) {
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        if (isKick) {
            intent.putExtra(IntentExtra.BOOLEAN_KICKED_BY_OTHER_USER, true);
        } else if (isLogout) {
            intent.putExtra(IntentExtra.BOOLEAN_USER_ABANDON, true);
        } else {
            intent.putExtra(IntentExtra.BOOLEAN_USER_BLOCKED, true);
        }
        sendLogoutNotify();
        startActivity(intent);
        finish();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initStatusBar();
    }

    private void initStatusBar() {
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        StatusBarUtil.setStatusBarColor(
                this,
                getResources()
                        .getColor(
                                io.rong.imkit.R.color
                                        .rc_background_main_color)); // Color.parseColor("#F5F6F9")
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initStatusBar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initStatusBar();
    }

    /**
     * 清除所有已存在的 Fragment 防止因重建 Activity 时，前 Fragment 没有销毁和重新复用导致界面重复显示 如果有自己实现 Fragment
     * 的复用，请复写此方法并不实现内容
     */
    public void clearAllFragmentExistBeforeCreate() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() == 0) return;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitNow();
    }

    /**
     * 是否隐藏状态栏全屏
     *
     * @return
     */
    protected boolean isFullScreen() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEnableListenKeyboardState) {
            addKeyboardStateListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeKeyBoardStateListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播
        if (isObserveLogout()) {
            unRegisterLogoutBroadcast();
            unRegisterLoginExpirationBroadcast();
        }
        // 移除所有
        handler.removeCallbacksAndMessages(null);
    }

    /** 隐藏键盘 */
    public void hideInputKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /** 设置沉浸式状态栏 */
    public void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /** 隐藏导航键 */
    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 启动键盘状态监听
     *
     * @param enable
     */
    public void enableKeyboardStateListener(boolean enable) {
        mEnableListenKeyboardState = enable;
    }

    /** 添加键盘显示监听 */
    private void addKeyboardStateListener() {
        getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(onKeyboardStateChangedListener);
    }

    /** 移除键盘显示监听 */
    private void removeKeyBoardStateListener() {
        getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .removeOnGlobalLayoutListener(onKeyboardStateChangedListener);
    }

    /** 监听键盘显示状态 */
    private ViewTreeObserver.OnGlobalLayoutListener onKeyboardStateChangedListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
                int mScreenHeight = 0;
                boolean isCurrentActive = false;

                private int getScreenHeight() {
                    if (mScreenHeight > 0) {
                        return mScreenHeight;
                    }
                    Point point = new Point();
                    ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                            .getDefaultDisplay()
                            .getSize(point);
                    mScreenHeight = point.y;
                    return mScreenHeight;
                }

                @Override
                public void onGlobalLayout() {
                    Rect rect = new Rect();
                    // 获取当前窗口显示范围
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                    int screenHeight = getScreenHeight();
                    int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
                    boolean isActive = false;
                    if (Math.abs(keyboardHeight) > screenHeight / 4) {
                        isActive = true; // 超过屏幕1/4则表示弹出了输入法
                    }

                    if (isCurrentActive != isActive) {
                        isCurrentActive = isActive;
                        onKeyboardStateChanged(isActive, keyboardHeight);
                    }
                }
            };

    /**
     * 当软键盘显示时回调 此回调在调用{@link BaseActivity#enableKeyboardStateListener(boolean)}启用监听键盘显示
     *
     * @param isShown
     * @param height
     */
    public void onKeyboardStateChanged(boolean isShown, int height) {}

    /**
     * 判断当前主题是否是透明悬浮
     *
     * @return
     */
    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes =
                    (int[])
                            Class.forName("com.android.internal.R$styleable")
                                    .getField("Window")
                                    .get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    /**
     * 改变当前的 Activity 的显示方向 解决当前Android 8.0 系统在透明主题时设定显示方向时崩溃的问题
     *
     * @return
     */
    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        /*
         * 修复 Android 8.0 手机在TargetSDK 大于 26 时，指定 Activity 方向时崩溃的问题
         */
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    public void showToast(String text) {
        // toast
        ToastUtils.showToast(text);
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    // 退出应用

    /**
     * 是否监听退出应用操作，默认监听， 如果不像监听， 可复写 此方法并返回 false
     *
     * @return
     */
    public boolean isObserveLogout() {
        return true;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLogoutBoardcast() {
        IntentFilter intentFilter = new IntentFilter("com.rong.im.action.logout");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logoutReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(logoutReceiver, intentFilter);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLoginExpiration() {
        IntentFilter intentFilter = new IntentFilter("com.rong.im.action.login.expiration");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(loginExpirationReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(loginExpirationReceiver, intentFilter);
        }
    }

    private void unRegisterLogoutBroadcast() {
        unregisterReceiver(logoutReceiver);
    }

    private void unRegisterLoginExpirationBroadcast() {
        unregisterReceiver(loginExpirationReceiver);
    }

    /** 通知通其他注册了登出广播的 Activity 关闭 */
    public void sendLogoutNotify() {
        // 发送广播
        Intent intent = new Intent("com.rong.im.action.logout");
        sendBroadcast(intent);
    }

    /** 安全原因触发退出登录 */
    public void logoutBySecurity() {
        sendLogoutNotify();
        IMManager.getInstance().resetKickedOfflineState();
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.putExtra(IntentExtra.BOOLEAN_KICKED_BY_SECURITY, true);
        startActivity(intent);
        finish();
    }

    // 记录dialog 显示创建时间
    private long dialogCreateTime;

    /**
     * 显示加载 dialog
     *
     * @param msg
     */
    public void showLoadingDialog(String msg) {
        if (dialog == null || (dialog.getDialog() != null && !dialog.getDialog().isShowing())) {
            dialogCreateTime = System.currentTimeMillis();
            dialog = new LoadingDialog();
            dialog.setLoadingInformation(msg);
            dialog.show(getSupportFragmentManager(), "loading_dialog");
        }
    }

    /**
     * 显示加载 dialog
     *
     * @param msgResId
     */
    public void showLoadingDialog(int msgResId) {
        showLoadingDialog(getString(msgResId));
    }

    /** 取消加载dialog */
    public void dismissLoadingDialog() {
        dismissLoadingDialog(null);
    }

    /**
     * 取消加载dialog. 因为延迟， 所以要延时完成之后， 再在 runnable 中执行逻辑.
     *
     * <p>延迟关闭时间是因为接口有时返回太快。
     */
    public void dismissLoadingDialog(Runnable runnable) {
        if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
            // 由于可能请求接口太快，则导致加载页面一闪问题， 所有再次做判断，
            // 如果时间太快（小于 500ms）， 则会延时 1s，再做关闭。
            if (System.currentTimeMillis() - dialogCreateTime < 500) {
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (runnable != null) {
                                    runnable.run();
                                }
                                if (dialog != null && !isFinishing() && !isDestroyed()) {
                                    dialog.dismiss();
                                    dialog = null;
                                }
                            }
                        },
                        1000);

            } else {
                dialog.dismiss();
                dialog = null;
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    /**
     * 为防止多次重复点击
     *
     * @return
     */
    public synchronized boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /** 从Intent取出ConversationIdentifier，没有的话取ConversationType、targetId、channelId构建 */
    public ConversationIdentifier initConversationIdentifier() {
        Intent intent = getIntent();
        if (intent.hasExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER)) {
            ConversationIdentifier identifier =
                    intent.getParcelableExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER);
            if (identifier != null) {
                return identifier;
            }
        }
        String targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        Conversation.ConversationType conversationType =
                (Conversation.ConversationType)
                        intent.getSerializableExtra(IntentExtra.SERIA_CONVERSATION_TYPE);
        return ConversationIdentifier.obtain(conversationType, targetId, "");
    }
}
