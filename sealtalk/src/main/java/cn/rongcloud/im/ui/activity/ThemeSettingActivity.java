package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ThemePreferenceManager;
import io.rong.imkit.config.IMKitThemeManager;
import io.rong.imkit.usermanage.component.HeadComponent;

/**
 * 主题设置页面
 *
 * <p>Debug 环境下：显示传统主题、欢快主题、活力橙主题，欢快主题可展开子选项（跟随系统、浅色、深色），默认跟随系统
 *
 * <p>非 Debug 环境下：显示传统主题、欢快主题，欢快主题默认跟随系统，无子选项，不显示活力橙
 */
public class ThemeSettingActivity extends BaseActivity {

    public static final String CUSTOM_ORANGE_THEME = "CUSTOM_ORANGE_THEME";

    private HeadComponent headComponent;
    private View livelyModeContainer;
    private SettingItemView traditionThemeSiv;
    private SettingItemView livelyThemeSiv;
    private SettingItemView livelyFollowSystemSubSiv;
    private SettingItemView livelyLightSubSiv;
    private SettingItemView livelyDarkSubSiv;
    private SettingItemView customOrangeSiv;

    // 待保存的主题选择
    private String pendingThemeType;
    private int pendingNightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_setting);
        initView();
        enforceSupportedTheme();
        updateThemeSelection();
    }

    /** 初始化布局 */
    private void initView() {
        // 初始化 HeadComponent
        headComponent = findViewById(R.id.head_component);

        // 添加保存按钮，点击后返回主界面的“我的”页
        headComponent.setRightClickListener(v -> applyPendingTheme());

        traditionThemeSiv = findViewById(R.id.siv_theme_origin_light);
        livelyThemeSiv = findViewById(R.id.siv_theme_lively_light);
        livelyFollowSystemSubSiv = findViewById(R.id.siv_theme_lively_follow_system_sub);
        livelyLightSubSiv = findViewById(R.id.siv_theme_lively_light_sub);
        livelyDarkSubSiv = findViewById(R.id.siv_theme_lively_dark_sub);
        customOrangeSiv = findViewById(R.id.siv_theme_custom_orange);
        livelyModeContainer = findViewById(R.id.layout_lively_mode_container);

        boolean isDebug = isDebugEnvironment();

        // Debug 环境下子选项容器初始隐藏，点击欢快主题时才显示
        // 非 Debug 环境下始终隐藏
        livelyModeContainer.setVisibility(View.GONE);

        // 活力橙选项：仅在 Debug 环境下显示
        customOrangeSiv.setVisibility(isDebug ? View.VISIBLE : View.GONE);

        // 初始化待保存的主题状态
        pendingThemeType = IMKitThemeManager.getCurrentThemeName();
        pendingNightMode = AppCompatDelegate.getDefaultNightMode();

        // 传统主题点击事件
        traditionThemeSiv.setOnClickListener(
                v -> {
                    pendingThemeType = IMKitThemeManager.TRADITION_THEME;
                    // 隐藏欢快主题的子选项
                    if (isDebug) {
                        livelyModeContainer.setVisibility(View.GONE);
                    }
                    updateThemeSelection();
                });

        // 欢快主题点击事件
        livelyThemeSiv.setOnClickListener(
                v -> {
                    if (isDebug) {
                        pendingThemeType = IMKitThemeManager.LIVELY_THEME;
                        // Debug 环境下：切换子选项显示/隐藏
                        if (livelyModeContainer.getVisibility() == View.VISIBLE) {
                            livelyModeContainer.setVisibility(View.GONE);
                        } else {
                            livelyModeContainer.setVisibility(View.VISIBLE);
                            // 确保当前选中的子选项正确显示
                            updateLivelySubSelection();
                        }
                        updateThemeSelection();
                    } else {
                        // 非 Debug 环境下：直接切换到欢快主题（跟随系统）
                        pendingThemeType = IMKitThemeManager.LIVELY_THEME;
                        pendingNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        updateThemeSelection();
                    }
                });

        // 欢快主题 - 跟随系统（仅 Debug 环境）
        livelyFollowSystemSubSiv.setOnClickListener(
                v -> {
                    pendingThemeType = IMKitThemeManager.LIVELY_THEME;
                    pendingNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    updateThemeSelection();
                });

        // 欢快主题 - 浅色模式（仅 Debug 环境）
        livelyLightSubSiv.setOnClickListener(
                v -> {
                    pendingThemeType = IMKitThemeManager.LIVELY_THEME;
                    pendingNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    updateThemeSelection();
                });

        // 欢快主题 - 深色模式（仅 Debug 环境）
        livelyDarkSubSiv.setOnClickListener(
                v -> {
                    pendingThemeType = IMKitThemeManager.LIVELY_THEME;
                    pendingNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    updateThemeSelection();
                });

        // 自定义主题 - 活力橙（仅 Debug 环境）
        customOrangeSiv.setOnClickListener(
                v -> {
                    pendingThemeType = CUSTOM_ORANGE_THEME;
                    pendingNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    // 隐藏欢快主题的子选项
                    if (isDebug) {
                        livelyModeContainer.setVisibility(View.GONE);
                    }
                    updateThemeSelection();
                });
    }

    /** 更新主题选中状态 */
    private void updateThemeSelection() {
        String currentTheme = pendingThemeType;

        traditionThemeSiv.setSelected(IMKitThemeManager.TRADITION_THEME.equals(currentTheme));
        livelyThemeSiv.setSelected(IMKitThemeManager.LIVELY_THEME.equals(currentTheme));
        customOrangeSiv.setSelected(CUSTOM_ORANGE_THEME.equals(currentTheme));

        // Debug 环境下，如果当前是欢快主题，显示子选项并更新选中状态
        if (isDebugEnvironment() && IMKitThemeManager.LIVELY_THEME.equals(currentTheme)) {
            livelyModeContainer.setVisibility(View.VISIBLE);
            updateLivelySubSelection();
        } else {
            livelyModeContainer.setVisibility(View.GONE);
        }
    }

    /** 更新欢快主题子选项的选中状态 */
    private void updateLivelySubSelection() {
        int nightMode = pendingNightMode;
        livelyFollowSystemSubSiv.setSelected(
                nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        livelyLightSubSiv.setSelected(nightMode == AppCompatDelegate.MODE_NIGHT_NO);
        livelyDarkSubSiv.setSelected(nightMode == AppCompatDelegate.MODE_NIGHT_YES);
    }

    /**
     * 切换主题
     *
     * @param themeType 主题类型字符串
     */
    private void changeTheme(String themeType) {
        ThemePreferenceManager.saveThemeType(this, themeType);
        ThemePreferenceManager.saveBaseTheme(this, null);

        // Debug 环境下默认跟随系统，非 Debug 环境下默认浅色模式
        int nightMode =
                isDebugEnvironment()
                        ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        : AppCompatDelegate.MODE_NIGHT_NO;

        ThemePreferenceManager.saveNightMode(this, nightMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
        IMKitThemeManager.changeInnerTheme(this, themeType);

        pendingThemeType = themeType;
        pendingNightMode = nightMode;
        updateThemeSelection();
        backToMainActivity();
    }

    /**
     * 切换欢快主题的深浅色模式
     *
     * @param nightMode 夜间模式，AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM（跟随系统）、
     *     AppCompatDelegate.MODE_NIGHT_NO（浅色）或 AppCompatDelegate.MODE_NIGHT_YES（深色）
     */
    private void changeLivelyThemeMode(int nightMode) {
        ThemePreferenceManager.saveThemeType(this, IMKitThemeManager.LIVELY_THEME);
        ThemePreferenceManager.saveBaseTheme(this, null);
        IMKitThemeManager.changeInnerTheme(this, IMKitThemeManager.LIVELY_THEME);
        ThemePreferenceManager.saveNightMode(this, nightMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        pendingThemeType = IMKitThemeManager.LIVELY_THEME;
        pendingNightMode = nightMode;
        updateThemeSelection();
        backToMainActivity();
    }

    /**
     * 切换到自定义主题
     *
     * @param customThemeType 自定义主题类型
     */
    private void changeCustomTheme(String customThemeType) {
        ThemePreferenceManager.saveThemeType(this, customThemeType);
        ThemePreferenceManager.saveBaseTheme(this, IMKitThemeManager.LIVELY_THEME);
        IMKitThemeManager.changeCustomTheme(this, customThemeType, IMKitThemeManager.LIVELY_THEME);
        ThemePreferenceManager.saveNightMode(this, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        pendingThemeType = customThemeType;
        pendingNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        updateThemeSelection();
        backToMainActivity();
    }

    /**
     * 强制使用支持的主题（非 Debug 环境下）
     *
     * <p>如果当前主题不是传统主题或欢快主题，则切换到欢快主题（浅色模式）
     */
    private void enforceSupportedTheme() {
        if (isDebugEnvironment()) {
            return;
        }

        String currentTheme = IMKitThemeManager.getCurrentThemeName();
        boolean isSupported =
                IMKitThemeManager.TRADITION_THEME.equals(currentTheme)
                        || IMKitThemeManager.LIVELY_THEME.equals(currentTheme);

        if (!isSupported) {
            // 切换到欢快主题（浅色模式）
            changeLivelyThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /** 判断是否为 Debug 环境 */
    private boolean isDebugEnvironment() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        return prefs.getBoolean("isDebug", false);
    }

    /** 返回主界面 */
    private void backToMainActivity() {
        // 重启主页面以应用最新语言，并跳转到“我的”页
        Intent mainIntent = new Intent(ThemeSettingActivity.this, MainActivity.class);
        mainIntent.putExtra(MainActivity.PARAMS_TAB_INDEX, MainActivity.ME);
        mainIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // 清空原任务栈，强制重建界面
        startActivity(mainIntent);
        finish();
    }

    /** 保存当前待选主题并返回 */
    private void applyPendingTheme() {
        if (pendingThemeType == null) {
            backToMainActivity();
            return;
        }

        if (IMKitThemeManager.TRADITION_THEME.equals(pendingThemeType)) {
            changeTheme(IMKitThemeManager.TRADITION_THEME);
            return;
        }

        if (IMKitThemeManager.LIVELY_THEME.equals(pendingThemeType)) {
            changeLivelyThemeMode(pendingNightMode);
            return;
        }

        if (CUSTOM_ORANGE_THEME.equals(pendingThemeType)) {
            changeCustomTheme(CUSTOM_ORANGE_THEME);
            return;
        }

        // 默认走主题切换逻辑
        changeTheme(pendingThemeType);
    }
}
