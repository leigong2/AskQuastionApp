package com.example.android.askquastionapp.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceVideoView extends SurfaceView implements SurfaceHolder.Callback {

    public SurfaceVideoView(Context context) {
        super(context);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SurfaceVideoPlayer.getInstance().play();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getHolder().removeCallback(this);
    }

    public void addCallBack() {
        getHolder().addCallback(this);
    }
}
