package com.example.android.askquastionapp.video;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.utils.DocumentsFileUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.SqlliteUtils;
import com.example.jsoup.bean.VideoBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.example.android.askquastionapp.MainActivity.EXTERNAL_FILE_CODE;
import static com.example.android.askquastionapp.utils.DocumentsFileUtils.OPEN_DOCUMENT_TREE_CODE;

public class WatchVideoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private String path;
    private List<MediaData> mDatas;
    private ProgressDialog progressDialog;
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
        Intent intent = new Intent(context, WatchVideoActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                //去选则文件夹，播放文件夹里的视频
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentsFileUtils.getInstance().showOpenDocumentTreeIgnore(this);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DocumentsFileUtils.getInstance().onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == OPEN_DOCUMENT_TREE_CODE && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
            File file = DocumentsFileUtils.getInstance().documentToFile(documentFile);
            getAllVideoFromPath(file.getPath());
        }
    }

    private void getAllVideoFromPath(String path) {
        Observable.just(path).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String path) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                File dir = new File(path);
                if (!dir.exists()) {
                    return datas;
                }
                File[] files = dir.listFiles();
                if (files == null) {
                    return datas;
                }
                appendAllData(files, datas);
                return datas;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<MediaData> o) {
                        VideoPlayerActivity.start(WatchVideoActivity.this, o, 0);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void appendAllData(File[] files, List<MediaData> datas) {
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getPath().endsWith(".mp4")
                    || file.getPath().endsWith(".mov")
                    || file.getPath().endsWith(".MP4")
                    || file.getPath().endsWith(".MOV")) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String string = decimalFormat.format(file.length() / 1024f / 1024f);//返回字符串
                MediaData data = new MediaData(string + "M", file.getPath(), file.getPath(), new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(file.lastModified())));
                datas.add(data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 0, "").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem first = menu.findItem(1);
        ImageView firstIcon = new ImageView(this);
        ((ImageView) firstIcon).setImageResource(R.mipmap.icon_download);
        ViewGroup.MarginLayoutParams firstParams = new ViewGroup.MarginLayoutParams(
                DensityUtil.dp2px(44 + 15 + 10)
                , DensityUtil.dp2px(44));
        firstIcon.setLayoutParams(firstParams);
        ((ImageView) firstIcon).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            firstIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        search = findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideSoftInput(WatchVideoActivity.this);
                    startSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isSearching) {
                    startSearch(search.getText().toString());
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(WatchVideoActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(WatchVideoActivity.this).inflate(R.layout.item_video, viewGroup, false));
                viewHolder.itemView.findViewById(R.id.on_download).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = (int) view.getTag();
                        MediaData mediaData = mDatas.get(position);
                        DownloadObjManager.getInstance().startDownWithPosition(mediaData.url
                                , getVideoPathname() + File.separator + mediaData.name.trim() + ".mp4");
                    }
                });
                return viewHolder;
            }

            @NotNull
            private String getVideoPathname() {
                String s = Environment.getExternalStorageDirectory() + File.separator + "Video";
                File file = new File(s);
                if (!file.exists()) {
                    file.mkdirs();
                }
                return s;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.findViewById(R.id.on_download).setTag(i);
                TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                if (!TextUtils.isEmpty(mDatas.get(i).addTime) && !TextUtils.isEmpty(mDatas.get(i).type)) {
                    textView.setText(String.format("%s : %s : %s", mDatas.get(i).type, mDatas.get(i).name, mDatas.get(i).addTime));
                } else if (!TextUtils.isEmpty(mDatas.get(i).type)) {
                    textView.setText(String.format("%s : %s", mDatas.get(i).type, mDatas.get(i).name));
                } else if (!TextUtils.isEmpty(mDatas.get(i).addTime)) {
                    textView.setText(String.format("%s : %s", mDatas.get(i).name, mDatas.get(i).addTime));
                } else {
                    textView.setText(String.format("%s", mDatas.get(i).name));
                }
                TextView videoView = viewHolder.itemView.findViewById(R.id.video_view);
                videoView.setText(mDatas.get(i).url);
                viewHolder.itemView.setOnClickListener(new OnClickListener(i) {
                    @Override
                    public void onClick(View view, int position) {
                        VideoPlayerActivity.start(WatchVideoActivity.this, mDatas.get(position).url);
                    }
                });
                viewHolder.itemView.setTag(i);
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = (int) view.getTag();
                        String url = mDatas.get(position).url;
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData mClipData = ClipData.newPlainText("Label", url);
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("链接已复制");
                        }
                        return false;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        path = getIntent().getStringExtra("path");
        loadData();
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mCurPage++;
                loadData();
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mCurPage = 0;
                mDatas.clear();
                loadData();
                refreshLayout.setEnableLoadMore(true);
                search.setText("");
            }
        });
    }

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
        Observable.just(keyWord).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String keyWord) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("video_name", keyWord);
                map.put("video_type", keyWord);
                map.put("video_add_time", keyWord);
                List<VideoBean> videoBean = SqlliteUtils.getInstance(path).queryData("video_bean", map, VideoBean.class);
                if (videoBean != null) {
                    for (VideoBean bean : videoBean) {
                        MediaData data = new MediaData(bean.getVideo_name(), bean.getVideo_url(), bean.getVideo_type(), bean.getVideo_add_time());
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
                        if (!TextUtils.isEmpty(search.getText().toString()) && !search.getText().toString().equals(WatchVideoActivity.this.keyWord)) {
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

    private int mCurPage;

    private void loadData() {
        Observable.just(path).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String path) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("page", mCurPage);
                List<VideoBean> videoBean = SqlliteUtils.getInstance(path).queryData("video_bean", map, VideoBean.class, 20);
                if (videoBean != null) {
                    for (VideoBean bean : videoBean) {
                        MediaData data = new MediaData(bean.getVideo_name(), bean.getVideo_url(), bean.getVideo_type(), bean.getVideo_add_time());
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
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishRefresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public static class MediaData {
        public String name;
        public String url;
        public String type;
        public String addTime;

        public MediaData(String name, String url, String type, String addTime) {
            this.name = name;
            this.url = url;
            this.type = type;
            this.addTime = addTime;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public abstract static class OnClickListener implements View.OnClickListener {
        int position;

        public OnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            onClick(view, position);
        }

        protected abstract void onClick(View view, int position);
    }
}
