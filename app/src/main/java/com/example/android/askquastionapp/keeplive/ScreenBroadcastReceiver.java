package com.example.android.askquastionapp.keeplive;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            // 开屏
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            //判断是否为锁屏状态
            if (km != null && km.inKeyguardRestrictedInputMode()) {
                ScreenOnTestActivity.start(context);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // 锁屏
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            // 解锁
        }
    }
}
