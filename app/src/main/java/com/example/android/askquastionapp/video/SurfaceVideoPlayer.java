package com.example.android.askquastionapp.video;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.FloatRange;

import com.blankj.utilcode.util.ScreenUtils;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class SurfaceVideoPlayer {
    private static SurfaceVideoPlayer sSurfaceVideoPlayController;
    private WatchVideoActivity.MediaData mediaData;

    private SurfaceVideoPlayer() {
    }

    private void startPlay(MediaPlayer mediaPlayer) {
        int videoHeight = mediaPlayer.getVideoHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int surfaceWidth = mSurfaceVideoView.getWidth() == 0 ? ScreenUtils.getScreenWidth() : mSurfaceVideoView.getWidth();
        mSurfaceVideoView.getLayoutParams().width = surfaceWidth;
        mSurfaceVideoView.getLayoutParams().height = (int) (videoHeight * 1f / videoWidth * surfaceWidth);
        mSurfaceVideoView.setVisibility(View.VISIBLE);
        mediaPlayer.setDisplay(mSurfaceVideoView.getHolder());
        mediaPlayer.start();
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
        if (mSurfaceVideoView != null) {
            mSurfaceVideoView.setVisibility(View.GONE);
        }
        mSurfaceVideoView = surfaceVideoView;
        mSurfaceVideoView.addCallBack();
    }

    public void bindSurfaceController(SurfaceControllerView videoController) {
        if (mSurfaceVideoController != null) {
            mSurfaceVideoController.release();
        }
        mSurfaceVideoController = videoController;
    }

    public void bindMedia(WatchVideoActivity.MediaData mediaData) {
        this.mediaData = mediaData;
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
        mSurfaceVideoController.release();
    }

    public void seek(@FloatRange(from = 0f, to = 1f) float position) {
        int duration = mCurMediaPlayer.getDuration();
        Log.i("zune: ", "position: " + position + ", duration = " + duration);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mCurMediaPlayer.seekTo((long) (position * duration), SEEK_CLOSEST);
        } else {
            mCurMediaPlayer.seekTo((int) (position * duration));
        }
    }

    public void playNext() {
    }

    public void playPre() {
    }

    public boolean isPlaying() {
        return mCurMediaPlayer != null && mCurMediaPlayer.isPlaying();
    }

    public void play() {
        if (mCurMediaPlayer != null) {
            mCurMediaPlayer.reset();
        }
        mCurMediaPlayer = new MediaPlayer();
        mCurMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
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
        try {
            mCurMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurMediaPlayer.setDataSource(mediaData.url);
            mCurMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentPosition() {
        return mCurMediaPlayer == null ? 0 : mCurMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mCurMediaPlayer == null ? 100 : mCurMediaPlayer.getDuration();
    }
}
