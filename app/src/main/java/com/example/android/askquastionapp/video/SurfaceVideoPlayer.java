package com.example.android.askquastionapp.video;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.FloatRange;

import com.blankj.utilcode.util.ScreenUtils;

import java.io.IOException;

public class SurfaceVideoPlayer {
    private static SurfaceVideoPlayer sSurfaceVideoPlayController;

    private SurfaceVideoPlayer() {
        if (mCurMediaPlayer == null) {
            resetPlayer();
        }
    }

    private void resetPlayer() {
        mCurMediaPlayer = new MediaPlayer();
        mCurMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("zune: ", "play onPrepared");
                startPlay(mCurMediaPlayer);
            }
        });
        mCurMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurMediaPlayer.seekTo(1);
            }
        });
        mCurMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mCurMediaPlayer.seekTo(0);
                return false;
            }
        });
    }

    private void startPlay(MediaPlayer mediaPlayer) {
        int videoHeight = mediaPlayer.getVideoHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int surfaceWidth = mSurfaceVideoView.getWidth() == 0 ? ScreenUtils.getScreenWidth() : mSurfaceVideoView.getWidth();
        mSurfaceVideoView.getLayoutParams().width = surfaceWidth;
        mSurfaceVideoView.getLayoutParams().height = (int) (videoHeight * 1f / videoWidth * surfaceWidth);
        mSurfaceVideoController.setDuration(mediaPlayer.getDuration());
        mediaPlayer.setDisplay(mSurfaceVideoView.getHolder());
        mediaPlayer.start();
        Log.i("zune: ", "play start");
        mSurfaceVideoController.setReleased(false);
        mSurfaceVideoController.startPlay();
    }

    public static SurfaceVideoPlayer getInstance() {
        if (sSurfaceVideoPlayController == null) {
            synchronized (SurfaceVideoPlayer.class) {
                if (sSurfaceVideoPlayController == null) {
                    sSurfaceVideoPlayController = new SurfaceVideoPlayer();
                }
            }
        }
        return sSurfaceVideoPlayController;
    }

    private SurfaceVideoView mSurfaceVideoView;
    private SurfaceControllerView mSurfaceVideoController;
    private MediaPlayer mCurMediaPlayer;

    public void bindSurfaceVideo(SurfaceVideoView surfaceVideoView) {
        mSurfaceVideoView = surfaceVideoView;
    }

    public void bindSurfaceController(SurfaceControllerView videoController) {
        mSurfaceVideoController = videoController;
    }

    public void play(String path) {
        Log.i("zune: ", "play path = " + path);
        resetPlayer();
        mCurMediaPlayer.reset();
        try {
            mCurMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurMediaPlayer.setDataSource(path);
            mCurMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("zune: ", "play: e = " + e);
        }
    }

    public void parse() {
        mCurMediaPlayer.pause();
    }

    public void start() {
        mCurMediaPlayer.start();
    }

    public void stop() {
        mCurMediaPlayer.stop();
    }

    public void release() {
        mCurMediaPlayer.release();
        mCurMediaPlayer = null;
        mSurfaceVideoController.setReleased(true);
    }

    public void seek(@FloatRange(from = 0f, to = 1f) float position) {
        int duration = mCurMediaPlayer.getDuration();
        mCurMediaPlayer.seekTo((int) (position * duration));
    }

    public void playNext() {
    }

    public void playPre() {
    }

    public boolean isPlaying() {
        return mCurMediaPlayer != null && mCurMediaPlayer.isPlaying();
    }
}
