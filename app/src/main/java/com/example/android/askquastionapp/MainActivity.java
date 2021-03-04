package com.example.android.askquastionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.aes.test;
import com.example.android.askquastionapp.bean.Company;
import com.example.android.askquastionapp.bean.UpdateCompanyBean;
import com.example.android.askquastionapp.besar.BesarActivity;
import com.example.android.askquastionapp.contacts.ContactBean;
import com.example.android.askquastionapp.expand.ExpandActivity;
import com.example.android.askquastionapp.expand.PushActivity;
import com.example.android.askquastionapp.fenbei.FenBeiActivity;
import com.example.android.askquastionapp.keeplive.ScreenBroadcastReceiver;
import com.example.android.askquastionapp.location.LocationActivity;
import com.example.android.askquastionapp.math.MathFunActivity;
import com.example.android.askquastionapp.math.WebWordProblemActivity;
import com.example.android.askquastionapp.media.MediaActivity;
import com.example.android.askquastionapp.media.PhotoSheetDialog;
import com.example.android.askquastionapp.ninepatch.NinePatchActivity;
import com.example.android.askquastionapp.picture.BigPictureActivity;
import com.example.android.askquastionapp.picture.BigTestPictureActivity;
import com.example.android.askquastionapp.picture.ImageViewActivity;
import com.example.android.askquastionapp.picture.PictureActivity;
import com.example.android.askquastionapp.pollnumber.PollNumberActivity;
import com.example.android.askquastionapp.read.ReadTxtActivity;
import com.example.android.askquastionapp.reader.ReaderListActivity;
import com.example.android.askquastionapp.scan.CaptureActivity;
import com.example.android.askquastionapp.scan.QCodeDialog;
import com.example.android.askquastionapp.utils.BitmapUtil;
import com.example.android.askquastionapp.utils.BrowserUtils;
import com.example.android.askquastionapp.utils.ClearUtils;
import com.example.android.askquastionapp.utils.ContactsUtils;
import com.example.android.askquastionapp.utils.CustomItemTouchHelperCallBack;
import com.example.android.askquastionapp.utils.DocumentsFileUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.SaveUtils;
import com.example.android.askquastionapp.utils.SetIpDialog;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.video.DownloadObjManager;
import com.example.android.askquastionapp.video.ListenMusicActivity;
import com.example.android.askquastionapp.video.VideoTurnGifActivity;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.views.ClearHolder;
import com.example.android.askquastionapp.views.ListDialog;
import com.example.android.askquastionapp.web.WebViewUtils;
import com.example.android.askquastionapp.wxapi.ShareDialog;
import com.example.android.askquastionapp.xmlparse.ExcelManager;
import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.KeyWords;
import com.example.jsoup.bean.LanguageWords;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import maximsblog.blogspot.com.jlatexmath.FromHelloWorld;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.io.File.separator;

/**
 * The type Main activity.
 *
 * @author wangzhilong
 */
public class MainActivity extends AppCompatActivity {

    public static final int EXTERNAL_FILE_CODE = 200;
    private static final int REQUEST_CODE_SCAN = 201;
    private ClearHolder clearHolder;
    private ShareDialog shareDialog;
    private static final int EXTERNAL_TXT_CHOOSE = 1;
    private static final int EXTERNAL_XSL_CHOOSE = 2;
    private int externalClick;
    private String musicFile;
    private String movieFile;
    private String avFile;
    private String baseDir;
    public static String imageDir;
    private String movieUrl = "https://github.com/leigong2/AskQuastionApp/blob/master/app/src/main/assets/movie_db.db";
    private String avUrl = "https://github.com/leigong2/AskQuastionApp/blob/master/app/src/main/assets/av_db.db";
    private String musicUrl = "https://github.com/leigong2/AskQuastionApp/blob/master/app/src/main/assets/music_db.db";

    public static String baseUrl = TextUtils.isEmpty(SPUtils.getInstance().getString("baseUrl")) ? "http://192.168.200.62" : SPUtils.getInstance().getString("baseUrl");

