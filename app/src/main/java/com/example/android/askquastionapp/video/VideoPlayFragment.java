package com.example.android.askquastionapp.video;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.MemoryCache;

public class VideoPlayFragment extends Fragment {

    private SurfaceVideoView videoView;
    private SurfaceControllerView videoController;
    private WatchVideoActivity.MediaData mediaData;

    public static VideoPlayFragment getInstance(WatchVideoActivity.MediaData mediaData) {
        VideoPlayFragment fragment = new VideoPlayFragment();
        MemoryCache.getInstance().put("mediaData", mediaData);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        return view;
    }

    protected void initView(View view) {
        Log.i("zune: ", "initView" + this);
        mediaData = MemoryCache.getInstance().remove("mediaData");
        videoView = view.findViewById(R.id.video_view);
        videoController = view.findViewById(R.id.video_controller);
    }

    public void play(@NonNull WatchVideoActivity.MediaData mediaData) {
        Log.i("zune: ", "play" + this);
        SurfaceVideoPlayer.getInstance().bindSurfaceController(videoController);
        SurfaceVideoPlayer.getInstance().bindSurfaceVideo(videoView);
        SurfaceVideoPlayer.getInstance().bindMedia(mediaData);
        SurfaceVideoPlayer.getInstance().play();
    }

    public int getLayoutId() {
        return R.layout.fragment_video_view;
    }
}
