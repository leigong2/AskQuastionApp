package com.example.android.askquastionapp.video;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.media.PictureCheckManager;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.ToastUtils;
import com.example.android.askquastionapp.views.BottomPop;
import com.example.android.askquastionapp.views.CommonDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        if (mediaData.pathUri != null) {
            return false;
        }
        BottomPop current = BottomPop.getCurrent(getActivity());
        current.addItemText("视频信息");
        current.addItemText("复制");
        current.addItemText("分享");
        current.addItemText("删除");
        current.show(getActivity());
        current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
            @Override
            public void onItemClick(BottomPop bottomPop, int position) {
                String tag = bottomPop.getPosition(position);
                String filePath = mediaData.path;
                File file = new File(filePath);
                bottomPop.dismiss();
                switch (tag) {
                    case "视频信息":
                        new CommonDialog(getContext()).setContent("路径：" + filePath
                                + "\n修改日期：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date(file.lastModified()))
                                + "\n大小：" + FileUtil.getFileSize(file)).show();
                        break;
                    case "复制":
                        if (getActivity() == null) {
                            return;
                        }
                        ClipboardManager clipboardmanager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboardmanager == null) {
                            return;
                        }
                        Uri copyUri = Uri.parse(filePath);
                        ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "URI", copyUri);
                        clipboardmanager.setPrimaryClip(clip);
                        ToastUtils.showShort(filePath + "\n已复制到剪贴板");
                        break;
                    case "删除":
                        boolean delete = file.delete();
                        ToastUtils.showShort(delete ? "删除成功" : "删除失败");
                        if (getActivity() instanceof VideoPlayerActivity) {
                            if (((VideoPlayerActivity) getActivity()).dialog != null) {
                                ((VideoPlayerActivity) getActivity()).dialog.remove(mediaData);
                            }
                            getActivity().finish();
                        }
                        break;
                    case "分享":
                    default:
                        Intent shareIntent = new Intent();
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setType("video/*");
                        Uri currentUri = FileUtil.getCurrentUri(getContext(), mediaData.path);
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

    private PictureCheckManager.MediaData mediaData;

    public void play(@NonNull PictureCheckManager.MediaData mediaData) {
        this.mediaData = mediaData;
        videoController.setVisibility(View.VISIBLE);
        SurfaceVideoPlayer.getInstance().bindSurfaceController(videoController);
        SurfaceVideoPlayer.getInstance().bindSurfaceVideo(videoView);
        SurfaceVideoPlayer.getInstance().bindMedia(mediaData);
        SurfaceVideoPlayer.getInstance().play();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (mediaData.pathUri != null) {
            retriever.setDataSource(getContext(), mediaData.pathUri);
        } else {
            retriever.setDataSource(mediaData.path);
        }
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (!TextUtils.isEmpty(title)) {
            videoController.setTitle(title);
        } else if (!TextUtils.isEmpty(mediaData.folder)) {
            String[] split = mediaData.folder.split(File.separator);
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
