package cn.rongcloud.im.common;

public class NetConstant {
    public static final int API_CONNECT_TIME_OUT = 10;
    public static final int API_IM_CONNECT_TIME_OUT = 10;
    public static final int API_READ_TIME_OUT = 10;
    public static final int API_WRITE_TIME_OUT = 10;
    public static final String API_SP_NAME_NET = "net";
    public static final String API_SP_KEY_NET_COOKIE_SET = "cookie_set";
    public static final String API_SP_KEY_NET_HEADER_AUTH = "header_auth";

    public static final int REQUEST_SUCCESS_CODE = 200;

    /** 服务端登录过期旧错误码 10000 */
    public static final int LOGIN_EXPIRATION_CODE_OLD = 10000;

    /** 服务端登录过期新错误码 1000 */
    public static final int LOGIN_EXPIRATION_CODE_NEW = 1000;
}
