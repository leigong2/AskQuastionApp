package com.example.android.askquastionapp.besar.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * create by zj on 2019/4/28
 * 水波纹
 */
public class WaveView extends View implements View.OnClickListener {


    private  ValueAnimator valueAnimator;
    private int waveLenth;
    private int screenWidth;
    private int screenHeight;

    private Paint mPaint;
    private Paint imgPaint;
    private float startX;
    private float startY;
    private int waveCount;

    private int offset;

    private Path mPath=new Path();
    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        imgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        imgPaint.setStrokeWidth(8);
        imgPaint.setColor(Color.RED);
        imgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setOnClickListener(this);
        //屏幕宽高
        screenWidth=w;
        screenHeight=h;

        startX=0;
        startY=h/2;

        //水波纹宽度
        waveLenth=200;
        //
        waveCount = (int) Math.round(screenWidth / waveLenth + 1.5);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPath.reset();
        mPath.moveTo(-waveLenth+offset,startY);
        //绘制贝塞尔曲线
        for (int i=0;i<waveCount;i++){
            int totalOffset=waveLenth*i+offset;
            mPath.quadTo(totalOffset-waveLenth/4*3,startY-100,totalOffset-waveLenth/2,startY);
            mPath.quadTo(totalOffset-waveLenth/4,startY+100,totalOffset,startY);
        }

        mPath.lineTo(screenWidth,screenHeight);
        mPath.lineTo(0,screenHeight);
        mPath.close();
        canvas.drawPath(mPath,mPaint);
    }
    public void startAnimation() {
        valueAnimator= ValueAnimator.ofInt(0, waveLenth);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //水平方向的偏移量
                offset = ( int ) animation.getAnimatedValue();
                Log.e("xxx",offset+"");
                invalidate();
            }

        });
        valueAnimator.start();
    }

    //停止动画
    public  void  onStop(){
       if (valueAnimator!=null){
           valueAnimator.end();
       }
    }


    @Override
    public void onClick(View v) {
        if (valueAnimator!=null){
            return;
        }
        startAnimation();
    }
}
