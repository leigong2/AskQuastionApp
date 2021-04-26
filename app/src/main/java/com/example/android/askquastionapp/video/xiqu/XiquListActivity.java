package com.example.android.askquastionapp.video.xiqu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.bean.HrefData;
import com.example.android.askquastionapp.utils.BrowserUtils;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.utils.SqlliteUtils;
import com.example.android.askquastionapp.utils.ToastUtils;
import com.example.android.askquastionapp.video.DownloadObjManager;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.views.BottomPop;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class XiquListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private final ArrayList<HrefData> mDatas = new ArrayList<>();
    private String path;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, XiquListActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        path = getIntent().getStringExtra("path");
        setTitle("戏曲");
        recyclerView.setLayoutManager(new LinearLayoutManager(XiquListActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(XiquListActivity.this).inflate(R.layout.item_xiqu_video, viewGroup, false));
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        BottomPop current = BottomPop.getCurrent(XiquListActivity.this);
                        current.addItemText("复制");
                        current.addItemText("删除");
                        current.show(XiquListActivity.this);
                        current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
                            @Override
                            public void onItemClick(BottomPop bottomPop, int position) {
                                int index = (int) v.getTag();
                                switch (position) {
                                    case 0:
                                        ClipboardManager clipboardmanager = (ClipboardManager) XiquListActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                        if (clipboardmanager == null) {
                                            return;
                                        }
                                        Uri copyUri = Uri.parse(mDatas.get(index).href);
                                        ClipData clipData = ClipData.newUri(getContentResolver(), "URL", copyUri);
                                        clipboardmanager.setPrimaryClip(clipData);
                                        ToastUtils.showShort(mDatas.get(index).href + "\n已复制到剪贴板");
                                        break;
                                    case 2:
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("title", mDatas.get(index).title);
                                        map.put("text", mDatas.get(index).text);
                                        map.put("href", mDatas.get(index).href);
                                        boolean delete = SqlliteUtils.getInstance(path).deleteData("xiqu_play_url", map);
                                        if (recyclerView.getAdapter() != null && delete) {
                                            mDatas.remove(index);
                                            recyclerView.getAdapter().notifyDataSetChanged();
                                        } else {
                                            ToastUtils.showToast(XiquListActivity.this, "删除失败");
                                        }
                                        break;
                                }
                                bottomPop.dismiss();
                            }
                        });
                        return false;
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int) v.getTag();
                        BrowserUtils.goToBrowser(XiquListActivity.this, mDatas.get(index).href);
//                        WebViewActivity.start(XiquListActivity.this, mDatas.get(index).href);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.setTag(i);
                ((TextView) viewHolder.itemView.findViewById(R.id.primary_title)).setText(mDatas.get(i).title);
                ((TextView) viewHolder.itemView.findViewById(R.id.secondary_title)).setText(mDatas.get(i).href);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mDatas.clear();
                loadData();
                refreshLayout.finishRefresh();
            }
        });
        loadData();
    }

    private void loadData() {
        Observable.just(1).map(new Function<Integer, List<HrefData>>() {
            @Override
            public List<HrefData> apply(Integer integer) throws Exception {
                List<HrefData> datas = SqlliteUtils.getInstance(path).queryAllData("xiqu_play_url", HrefData.class);
                if (datas == null) {
                    datas = new ArrayList<>();
                }
                Collections.sort(datas, new Comparator<HrefData>() {
                    @Override
                    public int compare(HrefData o1, HrefData o2) {
                        return o1.title.compareTo(o2.title);
                    }
                });
                SystemClock.sleep(300);
                return datas;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<HrefData>, Integer>(1, false) {
                    @Override
                    public void onNext(List<HrefData> datas, Integer integer2) {
                        if (datas != null) {
                            mDatas.addAll(datas);
                            if (recyclerView.getAdapter() != null) {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private List<HrefData> filerData(List<HrefData> datas) {
        List<HrefData> temp = new ArrayList<>();
        for (HrefData data : datas) {
            if (data.title.contains("全")) {
                temp.add(data);
            }
        }
        return temp;
    }
}
