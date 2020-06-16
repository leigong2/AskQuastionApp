package com.example.android.askquastionapp.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class VerticalCenterSpan extends ImageSpan {
    public VerticalCenterSpan(Drawable drawable) {
        super(drawable);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();
        if (fm != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            //获得文字、图片高度
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;
            //对于这段算法LZ表示也不解，正常逻辑应该同draw中的计算一样但是显示的结果不居中，经过几次调试之后才发现这么算才会居中
            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;

            fm.ascent = -bottom;
            fm.top = -bottom;
            fm.bottom = top;
            fm.descent = top;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();
        int transY = 0;
        //获得将要显示的文本高度-图片高度除2等居中位置+top(换行情况)
        transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
        //偏移画布后开始绘制
        canvas.translate(x, transY);
        //Canvas: trying to use a recycled bitmap android.graphics.Bitmap  throw java.lang.RuntimeException
        try {
            b.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        canvas.restore();
    }
}
