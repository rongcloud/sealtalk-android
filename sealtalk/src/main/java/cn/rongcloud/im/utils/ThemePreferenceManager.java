package cn.rongcloud.im.utils;

import android.content.Context;
import android.content.SharedPreferences;
import io.rong.imkit.config.IMKitThemeManager;

/** 主题配置管理类 用于持久化保存和读取用户选择的主题 */
public class ThemePreferenceManager {

    private static final String PREF_NAME = "theme_preferences";
    private static final String KEY_THEME_TYPE = "theme_type";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_BASE_THEME = "base_theme";

    /**
     * 保存主题类型
     *
     * @param context 上下文
     * @param themeType 主题类型字符串
     */
    public static void saveThemeType(Context context, String themeType) {
        if (context == null || themeType == null || themeType.isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THEME_TYPE, themeType);
        editor.apply();
    }

    /**
     * 获取保存的主题类型
     *
     * @param context 上下文
     * @return 主题类型字符串，如果没有保存则返回默认的 ORIGIN_LIGHT
     */
    public static String getThemeType(Context context) {
        if (context == null) {
            return IMKitThemeManager.TRADITION_THEME;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String themeTypeName = prefs.getString(KEY_THEME_TYPE, IMKitThemeManager.TRADITION_THEME);

        return themeTypeName;
    }

    /**
     * 保存夜间模式设置
     *
     * @param context 上下文
     * @param nightMode 夜间模式（AppCompatDelegate.MODE_NIGHT_*）
     */
    public static void saveNightMode(Context context, int nightMode) {
        if (context == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NIGHT_MODE, nightMode);
        editor.apply();
    }

    /**
     * 获取保存的夜间模式设置
     *
     * @param context 上下文
     * @return 夜间模式，默认返回跟随系统（MODE_NIGHT_FOLLOW_SYSTEM）
     */
    public static int getNightMode(Context context) {
        if (context == null) {
            return androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(
                KEY_NIGHT_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * 保存自定义主题的基础主题
     *
     * @param context 上下文
     * @param baseTheme 基础主题类型（TRADITION_THEME 或 LIVELY_THEME），如果为 null 表示非自定义主题
     */
    public static void saveBaseTheme(Context context, String baseTheme) {
        if (context == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (baseTheme == null) {
            editor.remove(KEY_BASE_THEME);
        } else {
            editor.putString(KEY_BASE_THEME, baseTheme);
        }
        editor.apply();
    }

    /**
     * 获取保存的基础主题
     *
     * @param context 上下文
     * @return 基础主题类型，如果没有保存则返回 null（表示非自定义主题）
     */
    public static String getBaseTheme(Context context) {
        if (context == null) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BASE_THEME, null);
    }

    /**
     * 清除保存的主题配置
     *
     * @param context 上下文
     */
    public static void clearThemeType(Context context) {
        if (context == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_THEME_TYPE);
        editor.remove(KEY_NIGHT_MODE);
        editor.remove(KEY_BASE_THEME);
        editor.apply();
    }
}
