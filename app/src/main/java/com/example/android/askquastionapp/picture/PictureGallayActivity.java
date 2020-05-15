package com.example.android.askquastionapp.picture;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PictureGallayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private ImageView anim;
    private View animLay;
    private List<String> mDatas = new ArrayList<>();
    private String path;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, PictureGallayActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        findViewById(R.id.search).setVisibility(View.GONE);
        path = getIntent().getStringExtra("path");
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        anim = findViewById(R.id.anim);
        animLay = findViewById(R.id.anim_lay);
        animLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAnim();
            }
        });
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_image, viewGroup, false));
                if (viewHolder.itemView.getTag() == null) {
                    viewHolder.itemView.setTag(0);
                }
                int position = (int) viewHolder.itemView.getTag();
                viewHolder.itemView.setOnLongClickListener(new MyLongClickListener(position) {
                    @Override
                    public void onLongClick(View v, int position) {
                        ToastUtils.showShort("已复制文件");
                        copyToClip(v.getContext(), mDatas.get(position));
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (animLay.getVisibility() == View.GONE) {
                            showAnim();
                        } else {
                            dismissAnim();
                        }
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.setTag(i);
                if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    ImageView imageView = viewHolder.itemView.findViewById(R.id.image_view);
                    GifImageView gifView = viewHolder.itemView.findViewById(R.id.gif_view);
                    if (mDatas.get(i).endsWith("gif")) {
                        gifView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        try {
                            GifDrawable gifFromUri = new GifDrawable(new File(mDatas.get(i)));
                            gifView.setImageDrawable(gifFromUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        gifView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(resizeBitmap(mDatas.get(i)));
                    }
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        loadData();
    }

    private Handler handler = new Handler();

    private int mCurAnimPosition;
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurAnimPosition < mDatas.size() - 1) {
                mCurAnimPosition ++;
                anim.setImageBitmap(resizeBitmap(mDatas.get(mCurAnimPosition)));
                handler.postDelayed(animRunnable, 100);
            } else {
                handler.removeCallbacks(animRunnable);
                animLay.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissAnim();
    }

    private void showAnim() {
        mCurAnimPosition = 0;
        handler.removeCallbacks(animRunnable);
        animLay.setVisibility(View.VISIBLE);
        handler.postDelayed(animRunnable, 100);
    }

    private void dismissAnim() {
        mCurAnimPosition = 0;
        animLay.setVisibility(View.GONE);
        handler.removeCallbacks(animRunnable);
    }

    public abstract class MyLongClickListener implements View.OnLongClickListener {
        private int position;

        public MyLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            onLongClick(v, position);
            return false;
        }

        public abstract void onLongClick(View v, int position);
    }

    public Bitmap resizeBitmap(String filePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int scaleWidth = (int) (w / 640);
        if (scaleWidth <= 1) {
            scaleWidth = 1;
        }
        newOpts.inSampleSize = scaleWidth;// 设置缩放比例, 以宽度为基准
        return BitmapFactory.decodeFile(filePath, newOpts);
    }

    private static Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");

    private void loadData() {
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File file : files) {
            mDatas.add(file.getPath());
        }
        if (!mDatas.isEmpty()) {
            Collections.sort(mDatas, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return getPosition(o1) - getPosition(o2);
                }
            });
        }
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private int getPosition(String o1) {
        String[] split = o1.split("\\.");
        for (int i = o1.length() - (split[split.length - 1].length() + 2); i >= 0; i--) {
            if (pattern.matcher(String.valueOf(o1.charAt(i))).matches()) {
                continue;
            }
            String substring = o1.substring(i + 1, o1.length() - split[split.length - 1].length() - 1);
            try {
                return Integer.parseInt(substring);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public void copyToClip(Context context, String s) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Uri copyUri = Uri.parse(s);
        ClipData clipData = ClipData.newUri(context.getContentResolver(), "URL", copyUri);
        clipboardManager.setPrimaryClip(clipData);
    }
}
