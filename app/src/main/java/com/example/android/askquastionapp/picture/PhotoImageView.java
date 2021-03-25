package com.example.android.askquastionapp.picture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PhotoImageView extends AbstractPhotoImageView {
    private ValueAnimator dismissAnimator;  //消失动画
    private boolean isSimple = true;

    public PhotoImageView(Context context) {
        super(context);
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void release() {
        super.release();
        if (dismissAnimator != null) {
            dismissAnimator.cancel();
        }
        dismissAnimator = null;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onDismissCallBack != null) {
            onDismissCallBack.onDismiss();
        }
    }

    @Override
    public void onGestureScroll(@GestureScrollType int type) {
        getParent().requestDisallowInterceptTouchEvent(false);
        switch (type) {
            case GESTURE_SCROLL_LEFT:
                if (onDismissCallBack != null) {
                    onDismissCallBack.onLeftLimit();
                }
                break;
            case GESTURE_SCROLL_TOP:
                if (!isSimple) {
                    return;
                }
                if (dismissAnimator == null || !dismissAnimator.isRunning()) {
                    return;
                }
                if (isDismiss) {
                    return;
                }
                isDismiss = true;
                if (dismissAnimator == null) {
                    dismissAnimator = ValueAnimator.ofFloat(1f, 0);
                    dismissAnimator.setDuration(300);
                } else if (dismissAnimator.isRunning()) {
                    return;
                }
                setBackgroundColor(Color.TRANSPARENT);
                dismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                        setAlpha(animatedValue);
                        setScaleX(animatedValue);
                        setScaleY(animatedValue);
                        setTranslationY(getMeasuredHeight() * (1 - animatedValue));
                        if (animatedValue == 0) {
                            dismiss();
                        }
                    }
                });
                dismissAnimator.start();
                break;
            case GESTURE_SCROLL_RIGHT:
                if (onDismissCallBack != null) {
                    onDismissCallBack.onRightLimit();
                }
                break;
            case GESTURE_SCROLL_BOTTOM:
                break;
        }
    }

    private OnLimitCallBack onDismissCallBack;

    public void setOnDismissCallBack(OnLimitCallBack onDismissCallBack) {
        this.onDismissCallBack = onDismissCallBack;
    }

    public void setSimple(boolean simple) {
        isSimple = simple;
    }

    public static final int GESTURE_SCROLL_LEFT = 1;
    public static final int GESTURE_SCROLL_TOP = 2;
    public static final int GESTURE_SCROLL_RIGHT = 3;
    public static final int GESTURE_SCROLL_BOTTOM = 4;

    @IntDef({GESTURE_SCROLL_LEFT, GESTURE_SCROLL_TOP, GESTURE_SCROLL_RIGHT, GESTURE_SCROLL_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureScrollType {

    }
}
