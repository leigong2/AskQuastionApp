package com.example.android.askquastionapp.video.exo;

import android.net.Uri;

import com.example.android.askquastionapp.BaseApplication;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class ExoPlayer {
    private static ExoPlayer instance;
    private SimpleExoPlayer mPlayer;

    public static ExoPlayer getInstance() {
        if (instance == null) {
            instance = new ExoPlayer();
        }
        return instance;
    }

    private ExoPlayer() {
        // 创建带宽
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 创建轨道选择工厂
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        // 创建轨道选择实例
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // 创建播放器实例
        mPlayer = ExoPlayerFactory.newSimpleInstance(BaseApplication.getInstance(), trackSelector);
    }

    public void bindView(PlayerView view) {
        view.setPlayer(mPlayer);
    }

    public void prepare(String path) {
        // 创建加载数据的工厂
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(BaseApplication.getInstance()
                , Util.getUserAgent(BaseApplication.getInstance(), BaseApplication.class.getSimpleName()), null);
        Uri uri = Uri.parse(path);
         // 创建资源
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        // 准备播放
        mPlayer.prepare(mediaSource);
        // 开始播放
        mPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        mPlayer.pause();
    }

    public void release() {
        mPlayer.release();
    }

    public void stop(boolean reset) {
        mPlayer.stop(true);
    }
}
