package com.example.android.askquastionapp.video;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.FloatRange;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.media.PictureCheckManager;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class SurfaceVideoPlayer {
    private static SurfaceVideoPlayer sSurfaceVideoPlayController;
    private PictureCheckManager.MediaData mediaData;

    private SurfaceVideoPlayer() {
    }

    public void startPlay(MediaPlayer mediaPlayer) {
        if (mSurfaceVideoView.getSurfaceTexture() == null || !prepared) {
            return;
        }
        int videoHeight = mediaPlayer.getVideoHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int surfaceWidth = mSurfaceVideoView.getWidth() == 0 ? ScreenUtils.getScreenWidth() : mSurfaceVideoView.getWidth();
        mSurfaceVideoView.getLayoutParams().width = surfaceWidth;
        mSurfaceVideoView.getLayoutParams().height = (int) (videoHeight * 1f / videoWidth * surfaceWidth);
        mSurfaceVideoView.setVisibility(View.VISIBLE);
        mediaPlayer.setSurface(new Surface(mSurfaceVideoView.getSurfaceTexture()));
        mediaPlayer.start();
        mSurfaceVideoController.startPlay();
        if (mSurfaceVideoView.getContext() instanceof VideoPlayerActivity && ((VideoPlayerActivity) mSurfaceVideoView.getContext()).mOrientation != mSurfaceVideoController.mOrientation) {
            mSurfaceVideoController.findViewById(R.id.change_orientation).callOnClick();
        }
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

    public void bindMedia(PictureCheckManager.MediaData mediaData) {
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
        if (mCurMediaPlayer != null) {
            mCurMediaPlayer.release();
        }
        mCurMediaPlayer = null;
        if (mSurfaceVideoController != null) {
            mSurfaceVideoController.release();
        }
    }

    public void seek(@FloatRange(from = 0f, to = 1f) float position) {
        int duration = getDuration();
        Log.i("zune: ", "position: " + position + ", duration = " + duration);
        if (prepared) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mCurMediaPlayer.seekTo((long) (position * duration), SEEK_CLOSEST);
                } else {
                    mCurMediaPlayer.seekTo((int) (position * duration));
                }
            } catch (Throwable ignore) {
            }
        }
    }

    public void playNext() {
    }

    public void playPre() {
    }

    public boolean isPlaying() {
        return mCurMediaPlayer != null && mCurMediaPlayer.isPlaying();
    }

    private boolean prepared = false;

    public void play() {
        if (mCurMediaPlayer != null) {
            mCurMediaPlayer.reset();
        }
        prepared = false;
        mCurMediaPlayer = new MediaPlayer();
        mCurMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                prepared = true;
                startPlay(mCurMediaPlayer);
            }
        });
        mCurMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (prepared) {
                    mCurMediaPlayer.seekTo(1);
                }
            }
        });
        mCurMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (prepared) {
                    mCurMediaPlayer.seekTo(0);
                }
                return false;
            }
        });
        try {
            mCurMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurMediaPlayer.setDataSource(mediaData.path);
            mCurMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentPosition() {
        return mCurMediaPlayer == null || !prepared ? 0 : mCurMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mCurMediaPlayer == null || !prepared ? 100 : mCurMediaPlayer.getDuration();
    }

    public MediaPlayer getCurMediaPlayer() {
        return mCurMediaPlayer;
    }
}
