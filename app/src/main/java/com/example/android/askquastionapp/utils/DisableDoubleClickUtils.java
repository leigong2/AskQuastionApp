package com.example.android.askquastionapp.utils;

import android.view.View;

public class DisableDoubleClickUtils {
    private static long mClickTime;
    private static long mCurViewId;

    /**zune: 默认2秒取一个点击事件**/
    public static boolean canClick(View view) {
        if (mCurViewId != view.hashCode()) {
            mCurViewId = view.hashCode();
            mClickTime = System.currentTimeMillis();
            return true;
        }
        if (mClickTime == 0 || System.currentTimeMillis() - mClickTime > 2000) {
            mClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**zune: 自定义多久取一个点击事件，单位毫秒**/
    public static boolean canClick(View view, long mill) {
        if (mCurViewId != view.hashCode()) {
            mCurViewId = view.hashCode();
            mClickTime = System.currentTimeMillis();
            return true;
        }
        if (mClickTime == 0 || System.currentTimeMillis() - mClickTime > mill) {
            mClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
