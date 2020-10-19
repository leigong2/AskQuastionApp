package com.example.android.askquastionapp.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceVideoView extends SurfaceView implements SurfaceHolder.Callback {

    private boolean create;

    public SurfaceVideoView(Context context) {
        super(context);
        init();
    }

    public SurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SurfaceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    private boolean isCreated;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isCreated = true;
        Log.i("zune: ", "surfaceCreated surfaceChanged");
        if (!TextUtils.isEmpty(mUrl)) {
            SurfaceVideoPlayer.getInstance().play(mUrl);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("zune: ", "SurfaceHolder surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("zune: ", "SurfaceHolder 被销毁");
        isCreated = false;
        SurfaceVideoPlayer.getInstance().release();
    }

    private String mUrl;

    public void setDataSource(String url) {
        this.mUrl = url;
        if (isCreated) {
            SurfaceVideoPlayer.getInstance().play(mUrl);
        }
    }
}
