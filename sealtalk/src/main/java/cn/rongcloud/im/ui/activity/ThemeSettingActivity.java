package cn.rongcloud.im.ui.activity;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ThemePreferenceManager;
import io.rong.imkit.config.IMKitThemeManager;

/** 主题设置页面 支持切换传统主题、现代主题和自定义主题 */
public class ThemeSettingActivity extends TitleBaseActivity {

    // 自定义主题常量
    private static final String CUSTOM_ORANGE_THEME = "CUSTOM_ORANGE_THEME";

    private SettingItemView traditionThemeSiv;
    private SettingItemView livelyThemeSiv;
    private SettingItemView livelyFollowSystemSubSiv;
    private SettingItemView livelyLightSubSiv;
    private SettingItemView livelyDarkSubSiv;
    private SettingItemView customOrangeSiv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_setting);
        initView();
        updateThemeSelection();
    }

    /** 初始化布局 */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_theme_setting_title);

        traditionThemeSiv = findViewById(R.id.siv_theme_origin_light);
        livelyThemeSiv = findViewById(R.id.siv_theme_lively_light);
        livelyFollowSystemSubSiv = findViewById(R.id.siv_theme_lively_follow_system_sub);
        livelyLightSubSiv = findViewById(R.id.siv_theme_lively_light_sub);
        livelyDarkSubSiv = findViewById(R.id.siv_theme_lively_dark_sub);
        customOrangeSiv = findViewById(R.id.siv_theme_custom_orange);

        // 传统主题
        traditionThemeSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeTheme(IMKitThemeManager.TRADITION_THEME);
                        // 隐藏子选项
                        livelyFollowSystemSubSiv.setVisibility(View.GONE);
                        livelyLightSubSiv.setVisibility(View.GONE);
                        livelyDarkSubSiv.setVisibility(View.GONE);
                    }
                });

        // 欢快主题（点击展开子选项）
        livelyThemeSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 切换子选项显示/隐藏
                        if (livelyFollowSystemSubSiv.getVisibility() == View.VISIBLE) {
                            livelyFollowSystemSubSiv.setVisibility(View.GONE);
                            livelyLightSubSiv.setVisibility(View.GONE);
                            livelyDarkSubSiv.setVisibility(View.GONE);
                        } else {
                            livelyFollowSystemSubSiv.setVisibility(View.VISIBLE);
                            livelyLightSubSiv.setVisibility(View.VISIBLE);
                            livelyDarkSubSiv.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // 欢快主题 - 跟随系统
        livelyFollowSystemSubSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeLivelyThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                });

        // 欢快主题 - 浅色模式
        livelyLightSubSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeLivelyThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                });

        // 欢快主题 - 深色模式
        livelyDarkSubSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeLivelyThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                });

        // 自定义主题 - 活力橙
        customOrangeSiv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeCustomTheme(CUSTOM_ORANGE_THEME);
                        // 隐藏欢快主题的子选项
                        livelyFollowSystemSubSiv.setVisibility(View.GONE);
                        livelyLightSubSiv.setVisibility(View.GONE);
                        livelyDarkSubSiv.setVisibility(View.GONE);
                    }
                });
    }

    /** 更新主题选中状态 */
    private void updateThemeSelection() {
        String currentTheme = IMKitThemeManager.getCurrentThemeName();

        traditionThemeSiv.setSelected(IMKitThemeManager.TRADITION_THEME.equals(currentTheme));
        customOrangeSiv.setSelected(CUSTOM_ORANGE_THEME.equals(currentTheme));

        // 如果是欢快主题，展开子选项并根据当前模式设置选中状态
        boolean isLivelyTheme = IMKitThemeManager.LIVELY_THEME.equals(currentTheme);
        livelyThemeSiv.setSelected(isLivelyTheme);

        if (isLivelyTheme) {
            livelyFollowSystemSubSiv.setVisibility(View.VISIBLE);
            livelyLightSubSiv.setVisibility(View.VISIBLE);
            livelyDarkSubSiv.setVisibility(View.VISIBLE);

            // 根据当前夜间模式设置子选项的选中状态
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            livelyFollowSystemSubSiv.setSelected(
                    nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            livelyLightSubSiv.setSelected(nightMode == AppCompatDelegate.MODE_NIGHT_NO);
            livelyDarkSubSiv.setSelected(nightMode == AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            livelyFollowSystemSubSiv.setVisibility(View.GONE);
            livelyLightSubSiv.setVisibility(View.GONE);
            livelyDarkSubSiv.setVisibility(View.GONE);
        }
    }

    /**
     * 切换主题
     *
     * @param themeType 主题类型字符串
     */
    private void changeTheme(String themeType) {
        // 保存主题选择到 SharedPreferences
        ThemePreferenceManager.saveThemeType(this, themeType);
        ThemePreferenceManager.saveBaseTheme(this, null); // 内置主题没有基础主题

        // 切换主题
        IMKitThemeManager.changeInnerTheme(this, themeType);

        // 更新选中状态
        updateThemeSelection();

        // 返回主页面并重启以应用新主题
        backToMainActivity();
    }

    /**
     * 切换欢快主题的深浅色模式
     *
     * @param nightMode 夜间模式，AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM（跟随系统）、
     *     AppCompatDelegate.MODE_NIGHT_NO（浅色）或 AppCompatDelegate.MODE_NIGHT_YES（深色）
     */
    private void changeLivelyThemeMode(int nightMode) {
        // 先切换到欢快主题
        ThemePreferenceManager.saveThemeType(this, IMKitThemeManager.LIVELY_THEME);
        ThemePreferenceManager.saveBaseTheme(this, null); // 内置主题没有基础主题
        IMKitThemeManager.changeInnerTheme(this, IMKitThemeManager.LIVELY_THEME);

        // 保存夜间模式设置
        ThemePreferenceManager.saveNightMode(this, nightMode);

        // 调用系统 API 设置深浅色模式
        AppCompatDelegate.setDefaultNightMode(nightMode);

        // 更新选中状态
        updateThemeSelection();

        // 返回主页面并重启以应用新主题
        backToMainActivity();
    }

    /**
     * 切换到自定义主题
     *
     * @param customThemeType 自定义主题类型
     */
    private void changeCustomTheme(String customThemeType) {
        // 保存主题选择到 SharedPreferences
        ThemePreferenceManager.saveThemeType(this, customThemeType);
        ThemePreferenceManager.saveBaseTheme(this, IMKitThemeManager.LIVELY_THEME); // 保存基础主题

        // 使用 changeCustomTheme 切换到自定义主题（基于欢快主题）
        IMKitThemeManager.changeCustomTheme(this, customThemeType, IMKitThemeManager.LIVELY_THEME);

        // 保存默认的夜间模式（跟随系统）
        ThemePreferenceManager.saveNightMode(this, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // 更新选中状态
        updateThemeSelection();

        // 返回主页面并重启以应用新主题
        backToMainActivity();
    }

    /** 返回主界面 */
    private void backToMainActivity() {
        Intent mainActivity = new Intent(ThemeSettingActivity.this, MainActivity.class);
        mainActivity.putExtra(MainActivity.PARAMS_TAB_INDEX, MainActivity.ME);
        Intent themeSettingActivity =
                new Intent(ThemeSettingActivity.this, ThemeSettingActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(ThemeSettingActivity.this);
        taskStackBuilder.addNextIntent(mainActivity);
        taskStackBuilder.addNextIntent(themeSettingActivity);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }
}
