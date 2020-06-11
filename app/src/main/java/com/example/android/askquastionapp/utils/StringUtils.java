package com.example.android.askquastionapp.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StringUtils {

    public static boolean hasChinese(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        //1个英文一个字节，1个 中文2个字节（GBK）
        return str.getBytes().length != str.length();
    }

    public static String urlEncodeToString(String str) {
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}
