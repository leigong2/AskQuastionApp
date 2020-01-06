package com.example.android.askquastionapp.video;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.example.android.askquastionapp.MemoryCache;
import com.example.android.askquastionapp.R;

import java.io.IOException;

import static android.app.Notification.PRIORITY_MAX;
import static android.os.Build.VERSION_CODES.O;

public class MusicPlayService extends Service implements IPlayListener {
    private String CHANNEL_ONE_ID = "com.example.android.askquastionapp";
    private MediaPlayer mPlayer;
    private Notification notification;
    private OnPlayListener mPlayListener;

    public static void start(Context context, ListenMusicActivity.MediaData url, OnPlayListener onPlayListener) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MemoryCache.getInstance().put(url);
        MemoryCache.getInstance().put("OnPlayListener", onPlayListener);
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
        mPlayListener = MemoryCache.getInstance().remove("OnPlayListener");
        ListenMusicActivity.MediaData mediaData = MemoryCache.getInstance().clear(ListenMusicActivity.MediaData.class);
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
                    R.layout.layout_play_controller_o);
            builder.setCustomHeadsUpContentView(remoteViews);
        } else {
            remoteViews = new RemoteViews(this.getPackageName(),
                    R.layout.layout_play_controller);
            builder = new Notification.Builder(this.getApplicationContext());
        }
        remoteViews.setTextViewText(R.id.tv_name, mediaData.name);
        notification = builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MAX)
                .build();
        notification.defaults = Notification.DEFAULT_SOUND;//设置为默认的声音
        notificationManager.notify(1001, notification);
        initClick(remoteViews);
        startForeground(1001, notification);// 开始前台服务
        playMusic(mediaData);
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

    private void initClick(RemoteViews remoteViews) {
        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction("playClick");
        Intent playIntent = new Intent("playClick");
        PendingIntent playClick = PendingIntent.getBroadcast(this, 0, playIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.on_play, playClick);
        registerReceiver(playClickReceiver, playFilter);

        IntentFilter preFilter = new IntentFilter();
        preFilter.addAction("preClick");
        Intent preIntent = new Intent("preClick");
        PendingIntent preClick = PendingIntent.getBroadcast(this, 1, preIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.on_pre, preClick);
        registerReceiver(preClickReceiver, preFilter);

        IntentFilter nextFilter = new IntentFilter();
        nextFilter.addAction("nextClick");
        Intent nextIntent = new Intent("nextClick");
        PendingIntent nextClick = PendingIntent.getBroadcast(this, 2, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.on_next, nextClick);
        registerReceiver(nextClickReceiver, nextFilter);

        remoteViews.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_pause_normal);
    }

    private BroadcastReceiver playClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteView = notification.contentView;
            if (TextUtils.equals(intent.getAction(), "playClick") && remoteView != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    remoteView.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_play_normal);
                } else {
                    mPlayer.start();
                    remoteView.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_pause_normal);
                }
            }
            startForeground(1001, notification);
        }
    };

    private BroadcastReceiver preClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteView = notification.contentView;
            if (TextUtils.equals(intent.getAction(), "preClick") && remoteView != null) {
                onPreClick();
            }
        }
    };

    private BroadcastReceiver nextClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteView = notification.contentView;
            if (TextUtils.equals(intent.getAction(), "nextClick") && remoteView != null) {
                onNextClick();
            }
        }
    };

    private void playMusic(ListenMusicActivity.MediaData data) {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(data.url);
            RemoteViews remoteViews = notification.contentView;
            remoteViews.setTextViewText(R.id.tv_name, data.name);
            remoteViews.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_play_normal);
            startForeground(1001, notification);
            mPlayer.setOnPreparedListener(mediaPlayer -> {
                mPlayer.start();
                RemoteViews remoteView = notification.contentView;
                remoteView.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_pause_normal);
                startForeground(1001, notification);
            });
            mPlayer.setOnCompletionListener(mediaPlayer -> {
                if (mPlayer != null) {
                    onNextClick();
                }
            });
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            onNextClick();
        }
    }

    private void onNextClick() {
        if (mPlayListener != null) {
            mPlayListener.onNext(this);
        }
    }

    private void onPreClick() {
        if (mPlayListener != null) {
            mPlayListener.onPre(this);
        }
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

    @Override
    public void onPlayUrl(ListenMusicActivity.MediaData url) {
        if (url == null) {
            if (mPlayer != null) {
                mPlayer.stop();
                stopForeground(true);
            }
            return;
        }
        playMusic(url);
    }

    public interface OnPlayListener {
        void onNext(IPlayListener playListener);

        void onPre(IPlayListener playListener);
    }
}
