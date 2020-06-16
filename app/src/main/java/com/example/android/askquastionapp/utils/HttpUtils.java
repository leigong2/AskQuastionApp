package com.example.android.askquastionapp.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class HttpUtils {
    public static String getDatas(String postUrl, String number) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(postUrl + "?tel=" + number);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置请求方式
            connection.setRequestMethod("POST");
            // 设置是否向HttpURLConnection输出
            connection.setDoOutput(true);
            // 设置是否从httpUrlConnection读入
            connection.setDoInput(true);
            // 设置是否使用缓存
            connection.setUseCaches(false);
            //设置参数类型是json格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //定义 BufferedReader输入流来读取URL的响应
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "gbk"));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                result.replace(0, "__GetZoneResult_ = ".length(),"");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    public static String reCheckUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            String charAt = String.valueOf(url.charAt(i));
            if (s.contains(charAt)) {
                sb.append(charAt);
                continue;
            }
            sb.append("_");
        }
        return sb.toString();
    }
}
