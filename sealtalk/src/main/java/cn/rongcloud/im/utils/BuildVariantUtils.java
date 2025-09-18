package cn.rongcloud.im.utils;

import cn.rongcloud.im.BuildConfig;

/** 构建变体工具类 用于根据不同的构建变体控制功能的启用状态 */
public class BuildVariantUtils {

    /** 是否为开发版本 */
    public static boolean isDevelopBuild() {
        return "develop".equals(BuildConfig.BUILD_VARIANT);
    }

    /** 是否为应用市场版本 */
    public static boolean isPublishStoreBuild() {
        return "publishstore".equals(BuildConfig.BUILD_VARIANT);
    }
}
