package com.example.android.askquastionapp.video;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.android.askquastionapp.R;

public class SurfaceControllerView extends FrameLayout {

    private ImageView mPreBtn;
    private ImageView mPlayBtn;
    private ImageView mNextBtn;
    private TextView mTvProgress;
    private TextView mTvTotal;
    private SeekBar mSeekBar;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public SurfaceControllerView(Context context) {
        super(context);
        inflate();
    }

    public SurfaceControllerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate();
    }

    public SurfaceControllerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate();
    }

    private void inflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.surface_controller_view, this, true);
        mPreBtn = findViewById(R.id.pre_btn);
        mPlayBtn = findViewById(R.id.play_btn);
        mNextBtn = findViewById(R.id.next_btn);
        mTvProgress = findViewById(R.id.tv_progress_time);
        mTvTotal = findViewById(R.id.tv_total_time);
        mSeekBar = findViewById(R.id.seek_bar);
        mPreBtn.setOnClickListener(v -> SurfaceVideoPlayer.getInstance().playPre());
        mNextBtn.setOnClickListener(v -> SurfaceVideoPlayer.getInstance().playNext());
        mPlayBtn.setOnClickListener(v -> {
            if (SurfaceVideoPlayer.getInstance().isPlaying()) {
                SurfaceVideoPlayer.getInstance().parse();
            } else {
                SurfaceVideoPlayer.getInstance().start();
            }
            refreshPlayIcon();
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SurfaceVideoPlayer.getInstance().seek(seekBar.getProgress() * 1f / SurfaceVideoPlayer.getInstance().getDuration());
                mHandler.removeCallbacksAndMessages(null);
                startPlay();
                refreshPlayIcon();
            }
        });
    }

    private boolean isPlayingIcon;

    private void refreshPlayIcon() {
        if (!SurfaceVideoPlayer.getInstance().isPlaying()) {
            if (!isPlayingIcon) {
                isPlayingIcon = true;
                mPlayBtn.setImageResource(android.R.drawable.ic_media_play);
            }
        } else {
            if (isPlayingIcon) {
                isPlayingIcon = false;
                mPlayBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    public void startPlay() {
        if (mHandler == null) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SurfaceVideoPlayer.getInstance().isPlaying()) {
                    startPlay();
                    mTvProgress.setText(getTotalUsTime(SurfaceVideoPlayer.getInstance().getCurrentPosition(), false));
                    mSeekBar.setProgress(SurfaceVideoPlayer.getInstance().getCurrentPosition());
                } else {
                    mTvProgress.setText("00:00");
                    mSeekBar.setProgress(0);
                    refreshPlayIcon();
                }
            }
        }, 1000);
        mTvTotal.setText(getTotalUsTime(SurfaceVideoPlayer.getInstance().getDuration(), true));
        mSeekBar.setMax(SurfaceVideoPlayer.getInstance().getDuration());
    }

    public static String getTotalUsTime(long mill, boolean roundDown) {
        if (mill <= 0) {
            return "00:00";
        }
        long second = mill / 1000 + (roundDown ? 0 : 1);
        long minute = second / 60;
        long hour = minute / 60;
        if (hour == 0) {
            if (minute < 10 && second % 60 < 10) {
                return String.format("0%s:0%s", minute, second % 60);
            } else if (minute < 10) {
                return String.format("0%s:%s", minute, second % 60);
            } else if (second % 60 < 10) {
                return String.format("%s:0%s", minute, second % 60);
            } else {
                return String.format("%s:%s", minute, second % 60);
            }
        } else {
            if (minute % 60 < 10 && second % 60 < 10) {
                return String.format("%s:0%s:0%s", hour, minute % 60, second % 60);
            } else if (minute % 60 < 10) {
                return String.format("%s:0%s:%s", hour, minute % 60, second % 60);
            } else if (second % 60 < 10) {
                return String.format("%s:%s:0%s", hour, minute % 60, second % 60);
            } else {
                return String.format("%s:%s:%s", hour, minute % 60, second % 60);
            }
        }
    }

    public void release() {
        if (mSeekBar != null) {
            mSeekBar.setProgress(0);
            mSeekBar.setOnSeekBarChangeListener(null);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
