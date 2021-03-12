package com.example.android.askquastionapp.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ScreenUtils;

public class SurfaceVideoView extends TextureView implements TextureView.SurfaceTextureListener {

    public SurfaceVideoView(Context context) {
        super(context);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addCallBack() {
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        SurfaceVideoPlayer.getInstance().startPlay(SurfaceVideoPlayer.getInstance().getCurMediaPlayer());
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public void onOrientationChange(int orientation) {
        int videoWidth = SurfaceVideoPlayer.getInstance().getCurMediaPlayer().getVideoWidth();
        int videoHeight = SurfaceVideoPlayer.getInstance().getCurMediaPlayer().getVideoHeight();
        if (orientation == LinearLayout.HORIZONTAL) {
            setRotation(90);
            boolean isHorizontal = videoWidth * 1f / videoHeight > ScreenUtils.getScreenHeight() * 1f / ScreenUtils.getScreenWidth();
            if (isHorizontal) {
                getLayoutParams().width = ScreenUtils.getScreenHeight();
                getLayoutParams().height = (int) (1f * getLayoutParams().width * videoHeight / videoWidth);
            } else {
                getLayoutParams().height = ScreenUtils.getScreenWidth();
                getLayoutParams().width = (int) (1f * getLayoutParams().height * videoWidth / videoHeight);
            }
        } else {
            setRotation(0);
            boolean isHorizontal = videoWidth * 1f / videoHeight > ScreenUtils.getScreenWidth() * 1f / ScreenUtils.getScreenHeight();
            if (isHorizontal) {
                getLayoutParams().width = ScreenUtils.getScreenWidth();
                getLayoutParams().height = (int) (1f * getLayoutParams().width * videoHeight / videoWidth);
            } else {
                getLayoutParams().height = ScreenUtils.getScreenHeight();
                getLayoutParams().width = (int) (1f * getLayoutParams().height * videoWidth / videoHeight);
            }
        }
    }
}
