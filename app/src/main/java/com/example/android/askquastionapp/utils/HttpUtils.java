package com.example.android.askquastionapp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
}
