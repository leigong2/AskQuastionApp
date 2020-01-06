package com.example.android.askquastionapp.besar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * create by zj on 2019/4/28
 * 二阶贝塞尔
 * 一个控制点
 */
public class BesselSecondView extends View {

    //绘制函数曲线
    private Paint besselPaint;

    //绘制控制点到起始点，终止点的线
    private Paint controlPaint;

    //文字
    private Paint textPaint;


    //起始点
    private float startPointX;
    private float startPointY;

    //结束点
    private float endPointX;
    private float endPointY;

    //控制点
    private float controlPointX=100;
    private float controlPointY=100;

    private   Path mPath=new Path();

    public BesselSecondView(Context context) {
        super(context);
    }

    public BesselSecondView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        besselPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        besselPaint.setStyle(Paint.Style.STROKE);
        besselPaint.setStrokeWidth(5);
        besselPaint.setColor(Color.RED);

        controlPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        controlPaint.setStyle(Paint.Style.STROKE);
        controlPaint.setStrokeWidth(2);
        controlPaint.setColor(Color.GRAY);

        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(1);
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLUE);

    }

    public BesselSecondView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startPointX=w/4;
        startPointY=h/2;

        endPointX=w/4*3;
        endPointY=h/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);





        //绘制控制点
        canvas.drawPoint(controlPointX,controlPointY,controlPaint);

        canvas.drawText("起始点",startPointX,startPointY,textPaint);
        canvas.drawText("结束点",endPointX,endPointY,textPaint);
        canvas.drawText("控制点",controlPointX,controlPointY,textPaint);


        mPath.reset();
        //起始点
        mPath.moveTo(startPointX,startPointY);
        //曲线
        mPath.quadTo(controlPointX,controlPointY,endPointX,endPointY);
        canvas.drawPath(mPath,besselPaint);

        canvas.drawLine(startPointX,startPointY,controlPointX,controlPointY,controlPaint);
        canvas.drawLine(controlPointX,controlPointY,endPointX,endPointY,controlPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                controlPointX=event.getX();
                controlPointY=event.getY();
                invalidate();
                break;
        }

        return true;
    }
}
