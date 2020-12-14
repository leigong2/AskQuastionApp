package com.example.android.askquastionapp.keeplive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.example.android.askquastionapp.R;

public class ScreenOnTestActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, ScreenOnTestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
        ActivityOptionsCompat.makeCustomAnimation(context, 0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        //四个标志位分别是锁屏状态下显示，解锁，保持屏幕长亮，打开屏幕
        //这样当Activity启动的时候，它会解锁并亮屏显示。
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_screen_on);
        setFinishOnTouchOutside(false);
        findViewById(R.id.root_view).setOnClickListener(view -> finish());
        findViewById(R.id.text).setOnClickListener(view -> finish());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //唤醒屏幕的代码, 再次亮起屏幕时，如果该Activity并未退出，会走onNewIntent（实际这个不加我的手机也能唤起）
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