    private String devolop, preProducation, production_blue, production, release, self, imgs;
    private List<String> mMainTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Documents";
        imageDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Documents" + separator + "img";
        musicFile = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Documents" + separator + "music_db.db";
        movieFile = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Documents" + separator + "movie_db.db";
        avFile = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Documents" + separator + "av_db.db";
        requestPermiss();
        initMainTags();
        RecyclerView recyclerView = findViewById(R.id.main_tags);
        recyclerView.setLayoutManager(ChipsLayoutManager.newBuilder(this)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .setOrientation(ChipsLayoutManager.HORIZONTAL).build());
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter
                = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main_tag, viewGroup, false);
                itemView.setOnClickListener(v -> onTagClick((String) v.getTag()));
                return new RecyclerView.ViewHolder(itemView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                TextView textTag = viewHolder.itemView.findViewById(R.id.tag);
                textTag.setText(mMainTags.get(position));
                viewHolder.itemView.setTag(textTag.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = getResources().getDrawable(R.drawable.bg_main_tag);
                    String[] randColor = getRandColor().split("\\*");
                    drawable.setTint(Color.parseColor(randColor[0]));
                    textTag.setBackground(drawable);
                    BigInteger bigint = new BigInteger(randColor[1], 16);
                    int textColor = bigint.intValue();
                    textTag.setTextColor(Color.BLACK);
                }
            }

            @Override
            public int getItemCount() {
                return mMainTags.size();
            }
        };
        CustomItemTouchHelperCallBack callback = new CustomItemTouchHelperCallBack();
        callback.setOnItemMove(new CustomItemTouchHelperCallBack.OnItemMove() {
            @Override
            public boolean onMove(int fromPosition, int toPosition) {
                //1、交换数据
                sort(mMainTags, fromPosition, toPosition);
                //2、刷新
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        ClearUtils.getInstance().getAppProcessName(this);
        resetIp();
        setTitle("ip = " + baseUrl);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.setBgResource(R.drawable.bg_toast);
        startScreenBroadcastReceiver();
    }

    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(new ScreenBroadcastReceiver(), filter);
    }

    private void resetIp() {
        devolop = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/facecastDevelop/debug/app-develop-armeabi-v7a-debug.apk";
        preProducation = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/facecastPreProducation/debug/app-preProducation-armeabi-v7a-debug.apk";
        production_blue = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/facecastProduction_blue/debug/app-production_blue-armeabi-v7a-debug.apk";
        production = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/facecastProduction/debug/app-production-armeabi-v7a-debug.apk";
        release = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/facecastProduction/release/app-production-armeabi-v7a-release.apk";
        self = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/other/release/app-release.apk";
        imgs = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/img0";
    }

    /*zune: 将fromPosition，转移到toPosition, 缺位的顺次补上**/
    private void sort(List<String> mainTags, int fromPosition, int toPosition) {
        List<String> tempSrc = new ArrayList<>(mainTags);
        if (fromPosition < toPosition) {
            for (int i = 0; i < mainTags.size(); i++) {
                if (i == fromPosition) {
                    for (int j = 0; j < toPosition - fromPosition; j++) {
                        mainTags.set(i + j, tempSrc.get(i + j + 1));
                    }
                    mainTags.set(toPosition, tempSrc.get(fromPosition));
                    break;
                }
            }
        }
        if (fromPosition > toPosition) {
            for (int i = 0; i < mainTags.size(); i++) {
                if (i == toPosition) {
                    mainTags.set(toPosition, tempSrc.get(fromPosition));
                    for (int j = 0; j < fromPosition - toPosition; j++) {
                        mainTags.set(i + j + 1, tempSrc.get(i + j));
                    }
                    break;
                }
            }
        }
    }

    private void initMainTags() {
        String json = SPUtils.getInstance().getString("mMainTags");
        List<String> temp = GsonGetter.getInstance().getGson().fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        List<String> added = new ArrayList<>();
        added.add("阅读");
        added.add("数学");
        added.add("贝塞尔曲线");
        added.add("分贝");
        added.add("Google地图");
        added.add("删除冗余文件夹");
        added.add("通讯录");
        added.add("分享");
        added.add("解析excel");
        added.add("爬虫测试");
        added.add("电影");
        added.add("视频");
        added.add("歌曲");
        added.add("去水印");
        added.add("图片");
        added.add("读者");
        added.add("js存储");
        added.add("js读取");
        added.add("androidQ notify");
        added.add("加载圆图");
        added.add("sd卡");
        added.add("selenium测试");
        added.add("八爪鱼");
        added.add("翻译文案");
        added.add("下载app");
        added.add("设置ip");
        added.add("展开文本");
        added.add("测试");
        added.add("测试@#");
        added.add("应用题");
        added.add("超大图加载");
        added.add("超大图加载测试");
        added.add("视频转gif");
        added.add("铃声获取");
        added.add("音视频");
        added.add("相册");
        added.add("加密解密");
        added.add("数字滚动");
        added.add("动态9patch");
        added.add("扫一扫");
        added.add("生成二维码");
        if (temp != null && !temp.isEmpty() && temp.size() == added.size()) {
            mMainTags.addAll(temp);
        } else {
            mMainTags.addAll(added);
        }
    }

    private void onTagClick(String text) {
        switch (text) {
            case "阅读":
                startReadTxt();
                break;
            case "数学":
                MathFunActivity.start(MainActivity.this);
                break;
            case "贝塞尔曲线":
                BesarActivity.start(MainActivity.this);
                break;
            case "分贝": //"分贝");
                starFenbei();
                break;
            case "Google地图": //Google地图");
                LocationActivity.start(MainActivity.this);
                break;
            case "删除冗余文件夹": //删除冗余文件夹");
                deleteDir();
                break;
            case "通讯录": //通讯录");
                getConstants();
                break;
            case "分享": //分享");
                share();
                break;
            case "解析excel": //解析excel");
                parseXsl();
                break;
            case "爬虫测试": //爬虫测试");
                parseUrl();
                break;
            case "电影": //电影");
                if (new File(movieFile).exists()) {
                    WatchVideoActivity.start(MainActivity.this, movieFile);
                } else {
                    gzipFiles();
                }
                break;
            case "视频": //视频");
                WatchVideoActivity.start(MainActivity.this, avFile);
                break;
            case "歌曲": //歌曲");
                if (new File(musicFile).exists()) {
                    ListenMusicActivity.start(MainActivity.this, musicFile);
                } else {
                    gzipFiles();
                }
                break;
            case "去水印": //去水印");
                replaceBitmap();
                break;
            case "图片": //图片");
                lookPic();
                break;
            case "读者": //读者");
                ReaderListActivity.start(MainActivity.this);
                break;
            case "js存储": //js存储");
                saveJs();
                break;
            case "js读取": //js读取");
                readJs();
                break;
            case "androidQ notify": //androidQ notify");
                startNotifycationQ();
                break;
            case "加载圆图": //加载圆图");
                startLoadImg();
                break;
            case "sd卡": //sd卡");
                sdCard();
                break;
            case "selenium测试": //selenium测试");
                testSelenium();
                break;
            case "八爪鱼": //八爪鱼");
                if (Build.VERSION.SDK_INT >= 23) {
                    int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        return;
                    }
                }
                readXls();
                break;
            case "翻译文案": //翻译文案");
                startTranslate();
                break;
            case "下载app": //下载app");
                showDownload();
                break;
            case "设置ip": //设置ip");
                SetIpDialog setIpDialog = SetIpDialog.showDialog(this);
                setIpDialog.setOnResultListener(new SetIpDialog.OnResultListener() {
                    @Override
                    public void onResult(String ip) {
                        if (!TextUtils.isEmpty(ip)) {
                            baseUrl = ip.startsWith("http://") ? ip : "http://" + ip;
                            resetIp();
                            SPUtils.getInstance().put("baseUrl", baseUrl);
                        }
                    }
                });
                break;
            case "展开文本":
                ExpandActivity.start(this);
                break;
            case "测试@#":
                PushActivity.start(this);
                break;
            case "测试": //测试");
//                String test = "http://192.168.200.60/img0/%E6%97%A5%E6%9C%AC%E7%86%9F%E5%A6%87NatsukoShunga%E5%B0%8F%E5%A5%97%E5%9B%BE-2[20P]/%E6%97%A5%E6%9C%AC%E7%86%9F%E5%A6%87NatsukoShunga%E5%B0%8F%E5%A5%97%E5%9B%BE-2[20P]19.jpeg";
////                String test = "http://192.168.200.60/img0/0689%E6%9D%BE%E4%BA%95%E5%9C%A3%E5%A5%8828%E6%AD%B3%5B30P%5D/0689%E6%9D%BE%E4%BA%95%E5%9C%A3%E5%A5%8828%E6%AD%B3%5B30P%5D1.jpg";
//                GlideUtils.getInstance().loadUrl(test, findViewById(R.id.title), true, false);
//                File localCache = GlideUtils.getInstance().getLocalCache(this, test);
                Intent intent = new Intent(this, FromHelloWorld.class);
                startActivity(intent);
                break;
            case "应用题":
                WebWordProblemActivity.start(this);
                break;
            case "超大图加载":
                BigPictureActivity.start(this);
                break;
            case "超大图加载测试":
                BigTestPictureActivity.start(this);
                break;
            case "视频转gif":
                if (Build.VERSION.SDK_INT >= 23) {
                    int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                    int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA);
                    int recordPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECORD_AUDIO);
                    if (writePermission != PackageManager.PERMISSION_GRANTED
                            || readPermission != PackageManager.PERMISSION_GRANTED
                            || recordPermission != PackageManager.PERMISSION_GRANTED
                            || cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.RECORD_AUDIO
                                , Manifest.permission.CAMERA}, 1);
                        return;
                    }
                }
                VideoTurnGifActivity.start(this);
                break;
            case "铃声获取":
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(this, sound);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    if (sound.getPath() != null) {
                        ToastUtils.showShort(sound.getPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "音视频":
                if (Build.VERSION.SDK_INT >= 23) {
                    int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                    int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA);
                    int recordPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECORD_AUDIO);
                    if (writePermission != PackageManager.PERMISSION_GRANTED
                            || readPermission != PackageManager.PERMISSION_GRANTED
                            || recordPermission != PackageManager.PERMISSION_GRANTED
                            || cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.RECORD_AUDIO
                                , Manifest.permission.CAMERA}, 1);
                        return;
                    }
                }
                MediaActivity.start(this);
                break;
            case "相册":
                PhotoSheetDialog dialog = new PhotoSheetDialog();
                dialog.show(getSupportFragmentManager(), PhotoSheetDialog.class.getSimpleName());
                break;
            case "加密解密":
                try {
                    test.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "数字滚动":
                PollNumberActivity.start(this);
                break;
            case "动态9patch":
                NinePatchActivity.start(this);
                break;
            case "扫一扫":
                if (Build.VERSION.SDK_INT >= 23) {
                    int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA);
                    if (writePermission != PackageManager.PERMISSION_GRANTED
                            || cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA}, 1);
                        return;
                    }
                }
                CaptureActivity.start(MainActivity.this, REQUEST_CODE_SCAN);
                break;
            case "生成二维码":
                if (Build.VERSION.SDK_INT >= 23) {
                    int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA);
                    if (writePermission != PackageManager.PERMISSION_GRANTED
                            || cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA}, 1);
                        return;
                    }
                }
                QCodeDialog.showDialog(this);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SPUtils.getInstance().put("mMainTags", GsonGetter.getInstance().getGson().toJson(mMainTags));
    }

    private void showDownload() {
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        ArrayList<String> datas = new ArrayList<>();
        datas.add("facecastDevelop");
        datas.add("facecastPreProducation");
        datas.add("facecastProduction_blue");
        datas.add("facecastProduction");
        datas.add("release");
        clearHolder.stopLoad(datas, false);
        clearHolder.setOnItemClickListener(new ClearHolder.OnItemClickListener() {
            @Override
            public void onItemClick(String path, int position) {
                switch (path) {
                    case "facecastDevelop":
                        BrowserUtils.goToBrowser(MainActivity.this, devolop);
                        break;
                    case "facecastPreProducation":
                        BrowserUtils.goToBrowser(MainActivity.this, preProducation);
                        break;
                    case "facecastProduction_blue":
                        BrowserUtils.goToBrowser(MainActivity.this, production_blue);
                        break;
                    case "facecastProduction":
                        BrowserUtils.goToBrowser(MainActivity.this, production);
                        break;
                    case "release":
                        BrowserUtils.goToBrowser(MainActivity.this, release);
                        break;
                    default:
                        BrowserUtils.goToBrowser(MainActivity.this, self);
                        break;
                }
                clearHolder.dismiss();
            }
        });
    }

    private void startTranslate() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        File fileName = FileUtil.assetsToFile(this, "待翻译文案.xls");
        Map<String, List<List<String>>> map = null;
        if (fileName != null) {
            map = ExcelManager.getInstance().analyzeXls(fileName.getPath());
        }
        if (map == null || map.isEmpty()) {
            map = ExcelManager.getInstance().analyzeXlsx(fileName);
        }
