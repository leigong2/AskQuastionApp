package com.example.android.askquastionapp.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewUtils {
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public static void getContentFromUrl(Context context, String url) {
        WebView webView = new WebView(context);
        // 开启JavaScript支持
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        // 设置WebView是否支持使用屏幕控件或手势进行缩放，默认是true，支持缩放
        webView.getSettings().setSupportZoom(true);
        // 设置WebView是否使用其内置的变焦机制，该机制集合屏幕缩放控件使用，默认是false，不使用内置变焦机制。
        webView.getSettings().setBuiltInZoomControls(true);
        // 设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webView.getSettings().setDomStorageEnabled(true);
        // 触摸焦点起作用.如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
        webView.requestFocus();
        // 设置此属性,可任意比例缩放,设置webview推荐使用的窗口
        webView.getSettings().setUseWideViewPort(true);
        // 设置webview加载的页面的模式,缩放至屏幕的大小
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("===zune: ", "onPageFinished: ");
                // 获取页面内容
                view.loadUrl("javascript:window.java_obj.showSource("
                        + "document.getElementsByTagName('html')[0].innerHTML);");

                // 获取解析<meta name="share-description" content="获取到的值">
                view.loadUrl("javascript:window.java_obj.showDescription("
                        + "document.querySelector('meta[name=\"share-description\"]').getAttribute('content')"
                        + ");");
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(url);
    }

    public static final class InJavaScriptLocalObj
    {
        @JavascriptInterface
        public void showSource(String html) {
            System.out.println("====>zune: html=" + html);
        }

        @JavascriptInterface
        public void showDescription(String str) {
            System.out.println("====>zune: html=" + str);
        }
    }
}
