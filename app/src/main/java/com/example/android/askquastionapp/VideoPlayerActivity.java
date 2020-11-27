package com.example.android.askquastionapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.utils.MemoryCache;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.video.exo.ExoVideoHolder;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private static final int PLAY_RETURN = 2 * 1000; // 2 seconds
    private static final String KEY_PLAY_POSITON = "paly_position";
    private static final String TOAST_ERROR_PLAY = "Paly error, please check url exist!";
    private static final String DIALOG_TITILE = "加载中，请稍后…";
    private ProgressDialog progressDialog;
    private String url;
    private RecyclerView recyclerView;
    private PagerSnapHelper mSnapHelper;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void start(Context context, List<WatchVideoActivity.MediaData> mediaData, int position) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        MemoryCache.getInstance().put("mediaData", mediaData);
        MemoryCache.getInstance().put("position", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video_view);
        videoView = findViewById(R.id.video_view);
        url = getIntent().getStringExtra("url");
        if (url == null) {
            videoView.setVisibility(View.GONE);
            resetVideosPlayer();
            return;
        }
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.cancel();
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                ToastUtils.showShort(TOAST_ERROR_PLAY);
                progressDialog.cancel();
                finish();
                return false;
            }
        });
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setKeepScreenOn(true);
        videoView.setMediaController(controller);
        videoView.start();
        initDialog();
    }

    private List<WatchVideoActivity.MediaData> mediaData;
    private int position;

    private void resetVideosPlayer() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(recyclerView);
        mediaData = MemoryCache.getInstance().remove("mediaData");
        position = MemoryCache.getInstance().remove("position");
        if (mediaData == null) {
            mediaData = new ArrayList<>();
        }
        if (position >= mediaData.size()) {
            position = 0;
        }
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ExoVideoHolder(parent);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof ExoVideoHolder) {
                    ((ExoVideoHolder) holder).onSetValue(mediaData.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mediaData.size();
            }
        });
//        mSnapHelper.
//        recyclerView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//            }
//        });
    }

    private void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(DIALOG_TITILE);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int palyPosition = videoView.getCurrentPosition();
        if (palyPosition > PLAY_RETURN) {
            palyPosition -= PLAY_RETURN;
        }
        outState.putInt(KEY_PLAY_POSITON, palyPosition);
        outState.putString("url", url);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        videoView.seekTo(savedInstanceState.getInt(KEY_PLAY_POSITON));
    }
}
