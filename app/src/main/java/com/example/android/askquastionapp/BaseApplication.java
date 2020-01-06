package com.example.android.askquastionapp;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

public class BaseApplication extends MultiDexApplication {
    private static Application application;

    public static Application getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}
