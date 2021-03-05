package com.example.android.askquastionapp.video;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;

public class VideoPlayFragment extends Fragment {

    private SurfaceVideoView videoView;
    private SurfaceControllerView videoController;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static VideoPlayFragment getInstance() {
        return new VideoPlayFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        view.setOnClickListener(v -> changeController());
        dismissControllerDelay();
        return view;
    }

    private void dismissControllerDelay() {
        mHandler.postDelayed(this::changeController, 5000);
    }

    private boolean isControllerHide;

    private void changeController() {
        if (isDetached() || getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing() || videoController == null) {
            return;
        }
        mHandler.removeCallbacksAndMessages(null);
        if (!isControllerHide) {
            ObjectAnimator.ofFloat(videoController, "translationY", 0, videoController.getMeasuredHeight())
                    .setDuration(300)
                    .start();
        } else {
            dismissControllerDelay();
            ObjectAnimator.ofFloat(videoController, "translationY", videoController.getMeasuredHeight(), 0)
                    .setDuration(300)
                    .start();
        }
        isControllerHide = !isControllerHide;
    }

    protected void initView(View view) {
        videoView = view.findViewById(R.id.video_view);
        videoController = view.findViewById(R.id.video_controller);
    }

    public void play(@NonNull WatchVideoActivity.MediaData mediaData) {
        videoController.setVisibility(View.VISIBLE);
        SurfaceVideoPlayer.getInstance().bindSurfaceController(videoController);
        SurfaceVideoPlayer.getInstance().bindSurfaceVideo(videoView);
        SurfaceVideoPlayer.getInstance().bindMedia(mediaData);
        SurfaceVideoPlayer.getInstance().play();
        if (isControllerHide) {
            changeController();
        }
    }

    public int getLayoutId() {
        return R.layout.fragment_video_view;
    }
}
