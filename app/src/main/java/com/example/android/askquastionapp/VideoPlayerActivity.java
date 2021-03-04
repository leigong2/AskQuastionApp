package com.example.android.askquastionapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.utils.MemoryCache;
import com.example.android.askquastionapp.video.SurfaceVideoPlayer;
import com.example.android.askquastionapp.video.VideoPlayFragment;
import com.example.android.askquastionapp.video.WatchVideoActivity;

import java.util.ArrayList;
import java.util.List;

import static androidx.viewpager2.widget.ViewPager2.ORIENTATION_VERTICAL;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private static final int PLAY_RETURN = 2 * 1000; // 2 seconds
    private static final String KEY_PLAY_POSITON = "paly_position";
    private static final String TOAST_ERROR_PLAY = "Paly error, please check url exist!";
    private static final String DIALOG_TITILE = "加载中，请稍后…";
    private ProgressDialog progressDialog;
    private String url;
    private ViewPager2 recyclerView;

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
        mediaData = MemoryCache.getInstance().remove("mediaData");
        position = MemoryCache.getInstance().remove("position");
        if (mediaData == null) {
            mediaData = new ArrayList<>();
        }
        if (position >= mediaData.size()) {
            position = 0;
        }
        recyclerView.setOrientation(ORIENTATION_VERTICAL);
        recyclerView.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return VideoPlayFragment.getInstance();
            }

            @Override
            public int getItemCount() {
                return mediaData.size();
            }
        });
        recyclerView.setOffscreenPageLimit(1);
        RecyclerView childAt = (RecyclerView) recyclerView.getChildAt(0);
        if (childAt.getLayoutManager() != null) {
            childAt.getLayoutManager().setItemPrefetchEnabled(false);
        }
        childAt.setItemViewCacheSize(0);
        recyclerView.registerOnPageChangeCallback(callback);
        recyclerView.setCurrentItem(position);
        if (position > 0) {
            BaseApplication.getInstance().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + recyclerView.getAdapter().getItemId(position));
                    if (fragment instanceof VideoPlayFragment) {
                        ((VideoPlayFragment) fragment).play(mediaData.get(position));
                    }
                }
            }, 500);
        }
    }

    ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + recyclerView.getAdapter().getItemId(position));
            if (fragment instanceof VideoPlayFragment) {
                ((VideoPlayFragment) fragment).play(mediaData.get(position));
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            switch (state) {
                case SCROLL_STATE_IDLE:  //空闲状态
                    break;
                case SCROLL_STATE_DRAGGING: //滑动状态
                    break;
                case SCROLL_STATE_SETTLING:  //滑动后自然沉降的状态
                    break;
                default:
                    break;
            }
        }
    };

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
        recyclerView.unregisterOnPageChangeCallback(callback);
        SurfaceVideoPlayer.getInstance().release();
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
