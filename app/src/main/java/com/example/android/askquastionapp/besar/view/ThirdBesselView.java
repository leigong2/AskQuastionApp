package com.example.android.askquastionapp.besar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * create by zj on 2019/4/28
 */
public class ThirdBesselView extends View {

    //绘制函数曲线
    private Paint besselPaint;

    private Paint pointPaint;

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
    private float control1PointX=100;
    private float control1PointY=100;


    private float control2PointX=300;
    private float control2PointY=100;
    private int mode;

    private Path mPath=new Path();
    public ThirdBesselView(Context context) {
        super(context);
    }

    public ThirdBesselView(Context context,  @Nullable AttributeSet attrs) {
        super(context, attrs);
        besselPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        besselPaint.setStyle(Paint.Style.STROKE);
        besselPaint.setStrokeWidth(5);
        besselPaint.setColor(Color.RED);

        pointPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(10);
        pointPaint.setColor(Color.RED);

        controlPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        controlPaint.setStyle(Paint.Style.STROKE);
        controlPaint.setStrokeWidth(2);
        controlPaint.setColor(Color.GRAY);

        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(1);
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLUE);

    }

    public ThirdBesselView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMode(int mode){
        this.mode=mode;
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

        //绘制点
        canvas.drawPoint(control1PointX,control1PointY,pointPaint);
        canvas.drawPoint(control2PointX,control2PointY,pointPaint);
        canvas.drawPoint(startPointX,startPointY,pointPaint);
        canvas.drawPoint(endPointX,endPointY,pointPaint);

        canvas.drawText("起始点",startPointX,startPointY,textPaint);
        canvas.drawText("结束点",endPointX,endPointY,textPaint);
        canvas.drawText("控制点1",control1PointX,control1PointY,textPaint);
        canvas.drawText("控制点2",control2PointX,control2PointY,textPaint);

        mPath.reset();
        mPath.moveTo(startPointX,startPointY);
        //曲线
        mPath.cubicTo(control1PointX,control1PointY,control2PointX,control2PointY,endPointX,endPointY);
        canvas.drawPath(mPath,besselPaint);

        canvas.drawLine(startPointX,startPointY,control1PointX,control1PointY,controlPaint);
        canvas.drawLine(control1PointX,control1PointY,control2PointX,control2PointY,controlPaint);
        canvas.drawLine(control2PointX,control2PointY,endPointX,endPointY,controlPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if (mode==0){
                    control1PointX=event.getX();
                    control1PointY=event.getY();
                }else{
                    control2PointX=event.getX();
                    control2PointY=event.getY();
                }
                invalidate();
                break;
        }

        return true;
    }

}
