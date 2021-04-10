package com.example.android.askquastionapp.reader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.bean.HrefData;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ReaderActivity extends AppCompatActivity {
    String[] userAgents = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",};
    String[] ip = {
            "223.198.25.148:9999"
    };
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<HrefData> mDatas;
    private String mUrl;
    private TextView menuTextView;

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
                        ReaderDetailActivity.start(ReaderActivity.this, mDatas.get((Integer) v.getTag()).href, mDatas.get((Integer) v.getTag()).text);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.setTag(i);
                TextView firstView = viewHolder.itemView.findViewById(R.id.text_view);
                TextView secondView = viewHolder.itemView.findViewById(R.id.video_view);
                firstView.setText(mDatas.get(i).text);
                secondView.setText(mDatas.get(i).href);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        loadData(1);
        recyclerView.getAdapter().notifyDataSetChanged();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData(1);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(++mCurPage);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, 0, "更多").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem item = menu.findItem(Menu.FIRST);
        menuTextView = (TextView) item.getActionView();
        if (menuTextView == null) {
            menuTextView = new TextView(this);
        }
        menuTextView.setPadding(DensityUtil.dp2px(16), 0, DensityUtil.dp2px(16), 0);
        menuTextView.setTextColor(Color.parseColor("#FFFFFF"));
        menuTextView.setText("更多");
        menuTextView.setEnabled(true);
        menuTextView.setMaxLines(1);
        menuTextView.setEllipsize(TextUtils.TruncateAt.END);
        boolean onCreateOptionsMenu = super.onCreateOptionsMenu(menu);
        menuTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop(v);
            }
        });
        item.setActionView(menuTextView);
        return onCreateOptionsMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                showPop(item.getActionView());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPop(View v) {
        MorePop pop = new MorePop(this);
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 167; i++) {
            datas.add(String.valueOf(i + 1));
        }
        pop.setDatas(datas);
        pop.showAsDropDown(v, -DensityUtil.dp2px(20), DensityUtil.dp2px(20));
        pop.setOnItemClick(new MorePop.OnItemClick() {
            @Override
            public void onItem(String index) {
                mDatas.clear();
                loadData(Integer.parseInt(index));
            }
        });
    }

    private int mCurPage;

    private void loadData(int page) {
        mCurPage = page;
        if (page > 1) {
            mUrl = mUrl.split(".html")[0].split("list_")[0] + "list_" + page + ".html";
        } else {
            mDatas.clear();
        }
        Observable.just(page).map(new Function<Integer, Document>() {
            @Override
            public Document apply(Integer integer) throws Exception {
                Document document = null;
                String userAgent = userAgents[(int) (userAgents.length * Math.random())];
                try {
                    int i = (int) (Math.random() * 1000);////做一个随机延时，防止网站屏蔽
                    while (i != 0) {
                        i--;
                    }
                    document = Jsoup.connect(mUrl)
                            .userAgent(userAgent)
                            .timeout(30000).get();
                } catch (Exception e) {
                    try {
                        document = Jsoup.connect(mUrl)
                                .userAgent(userAgent)
                                .timeout(30000).post();
                        System.out.println("正常：" + mUrl);
                    } catch (Exception e1) {
                        System.out.println("异常：" + mUrl + "..........." + e1);
                    }
                }
                return document;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Document>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Document document) {
                        getHomePage(document);
                    }

                    @Override
                    public void onError(Throwable e) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getHomePage(Document document) {
        if (document == null) {
            return;
        }
        Elements elements = document.getElementsByTag("div");
        StringBuilder title = new StringBuilder();
        for (Element element : elements) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("weizhi".equalsIgnoreCase(s)) {
                Elements as = element.getElementsByTag("a");
                for (Element a : as) {
                    for (int i = 0; i < a.childNodeSize(); i++) {
                        Node node = a.childNode(i);
                        title.append(node).append(":");
                    }
                }
                System.out.println(title.deleteCharAt(title.length() - 1));
            }
        }
        Elements ul = document.getElementsByTag("ul");
        String tempTitle = "";
        for (Element element : ul) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("p7".equalsIgnoreCase(s)) {
                Elements hrefs = element.getElementsByTag("a");
                for (Element a : hrefs) {
                    //实体对象构造方法，代码省略
                    HrefData hrefData = new HrefData(a.attr("href"), a.attr("title"), a.text());
                    tempTitle = title.toString();
                    mDatas.add(hrefData);
                }
            }
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
        setTitle(tempTitle);
    }
}