//        [{xxx:"xxx", "xx":xx}, {yyy:"yyy", yy:"yy"}]
//        {xxx:"xxx", xxx:"xxx"}
        for (String key : map.keySet()) {
            List<List<String>> lists = map.get(key);
            if (lists == null) {
                return;
            }
            List<LanguageWords> languageWords = new ArrayList<>();
            for (int i = 1; i < lists.size(); i++) {
                List<String> strings = lists.get(i);
                LanguageWords languageWord = new LanguageWords();
                for (int j = 2; j < strings.size(); j++) {
                    LanguageWords.KeyWord keyWord = new LanguageWords.KeyWord();
                    keyWord.key = strings.get(0);
                    keyWord.word = strings.get(j);
                    switch (j) {
                        case 2:
                            languageWord.rCN = keyWord;
                            break;
                        case 3:
                            languageWord.en = keyWord;
                            break;
                        case 4:
                            languageWord.ar = keyWord;
                            break;
                        case 5:
                            languageWord.de = keyWord;
                            break;
                        case 6:
                            languageWord.es = keyWord;
                            break;
                        case 7:
                            languageWord.fr = keyWord;
                            break;
                        case 8:
                            languageWord.hi = keyWord;
                            break;
                        case 9:
                            languageWord.in = keyWord;
                            break;
                        case 10:
                            languageWord.it = keyWord;
                            break;
                        case 11:
                            languageWord.ja = keyWord;
                            break;
                        case 12:
                            languageWord.ko = keyWord;
                            break;
                        case 13:
                            languageWord.pt = keyWord;
                            break;
                        case 14:
                            languageWord.ru = keyWord;
                            break;
                        case 15:
                            languageWord.th = keyWord;
                            break;
                        case 16:
                            languageWord.vi = keyWord;
                            break;
                        case 17:
                            languageWord.rHK = keyWord;
                            break;
                    }
                }
                languageWords.add(languageWord);
            }
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    appendXml(languageWords);
                    BaseApplication.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            clearHolder.stopLoad(new ArrayList<>(), false);
                        }
                    });
                }
            }.start();
            if (clearHolder == null) {
                clearHolder = new ClearHolder(findViewById(R.id.clear_root));
                clearHolder.startLoad();
            }
        }
    }

    private void appendXml(List<LanguageWords> languageWords) {
        try {
            for (LanguageWords languageWord : languageWords) {
                for (int i = 2; i < 18; i++) {
                    switch (i) {
                        case 2:
                            startWrite(languageWord.rCN, "values-zh-rCN");
                            break;
                        case 3:
                            startWrite(languageWord.en, "values");
                            break;
                        case 4:
                            startWrite(languageWord.ar, "values-ar");
                            break;
                        case 5:
                            startWrite(languageWord.de, "values-de");
                            break;
                        case 6:
                            startWrite(languageWord.es, "values-es");
                            break;
                        case 7:
                            startWrite(languageWord.fr, "values-fr");
                            break;
                        case 8:
                            startWrite(languageWord.hi, "values-hi");
                            break;
                        case 9:
                            startWrite(languageWord.in, "values-in");
                            break;
                        case 10:
                            startWrite(languageWord.it, "values-it");
                            break;
                        case 11:
                            startWrite(languageWord.ja, "values-ja");
                            break;
                        case 12:
                            startWrite(languageWord.ko, "values-ko");
                            break;
                        case 13:
                            startWrite(languageWord.pt, "values-pt");
                            break;
                        case 14:
                            startWrite(languageWord.ru, "values-ru");
                            break;
                        case 15:
                            startWrite(languageWord.th, "values-th");
                            break;
                        case 16:
                            startWrite(languageWord.vi, "values-vi");
                            break;
                        case 17:
                            startWrite(languageWord.rHK, "values-zh-rHK");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startWrite(LanguageWords.KeyWord keyWord, String country) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/words/" + country + "/strings.xml");
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        StringBuilder temp = new StringBuilder();
        while (true) {
            String s = fileReader.readLine();
            if (s == null) {
                break;
            }
            temp.append(s).append("\n");
        }
        temp = new StringBuilder(temp.toString().trim());
        int index = temp.toString().lastIndexOf("\n");
        String normal = temp.toString().substring(0, index);
        String last = temp.toString().substring(index, temp.length());
        FileWriter fos = new FileWriter(file, false);
        String key = keyWord.key;
        String value = resetWord(keyWord.word);
        String string = String.format("    <string name=\"%s\">%s</string>", key, value);
        String[] split = normal.split("\n");
        for (String s : split) {
            fos.write(s);
            fos.write("\n");
        }
        fos.write(string);
        fos.write(last);
        fileReader.close();
        fos.close();
    }

    private String resetWord(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '\'') {
                sb.append("\\");
            }
            sb.append(word.charAt(i));
        }
        return sb.toString().replaceAll("XXX", "%s");
    }

    private void startDownload() {
        if (new File(movieFile).exists()
                && new File(musicFile).exists()
                && new File(avFile).exists()) {
            return;
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        new Thread() {
            @Override
            public void run() {
                super.run();
                File file = new File(baseDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                startDownload(musicUrl, musicFile, () -> startDownload(movieUrl, movieFile, () -> startDownload(avUrl, avFile, () -> {
                    Disposable subscribe = Observable.just(1).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(integer -> clearHolder.dismiss());
                })));
            }
        }.start();
    }

    private void startDownload(String url, String file, CallBack callBack) {
        DownloadObjManager.getInstance().startDownload(url, file, callBack);
    }

    public interface CallBack {
        void onCallBack();
    }

    private void readXls() {
        ListDialog<ListDialog.BaseData> listDialog = ListDialog.showDialog(MainActivity.this, true);
        String assetsZhilian = "企业最新招聘信息_求职信息_找工作上智联招聘.xlsx";
        String assetsBoss = "「郑州招聘信息」郑州招聘网 - BOSS直聘.xlsx";
        String assetsLiepin = "【郑州招聘信息_郑州招聘_郑州招聘网】-郑州猎聘.xlsx";
        String assets51 = "【郑州,android招聘，求职】-前程无忧.xlsx";
        readAssets(assetsZhilian, assetsBoss, assetsLiepin, assets51, new SimpleObserver<List<ListDialog.BaseData>, ListDialog<ListDialog.BaseData>>(listDialog, false) {
            @Override
            public void onNext(List<ListDialog.BaseData> datas, ListDialog<ListDialog.BaseData> listDialog) {
                listDialog.showWithData(datas, false);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                LogUtils.i("zune：", "e = " + e);
            }
        });
    }

    private void readAssets(String assetsZhilian, String assetsBoss, String assetsLiepin, String assets51, SimpleObserver<List<ListDialog.BaseData>, ListDialog<ListDialog.BaseData>> observer) {
        Observable.just(new String[]{assetsZhilian, assetsBoss, assetsLiepin, assets51}).map(new Function<String[], List<ListDialog.BaseData>>() {
            @Override
            public List<ListDialog.BaseData> apply(String[] assets) throws Exception {
                Map<String, List<List<String>>> map = ExcelManager.getInstance().getStringListMap(MainActivity.this, assets);
                List<Company> newData = ExcelManager.getInstance().getData(Company.class, map);
                statisticsTime(newData);
                List<Company> localData = GsonGetter.getInstance().getGson().fromJson(ExcelManager.getInstance().getJson("localData.json", MainActivity.this), new TypeToken<List<Company>>() {
                }.getType());
                UpdateCompanyBean updateCompanyBean = insertToLocal(newData, localData);
                String update = "\"" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(System.currentTimeMillis())) + "\":" + GsonGetter.getInstance().getGson().toJson(updateCompanyBean);
                writeToPrivate(update, "updateCompanyBean.txt");
                String localDataJson = GsonGetter.getInstance().getGson().toJson(localData);
                writeToPrivate(localDataJson, "localData.txt");
                List<ListDialog.BaseData> datas = new ArrayList<>();
                ListDialog.BaseData data = new ListDialog.BaseData("新增:" + updateCompanyBean.addCount + " # 重合:"
                        + updateCompanyBean.repeatCount + " # 过期:" + updateCompanyBean.timeLimitCount
                        + " # min:" + updateCompanyBean.minMoney + " # max:" + updateCompanyBean.maxMoney);
                datas.add(data);
                for (int i = 0; i < localData.size(); i++) {
                    Company companyCount = localData.get(localData.size() - 1 - i);
                    companyCount.text = companyCount.company + " # " + companyCount.money + " # "
                            + (TextUtils.isEmpty(companyCount.address) ? "郑州" : companyCount.address) + " :"
                            + (TextUtils.isEmpty(companyCount.repeatCount) ? "新" : companyCount.repeatCount);
                    datas.add(companyCount);
                }
                return datas;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void writeToPrivate(String text, String path) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    File file = new File(getCacheDir(), path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    osw.write(text);
                    osw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private UpdateCompanyBean insertToLocal(List<Company> newData, List<Company> localData) {
        UpdateCompanyBean bean = new UpdateCompanyBean();
        int totalCount = localData.size();
        int moneyCount = 0;
        float minMoney = 0;
        float maxMoney = 0;
        for (Company company : newData) {
            if (!(company.os.contains("Android") || company.os.contains("android") || company.os.contains("安卓") || company.os.contains("app") || company.os.contains("APP")
                    || company.os.contains("移动") || company.os.contains("flutter") || company.os.contains("Flutter") || company.os.contains("逆向"))) {
                continue;
            }
            int index = contains(localData, company);
            if (index == -1) {
                Company temp = new Company();
                temp.os = company.os;
                temp.address = company.address.split("\\|")[0].trim();
                String money = company.money;
                float[] moneys = ExcelManager.getInstance().isMoneyEnable(money);
                if (moneys == null || moneys.length == 1 || moneys[1] < 11) {
                    continue;
                }
                temp.money = money;
                temp.company = company.company;
                temp.minMoney = moneys[0];
                temp.maxMoney = moneys[1];
                temp.timeLimit = false;
                localData.add(temp);
                bean.addCount++;
                moneyCount++;
                minMoney += temp.minMoney;
                maxMoney += temp.maxMoney;
            } else {
                totalCount--;
                Company indexCompany = localData.get(index);
                String repeatCount = indexCompany.repeatCount;
                int count = 0;
                if (!TextUtils.isEmpty(repeatCount)) {
                    try {
                        count = Integer.parseInt(repeatCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                indexCompany.repeatCount = String.valueOf(++count);
                indexCompany.timeLimit = false;
                localData.set(index, indexCompany);
                bean.repeatCount++;
                moneyCount++;
                minMoney += indexCompany.minMoney;
                maxMoney += indexCompany.maxMoney;
            }
        }
        bean.timeLimitCount = Math.max(totalCount, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            localData.sort((t1, t2) -> (TextUtils.isEmpty(t2.repeatCount) ? 0 : Integer.parseInt(t2.repeatCount)) - (TextUtils.isEmpty(t1.repeatCount) ? 0 : Integer.parseInt(t1.repeatCount)));
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        bean.minMoney = decimalFormat.format(minMoney / moneyCount);
        bean.maxMoney = decimalFormat.format(maxMoney / moneyCount);
        return bean;
    }

    private int contains(List<Company> localData, Company company) {
        for (int i = 0; i < localData.size(); i++) {
            Company companyLocal = localData.get(i);
            if (ObjectUtils.equals(companyLocal.company.trim(), company.company.trim())) {
                return i;
            }
        }
        return -1;
    }

    //北京
//    private final String lastData = "[\"北京独创时代科技有限公司 # 10K-15K # android开发工程师 :727\",\"小米通讯技术有限公司 # 20K-30K # Linux/android相机驱动工程师-小米电视 :725\",\"百度在线网络技术(北京)有限公司 # 15K-20K # 企业智能平台部_android开发工程师 :582\",\"易才集团 # 10K-15K # android开发工程师 :435\",\"深圳市法本信息技术股份有限公司 # 13K-16K # android开发工程师 :297\",\"深圳市腾讯计算机系统有限公司 # 薪资面议 # 26699- 智慧零售android 开发工程师 :293\",\"京东方科技集团 # 20K-30K # android应用资深开发工程师(J23618) :291\",\"江苏润和软件股份有限公司 # 15K-20K # android开发工程师 :290\",\"联想集团 # 薪资面议 # android 多媒体开发工程师 :290\",\"柯锐特互动(北京)科技有限公司 # 15K-20K # android开发工程师 :290\",\"苍穹数码技术股份有限公司 # 15K-30K # 高级android开发工程师 :290\",\"北京巨榴莲科技有限公司 # 20K-30K # android开发工程师 :290\",\"北京腾信软创科技股份有限公司 # 10K-15K # android :290\",\"中信网络科技股份有限公司 # 10K-15K # 中级android开发工程师 :290\",\"北京红棉小冰科技有限公司 # 20K-30K # android SDK开发工程师 :290\",\"中软国际科技服务有限公司 # 薪资面议 # android开发工程师 :150\",\"北京汉克时代科技有限公司 # 10K-15K # android开发工程师 :148\",\"诚迈科技(南京)股份有限公司 # 11K-22K # android开发工程师 :147\",\"北京捷通华声科技股份有限公司 # 15K-25K # android开发工程师 :146\",\"中移雄安信息通信科技有限公司 # 薪资面议 # android 开发工程师 :145\",\"北京金色华勤数据服务有限公司 # 15K-25K # android高级开发工程师 :145\",\"北京恒天财富投资管理有限公司 # 15K-30K # android高级开发工程师 :145\",\"智联RPO # 薪资面议 # android开发工程师 :145\",\"人民网股份有限公司 # 18K-23K # android开发工程师 :145\",\"北京联合盈鑫信息技术有限公司 # 15K-25K # android开发工程师 :145\",\"聚民惠贸易有限公司 # 10K-16K # android开发工程师 :145\",\"中国电子系统技术有限公司 # 15K-30K # android开发工程师 :145\",\"鸿合科技股份有限公司 # 15K-20K # android系统开发工程师 :145\",\"汽车之家 # 20K-30K # android开发工程师 :145\",\"北京狸米科技有限公司 # 25K-35K # android高级开发工程师 :145\",\"北京品高辉煌科技有限责任公司 # 10K-15K # 安卓（android）开发工程师-中级 :145\",\"中科创达软件股份有限公司 # 15K-20K # android Modem开发工程师 :145\",\"纬创软件(北京) # 10K-15K # android开发工程师 :145\",\"完美世界(北京)软件有限公司 # 15K-25K # android开发工程师 :145\",\"比亚迪股份有限公司 # 13K-26K # android升级包制作工程师 :145\",\"东华软件 # 薪资面议 # 中级android开发工程师 :145\",\"北京同仁堂健康药业股份有限公司 # 20K-30K # android开发高级工程师 :145\",\"北京中油瑞飞信息技术有限责任公司 # 15K-20K # 软件开发工程师（android移动开发） :145\",\"大唐移动通信设备有限公司 # 薪资面议 # android开发工程师 :145\",\"创维集团有限公司 # 薪资面议 # android应用开发工程师 :145\",\"敦煌网 # 12K-20K # 高级android工程师-互联网电商 :145\",\"北京石头世纪科技股份有限公司 # 15K-30K # android 开发工程师 :145\",\"厦门唐普信息技术有限公司 # 15K-20K # android开发工程师 :145\",\"象蚁(北京)科技有限公司 # 12K-15K # android开发工程师 :145\",\"徐州英普瑞斯文化传媒有限公司 # 15K-20K # android开发工程师 :145\",\"北京琥珀创想科技有限公司 # 6K-8K # android开发实习生 :145\",\"北京酷得少年科技有限公司 # 20K-40K # android开发工程师 :145\",\"维恩贝特科技有限公司 # 8K-15K # android客户端开发工程师 :145\",\"广州碧软信息科技有限公司 # 10K-20K # android开发工程师 :145\",\"北京智汇盈科信息工程有限公司 # 10K-15K # android软件工程师 :145\",\"北京海创高科科技有限公司 # 10K-20K # android开发工程师 :145\",\"北京朗视仪器有限公司 # 10K-15K # android开发工程师（兼职） :145\",\"北京中园搏望科技发展有限公司 # 10K-20K # 高级android开发工程师 :145\",\"戴姆勒大中华区投资有限公司 # 薪资面议 # android and Linux System Engineer 安卓和Linux系统工程师 :145\",\"北京天龟教育科技有限公司 # 10K-15K # android开发工程师 :145\",\"易车公司 # 20K-40K # android高级研发工程师（数据采集方向）(J10954) :145\",\"北京晋辉科技有限公司 # 10K-15K # android开发工程师 :145\",\"天信达信息技术有限公司 # 8K-12K # android开发工程师 :145\",\"北京趣加科技有限公司 # 23K-40K # android（APP/SDK）工程师 :145\",\"北方天途航空技术发展(北京)有限公司 # 8K-15K # android开发工程师 :145\",\"北京金控数据技术股份有限公司 # 10K-15K # android开发工程师 :145\",\"北京古德兆伯咨询有限公司 # 15K-20K # android开发工程师 :145\",\"北京永生鼎立信息技术有限公司 # 15K-20K # android开发工程师 :145\",\"北京中科建友科技股份有限公司 # 10K-15K # android开发工程师    lmx :145\",\"贝塔智能科技(北京)有限公司 # 15K-25K # 高级android开发工程师 :145\",\"北京合胜易达科技有限公司 # 10K-15K # 软件android开发工程师（中、高级） :145\",\"北京字节跳动科技有限公司 # 25K-50K # android研发工程师 — 抖音/抖音火山版/直播 :23\",\"软通动力信息技术(集团)有限公司 # 15K-20K # android高级研发工程师 :14\",\"北京瑞友科技股份有限公司 # 10K-15K # android开发工程师 :10\",\"美团点评（中国大陆地区） # 薪资面议 # 美团搜索_android高级开发工程师 :5\",\"上海华钦信息科技股份有限公司 # 15K-20K # android开发工程师 :5\",\"天宇正清科技有限公司 # 10K-15K # android开发 :3\",\"阿里巴巴集团 # 薪资面议 # 创新事业群融媒体发展事业部-android开发专家.Y-北京 :2\",\"北京聚点艺盛文化传播有限责任公司 # 10K-15K # 初中级android工程师 :2\",\"北京奇客创想科技股份有限公司 # 12K-18K # iOS\\u0026android开发工程师 :2\",\"深圳市拓保软件有限公司 # 10K-15K # android开发工程师 :2\",\"乐普(北京)医疗器械股份有限公司 # 10K-20K # 高级android工程师 :2\",\"中科软科技股份有限公司 # 6K-10K # android工程师 :2\",\"北京奥鹏远程教育中心有限公司 # 2K-4K # android教学实习生 :2\",\"浩鲸新智能科技股份有限公司 # 30K-50K # 阿里巴巴前端专家(android、iOS、H5) :2\",\"北京视游互动科技有限公司 # 15K-25K # android开发工程师（教育） :2\",\"北京牧家科技有限公司 # 15K-25K # android开发工程师 :1\",\"北京搜房互联网信息服务有限公司/北京搜房网络技术有限公司／北京搜房科技发展有限公司 # 10K-15K # android高级研发工程师 :1\",\"北京六智信息技术股份有限公司 # 10K-12K # android安卓开发工程师 :1\",\"山东康威通信技术股份有限公司 # 10K-15K # android开发工程师 :1\",\"北京立诚拓业科技有限公司 # 10K-15K # android 工程师 :1\",\"中图云创智能科技(北京)有限公司 # 15K-25K # android开发工程师 :1\",\"中航材导航技术(北京)有限公司 # 15K-20K # android高级开发工程师 :1\",\"北京风行在线技术有限公司 # 20K-25K # android开发工程师 :1\",\"北京亿彩众邦科技有限公司 # 10K-15K # android开发工程师 :1\",\"广东一一五科技股份有限公司 # 12K-18K # android中级开发工程师 :1\",\"山东新北洋信息技术股份有限公司北京分公司 # 20K-30K # android工程师 :1\",\"纳恩博(北京)科技有限公司 # 20K-35K # android开发工程师 :1\",\"北京通泰信诚科技有限公司 # 10K-20K # android开发工程师 :1\",\"国科政信科技(北京)股份有限公司 # 15K-18K # android 工程师 :1\",\"北京平治东方科技股份有限公司 # 10K-15K # android开发工程师 :1\",\"北京讯通安添通讯科技有限公司 # 15K-20K # android系统多媒体模块开发 :1\",\"北京天酷信诚网络科技有限公司 # 8K-10K # android开发工程师 :1\",\"北京微美云息软件有限公司 # 8K-10K # android开发工程师 :1\",\"聚信互联（北京）科技有限公司 # 11K-20K # android开发工程师 :1\",\"北京多闻有道文化传媒有限公司 # 15K-20K # android开发工程师 :1\",\"北京诵读文化发展有限公司 # 10K-15K # android工程师 :1\",\"北京翔云在线数据技术有限公司 # 10K-15K # android开发工程师 :1\",\"北京指南科技有限公司 # 15K-25K # android开发工程师 :1\",\"北京明策智数科技有限公司 # 10K-18K # android中级开发工程师 :1\",\"北软互联(北京)科技有限公司 # 10K-15K # android安卓开发工程师-北京-00017 :1\",\"北京金榜苑科技有限公司 # 15K-30K # android开发工程师 :1\",\"心韵恒安医疗科技(北京)有限公司 # 10K-15K # android研发工程师 :1\",\"上海赛连信息科技有限公司 # 15K-25K # android app开发工程师-北京 :1\",\"北京地拓科技发展有限公司 # 10K-15K # android开发工程师 :1\",\"北京三信时代信息公司 # 10K-20K # android即时通信开发工程师 :1\",\"浙江瑞华康源科技有限公司北京分公司 # 15K-20K # android BSP开发工程师 :1\",\"北京大医云慈医疗科技有限公司 # 10K-20K # android开发工程师 :1\",\"北京众成天极信息技术有限责任公司 # 14K-15K # android开发工程师 :1\",\"大唐半导体科技有限公司 # 15K-20K # android/Linux系统工程师 :1\",\"北京百家互联科技有限公司 # 10K-20K # android开发工程师-校招职位 :1\",\"北京中公教育科技有限公司 # 15K-30K # IT培训讲师（android） :1\",\"北京雍禾医疗投资管理有限公司 # 10K-15K # android开发工程师 :1\",\"苏州方位通讯科技有限公司北京分公司 # 9K-18K # android软件开发工程师 :1\",\"创而新(北京)教育科技有限公司 # 10K-16K # android应用开发 :1\",\"北京世纪超星信息技术发展有限责任公司 # 8K-15K # android客户端开发（JAVA） :1\",\"维豪集团 # 18K-28K # android开发工程师 :1\",\"北京凤凰学易科技有限公司 # 15K-25K # android开发工程师（全栈） :1\",\"北京环球国广媒体科技有限公司 # 15K-20K # android开发工程师(J10601) :1\",\"纳络维网络技术(北京)有限公司 # 80K-130K # android开发工程师 :1\",\"华晨宝马汽车有限公司 # 薪资面议 # Manager Product Digital PoC MyCar android :1\",\"贝乐英语 # 15K-20K # android开发工程师 :1\",\"广联达科技股份有限公司 # 20K-30K # android高级开发工程师 (MJ003222) :1\",\"北京东大正保科技有限公司 # 15K-25K # android开发工程师-有Flutter开发经验 :1\",\"北京人瑞人力资源服务有限公司 # 8K-10K # android 开发工程师 :1\",\"航天宏图信息技术股份有限公司 # 10K-18K # android初级工程师 :1\",\"青岛青软锐芯电子科技有限公司 # 10K-15K # android开发工程师 :1\",\"华夏银行股份有限公司信用卡中心 # 薪资面议 # android开发 :1\",\"北京鑫美网络科技有限公司 # 8K-10K # android开发工程师 :1\",\"银河互联网电视有限公司 # 15K-25K # 高级android工程师 :1\",\"北京银河创想信息技术有限公司 # 11K-16K # android开发工程师 :1\",\"北京金英杰教育科技有限公司 # 12K-16K # android开发工程师 :1\",\"北京怡生乐居信息服务有限公司 # 15K-20K # android开发工程师 :1\",\"世纪佳缘 # 2K-4K # 客户端开发（android，iOS 和 前端）实习生 :1\",\"文思海辉技术有限公司PacteraTechnologyInternationalLimited # 薪资面议 # android开发工程师 :1\",\"北京达佳互联信息技术有限公司 # 20K-30K # android开发工程师-【快手APP】 :1\",\"北京宇信科技集团股份有限公司 # 10K-15K # android开发工程师 :1\",\"北京鼎普科技股份有限公司 # 15K-20K # android开发工程师 :1\",\"东软云科技有限公司 # 8K-15K # android开发工程师 :1\",\"北京京天威科技发展有限公司 # 10K-15K # 高级android开发工程师 :1\",\"传知(北京)教育科技有限公司 # 70K-100K # android开发工程师 :1\",\"北京仕邦达人力资源服务有限公司上海分公司 # 20K-40K # android开发工程师 :1\",\"北京师范大学 # 10K-15K # android研发工程师 :1\",\"万方数据股份有限公司 # 10K-15K # android中级开发 (MJ000051) :1\",\"北京天鹏恒宇科技发展有限公司 # 8K-15K # android研发工程师 :1\",\"首聘(天津)信息科技有限公司 # 9K-15K # android开发工程师 :1\",\"盈科美辰国际旅行社有限公司 # 15K-25K # android开发工程师 :1\",\"北京芯盾集团有限公司 # 15K-30K # android 开发工程师 :1\",\"神州通誉软件(上海)股份有限公司 # 15K-20K # android开发工程师（外派一线互联网公司） :1\",\"北京电旗通讯技术股份有限公司 # 10K-16K # 中级android工程师 :1\",\"中教未来国际教育科技(北京)有限公司 # 10K-15K # android开发工程师 :1\",\"大连斯锐信息技术有限公司 # 15K-20K # android开发工程师 :1\"]";

    /*zune:
     * 北京：21.58 - 38.12
     * 上海：19.65 - 32.68
     * 深圳：16.61 - 28.71
     * 广州： 11.97 - 19.00
     * 杭州：19.04 - 32.64
     * 苏州：10.71 - 16.38
     * 成都：10.86 - 16.86
     * 重庆：8.12 - 12.90
     * 武汉：10.24 - 16.53
     * 郑州：8.31 - 12.41
     * **/

    /*zune: 统计文字出现的次数**/
    private void statisticsTime(List<Company> companyCounts) {
        Observable.just(companyCounts).map(new Function<List<Company>, Map<String, Integer>>() {
            @Override
            public Map<String, Integer> apply(List<Company> strings) throws Exception {
                long time = System.currentTimeMillis();
                Map<String, Integer> temp = new HashMap<>();
                temp.put("flutter", 0);
                temp.put("kotlin", 0);
                temp.put("jni", 0);
                temp.put("python", 0);
                temp.put("自定义", 0);
                temp.put("通信", 0);
                temp.put("im", 0);
                temp.put("网络协议", 0);
                temp.put("framework", 0);
                temp.put("底层", 0);
                temp.put("ndk", 0);
                temp.put("react", 0);
                temp.put("javascript", 0);
                temp.put("内存", 0);
                temp.put("rxjava", 0);
                temp.put("glide", 0);
                temp.put("retrofit", 0);
                temp.put("xposed", 0);
                temp.put("sqlite", 0);
                temp.put("socket", 0);
                temp.put("屏幕", 0);
                temp.put("适配", 0);
                temp.put("算法", 0);
                temp.put("性能", 0);
                temp.put("组件", 0);
                temp.put("插件", 0);
                for (Company string : strings) {
                    Set<Map.Entry<String, Integer>> entries = temp.entrySet();
                    for (Map.Entry<String, Integer> entry : entries) {
                        if (string.scale != null && string.scale.contains(entry.getKey().toLowerCase())) {
                            Integer value = temp.get(entry.getKey());
                            if (value == null) {
                                value = 0;
                            }
                            temp.put(entry.getKey(), value + 1);
                        }
                    }
                }
                Log.i("zune", "time = " + (System.currentTimeMillis() - time));
                List<KeyWords> keyWords = new ArrayList<>();
                for (String s : temp.keySet()) {
                    if (temp.get(s) == null || temp.get(s) < 5) {
                        continue;
                    }
                    KeyWords keyWord = new KeyWords();
                    keyWord.keyWord = s;
                    keyWord.time = temp.get(s);
                    keyWords.add(keyWord);
                }
                Collections.sort(keyWords, new Comparator<KeyWords>() {
                    @Override
                    public int compare(KeyWords o1, KeyWords o2) {
                        return o2.time - o1.time;
                    }
                });
                String s = GsonGetter.getInstance().getGson().toJson(keyWords);
                Log.i("zune", s);
                return temp;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    private void testSelenium() {
        WebViewUtils.getContentFromUrl(this, "http://list.iqiyi.com/www/2/-------------24-1-1-iqiyi--.html");
    }

    private void sdCard() {
        if (!DocumentsFileUtils.getInstance().hasPermissions(this)) {
            return;
        }
        readSdCard();
    }


    /**
     * Write to sd card.
     *
     * @param extraPath the extra path, 除了根目录之外的全路径
     */
    private void writeToSdCard(String extraPath) {
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return;
        }
        File file = new File(DocumentsFileUtils.getInstance().rootPath[DocumentsFileUtils.getInstance().rootPath.length - 1] + separator + extraPath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write("测试".getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write to sd card.
     *
     * @param extraPath the extra path, 除了根目录之外的全路径
     */
    private void writeDocumentToSdCard(String extraPath, @DocumentsFileUtils.NormalMimeType String mimeType) {
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return;
        }
        DocumentFile documentFile = DocumentsFileUtils.getInstance().getUriDocumentFile(DocumentsFileUtils.getInstance().rootPath[DocumentsFileUtils.getInstance().rootPath.length - 1]);
        String[] parts = extraPath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = documentFile.findFile(parts[i]);
            if (nextDocument == null) {
                if ((i < parts.length - 1)) {
                    nextDocument = documentFile.createDirectory(parts[i]);
                } else {
                    nextDocument = documentFile.createFile(mimeType, parts[i]);
                }
            }
            documentFile = nextDocument;
        }
    }

    private void readSdCard() {
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentsFileUtils.getInstance().showOpenDocumentTree(this);
            if (!DocumentsFileUtils.getInstance().hasUriTree(DocumentsFileUtils.getInstance().rootPath[DocumentsFileUtils.getInstance().rootPath.length - 1])) {
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        Observable.just(1).map(new Function<Integer, List<String>>() {
            @Override
            public List<String> apply(Integer integer) throws Exception {
                List<String> strings = getWechatFile("tencent/MicroMsg");
                copyToWeixin(strings);
                return strings;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<String> strings) {
                        clearHolder.stopLoad(strings, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void copyToWeixin(List<String> strings) {
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return;
        }
        for (String string : strings) {
            String[] split = string.split(", ");
            String path = split[0];
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            if (System.currentTimeMillis() - file.lastModified() < 7 * 24 * 3600 * 1000L) {
                Log.i("zune", file.getPath());
                continue;
            }
            String fileType = getType(file);
            if (TextUtils.isEmpty(fileType)) {
                continue;
            }
            String root = DocumentsFileUtils.getInstance().rootPath[DocumentsFileUtils.getInstance().rootPath.length - 1];
            String type = split[split.length - 1];
            String descPath = root + separator + "weixin" + separator + type + separator + file.getName() + (file.getName().contains(".") ? "" : fileType);
            File descDir = new File(root + separator + "weixin" + separator + type);
            if (!descDir.exists()) {
                descDir.mkdirs();
            }
            File desc = new File(descPath);
            FileUtil.copyFile(file, desc);
            /*@DocumentsFileUtils.NormalMimeType String mimeType = getMimeType(fileType);
            DocumentsFileUtils.copyFile(this, file, DocumentsFileUtils.getInstance().fileToDocument(desc, false, this), mimeType, descPath);*/
        }
    }

    private @DocumentsFileUtils.NormalMimeType
    String getMimeType(String fileType) {
        switch (fileType) {
            case ".png":
                return DocumentsFileUtils.IMAGE_TYPE;
            case ".mp4":
                return DocumentsFileUtils.VIDEO_TYPE;
            case ".mp3":
                return DocumentsFileUtils.VOICE_TYPE;
            case ".txt":
                return DocumentsFileUtils.TXT_TYPE;
        }
        return null;
    }

    private String getType(File file) {
        String fileType = getFileType(file);
        switch (fileType) {
            case "图片":
                return ".png";
            case "视频":
                return ".mp4";
            case "音频":
                return ".mp3";
            case "文本":
                return ".txt";
        }
        return "";
    }

    private List<String> getWechatFile(@NotNull String path) {
        ArrayList<String> strings = new ArrayList<>();
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return strings;
        }
        File rootFile = DocumentsFileUtils.getInstance().documentToFile(
                DocumentsFileUtils.getInstance().getUriDocumentFile(
                        DocumentsFileUtils.getInstance().rootPath[0]
                )
        );
        if (rootFile != null && rootFile.exists() && rootFile.isDirectory()) {
            strings = getListFile(strings, new File(rootFile.getPath() + separator + path));
        }
        return strings;
    }

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ArrayList<String> getListFile(List<String> strings, File rootFile) {
        if (rootFile != null && rootFile.exists() && rootFile.isDirectory()) {
            for (File file : rootFile.listFiles()) {
                if (file.isDirectory()) {
                    getListFile(strings, file);
                } else if (file.length() > 1024 * 10) {
                    long createTime;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        createTime = getCreateTime(file);
                    } else {
                        createTime = file.lastModified();
                    }
                    String createFileTime = simpleDateFormat.format(new Date(createTime));
                    float f = file.length() / 1024f / 1024f;
                    DecimalFormat df = new DecimalFormat("######0.00");
                    String s = df.format(f);
                    String type = getFileType(file);
                    strings.add(String.format("%s, %s, %s, %s", file.getPath(), s + "M", createFileTime, type));
                }
            }
        }
        return new ArrayList<>(strings);
    }

    private String getFileType(File file) {
        if (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".gif") || file.getPath().endsWith(".png")
                || file.getPath().contains("image")) {
            return "图片";
        }
        if (file.getPath().endsWith(".mp4") || file.getPath().endsWith(".m3u8") || file.getPath().endsWith(".avi")
                || file.getPath().contains("video")) {
            return "视频";
        }
        if (file.getPath().endsWith(".mp3") || file.getPath().endsWith(".amr") || file.getPath().endsWith(".wmv")
                || file.getPath().contains("voice")) {
            return "音频";
        }
        if (file.getPath().endsWith(".doc") || file.getPath().endsWith(".docx") || file.getPath().endsWith(".txt")
                || file.getPath().contains("document")) {
            return "文本";
        }
        return "其它";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private long getCreateTime(File file) {
        try {
            Path path = Paths.get(file.getPath());
            BasicFileAttributeView basicview = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            BasicFileAttributes attr = basicview.readAttributes();
            return attr.creationTime().toMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return file.lastModified();
        }
    }

    private void startLoadImg() {
        startActivity(new Intent(this, ImageViewActivity.class));
    }

    private void startNotifycationQ() {
        TestNotifyService.start(this);
    }

    private void readJs() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 实现8倍缓存
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        webView.getSettings().setAppCachePath(path);
        webView.getSettings().setDatabaseEnabled(true);
        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:getData()");
            }
        };
        webView.setWebViewClient(client);
        webView.loadUrl("file:///android_asset/javaScript.html");
        webView.addJavascriptInterface(new AppClass(this), "android");
    }

    private void saveJs() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 实现8倍缓存
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        webView.getSettings().setAppCachePath(path);
        webView.getSettings().setDatabaseEnabled(true);
        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:saveData('123')");
            }
        };
        webView.setWebViewClient(client);
        webView.loadUrl("file:///android_asset/javaScript.html");
        webView.addJavascriptInterface(new AppClass(this), "android");
    }

    private void lookPic() {
        PictureActivity.start(this, imageDir, imgs);
    }

    private void replaceBitmap() {
        File file = new File(baseDir + separator + "old_file.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
        if (bitmap == null) {
            return;
        }
        Bitmap replaceBitmapColor = BitmapUtil.replaceBitmapColor(bitmap, "#FFD02222", "#FFFFFF01");

        File newFile = new File(baseDir + separator + "new_file.jpg");
        if (!newFile.exists()) {
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(newFile);
            replaceBitmapColor.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void gzipFiles() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        long time = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                super.run();
                File fileDir = new File(baseDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String musicDb = "music_db.db";
                String movieDb = "movie_db.db";
                String avDb = "av_db.db";
                zipFile(musicDb, new File(musicFile));
                zipFile(movieDb, new File(movieFile));
                zipFile(avDb, new File(avFile));
                Log.i("zune:", "复制完成，耗时：" + (System.currentTimeMillis() - time));
                clearHolder.view.post(new Runnable() {
                    @Override
                    public void run() {
                        clearHolder.stopLoad();
                    }
                });
            }
        }.start();
    }

    private void zipFile(String srcFile, File toFile) {
        FileOutputStream fos = null;
        try {
            if (toFile.exists()) {
                toFile.createNewFile();
            }
            InputStream is = getAssets().open(srcFile);
            byte[] data = new byte[1024];
            int nbread = 0;
            fos = new FileOutputStream(toFile);
            while ((nbread = is.read(data)) > -1) {
                fos.write(data, 0, nbread);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseUrl() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
    }

    private void parseXsl() {
        externalClick = EXTERNAL_XSL_CHOOSE;
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.stopLoad(new ArrayList<>(), true);
        clearHolder.setOnItemClickListener(new ClearHolder.OnItemClickListener() {
            @Override
            public void onItemClick(String data, int position) {
                clearHolder.dismiss();
                if (position == 0) {
                    List<ContactBean> contactBeans = startParseXsl(data);
                    List<String> contacts = new ArrayList<>();
                    for (ContactBean contactBean : contactBeans) {
                        contacts.add(contactBean.name + " : " + contactBean.phone);
                    }
                    clearHolder.stopLoad(contacts, false);
                    clearHolder.setOnItemClickListener(new ClearHolder.OnItemClickListener() {
                        @Override
                        public void onItemClick(String data, int position) {
                            clearHolder.dismiss();
                        }
                    });
                } else {
                    startFileChoose();
                }
            }
        });
    }

    private List<ContactBean> startParseXsl(String fileName) {
        List<ContactBean> temp = new ArrayList<>();
        Map<String, List<List<String>>> map = ExcelManager.getInstance().analyzeXls(fileName);
        if (map == null || map.isEmpty()) {
            map = ExcelManager.getInstance().analyzeXlsx(new File(fileName));
        }
        if (map != null) {
            for (String s : map.keySet()) {
                List<List<String>> lists = map.get(s);
                if (lists != null) {
                    for (List<String> list : lists) {
                        if (list != null) {
                            if (list.size() < 4) {
                                continue;
                            }
                            String name = list.get(1).trim();
                            String sex = list.get(2);
                            String phone = list.get(3);
                            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)) {
                                ContactBean bean = new ContactBean();
                                bean.name = name;
                                bean.phone = phone;
                                temp.add(bean);
                            }
                        }
                    }
                }
            }
        }
        JSONObject jsonObj = new JSONObject(map);
        Log.i("zune:", "json = " + jsonObj.toString());
        return temp;
    }

    private void share() {
        shareDialog = ShareDialog.showDialog(this);
    }

    /**
     * zune: 开始检测分贝
     **/
    private void starFenbei() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }
        }
        FenBeiActivity.start(MainActivity.this);
    }

    /**
     * zune: 获取通讯录
     **/
    private void getConstants() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_CONTACTS);
            if (permission1 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS
                        , Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CALL_LOG
                        , Manifest.permission.WRITE_CALL_LOG}, 1);
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        ContactsUtils.getInstance().getFastContacts(this).subscribe(new Observer<List<ContactBean>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(List<ContactBean> contactBeans) {
                List<String> contacts = new ArrayList<>();
                for (ContactBean contactBean : contactBeans) {
                    contacts.add(contactBean.name + " : " + contactBean.phone);
                }
                clearHolder.stopLoadContact(contacts);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * zune: 开始阅读
     **/
    private void startReadTxt() {
        externalClick = EXTERNAL_TXT_CHOOSE;
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        SaveUtils.SaveBean saveBean = SaveUtils.get();
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        List<String> paths = new ArrayList<>();
        if (saveBean != null && saveBean.saves != null && !saveBean.saves.isEmpty()) {
            for (SaveUtils.SaveBean.Save save : saveBean.saves) {
                paths.add(save.path);
            }
        }
        clearHolder.stopLoad(paths, true);
        clearHolder.setOnItemClickListener(new ClearHolder.OnItemClickListener() {
            @Override
            public void onItemClick(String data, int index) {
                if (clearHolder.getResults().getAdapter() == null) {
                    return;
                }
                if (index == clearHolder.getResults().getAdapter().getItemCount() - 1) {
                    startFileChoose();
                } else if (index >= 2) {
                    ReadTxtActivity.start(MainActivity.this, saveBean, index - 2);
                } else {
                    ReadTxtActivity.start(MainActivity.this, data);
                }
                clearHolder.dismiss();
            }
        });
    }

    private void startFileChoose() {
        File file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //7.0以上跳转系统文件需用FileProvider，参考链接：https://blog.csdn.net/growing_tree/article/details/71190741
        Uri uri = FileUtil.getUriFromFile(this, file);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, EXTERNAL_FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTERNAL_FILE_CODE && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            String path = FileUtil.getPath(this, uri);
            if (externalClick == EXTERNAL_TXT_CHOOSE) {
                ReadTxtActivity.start(MainActivity.this, path);
            } else if (externalClick == EXTERNAL_XSL_CHOOSE) {
                if (clearHolder == null) {
                    clearHolder = new ClearHolder(findViewById(R.id.clear_root));
                }
                clearHolder.startLoad();
                List<ContactBean> contactBeans = startParseXsl(path);
                List<String> contacts = new ArrayList<>();
                for (ContactBean contactBean : contactBeans) {
                    contacts.add(contactBean.name + " : " + contactBean.phone);
                }
                clearHolder.stopLoad(contacts, false);
                clearHolder.setOnItemClickListener(new ClearHolder.OnItemClickListener() {
                    @Override
                    public void onItemClick(String data, int position) {
                        clearHolder.dismiss();
                    }
                });
            }
        }
        if (shareDialog != null) {
            shareDialog.onActivityResult(requestCode, resultCode, data);
        }
        DocumentsFileUtils.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * zune: 删除文件夹
     **/
    private void deleteDir() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        ClearUtils.getInstance().delete(Environment.getExternalStorageDirectory().getPath(), new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(List<String> integer) {
                ToastUtils.showShort("删除完成");
                clearHolder.stopLoad(integer, false);
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort("刪除失败");
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * zune: 请求定位权限
     **/
    private void requestPermiss() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, 11);
            }
        }
    }

    private class AppClass {
        private Context c;

        public AppClass(Context baseContext) {
            this.c = baseContext;
        }

        @JavascriptInterface
        public void callBack(String userKey) {
            Toast.makeText(c, userKey + "", Toast.LENGTH_SHORT).show();
            Log.e("Tag", "设置了userKey : " + userKey);
        }

        @JavascriptInterface
        public void getUserKey(String userKey) {
            Toast.makeText(c, userKey + "", Toast.LENGTH_SHORT).show();
            Log.e("Tag", "读取到userKey : " + userKey);
        }

    }

    /**
     * 获取十六进制的颜色代码.例如  "#5A6677"
     * 分别取R、G、B的随机值，然后加起来即可
     *
     * @return String
     */
    public static String getRandColor() {
        String R, G, B, R1, G1, B1;
        Random random = new Random();
        int r = random.nextInt(256);
        R1 = Integer.toHexString(255 - r).toUpperCase();
        R = Integer.toHexString(r).toUpperCase();
        int g = random.nextInt(256);
        G1 = Integer.toHexString(255 - g).toUpperCase();
        G = Integer.toHexString(g).toUpperCase();
        int b = random.nextInt(256);
        B1 = Integer.toHexString(255 - b).toUpperCase();
        B = Integer.toHexString(b).toUpperCase();

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        return "#80" + R + G + B + "*" + "FF" + R1 + G1 + B1;
    }
}
