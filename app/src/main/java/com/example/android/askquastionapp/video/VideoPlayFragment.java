package com.example.android.askquastionapp.video;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;

import static android.os.Build.VERSION_CODES.N;

public class VideoPlayFragment extends Fragment {

    private SurfaceVideoView videoView;
    private SurfaceControllerView videoController;

    public static VideoPlayFragment getInstance() {
        return new VideoPlayFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        view.setOnClickListener(v -> changeController());
        view.setOnLongClickListener(v -> onLongClick());
        dismissControllerDelay();
        return view;
    }

    private boolean onLongClick() {
        Intent shareIntent = new Intent();
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType("*/*");
        Uri currentUri = getCurrentUri();
        if (currentUri == null) {
            return false;
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, currentUri);
        startActivity(Intent.createChooser(shareIntent, "Here is the title of video"));
        return false;
    }

    private Uri getCurrentUri() {
        if (getActivity() == null) {
            return null;
        }
        String filePath = mediaData.url;
        File file = new File(filePath);
        if (Build.VERSION.SDK_INT < N) {
            return Uri.fromFile(file);
        } else if (Build.VERSION.SDK_INT < 29) {
            return FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".FileProvider", file);
        } else {
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (file.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }
    }

    private void dismissControllerDelay() {
        if (getActivity() instanceof VideoPlayerActivity) {
            ((VideoPlayerActivity) getActivity()).mHandler.postDelayed(this::changeController, 5000);
        }
    }

    private boolean isControllerHide;

    private void changeController() {
        if (isDetached() || getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing() || videoController == null) {
            return;
        }
        if (getActivity() instanceof VideoPlayerActivity) {
            ((VideoPlayerActivity) getActivity()).mHandler.removeCallbacksAndMessages(null);
        }
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

    private WatchVideoActivity.MediaData mediaData;

    public void play(@NonNull WatchVideoActivity.MediaData mediaData) {
        this.mediaData = mediaData;
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
