package cn.rongcloud.im.utils;

import android.text.TextUtils;
import java.util.regex.Pattern;

public class PatternUtils {

    /** 在使用正则表达式时，利用好其预编译功能，可以有效加快正则匹配速度。 说明：不要在方法体内定义：Pattern pattern = Pattern.compile(规则); */
    private static final Pattern LETTER_PATTERN = Pattern.compile("^[A-Za-z]");

    private static final Pattern UPPER_LETTER_PATTERN = Pattern.compile("[A-Z]");

    public static boolean matchLetter(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return LETTER_PATTERN.matcher(str).matches();
    }

    public static boolean matchUpperLetter(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return UPPER_LETTER_PATTERN.matcher(str).matches();
    }
}
