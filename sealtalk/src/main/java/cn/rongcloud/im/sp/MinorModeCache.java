package cn.rongcloud.im.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import cn.rongcloud.im.im.IMManager;

/** 未成年人模式密码缓存管理类 存储结构：user_id 对应 minor_password */
public class MinorModeCache {
    private static final String SP_NAME = "minor_mode_cache";
    private static final String KEY_MINOR_PASSWORD_PREFIX = "minor_password_";
    private static final String DEFAULT_PASSWORD = "000000";

    private static volatile MinorModeCache instance;
    private final SharedPreferences sp;

    private MinorModeCache() {
        sp =
                IMManager.getInstance()
                        .getContext()
                        .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static MinorModeCache getInstance() {
        if (instance == null) {
            synchronized (MinorModeCache.class) {
                if (instance == null) {
                    instance = new MinorModeCache();
                }
            }
        }
        return instance;
    }

    /**
     * 设置未成年人模式密码
     *
     * @param userId 用户ID
     * @param password 密码（6位数字）
     */
    public void setMinorPassword(String userId, String password) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        sp.edit().putString(KEY_MINOR_PASSWORD_PREFIX + userId, password).apply();
    }

    /**
     * 获取未成年人模式密码
     *
     * @param userId 用户ID
     * @return 密码，如果未设置返回null
     */
    public String getMinorPassword(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        return sp.getString(KEY_MINOR_PASSWORD_PREFIX + userId, null);
    }

    /**
     * 判断是否开启了未成年人模式
     *
     * @param userId 用户ID
     * @return true表示已开启，false表示未开启
     */
    public boolean isMinorModeEnabled(String userId) {
        return !TextUtils.isEmpty(getMinorPassword(userId));
    }

    /**
     * 验证密码是否无效
     *
     * @param userId 用户ID
     * @param password 待验证的密码
     * @return true表示密码正确
     */
    public boolean verifyPasswordInvalid(String userId, String password) {
        String storedPassword = getMinorPassword(userId);
        return TextUtils.isEmpty(storedPassword)
                || TextUtils.isEmpty(password)
                || !storedPassword.equals(password);
    }

    /**
     * 清除未成年人模式密码（关闭未成年人模式）
     *
     * @param userId 用户ID
     */
    public void clearMinorPassword(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        sp.edit().remove(KEY_MINOR_PASSWORD_PREFIX + userId).apply();
    }

    /**
     * 重置密码为默认密码 "000000"
     *
     * @param userId 用户ID
     */
    public void resetPasswordToDefault(String userId) {
        setMinorPassword(userId, DEFAULT_PASSWORD);
    }

    /**
     * 验证密码格式是否正确（6位数字）
     *
     * @param password 密码
     * @return true表示格式正确
     */
    public static boolean isInvalidPasswordFormat(String password) {
        if (TextUtils.isEmpty(password)) {
            return true;
        }
        return !password.matches("^\\d{6}$");
    }
}
