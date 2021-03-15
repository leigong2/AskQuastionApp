package com.example.android.askquastionapp.video;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.views.BottomPop;

import java.io.File;

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
        view.setOnLongClickListener(v -> onLongClick());
        return view;
    }

    private boolean onLongClick() {
        BottomPop current = BottomPop.getCurrent(getActivity());
        current.addItemText("删除");
        current.addItemText("分享");
        current.show(getActivity());
        current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
            @Override
            public void onItemClick(BottomPop bottomPop, int position) {
                bottomPop.dismiss();
                switch (position) {
                    case 0:
                        String filePath = mediaData.url;
                        File file = new File(filePath);
                        boolean delete = file.delete();
                        ToastUtils.showShort(delete ? "删除成功" : "删除失败");
                        if (getActivity() instanceof VideoPlayerActivity) {
                            if (((VideoPlayerActivity) getActivity()).dialog != null) {
                                ((VideoPlayerActivity) getActivity()).dialog.remove(mediaData);
                            }
                            getActivity().finish();
                        }
                        break;
                    case 1:
                    default:
                        Intent shareIntent = new Intent();
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setType("video/*");
                        Uri currentUri = getCurrentUri();
                        if (currentUri == null) {
                            return;
                        }
                        shareIntent.putExtra(Intent.EXTRA_STREAM, currentUri);
                        startActivity(Intent.createChooser(shareIntent, "Here is the title of video"));
                        break;
                }
            }
        });
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

    protected void initView(View view) {
        videoView = view.findViewById(R.id.video_view);
        videoController = view.findViewById(R.id.video_controller);
        videoController.setOnOrientationChangeListener(new SurfaceControllerView.OnOrientationChangeListener() {
            @Override
            public void onOrientationChange(int orientation) {
                if (getActivity() instanceof VideoPlayerActivity) {
                    ((VideoPlayerActivity) getActivity()).mOrientation = orientation;
                }
                videoView.onOrientationChange(orientation);
            }

            @Override
            public void onRootViewLongClick() {
                onLongClick();
            }
        });
    }

    private WatchVideoActivity.MediaData mediaData;

    public void play(@NonNull WatchVideoActivity.MediaData mediaData) {
        this.mediaData = mediaData;
        videoController.setVisibility(View.VISIBLE);
        SurfaceVideoPlayer.getInstance().bindSurfaceController(videoController);
        SurfaceVideoPlayer.getInstance().bindSurfaceVideo(videoView);
        SurfaceVideoPlayer.getInstance().bindMedia(mediaData);
        SurfaceVideoPlayer.getInstance().play();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mediaData.url);
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (!TextUtils.isEmpty(title)) {
            videoController.setTitle(title);
        } else if (!TextUtils.isEmpty(mediaData.name)) {
            String[] split = mediaData.name.split(File.separator);
            videoController.setTitle(split[split.length - 1]);
        }
        if (videoController.isControllerHide) {
            videoController.changeController();
        }
    }

    public int getLayoutId() {
        return R.layout.fragment_video_view;
    }
}
