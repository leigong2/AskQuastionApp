package com.example.android.askquastionapp.clean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveView extends View {

    private Paint mPaint;
    private float mCenterX;
    private float mCenterY;
    private float mFirstRadius;
    String baseColor = "FF0000";
    private int circleTime = 6; //一周时间
    private int circleCount = 6;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#" + baseColor));
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterX = (right - left) / 2f;
        mCenterY = (bottom - top) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(mFirstRadius, canvas);
    }

    private void drawCircle(float radius, Canvas canvas) {
        for (int i = 0; i < circleCount; i++) {
            float curRadius = radius + (i + 1) * (getWidth() / 2f / circleCount);
            if (curRadius >= getWidth() / 2f) {
                curRadius -= getWidth() / 2f;
            }
            String alphaColor = getAlphaColor(1 - curRadius / (getWidth() / 2f));
            mPaint.setColor(Color.parseColor("#" + alphaColor + baseColor));
            canvas.drawCircle(mCenterX, mCenterY, curRadius, mPaint);
        }
        float circles = circleTime * 1000 / 20f;
        if (this.mFirstRadius >= getWidth() / 2) {
            this.mFirstRadius = 0;
        } else {
            float offset = getWidth() / circles;
            this.mFirstRadius += offset;
        }
        postInvalidateDelayed(20);
    }

    private String getAlphaColor(float position) {
        /**zune: 00 - 256**/
        if (position == 1) {
            return "FF";
        }
        int cur = (int) (100 * position);
        String s = Integer.toHexString(cur);
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }
}
