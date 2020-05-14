package com.example.android.askquastionapp.utils;

import android.text.TextUtils;

public class StringUtils {

    public static boolean hasChinese(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        //1个英文一个字节，1个 中文2个字节（GBK）
        return str.getBytes().length != str.length();
    }
}
