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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.MemoryCache;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.CustomGlideModule;
import com.example.android.askquastionapp.utils.GlideUtils;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.jsoup.JsoupUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static java.io.File.separator;

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
                        try {
                            GifDrawable gifFromUri = new GifDrawable(new File(mDatas.get(i).text));
                            gifView.setImageDrawable(gifFromUri);
                            gifView.getLayoutParams().height = (int) ((float) gifFromUri.getIntrinsicHeight() / gifFromUri.getIntrinsicWidth() * ScreenUtils.getScreenWidth());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        gifView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        Bitmap bm = resizeBitmap(mDatas.get(i).text);
                        if (bm != null) {
                            imageView.setImageBitmap(bm);
                            imageView.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
                        }
                    }
                } else if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    gifView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    if (mDatas.get(i).text.endsWith("gif")) {
                        gifView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        if (android.os.Build.VERSION.SDK_INT >= 17) {
                            File localCache = GlideUtils.getInstance().getLocalCache(PictureGallayActivity.this, MainActivity.baseUrl + mDatas.get(i).href);
                            if (localCache != null) {
                                try {
                                    GifDrawable gifFromUri = new GifDrawable(localCache);
                                    gifView.setImageDrawable(gifFromUri);
                                    gifView.getLayoutParams().height = (int) ((float) gifFromUri.getIntrinsicHeight() / gifFromUri.getIntrinsicWidth() * ScreenUtils.getScreenWidth());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        gifView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        if (android.os.Build.VERSION.SDK_INT >= 17) {
                            File localCache = GlideUtils.getInstance().getLocalCache(PictureGallayActivity.this, MainActivity.baseUrl + mDatas.get(i).href);
                            if (localCache != null) {
                                Bitmap bm = resizeBitmap(localCache.getPath());
                                if (bm != null) {
                                    imageView.setImageBitmap(bm);
                                    imageView.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
                                }
                            }
                        }
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

    private Handler mHandler = new Handler();


    private void resaveFile(File src) {
        long length = src.length();
        if (!src.exists()) {
            return;
        }
        String srcName = src.getName();
        String parent = src.getParent();
        File tempDir = new File(CustomGlideModule.directory + separator + "temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File temp = new File(tempDir, src.getName());
        if (!temp.exists()) {
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //实现压缩，并重新生成BitMap对象
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(src.getAbsolutePath(), newOpts);
        float w = newOpts.outWidth;
        int scaleWidth = (int) (w / ScreenUtils.getScreenWidth() + 1);
        if (scaleWidth <= 1) {
            scaleWidth = 1;
        }
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        newOpts.inSampleSize = scaleWidth;// 设置缩放比例, 以宽度为基准
        newOpts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(src.getAbsolutePath(), newOpts);
        try {
            FileOutputStream out = new FileOutputStream(temp);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            //已将压缩图片保存为temp文件
            out.flush();
            out.close();
            bos.flush();
            bos.close();
            boolean delete = src.delete();
            File dest = new File(parent, srcName);
            boolean b = temp.renameTo(dest);
            Log.i("zune:", "resaveFile: delete = " + delete + ", rename = " + b + ", count = " + mCount++ + ", 原bitmap w = " + w + ", 压缩后bitmap w = " + bitmap.getWidth()
                    + "压缩前的length = " + length
                    + "srcLength = " + dest.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int mCount;

    private int mCurAnimPosition;
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurAnimPosition < mDatas.size() - 1) {
                mCurAnimPosition++;
                if (TextUtils.isEmpty(mDatas.get(mCurAnimPosition).href)) {
                    anim.setImageBitmap(resizeBitmap(mDatas.get(mCurAnimPosition).text));
                } else {
                    File localCache = GlideUtils.getInstance().getLocalCache(PictureGallayActivity.this, MainActivity.baseUrl + mDatas.get(mCurAnimPosition).href);
                    if (localCache != null) {
                        anim.setImageBitmap(resizeBitmap(localCache.getPath()));
                    }
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
    }

    private void showAnim() {
        mCurAnimPosition = 0;
        mHandler.removeCallbacks(animRunnable);
        animLay.setVisibility(View.VISIBLE);
        mHandler.postDelayed(animRunnable, 100);
    }

    private void dismissAnim() {
        mCurAnimPosition = 0;
        animLay.setVisibility(View.GONE);
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
        int w = newOpts.outWidth;
        int scaleWidth = (int) (w / 640);
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
                                mDatas.addAll(JsoupUtils.getHrefs(document));
                                preloadData();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Collections.sort(mDatas, new Comparator<HrefData>() {
                                            @Override
                                            public int compare(HrefData o1, HrefData o2) {
                                                return o1.text.compareTo(o2.text);
                                            }
                                        });
                                        Log.i("zune: ", "run: mDatas.size = " + mDatas.size());
                                        if (recyclerView.getAdapter() != null) {
                                            recyclerView.getAdapter().notifyDataSetChanged();
                                        }
                                        refreshLayout.finishRefresh();
                                        refreshLayout.finishLoadMore();
                                        loading.setVisibility(View.GONE);
                                    }
                                });
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

    private void preloadData() {
        for (HrefData data : mDatas) {
            String url = MainActivity.baseUrl + data.href;
            if (data.text.endsWith("gif")) {
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    Glide.with(this)
                            .asFile()
                            .load(url)
                            .submit();
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    if (GlideUtils.getInstance().getLocalCache(PictureGallayActivity.this, url) != null) {
                        return;
                    }
                    FutureTarget<File> submit = Glide.with(this)
                            .asFile()
                            .load(url)
                            .submit();
                    try {
                        File file = submit.get();
                        resaveFile(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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
