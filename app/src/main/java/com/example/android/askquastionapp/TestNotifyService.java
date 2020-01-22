package com.example.android.askquastionapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import static android.app.Notification.PRIORITY_MAX;
import static android.os.Build.VERSION_CODES.O;

public class TestNotifyService extends Service {
    private String CHANNEL_ONE_ID = "com.example.android.askquastionapp";
    private MediaPlayer mPlayer;
    private Notification notification;

    public static void start(Context context) {
        Intent intent = new Intent(context, TestNotifyService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 获取remoteViews（参数一：包名；参数二：布局资源）
        RemoteViews remoteViews;
        // 设置自定义的Notification内容
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= O) {
            builder = new Notification.Builder(this.getApplicationContext(), CHANNEL_ONE_ID);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ONE_ID, "AskQuastionApp", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ONE_ID);
            remoteViews = new RemoteViews(this.getPackageName(),
                    R.layout.layout_test_notify);
            remoteViews.setTextViewText(R.id.tv_name, "测试标题");
            remoteViews.setTextViewText(R.id.tv_content, "测试内容");
            builder.setCustomHeadsUpContentView(remoteViews);
        } else {
            remoteViews = new RemoteViews(this.getPackageName(),
                    R.layout.layout_test_notify);
            remoteViews.setTextViewText(R.id.tv_name, "测试标题");
            remoteViews.setTextViewText(R.id.tv_content, "测试内容");
            builder = new Notification.Builder(this.getApplicationContext());
        }
        notification = builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MAX)
                .build();
        notification.defaults = Notification.DEFAULT_SOUND;//设置为默认的声音
        notificationManager.notify(1001, notification);
        startForeground(1001, notification);// 开始前台服务
        initFocus();
        return START_NOT_STICKY;
    }

    private void initFocus() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    if (!mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.stop();
                        stopForeground(true);
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.stop();
                        stopForeground(true);
                    }
                }
            }
        };
        am.abandonAudioFocus(afChangeListener);
    }

    @Override
    public void onDestroy() {
        // 停止前台服务--参数：表示是否移除之前的通知
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mPlayer != null) {
            mPlayer.stop();
            stopForeground(true);
        }
        return super.onUnbind(intent);
    }
}
