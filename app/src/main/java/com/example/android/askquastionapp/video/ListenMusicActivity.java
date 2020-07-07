package com.example.android.askquastionapp.video;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.DisableDoubleClickUtils;
import com.example.android.askquastionapp.utils.SqlliteUtils;
import com.example.jsoup.bean.MusicBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ListenMusicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private String path;
    private List<MediaData> mDatas = new ArrayList<>();
    private int mCurPage;
    private int mCurPlayPosition;
    private EditText search;

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
        Intent intent = new Intent(context, ListenMusicActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_music);
        search = findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideSoftInput(ListenMusicActivity.this);
                    startSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListenMusicActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new WatchVideoActivity.ViewHolder(LayoutInflater.from(ListenMusicActivity.this).inflate(R.layout.item_music, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    ImageView imageView = viewHolder.itemView.findViewById(R.id.iv_pic);
                    loadImage(imageView, mDatas.get(i).pic);
                    TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                    textView.setText(mDatas.get(i).name);
                    TextView videoView = viewHolder.itemView.findViewById(R.id.video_view);
                    videoView.setText(mDatas.get(i).url);
                    View downloadView = viewHolder.itemView.findViewById(R.id.on_download);
                    if (mDatas.get(i).hideDownload) {
                        downloadView.setVisibility(View.GONE);
                    } else {
                        downloadView.setVisibility(View.VISIBLE);
                    }
                    downloadView.setTag(i);
                    downloadView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!DisableDoubleClickUtils.canClick(v)) {
                                return;
                            }
                            int position = (int) v.getTag();
                            MediaData mediaData = mDatas.get(position);
                            File dir = new File(getMusicPathname());
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            DownloadObjManager.getInstance().startDownWithPosition(mediaData.url
                                    , getMusicPathname() + File.separator + mediaData.name + ".mp3");
                        }
                    });
                    viewHolder.itemView.setOnClickListener(new WatchVideoActivity.OnClickListener(i) {
                        @Override
                        public void onClick(View view, int position) {
                            mCurPlayPosition = position;
                            MusicPlayService.start(ListenMusicActivity.this, mDatas.get(mCurPlayPosition), new MusicPlayService.OnPlayListener() {
                                @Override
                                public void onNext(IPlayListener listener) {
                                    if (mDatas.size() == mCurPlayPosition + 1) {
                                        if (mCurPage == -1) {
                                            listener.onPlayUrl(null);
                                        } else {
                                            mCurPage++;
                                            loadData(listener);
                                        }
                                    } else {
                                        listener.onPlayUrl(mDatas.get(++mCurPlayPosition));
                                    }
                                }

                                @Override
                                public void onPre(IPlayListener listener) {
                                    if (mCurPlayPosition > 0) {
                                        listener.onPlayUrl(mDatas.get(--mCurPlayPosition));
                                    } else {
                                        listener.onPlayUrl(null);
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        path = getIntent().getStringExtra("path");
        preLoad();
        loadData(null);
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mCurPage++;
                loadData(null);
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mCurPage = 0;
                mDatas.clear();
                preLoad();
                loadData(null);
                refreshLayout.setEnableLoadMore(true);
                search.setText("");
            }
        });
       /* findViewById(R.id.on_pre).setOnClickListener((view -> onPreClick()));
        findViewById(R.id.on_play).setOnClickListener((view -> onPlayClick()));
        findViewById(R.id.on_next).setOnClickListener((view -> onNextClick()));*/
    }

    @NotNull
    private String getMusicPathname() {
        return Environment.getExternalStorageDirectory() + File.separator + "Music";
    }

    private void preLoad() {
        File dir = new File(getMusicPathname());
        if (!dir.exists() || dir.list() == null || dir.list().length == 0) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            MediaData media = new MediaData(file.getName(), file.getPath(), "");
            media.hideDownload = true;
            mDatas.add(media);
        }
    }

    /*
    private void onNextClick() {
    }

    private void onPlayClick() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else if (!mDatas.isEmpty() && mDatas.size() > mCurPlayPosition){
            playMusic(mDatas.get(mCurPlayPosition));
        }
    }

    private void onPreClick() {
    }*/

    public static class MediaData {
        public String pic;
        public String name;
        public String url;
        public boolean hideDownload;

        public MediaData(String name, String url, String pic) {
            this.name = name;
            this.url = url;
            this.pic = pic;
        }
    }

    private void loadData(IPlayListener listener) {
        Observable.just(path).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String path) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("page", mCurPage);
                List<MusicBean> musicBean = SqlliteUtils.getInstance(path).queryData("music_bean", map, MusicBean.class);
                if (musicBean != null) {
                    for (MusicBean bean : musicBean) {
                        MediaData data = new MediaData(bean.singer + ":" + bean.mname, bean.wma, bean.gspic);
                        if (contains(data)) {
                            continue;
                        }
                        datas.add(data);
                    }
                }
                return datas;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<MediaData> o) {
                        int first = mDatas.size();
                        mDatas.addAll(o);
                        int second = mDatas.size();
                        if (recyclerView.getAdapter() != null) {
                            recyclerView.getAdapter().notifyItemRangeChanged(first, second - first);
                        }
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishRefresh();
                        if (listener != null) {
                            listener.onPlayUrl(mDatas.get(++mCurPlayPosition));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private boolean contains(MediaData data) {
        for (MediaData mediaData : mDatas) {
            if (mediaData.name != null) {
                if (mediaData.name.endsWith(".mp3") && mediaData.name.substring(0, mediaData.name.length() - 4).equals(data.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ProgressDialog progressDialog;

    boolean isSearching;
    String keyWord;

    private void startSearch(String keyWord) {
        if (TextUtils.isEmpty(keyWord)) {
            return;
        }
        this.keyWord = keyWord;
        isSearching = true;
        refreshLayout.setEnableLoadMore(false);
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("");
            progressDialog.setCancelable(true);
        }
        progressDialog.show();
        mDatas.clear();
        mCurPage = -1;
        Observable.just(keyWord).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String keyWord) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("mname", keyWord);
                map.put("singer", keyWord);
                List<MusicBean> musicBean = SqlliteUtils.getInstance(path).queryData("music_bean", map, MusicBean.class);
                if (musicBean != null) {
                    for (MusicBean bean : musicBean) {
                        MediaData data = new MediaData(bean.singer + ":" + bean.mname, bean.wma, bean.gspic);
                        datas.add(data);
                    }
                }
                return datas;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<MediaData> o) {
                        mDatas.addAll(o);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        isSearching = false;
                        if (!TextUtils.isEmpty(search.getText().toString()) && !search.getText().toString().equals(ListenMusicActivity.this.keyWord)) {
                            startSearch(search.getText().toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void loadImage(ImageView imageView, String url) {
        imageView.setImageResource(R.mipmap.default_head);
        Observable.just(url).map(new Function<String, Bitmap>() {
            @Override
            public Bitmap apply(String url) throws Exception {
                URL imageurl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BitmapObserver<Bitmap>(imageView) {
                    @Override
                    public void onNext(Bitmap bitmap, ImageView imageView) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }

    public abstract class BitmapObserver<T> implements Observer<T> {

        private ImageView imageView;

        public BitmapObserver(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onSubscribe(Disposable d) {

        }

        public abstract void onNext(T t, ImageView imageView);

        @Override
        public void onNext(T o) {
            onNext(o, imageView);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }
}
