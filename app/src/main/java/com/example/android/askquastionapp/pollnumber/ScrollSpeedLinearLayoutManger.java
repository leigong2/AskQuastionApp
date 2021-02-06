package com.example.android.askquastionapp.pollnumber;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.BaseApplication;

/**
 * 控制滑动速度的LinearLayoutManager
 */
public class ScrollSpeedLinearLayoutManger extends LinearLayoutManager {
    private float MILLISECONDS_PER_INCH = 0.03f;

    public ScrollSpeedLinearLayoutManger(Context context) {
        super(context);
        setSpeed(2.35f);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ScrollSpeedLinearLayoutManger.this.computeScrollVectorForPosition(targetPosition);
            }

            //This returns the milliseconds it takes to
            //scroll one pixel.
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                //返回滑动一个pixel需要多少毫秒
                return MILLISECONDS_PER_INCH / displayMetrics.density;
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                /*zune：滚动到中间**/
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public void setSpeed(float speed) {
        //0.35 ms 移动一个像素，ui认为这个效果最好
        MILLISECONDS_PER_INCH = BaseApplication.getInstance().getResources().getDisplayMetrics().density * speed;
    }
}