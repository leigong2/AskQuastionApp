package com.example.android.askquastionapp.video;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.example.android.askquastionapp.R;

public class SurfaceControllerView extends FrameLayout {

    private ImageView mPreBtn;
    private ImageView mPlayBtn;
    private ImageView mNextBtn;
    private TextView mTvProgress;
    private TextView mTvTotal;
    private SeekBar mSeekBar;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private View mVideoBack;
    private TextView mVideoTitle;
    private View mBottomLay;
    private View mTopLay;

    private int mBigLength;
    private int mSmallLength;

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

    @SuppressLint("ClickableViewAccessibility")
    private void inflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.surface_controller_view, this, true);
        mPreBtn = findViewById(R.id.pre_btn);
        mPlayBtn = findViewById(R.id.play_btn);
        mNextBtn = findViewById(R.id.next_btn);
        mTvProgress = findViewById(R.id.tv_progress_time);
        mTvTotal = findViewById(R.id.tv_total_time);
        mSeekBar = findViewById(R.id.seek_bar);
        ((ViewGroup) mSeekBar.getParent()).setOnTouchListener((view, event) -> expansionSeekBar(event));
        mVideoBack = findViewById(R.id.video_back);
        mVideoBack.setOnClickListener(v -> ((Activity) getContext()).finish());
        mVideoTitle = findViewById(R.id.video_title);
        mTopLay = findViewById(R.id.top_lay);
        findViewById(R.id.change_orientation).setOnClickListener(this::changeOrientation);
        ((MarginLayoutParams) mTopLay.getLayoutParams()).topMargin = BarUtils.getStatusBarHeight();
        mBottomLay = findViewById(R.id.bottom_lay);
        findViewById(R.id.bg_view).setOnClickListener(v -> changeController());
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
                dismissControllerDelay();
                SurfaceVideoPlayer.getInstance().seek(seekBar.getProgress() * 1f / SurfaceVideoPlayer.getInstance().getDuration());
                mHandler.removeCallbacksAndMessages(null);
                startPlay();
                refreshPlayIcon();
            }
        });
        dismissControllerDelay();
    }

    public int mOrientation = LinearLayout.VERTICAL;

    private void changeOrientation(View v) {
        if (mBigLength == 0) {
            mBigLength = Math.max(getWidth(), getHeight());
        }
        if (mSmallLength == 0) {
            mSmallLength = Math.min(getWidth(), getHeight());
        }
        mOrientation = mOrientation == LinearLayout.VERTICAL ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
        if (v instanceof ImageView) {
            ((ImageView) v).setImageResource(mOrientation == LinearLayout.VERTICAL ? R.mipmap.ic_video_shrink : R.mipmap.ic_video_enlarge);
        }
        if (onOrientationChangeListener != null) {
            switch (mOrientation) {
                case LinearLayout.HORIZONTAL:
                    mTopLay.setTranslationY(mBigLength - mSmallLength);
                    getLayoutParams().width = mBigLength;
                    setRotation(90);
                    break;
                case LinearLayout.VERTICAL:
                    mTopLay.setTranslationY(0);
                    getLayoutParams().width = mSmallLength;
                    setRotation(0);
                default:
                    break;
            }
            onOrientationChangeListener.onOrientationChange(mOrientation);
        }
    }

    private boolean expansionSeekBar(MotionEvent event) {
        Rect seekRect = new Rect();
        mSeekBar.getHitRect(seekRect);
        if ((event.getY() >= (seekRect.top - 500)) && (event.getY() <= (seekRect.bottom + 500))) {
            float y = seekRect.top + seekRect.height() / 2f;
            float x = event.getX() - seekRect.left;
            if (x < 0) {
                x = 0;
            } else if (x > seekRect.width()) {
                x = seekRect.width();
            }
            MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                    event.getAction(), x, y, event.getMetaState());
            return mSeekBar.onTouchEvent(me);
        }
        return false;
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

    public void setTitle(@NonNull String title) {
        mVideoTitle.setText(title);
    }

    public Handler mChangeHandler = new Handler(Looper.getMainLooper());


    private void dismissControllerDelay() {
        mChangeHandler.removeCallbacksAndMessages(null);
        mChangeHandler.postDelayed(this::changeController, 5000);
    }

    boolean isControllerHide;


    void changeController() {
        if (!(getContext() instanceof Activity)) {
            return;
        }
        if (getContext() == null || ((Activity) getContext()).isDestroyed() || ((Activity) getContext()).isFinishing()) {
            return;
        }
        mChangeHandler.removeCallbacksAndMessages(null);
        int topHeight = -mTopLay.getMeasuredHeight() - BarUtils.getStatusBarHeight();
        if (mOrientation == LinearLayout.HORIZONTAL) {
            topHeight -= (mBigLength - mSmallLength);
        }
        if (!isControllerHide) {
            ObjectAnimator.ofFloat(mBottomLay, "translationY", 0, mBottomLay.getMeasuredHeight())
                    .setDuration(300)
                    .start();
            ObjectAnimator.ofFloat(mTopLay, "translationY", mOrientation == LinearLayout.HORIZONTAL ? mBigLength - mSmallLength : 0, topHeight)
                    .setDuration(300)
                    .start();
        } else {
            dismissControllerDelay();
            ObjectAnimator.ofFloat(mBottomLay, "translationY", mBottomLay.getMeasuredHeight(), 0)
                    .setDuration(300)
                    .start();
            ObjectAnimator.ofFloat(mTopLay, "translationY", topHeight, mOrientation == LinearLayout.HORIZONTAL ? mBigLength - mSmallLength : 0)
                    .setDuration(300)
                    .start();
        }
        isControllerHide = !isControllerHide;
    }

    public interface OnOrientationChangeListener {
        void onOrientationChange(int orientation);
    }

    private OnOrientationChangeListener onOrientationChangeListener;

    public void setOnOrientationChangeListener(OnOrientationChangeListener onOrientationChangeListener) {
        this.onOrientationChangeListener = onOrientationChangeListener;
    }
}
