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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.StringUtils;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.jsoup.JsoupUtils;
import com.example.jsoup.jsoup.webloaddata.BaseWebLoadUtils;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PictureActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private List<HrefData> mDatas = new ArrayList<>();
    private String path;
    private String imgUrls;
    public static boolean sRandomSort;

    public static void start(Context context, String path, String imgUrls) {
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
        intent.putExtra("imgUrls", imgUrls);
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
                        if (mDatas == null || mDatas.isEmpty()) {
                            ToastUtils.showShort("图片为空文件夹");
                            return;
                        }
                        PictureGallayActivity.start(PictureActivity.this, mDatas, (int) view.getTag());
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = (int) view.getTag();
                        HrefData url = mDatas.get(position);
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData mClipData = ClipData.newPlainText("Label", url.text);
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
                    textView.setText(mDatas.get(i).text);
                    viewHolder.itemView.setTag(i);
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        path = getIntent().getStringExtra("path");
        imgUrls = getIntent().getStringExtra("imgUrls");
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
        mDatas.clear();
        if (recyclerView.getAdapter() == null) {
            return;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.finishRefresh();
            if (mDatas.isEmpty()) {
                loadNetDatas(imgUrls);
            }
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshLayout.finishRefresh();
            if (mDatas.isEmpty()) {
                loadNetDatas(imgUrls);
            }
            return;
        }
        for (File file : files) {
            String path = file.getPath();
            HrefData hrefData = new HrefData("", "", path);
            mDatas.add(hrefData);
        }
        Collections.sort(mDatas, new Comparator<HrefData>() {
            @Override
            public int compare(HrefData o1, HrefData o2) {
                return o1.text.compareTo(o2.text);
            }
        });
        recyclerView.getAdapter().notifyDataSetChanged();
        refreshLayout.finishRefresh();
        if (mDatas.isEmpty()) {
            loadNetDatas(imgUrls);
        }
    }

    private void loadNetDatas(String imgUrls) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(imgUrls)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@Nullable Call call, @Nullable IOException e) {
                Log.i("zune", GsonGetter.getInstance().getGson().toJson(e));
                String json = SPUtils.getInstance().getString("PictureActivityData");
                List<HrefData> data = GsonGetter.getInstance().getGson().fromJson(json, new TypeToken<List<HrefData>>() {
                }.getType());
                if (data != null) {
                    mDatas.addAll(data);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView.getAdapter() != null) {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@Nullable Call call, @Nullable Response response) throws IOException {
                if (response != null && response.body() != null) {
                    String html = new String(response.body().bytes());
                    Document document = Jsoup.parseBodyFragment(html);
                    List<HrefData> hrefs = BaseWebLoadUtils.getHrefs(document);
                    mDatas.addAll(hrefs);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (sRandomSort) {
                                Collections.shuffle(mDatas);
                            } else {
                                sRandomSort = true;
                                Collections.sort(mDatas, new Comparator<HrefData>() {
                                    @Override
                                    public int compare(HrefData o1, HrefData o2) {
                                        return o1.text.compareTo(o2.text);
                                    }
                                });
                            }
                            if (recyclerView.getAdapter() != null) {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                        }
                    });
                } else {
                    onFailure(call, null);
                }
            }
        });
    }

    private Handler mHandler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
        SPUtils.getInstance().put("PictureActivityData", GsonGetter.getInstance().getGson().toJson(mDatas));
    }
}
