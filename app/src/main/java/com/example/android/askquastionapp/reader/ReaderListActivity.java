package com.example.android.askquastionapp.reader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;

public class ReaderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<String> mDatas;
    private String baseUrl = "http://www.rensheng5.com/";

    public static void start(Context context) {
        Intent intent = new Intent(context, ReaderListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        setTitle("读者");

        recyclerView.setLayoutManager(new LinearLayoutManager(ReaderListActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(ReaderListActivity.this).inflate(R.layout.item_video, viewGroup, false));
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String s = ((TextView) v.findViewById(R.id.video_view)).getText().toString();
                        ReaderActivity.start(ReaderListActivity.this, s);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                TextView firstView = viewHolder.itemView.findViewById(R.id.text_view);
                firstView.setText(mDatas.get(i));
                TextView secondView = viewHolder.itemView.findViewById(R.id.video_view);
                switch (i) {
                    case 0:
                        secondView.setText(String.format("%s%s", baseUrl, "renshengganwu/"));
                        break;
                    case 1:
                        secondView.setText(String.format("%s%s", baseUrl, "renshengzheli/"));
                        break;
                    case 2:
                        secondView.setText(String.format("%s%s", baseUrl, "zheligushi/"));
                        break;
                    case 3:
                        secondView.setText(String.format("%s%s", baseUrl, "lizhigushi/"));
                        break;
                    case 4:
                        secondView.setText(String.format("%s%s", baseUrl, "yuyangushi/"));
                        break;
                    case 5:
                        secondView.setText(String.format("%s%s", baseUrl, "yisuoyuyan/"));
                        break;
                    case 6:
                        secondView.setText(String.format("%s%s", baseUrl, "qingnianwenzhai/"));
                        break;
                    case 7:
                        secondView.setText(String.format("%s%s", baseUrl, "duzhewenzhai/"));
                        break;
                    case 8:
                        secondView.setText(String.format("%s%s", baseUrl, "yilinzazhi/"));
                        break;
                    case 9:
                        secondView.setText(String.format("%s%s", baseUrl, "gushihui/"));
                        break;
                    case 10:
                        secondView.setText(String.format("%s%s", baseUrl, "xiaogushi/"));
                        break;
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        loadData();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void loadData() {
        mDatas.add("人生感悟");
        mDatas.add("人生哲理");
        mDatas.add("励志故事");
        mDatas.add("哲理故事");
        mDatas.add("寓言故事");
        mDatas.add("伊索寓言");
        mDatas.add("青年");
        mDatas.add("读者");
        mDatas.add("意林");
        mDatas.add("故事会");
        mDatas.add("小故事大道理");
    }
}
