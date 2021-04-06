package com.example.android.askquastionapp.utils;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.widget.TextView;

/**
 * Created by 王志龙 on 2018/1/19 019.
 */

public class TextDrawableUtil {
    public static void addDrawableStart(TextView textView, int resId) {
        Drawable img = textView.getContext().getResources().getDrawable(resId);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        textView.setCompoundDrawablesRelative(img, null, null, null);
    }

    public static void addDrawableEnd(TextView textView, int resId) {
        Drawable img = textView.getContext().getResources().getDrawable(resId);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        textView.setCompoundDrawablesRelative(null, null, img, null);
    }

    public static void addDrawableBottom(TextView textView, int resId) {
        Drawable img = textView.getContext().getResources().getDrawable(resId);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        textView.setCompoundDrawablesRelative(null, null, null, img);
    }

    public static void addDrawableTop(TextView textView, int resId) {
        Drawable img = textView.getContext().getResources().getDrawable(resId);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        textView.setCompoundDrawablesRelative(null, img, null, null);
    }

    public static void clearDrawable(TextView textView) {
        textView.setCompoundDrawablesRelative(null, null, null, null);
    }

    public static void drawDownLine(TextView textView) {
        TextPaint paint = textView.getPaint();
        paint.setAntiAlias(true);
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
    }

    public static abstract class UrlThread extends Thread {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            super.run();
            run(url);
        }

        public abstract void run(String url);
    }

    public static abstract class MyRunnable implements Runnable {
        private Drawable drawable;

        public MyRunnable(Drawable drawable) {
            this.drawable = drawable;
        }

        public abstract void run(Drawable drawable);

        @Override
        public void run() {
            run(drawable);
        }
    }
}
