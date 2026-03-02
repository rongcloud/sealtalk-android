package cn.rongcloud.im.utils;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/** 自定义密码转换方法，使用星号(*)替代默认的圆点(•) */
public class StarPasswordTransformationMethod extends PasswordTransformationMethod {

    private static StarPasswordTransformationMethod instance;

    public static StarPasswordTransformationMethod getInstance() {
        if (instance == null) {
            instance = new StarPasswordTransformationMethod();
        }
        return instance;
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private static class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source;
        }

        public char charAt(int index) {
            return '*'; // 返回星号而不是默认的圆点
        }

        public int length() {
            return mSource.length();
        }

        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }
    }
}
