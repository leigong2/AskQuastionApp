package com.example.android.askquastionapp.picture;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.utils.MemoryCache;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.GlideUtils;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.jsoup.JsoupUtils;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class PictureGallayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private ImageView anim;
    private View animLay;
    private List<HrefData> mDatas = new ArrayList<>();
    private HrefData path;
    private Integer curPosition;
    private List<HrefData> paths;
    private View loading;
    private Bitmap mDefaultBitmap;

    public static void start(Context context, List<HrefData> path, int position) {
        Intent intent = new Intent(context, PictureGallayActivity.class);
        MemoryCache.getInstance().put("path", path);
        MemoryCache.getInstance().put("position", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        findViewById(R.id.search).setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        loading = findViewById(R.id.clear_root);
        paths = MemoryCache.getInstance().remove("path");
        if (paths == null) {
            paths = new ArrayList<>();
        }
        curPosition = MemoryCache.getInstance().remove("position");
        if (curPosition == null) {
            curPosition = 0;
        }
        if (paths.isEmpty()) {
            finish();
            ToastUtils.showShort("文件夹为空");
            return;
        }
        this.path = paths.get(curPosition);
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
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                curPosition++;
                if (curPosition == paths.size()) {
                    curPosition = 0;
                }
                path = paths.get(curPosition);
                loadData();
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                curPosition--;
                if (curPosition == -1) {
                    curPosition = paths.size() - 1;
                }
                path = paths.get(curPosition);
                loadData();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                if (state.hasTargetScrollPosition() || true) {
                    return getResources().getDisplayMetrics().heightPixels * 5;
                } else {
                    return super.getExtraLayoutSpace(state);
                }
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_image, viewGroup, false));
                ImageView imageView = viewHolder.itemView.findViewById(R.id.image_view);
                GifImageView gifView = viewHolder.itemView.findViewById(R.id.gif_view);
                if (mDefaultBitmap != null) {
                    imageView.getLayoutParams().height = (int) ((float) mDefaultBitmap.getHeight() / mDefaultBitmap.getWidth() * ScreenUtils.getScreenWidth());
                    gifView.getLayoutParams().height = (int) ((float) mDefaultBitmap.getHeight() / mDefaultBitmap.getWidth() * ScreenUtils.getScreenWidth());
                }
                if (viewHolder.itemView.getTag() == null) {
                    viewHolder.itemView.setTag(0);
                }
                int position = (int) viewHolder.itemView.getTag();
                viewHolder.itemView.setOnLongClickListener(new MyLongClickListener(position) {
                    @Override
                    public void onLongClick(View v, int position) {
                        ToastUtils.showShort("已复制文件");
                        copyToClip(v.getContext(), mDatas.get(position).text);
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
                ImageView imageView = viewHolder.itemView.findViewById(R.id.image_view);
                GifImageView gifView = viewHolder.itemView.findViewById(R.id.gif_view);
                if (viewHolder instanceof WatchVideoActivity.ViewHolder && TextUtils.isEmpty(mDatas.get(i).href)) {
                    if (mDatas.get(i).text.endsWith("gif")) {
                        gifView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        GlideUtils.getInstance().setFileToView(new File(mDatas.get(i).text), gifView, true);
                    } else {
                        gifView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        GlideUtils.getInstance().loadUrl(mDatas.get(i).text, imageView, true, true);
                    }
                } else if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    gifView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    String url = MainActivity.baseUrl + mDatas.get(i).href;
                    if (mDatas.get(i).text.endsWith("gif")) {
                        gifView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        GlideUtils.getInstance().loadUrl(url, gifView, true, false);
                    } else {
                        gifView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        GlideUtils.getInstance().loadUrl(url, imageView, true, false);
                    }
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        Drawable drawable = getResources().getDrawable(R.mipmap.place_loading);
        BitmapDrawable bd = (BitmapDrawable) drawable;
        mDefaultBitmap = bd.getBitmap();
        loadData();
    }

    private Handler mHandler = new Handler();

    private int mCurAnimPosition;
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurAnimPosition < mDatas.size() - 1) {
                mCurAnimPosition++;
                if (TextUtils.isEmpty(mDatas.get(mCurAnimPosition).href)) {
                    anim.setImageBitmap(resizeBitmap(mDatas.get(mCurAnimPosition).text));
                } else {
                    GlideUtils.getInstance().loadUrlWithoutDefault(MainActivity.baseUrl + mDatas.get(mCurAnimPosition).href, anim);
                }
                mHandler.postDelayed(animRunnable, 100);
            } else {
                mHandler.removeCallbacks(animRunnable);
                animLay.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissAnim();
        if (path != null && !TextUtils.isEmpty(path.href) && !mDatas.isEmpty()) {
            SPUtils.getInstance().put(MainActivity.baseUrl + path.href, GsonGetter.getInstance().getGson().toJson(mDatas));
        }
    }

    private void showAnim() {
        mCurAnimPosition = 0;
        mHandler.removeCallbacks(animRunnable);
        animLay.setVisibility(View.VISIBLE);
        mHandler.postDelayed(animRunnable, 100);
    }

    private void dismissAnim() {
        mCurAnimPosition = 0;
        if (animLay != null) {
            animLay.setVisibility(View.GONE);
        }
        mHandler.removeCallbacks(animRunnable);
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
        float w = newOpts.outWidth;
        int scaleWidth = (int) (w / ScreenUtils.getScreenWidth() + 1);
        if (scaleWidth <= 1) {
            scaleWidth = 1;
        }
        newOpts.inSampleSize = scaleWidth;// 设置缩放比例, 以宽度为基准
        newOpts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, newOpts);
    }

    private static Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");

    private void loadData() {
        if (recyclerView != null && mDatas.size() > 0) {
            recyclerView.scrollToPosition(0);
        }
        mDatas.clear();
        if (TextUtils.isEmpty(path.href)) {
            File dir = new File(path.text);
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    for (File listFile : file.listFiles()) {
                        String path = listFile.getPath();
                        HrefData hrefData = new HrefData("", "", path);
                        mDatas.add(hrefData);
                    }
                } else {
                    HrefData hrefData = new HrefData("", "", file.getPath());
                    mDatas.add(hrefData);
                }
            }
            if (!mDatas.isEmpty()) {
                Collections.sort(mDatas, new Comparator<HrefData>() {
                    @Override
                    public int compare(HrefData o1, HrefData o2) {
                        return getPosition(o1.text) - getPosition(o2.text);
                    }
                });
            }
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
            if (refreshLayout != null) {
                refreshLayout.finishLoadMore();
                refreshLayout.finishRefresh();
            }
        } else {
            loadNetData();
        }
    }

    private void loadNetData() {
        loading.setVisibility(View.VISIBLE);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + path.href)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@Nullable Call call, @Nullable IOException e) {
                Log.i("zune", GsonGetter.getInstance().getGson().toJson(e));
                if (path != null && !TextUtils.isEmpty(path.href)) {
                    String json = SPUtils.getInstance().getString(MainActivity.baseUrl + path.href);
                    List<HrefData> data = GsonGetter.getInstance().getGson().fromJson(json, new TypeToken<List<HrefData>>() {
                    }.getType());
                    mDatas.addAll(data);
                    dispatchCurData();
                }
            }

            @Override
            public void onResponse(@Nullable Call call, @Nullable Response response) throws IOException {
                if (response != null && response.body() != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                String html = new String(response.body().bytes());
                                Document document = Jsoup.parseBodyFragment(html);
                                List<HrefData> hrefs = JsoupUtils.getHrefs(document);
                                if (!hrefs.isEmpty()) {
                                    mDatas.addAll(hrefs.subList(1, hrefs.size() - 1));
                                }
                                dispatchCurData();
                            } catch (Exception e) {
                                e.printStackTrace();
                                onFailure(call, null);
                            }
                        }
                    }.start();
                } else {
                    onFailure(call, null);
                }
            }
        });
    }

    private void dispatchCurData() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Collections.sort(mDatas, new Comparator<HrefData>() {
                    @Override
                    public int compare(HrefData o1, HrefData o2) {
                        Pattern pattern = Pattern.compile("[0-9]+(?=[^0-9]*$)", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(o1.text);
                        String group = ".0.";
                        while (matcher.find()) {
                            group = matcher.group();
                        }
                        Matcher matcher2 = pattern.matcher(o2.text);
                        String group2 = ".0.";
                        while (matcher2.find()) {
                            group2 = matcher2.group();
                        }
                        return Integer.parseInt(group) - Integer.parseInt(group2);
                    }
                });
                if (recyclerView.getAdapter() != null) {
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                loading.setVisibility(View.GONE);
            }
        });
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
