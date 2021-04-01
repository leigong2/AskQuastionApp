package com.example.android.askquastionapp.video.exo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.media.PictureCheckManager;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.google.android.exoplayer2.ui.PlayerView;

public class ExoVideoHolder extends RecyclerView.ViewHolder {

    private PlayerView videoView;

    public ExoVideoHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_exo_video_view, parent, false));
        videoView = itemView.findViewById(R.id.video_view);
    }

    private PictureCheckManager.MediaData mediaData;

    public void onSetValue(PictureCheckManager.MediaData mediaData) {
        this.mediaData = mediaData;
    }

    public void play(@NonNull PictureCheckManager.MediaData mediaData) {
        ExoPlayer.getInstance().bindView(videoView);
        ExoPlayer.getInstance().prepare(mediaData.path);
    }

    public int getLayoutId() {
        return R.layout.fragment_exo_video_view;
    }
}
