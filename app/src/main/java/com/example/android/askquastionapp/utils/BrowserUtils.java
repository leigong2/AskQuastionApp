package com.example.android.askquastionapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class BrowserUtils {

    /**
     * 调用第三方浏览器打开
     *
     * @param url 要浏览的资源地址
     */
    public static void goToBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            intent.setClassName("com.tencent.mtt", "com.tencent.mtt.MainActivity");//打开QQ浏览器
            context.startActivity(intent);
        } catch (Exception e1) {
            try {
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                context.startActivity(intent);
            } catch (Exception e2) {
                try {
                    intent.setClassName("mark.via", "mark.via.ui.activity.BrowserActivity");
                    context.startActivity(intent);
                } catch (Exception e3) {
                    // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
                    // 官方解释 : Name of the component implementing an activity that can display the intent
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
                    } else {
                        Toast.makeText(context.getApplicationContext(), "请下载浏览器", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}
