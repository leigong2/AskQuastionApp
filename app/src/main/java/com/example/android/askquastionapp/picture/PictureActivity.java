package com.example.android.askquastionapp.picture;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PictureActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private List<String> mDatas;
    private String path;

    public static void start(Context context, String path) {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        findViewById(R.id.search).setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_video, viewGroup, false));
                viewHolder.itemView.setOnClickListener(new WatchVideoActivity.OnClickListener(i) {
                    @Override
                    public void onClick(View view, int position) {
                        int positions = (int) view.getTag();
                        PictureGallayActivity.start(PictureActivity.this, mDatas.get(positions));
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = (int) view.getTag();
                        String url = mDatas.get(position);
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData mClipData = ClipData.newPlainText("Label", url);
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("链接已复制");
                        }
                        return false;
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                    textView.setText(mDatas.get(i));
                    viewHolder.itemView.setTag(i);
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        path = getIntent().getStringExtra("path");
        loadData();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData();
            }
        });
        refreshLayout.setEnableLoadMore(false);
    }

    private void loadData() {
        if (recyclerView.getAdapter() == null) {
            return;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.finishRefresh();
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.finishRefresh();
            return;
        }
        for (File file : files) {
            mDatas.add(file.getPath());
        }
        Collections.sort(mDatas);
        recyclerView.getAdapter().notifyDataSetChanged();
        refreshLayout.finishRefresh();
    }
}
