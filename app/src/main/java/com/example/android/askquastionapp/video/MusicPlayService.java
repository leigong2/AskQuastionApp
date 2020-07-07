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
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.MemoryCache;

import java.io.File;
import java.io.FileInputStream;
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

        if (mCurUrl != null && mCurUrl.startsWith("http")) {
            IntentFilter downloadFilter = new IntentFilter();
            downloadFilter.addAction("downloadClick");
            Intent downloadIntent = new Intent("downloadClick");
            PendingIntent downloadClick = PendingIntent.getBroadcast(this, 2, downloadIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.on_download, downloadClick);
            registerReceiver(downloadClickReceiver, downloadFilter);
            remoteViews.setViewVisibility(R.id.on_download, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.on_download, View.GONE);
        }
        remoteViews.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_pause_normal);
    }

    private BroadcastReceiver playClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteView = notification.contentView;
            if (TextUtils.equals(intent.getAction(), "playClick") && remoteView != null) {
                if (mPlayer.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }
            startForeground(1001, notification);
        }
    };

    private int mCurProgress;
    private boolean isPaused;
    private boolean isTimingCount;
    private Handler timer = new Handler();
    private Runnable timeTask = new Runnable() {
        @Override
        public void run() {
            if (isPaused) {
                isTimingCount = false;
                return;
            }
            mCurProgress++;
            if (mCurProgress >= mPlayer.getDuration() / 1000) {
                onNextClick();
                isTimingCount = false;
                return;
            }
            isTimingCount = true;
            timer.postDelayed(timeTask, 1000);
            Log.i("zune ", "run:  mCurProgress = " + mCurProgress);
            RemoteViews remoteView = notification.contentView;
            remoteView.setTextViewText(R.id.tv_name, String.format("%s(%s/%s)", mCurName, getFormatTime(mCurProgress), getFormatTime(mPlayer.getDuration() / 1000)));
            startForeground(1001, notification);
        }
    };

    private String getFormatTime(int progress) {
        StringBuilder sb = new StringBuilder();
        if (progress < 10) {
            sb.append("00:0").append(progress);
        } else if (progress < 60) {
            sb.append("00:").append(progress);
        } else {
            int minute = (int) (progress / 60f);
            int second = progress % 60;
            if (minute < 10) {
                sb.append("0").append(minute).append(":");
            } else {
                sb.append(minute).append(":");
            }
            if (second < 10) {
                sb.append("0").append(second);
            } else {
                sb.append(second);
            }
        }
        return sb.toString();
    }

    private void startPlay() {
        isPaused = false;
        RemoteViews remoteView = notification.contentView;
        mPlayer.start();
        remoteView.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_pause_normal);
        remoteView.setTextViewText(R.id.tv_name, String.format("%s(%s/%s)", mCurName, getFormatTime(mCurProgress), getFormatTime(mPlayer.getDuration() / 1000)));
        startForeground(1001, notification);
        if (!isTimingCount) {
            isTimingCount = true;
            timer.postDelayed(timeTask, 1000);
            Log.i("zune ", "run:  mCurProgress = " + mCurProgress);
        }
    }

    private void pausePlay() {
        isPaused = true;
        RemoteViews remoteView = notification.contentView;
        remoteView.setImageViewResource(R.id.on_play, R.mipmap.ic_vod_play_normal);
        mPlayer.pause();
    }

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

    private BroadcastReceiver downloadClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteView = notification.contentView;
            if (TextUtils.equals(intent.getAction(), "downloadClick") && remoteView != null) {
                downloadClick();
            }
        }
    };

    private String mCurUrl;
    private String mCurName;

    private void playMusic(ListenMusicActivity.MediaData data) {
        try {
            mPlayer.reset();
            if (mCurUrl != null && !mCurUrl.startsWith("http")) {
                FileInputStream fis = new FileInputStream(new File(data.url));
                mPlayer.setDataSource(fis.getFD());
            } else {
                mPlayer.setDataSource(this, Uri.parse(data.url));
            }
            mCurUrl = data.url;
            mCurName = data.name;
            mPlayer.setOnPreparedListener(mediaPlayer -> {
                startPlay();
            });
            if (mCurUrl != null && !mCurUrl.startsWith("http")) {
                try {
                    mPlayer.prepare();
                    startPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                    retryPlay(data);
                }
            } else {
                mPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retryPlay(ListenMusicActivity.MediaData data) {
        BaseApplication.getInstance().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playMusic(data);
            }
        }, 500);
    }

    private void onNextClick() {
        mCurProgress = 0;
        if (mPlayListener != null) {
            mPlayListener.onNext(this);
        }
    }

    private void onPreClick() {
        mCurProgress = 0;
        if (mPlayListener != null) {
            mPlayListener.onPre(this);
        }
    }

    private void downloadClick() {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Music");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        DownloadObjManager.getInstance().startDownWithPosition(mCurUrl
                , Environment.getExternalStorageDirectory() + File.separator + "Music" + File.separator + mCurName + ".mp3");
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
