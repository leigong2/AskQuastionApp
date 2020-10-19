package com.example.android.askquastionapp.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;

public class SurfaceControllerView extends FrameLayout {

    private ImageView mPreBtn;
    private ImageView mPlayBtn;
    private ImageView mNextBtn;
    private TextView mTvProgress;
    private TextView mTvTotal;
    private SeekBar mSeekBar;
    private int mDuration = 100;

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
        mPreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SurfaceVideoPlayer.getInstance().playPre();
            }
        });
        mNextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SurfaceVideoPlayer.getInstance().playNext();
            }
        });
        mPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SurfaceVideoPlayer.getInstance().isPlaying()) {
                    SurfaceVideoPlayer.getInstance().parse();
                } else {
                    SurfaceVideoPlayer.getInstance().start();
                }
                refreshPlayIcon();
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("zune: ", "onProgressChanged: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("zune: ", "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("zune: ", "onStopTrackingTouch: ");
                int progress = seekBar.getProgress();
                SurfaceVideoPlayer.getInstance().seek(progress * 1f / mDuration);
            }
        });
    }

    /**
     * Sets duration.
     *
     * @param duration the duration 毫秒
     */
    public void setDuration(int duration) {
        this.mDuration = duration;
        mTvTotal.setText(getTotalUsTime(mDuration));
    }

    public void setSeekPosition(@FloatRange(from = 0f, to = 1f) float position) {
        if (position == 1) {
            mSeekBar.setProgress(mDuration);
            return;
        }
        int progress = (int) (position * mDuration);
        mSeekBar.setProgress(progress);
    }

    private void refreshPlayIcon() {
        if (SurfaceVideoPlayer.getInstance().isPlaying()) {
            mPlayBtn.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mPlayBtn.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private int mCurPosition;
    private boolean isReleased;

    public void setReleased(boolean released) {
        isReleased = released;
    }

    public void startPlay() {
        BaseApplication.getInstance().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SurfaceVideoPlayer.getInstance().isPlaying() && !isReleased) {
                    mCurPosition++;
                    startPlay();
                    mTvProgress.setText(getTotalUsTime(mCurPosition * 1000L));
                    setSeekPosition(mCurPosition * 1f / mDuration);
                }
            }
        }, 1000);
    }

    public static String getTotalUsTime(long mill) {
        if (mill <= 0) {
            return "00:00";
        }
        long second = mill / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        if (hour == 0) {
            if (minute < 10 && second % 60 < 10) {
                return String.format("0%s:0%s", minute, second % 60);
            } else if (minute < 10 && second % 60 >= 10) {
                return String.format("0%s:%s", minute, second % 60);
            } else if (minute >= 10 && second % 60 < 10) {
                return String.format("%s:0%s", minute, second % 60);
            } else if (minute >= 10 && second % 60 >= 10) {
                return String.format("%s:%s", minute, second % 60);
            }
        } else {
            if (minute % 60 < 10 && second % 60 < 10) {
                return String.format("%s:0%s:0%s", hour, minute % 60, second % 60);
            } else if (minute % 60 < 10 && second % 60 >= 10) {
                return String.format("%s:0%s:%s", hour, minute % 60, second % 60);
            } else if (minute % 60 >= 10 && second % 60 < 10) {
                return String.format("%s:%s:0%s", hour, minute % 60, second % 60);
            } else if (minute % 60 >= 10 && second % 60 >= 10) {
                return String.format("%s:%s:%s", hour, minute % 60, second % 60);
            }
        }
        return "00:00";
    }

    public void release() {
        isReleased = true;
    }
}
