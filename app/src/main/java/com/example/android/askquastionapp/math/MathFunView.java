package com.example.android.askquastionapp.math;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Path.FillType.EVEN_ODD;

public class MathFunView extends View {
    private float mOx;
    private float mOy;
    private float mMaxX;
    private float mMaxY;

    private List<Float> mCurX = new ArrayList<>();
    private List<Float> mCurY = new ArrayList<>();
    private Paint mPaint;

    public MathFunView(Context context) {
        super(context);
        init();
    }

    public MathFunView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MathFunView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setO(float x, float y) {
        this.mOx = x;
        this.mOy = y;
    }

    public void setMaxX(float maxX) {
        this.mMaxX = maxX;
        resetMaxY();
    }

    public void resetMaxY() {
        for (int i = 0; i <= mMaxX; i++) {
            if (mMaxY < setFunX(position, i)) {
                mMaxY = setFunX(position, i);
            }
        }
    }

    private int position;

    public void setTest(int position) {
        this.position = position;
    }

    public float setFunX(int position, float x) {
        switch (position) {
            case 0:
                return x * x;
            case 1:
                return x <= 0 ? 0 : (float) Math.pow(x, 0.5);
            case 2:
                return (float) Math.pow(x, 3) - (float) Math.pow(x, 2) + x;
            case 3:
                return (float) (Math.pow(x, 2) - mMaxX / 2 * x + mMaxX);
            case 4:
            default:
                if (mMaxX - 1 / Math.pow(x, 2) > 0) {
                    return (float) Math.pow(mMaxX - 1 / Math.pow(x, 2), 0.5);
                } else {
                    return (float) Math.pow(mMaxX / Math.pow(x, 2) - 1, 0.5);
                }
        }
    }

    /**
     * zune: 获取适应的x坐标
     **/
    public float translateWrapX(float x) {
        return getPaddingLeft() + mOx + x;
    }

    /**
     * zune: 获取适应的y坐标
     **/
    public float translateWrapY(float y) {
        return getPaddingTop() + mOy - y / (mMaxY / mMaxX);
    }

    /**
     * zune: 获取真实的x坐标
     **/
    public float translateX(float x) {
        return getPaddingLeft() + mOx + x;
    }

    /**
     * zune: 获取真实的x坐标
     **/
    public float translateY(float y) {
        return getPaddingTop() + mOy - y;
    }

    public void drawFun() {
        mCurX.clear();
        mCurY.clear();
        int lenth = (int) (mMaxX * 2);
        for (int i = 0; i <= lenth; i++) {
            float x = i - mMaxX;
            mCurX.add(x);
            mCurY.add(setFunX(position, x));
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(translateX(Math.max(mOx, mOy)), mOy, translateX(-Math.max(mOx, mOy)), mOy, mPaint);
        canvas.drawLine(mOx + getPaddingLeft(), translateY(Math.max(mOx, mOy)), mOx + getPaddingLeft(), translateY(-Math.max(mOx, mOy)), mPaint);
        @SuppressLint("DrawAllocation") Path path = new Path();
        path.setFillType(EVEN_ODD);
        float maxX = 0;
        for (int i = 0; i < mCurX.size(); i++) {
            float x = translateX(mCurX.get(i));
            float y = translateWrapY(mCurY.get(i));
            if (maxX < x) {
                maxX = x;
            }
            if (i == 0) {
                path.moveTo(x, y);
            } else if (i < mCurX.size()) {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, mPaint);
        canvas.drawText(String.valueOf(mMaxX), maxX, translateWrapY(0), mPaint);
        float y = maxX - 2 * (translateWrapY(mOy) - getPaddingTop()) + SizeUtils.dp2px(20);
        canvas.drawText(String.valueOf(mMaxY), translateX(0), y, mPaint);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(SizeUtils.dp2px(1));
        mPaint.setTextSize(SizeUtils.sp2px(12));
    }
}
