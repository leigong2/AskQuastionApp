package com.example.android.askquastionapp;

import android.os.Handler;
import android.support.multidex.MultiDexApplication;

public class BaseApplication extends MultiDexApplication {
    private static BaseApplication application;
    private static Handler sHandler;

    public Handler getHandler() {
        return sHandler;
    }

    public static BaseApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        sHandler = new Handler();
    }
}
