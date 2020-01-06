package com.example.android.askquastionapp.reader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;

public class ReaderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<String> mDatas;
    private String mUrl;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mUrl = getIntent().getStringExtra("url");
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        setTitle("读者");
        recyclerView.setLayoutManager(new LinearLayoutManager(ReaderActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(ReaderActivity.this).inflate(R.layout.item_video, viewGroup, false));
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                TextView firstView = viewHolder.itemView.findViewById(R.id.text_view);
                TextView secondView = viewHolder.itemView.findViewById(R.id.video_view);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        loadData();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void loadData() {

    }
}
