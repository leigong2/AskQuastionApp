package com.example.android.askquastionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.bean.Company;
import com.example.android.askquastionapp.bean.JobBean;
import com.example.android.askquastionapp.besar.BesarActivity;
import com.example.android.askquastionapp.contacts.ContactBean;
import com.example.android.askquastionapp.expand.ExpandActivity;
import com.example.android.askquastionapp.expand.PushActivity;
import com.example.android.askquastionapp.fenbei.FenBeiActivity;
import com.example.android.askquastionapp.location.LocationActivity;
import com.example.android.askquastionapp.math.MathFunActivity;
import com.example.android.askquastionapp.picture.ImageViewActivity;
import com.example.android.askquastionapp.picture.PictureActivity;
import com.example.android.askquastionapp.read.ReadTxtActivity;
import com.example.android.askquastionapp.reader.ReaderListActivity;
import com.example.android.askquastionapp.utils.BitmapUtil;
import com.example.android.askquastionapp.utils.ClearUtils;
import com.example.android.askquastionapp.utils.ContactsUtils;
import com.example.android.askquastionapp.utils.CustomItemTouchHelperCallBack;
import com.example.android.askquastionapp.utils.DocumentsFileUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.GlideUtils;
import com.example.android.askquastionapp.utils.SaveUtils;
import com.example.android.askquastionapp.utils.SetIpDialog;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.video.DownloadObjManager;
import com.example.android.askquastionapp.video.ListenMusicActivity;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.views.ClearHolder;
import com.example.android.askquastionapp.web.WebViewUtils;
import com.example.android.askquastionapp.wxapi.ShareDialog;
import com.example.android.askquastionapp.xmlparse.ExcelManager;
import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.KeyWords;
import com.example.jsoup.bean.LanguageWords;
import com.example.jsoup.jsoup.JsoupUtils;
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
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.io.File.separator;

/**
 * The type Main activity.
 *
 * @author wangzhilong
 */
public class MainActivity extends AppCompatActivity {

    private static final int EXTERNAL_FILE_CODE = 200;
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

    public static String baseUrl = TextUtils.isEmpty(SPUtils.getInstance().getString("baseUrl")) ? "http://192.168.200.51" : SPUtils.getInstance().getString("baseUrl");

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
    }

    private void resetIp() {
        devolop = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/develop/debug/app-develop-armeabi-v7a-debug.apk";
        preProducation = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/preProducation/debug/app-preProducation-armeabi-v7a-debug.apk";
        production_blue = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/production_blue/debug/app-production_blue-armeabi-v7a-debug.apk";
        production = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/production/debug/app-production-armeabi-v7a-debug.apk";
        release = (baseUrl.startsWith("http://") ? baseUrl : "http://" + baseUrl) + "/production/release/app-production-armeabi-v7a-release.apk";
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
        if (temp != null && !temp.isEmpty()) {
            mMainTags.addAll(temp);
            return;
        }
        this.mMainTags.add("阅读");
        this.mMainTags.add("数学");
        this.mMainTags.add("贝塞尔曲线");
        this.mMainTags.add("分贝");
        this.mMainTags.add("Google地图");
        this.mMainTags.add("删除冗余文件夹");
        this.mMainTags.add("通讯录");
        this.mMainTags.add("分享");
        this.mMainTags.add("解析excel");
        this.mMainTags.add("爬虫测试");
        this.mMainTags.add("电影");
        this.mMainTags.add("视频");
        this.mMainTags.add("歌曲");
        this.mMainTags.add("去水印");
        this.mMainTags.add("图片");
        this.mMainTags.add("读者");
        this.mMainTags.add("js存储");
        this.mMainTags.add("js读取");
        this.mMainTags.add("androidQ notify");
        this.mMainTags.add("加载圆图");
        this.mMainTags.add("sd卡");
        this.mMainTags.add("selenium测试");
        this.mMainTags.add("八爪鱼");
        this.mMainTags.add("翻译文案");
        this.mMainTags.add("下载app");
        this.mMainTags.add("设置ip");
        this.mMainTags.add("展开文本");
        this.mMainTags.add("测试");
        this.mMainTags.add("测试@#");
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
//                parseXsl();
                readXls();
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
                if (new File(avFile).exists()) {
                    WatchVideoActivity.start(MainActivity.this, avFile);
                } else {
                    gzipFiles();
                }
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
//                readXls();
                readBossXls();
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
                String test = "http://192.168.200.60/img0/%E6%97%A5%E6%9C%AC%E7%86%9F%E5%A6%87NatsukoShunga%E5%B0%8F%E5%A5%97%E5%9B%BE-2[20P]/%E6%97%A5%E6%9C%AC%E7%86%9F%E5%A6%87NatsukoShunga%E5%B0%8F%E5%A5%97%E5%9B%BE-2[20P]19.jpeg";
//                String test = "http://192.168.200.60/img0/0689%E6%9D%BE%E4%BA%95%E5%9C%A3%E5%A5%8828%E6%AD%B3%5B30P%5D/0689%E6%9D%BE%E4%BA%95%E5%9C%A3%E5%A5%8828%E6%AD%B3%5B30P%5D1.jpg";
                GlideUtils.getInstance().loadUrl(test, findViewById(R.id.test), true, false);
                File localCache = GlideUtils.getInstance().getLocalCache(this, test);
                break;
        }
    }

    private void readBossXls() {
        File fileName = FileUtil.assetsToFile(this, "test_boss.xlsx");
        boolean b = fileName.exists();
        Map<String, List<List<String>>> map = ExcelManager.getInstance().analyzeXls(fileName.getPath());
        if (map == null || map.isEmpty()) {
            map = ExcelManager.getInstance().analyzeXlsx(fileName.getPath());
        }
//        {xxx:"xxx", xxx:"xxx"}
        for (String key : map.keySet()) {
            List<List<String>> lists = map.get(key);
            if (lists == null) {
                return;
            }
            List<StringBuilder> jsons = new ArrayList<>();
            for (int i = 1; i < lists.size(); i++) {
                StringBuilder json = new StringBuilder();
                for (int j = 0; j < lists.get(i).size(); j++) {
                    String value = lists.get(i).get(j);
                    if (value == null) {
                        value = "";
                    }
                    String str = value.replaceAll("\\\\", "\\\\\\\\");
                    if (j == 0) {
                        json.append("{\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                    } else if (j == lists.get(i).size() - 1) {
                        json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\"}");
                    } else {
                        json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                    }
                }
                jsons.add(json);
            }
            String msg = GsonGetter.getInstance().getGson().toJson(jsons);
            Log.i("zune", msg);
            List<String> o = GsonGetter.getInstance().getGson().fromJson(msg, new TypeToken<List<String>>() {
            }.getType());
            if (o != null) {
                List<JobBean> jobBeans = new ArrayList<>();
                for (String string : o) {
                    Company company = GsonGetter.getInstance().getGson().fromJson(string, Company.class);
                    JobBean jobBean = JobBean.companyToJob(company);
                    jobBeans.add(jobBean);
                }
                String s = GsonGetter.getInstance().getGson().toJson(jobBeans);
                Log.i("zune", s);
            }
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
        datas.add("devolop");
        datas.add("preProducation");
        datas.add("production_blue");
        datas.add("production");
        datas.add("release");
        clearHolder.stopLoad(datas, false);
        clearHolder.getResults().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = (String) clearHolder.getResults().getAdapter().getItem(position);
                switch (path) {
                    case "devolop":
                        startDownloadApp(devolop, path);
                        break;
                    case "preProducation":
                        startDownloadApp(preProducation, path);
                        break;
                    case "production_blue":
                        startDownloadApp(production_blue, path);
                        break;
                    case "production":
                        startDownloadApp(production, path);
                        break;
                    case "release":
                        startDownloadApp(release, path);
                        break;
                    default:
                        startDownloadApp(self, path);
                        break;
                }
                clearHolder.dismiss();
            }
        });
    }

    /**
     * 调用第三方浏览器打开
     *
     * @param url 要浏览的资源地址
     */
    private void startDownloadApp(String url, String path) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            intent.setClassName("com.tencent.mtt", "com.tencent.mtt.MainActivity");//打开QQ浏览器
            startActivity(intent);
        } catch (Exception e1) {
            try {
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                startActivity(intent);
            } catch (Exception e2) {
                try {
                    intent.setClassName("mark.via", "mark.via.ui.activity.BrowserActivity");
                    startActivity(intent);
                } catch (Exception e3) {
                    // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
                    // 官方解释 : Name of the component implementing an activity that can display the intent
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, "请选择浏览器"));
                    } else {
                        Toast.makeText(getApplicationContext(), "请下载浏览器", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
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
        Map<String, List<List<String>>> map = ExcelManager.getInstance().analyzeXls(fileName.getPath());
        if (map == null || map.isEmpty()) {
            map = ExcelManager.getInstance().analyzeXlsx(fileName.getPath());
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
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.startLoad();
        Observable.just(1).map(new Function<Integer, List<String>>() {
            @Override
            public List<String> apply(Integer integer) throws Exception {
                File fileName = FileUtil.assetsToFile(MainActivity.this, "企业最新招聘信息_求职信息_找工作上智联招聘.xlsx");
                Map<String, List<List<String>>> map = ExcelManager.getInstance().analyzeXls(fileName.getPath());
                if (map == null || map.isEmpty()) {
                    map = ExcelManager.getInstance().analyzeXlsx(fileName.getPath());
                }
//        {xxx:"xxx", xxx:"xxx"}
                for (String key : map.keySet()) {
                    List<List<String>> lists = map.get(key);
                    if (lists == null) {
                        return new ArrayList<>();
                    }
                    List<Company> jsons = new ArrayList<>();
                    List<String> companyNames = new ArrayList<>();
                    List<String> companyCounts = new ArrayList<>();
                    for (int i = 1; i < lists.size(); i++) {
                        StringBuilder json = new StringBuilder();
                        for (int j = 0; j < lists.get(i).size(); j++) {
                            String value = lists.get(i).get(j);
                            if (value == null) {
                                value = "";
                            }
                            String str = value.replaceAll("\\\\", "\\\\\\\\");
                            if (j == 0) {
                                json.append("{\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                            } else if (j == lists.get(i).size() - 1) {
                                json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\"}");
                            } else {
                                json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                            }
                        }
                        Company company = GsonGetter.getInstance().getGson().fromJson(json.toString(), Company.class);
                        if (company.os.contains("Android") || company.os.contains("android") || company.os.contains("安卓") || company.os.contains("移动"))
                            if (!companyNames.contains(company.company)) {
                                jsons.add(company);
                                companyNames.add(company.company.replaceAll("\n", "").replaceAll(" ", ""));
                                companyCounts.add(company.company + " # " + company.money + " # " + company.os + " :" + 1);
                            } else {
                                int index = companyNames.indexOf(company.company);
                                if (jsons.size() > index) {
                                    String temp = companyCounts.get(index);
                                    String s = temp.split(":")[1];
                                    companyCounts.set(index, temp.split(":")[0] + ":" + (Integer.parseInt(s) + 1));
                                }
                            }
                    }
                    String msg = GsonGetter.getInstance().getGson().toJson(jsons);
                    Log.i("zune", msg);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        companyCounts.sort(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return Integer.parseInt(o2.split(":")[1]) - Integer.parseInt(o1.split(":")[1]);
                            }
                        });
                    }
                    saveToLocal(companyCounts);
                    return getShowData(companyCounts);
                }
                return new ArrayList<>();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<String>, Integer>(1, false) {
            @Override
            public void onNext(List<String> companyCounts, Integer o2) {
                clearHolder.stopLoad(companyCounts, false);
                statisticsTime(companyCounts);
            }
        });
    }

    private List<String> getShowData(List<String> companyCounts) {
        List<String> showData = new ArrayList<>();
        List<String> showNew = new ArrayList<>();
        List<String> showOld = new ArrayList<>();
        List<String> timeLimit = new ArrayList<>();
        List<String> localCompany = getLocalCompany();
        List<String> oldCompanyName = new ArrayList<>();
        List<String> netCompanyName = new ArrayList<>();
        for (String s : localCompany) {
            oldCompanyName.add(s.split("#")[0].trim());
        }
        for (String companyCount : companyCounts) {
            String companayName = companyCount.split("#")[0].trim();
            netCompanyName.add(companayName);
            if (!oldCompanyName.contains(companayName)) {
                showNew.add(companyCount);
            } else {
                showOld.add(companyCount);
            }
        }
        showData.add("新增 " + showNew.size() + "=======================");
        showData.addAll(showNew);
        showData.add("重合 " + showOld.size() + "=======================");
        showData.addAll(showOld);
        for (String s : localCompany) {
            String companayName = s.split("#")[0].trim();
            if (!netCompanyName.contains(companayName)) {
                timeLimit.add(s);
            }
        }
        showData.add("过期 " + timeLimit.size() + "=======================");
        showData.addAll(timeLimit);
        showData.add(0, "有效总数：" + companyCounts.size() + "/全部总数：" + (showNew.size() + showOld.size() + timeLimit.size()) + "========");
        return showData;
    }

    private void saveToLocal(List<String> companyCounts) {
        List<String> localCompany = getLocalCompany();
        List<String> localCompanyName = new ArrayList<>();
        for (String s : localCompany) {
            String name = s.split("#")[0];
            localCompanyName.add(name);
        }
        List<String> temp = new ArrayList<>();
        for (String companyCount : companyCounts) {
            String name = companyCount.split("#")[0];
            if (localCompanyName.contains(name)) {
                int i = companyCounts.indexOf(companyCount);
                String[] split = companyCount.split(":");
                String count = split[1];
                String tempStr = split[0] + ":" + (Integer.parseInt(count) + 1);
                companyCounts.set(i, tempStr);
                continue;
            }
            temp.add(companyCount);
        }
        temp.addAll(localCompany);
        SPUtils.getInstance("Company").put("localMsg", GsonGetter.getInstance().getGson().toJson(temp));
    }

    public List<String> getLocalCompany() {
        String lastData = "[\"羽林 # 6-9K # Android :2\",\"叁柒壹 # 5-8K # Android :2\",\"winderInfo # 7-12K # 诚聘安卓开发工程师 :2\",\"优租 # 8-15K # 安卓Android开发工程师 :2\",\"指针软件 # 5-7K # 安卓开发工程师 :2\",\"建达软件 # 6-11K # 安卓软件工程师 :2\",\"七七网络科技 # 8-13K # Android :2\",\"河南帮枭 # 5-10K # 安卓 :2\",\"比比西 # 8-9K # 安卓开发工程师 :2\",\"欣联科技 # 6-9K # 安卓开发工程师 :2\",\"喜付网络 # 7-12K·13薪 # 安卓Xposed开发工程师 :2\",\"中冠网科 # 5-9K # 安卓开发工程师 :2\",\"郑州点都公司 # 7-10K # 安卓开发 :2\",\"新开普 # 6-11K·13薪 # android开发工程师 :2\",\"万峰格网络科技 # 6-10K # Android :1\",\"迅安达网络 # 8-12K # Android :1\",\"时光机信息科技 # 5-10K # Android :1\",\"禹和新能源 # 5-10K # Android工程师 :1\",\"深圳迅科 # 6-8K # 安卓开发工程师 :1\",\"品辰电子 # 6-8K # android工程师 :1\",\"福务达 # 3-5K # Android :1\",\"新中科技 # 6-8K # Android :1\",\"兴邦股份 # 4-6K # Android :1\",\"帝通 # 8-10K # Android工程开发师 :1\",\"万商元 # 10-11K # 安卓高级开发工程师 :1\",\"郑州帕博文化传媒 # 10-20K # Android 开发工程师 :1\",\"保驾护航 # 5-10K # Android :1\",\"乐而知教育 # 7-10K # Android :1\",\"东亨网络科技 # 8-9K # Android :1\",\"凝聚网络 # 6-9K # Android :1\",\"新城未来 # 3-6K # Android :1\",\"闪电云 # 5-10K # 高级Android工程师 :1\",\"米学科技 # 8-12K # 高级Android工程师 :1\",\"微栈科技 # 6-10K # Android :1\",\"融企信网络 # 3-6K # Android :1\",\"勋为信息技术 # 7-12K·13薪 # 安卓开发工程师 :1\",\"九维网络 # 7-12K # Android :1\",\"龙商汇电子商务 # 8-9K # Android :1\",\"郑州方洛 # 9-12K # 安卓开发工程师 :1\",\"中部时代 # 6-9K # SDK开发工程师（Android） :1\",\"北京盛世唯信科技 # 4-6K # Android :1\",\"中数物联网科技 # 6-8K # Android :1\",\"一品凡客 # 8-15K # Android   ios  app开发 :1\",\"旭扬网络 # 6-10K # Android :1\",\"大觉慧海网络科技 # 7-9K # 安卓 :1\",\"商联供应链 # 6-8K # android :1\",\"野石头科技 # 7-8K # Android :1\",\"天问网络 # 10-11K # Android程序员 :1\",\"哲硕网络科技有限公司 # 4-7K # 安卓 :1\",\"河南天华教育 # 8-12K # 安卓开发工程师 :1\",\"数字郑州 # 15-25K·15薪 # iOS/Android高级开发工程师 :1\",\"万杰科技 # 8-12K # Android :1\",\"纤原网络 # 7-10K # 安卓 :1\",\"郑州竹叶网络公司 # 4-6K # 安卓开发工程师 :1\",\"中荆投资 # 7-8K # 安卓 :1\",\"捷之捷 # 5-10K # 安卓前端 :1\",\"车服科技 # 6-7K # Android :1\",\"大觉慧海 # 7-12K # 安卓开发工程师 :1\",\"晨泽电子科技 # 6-9K # 安卓 :1\",\"迪迪贷 # 6-8K # 安卓工程师 :1\",\"博观电子 # 4-6K # 安卓工程师 :1\",\"骏龙数据 # 8-12K # 安卓开发工程师 :1\",\"嘎纳时代 # 5-7K # 安卓前端工程师 :1\",\"五二科技 # 5-8K # 安卓/Android开发工程师 :1\",\"郑州思优软件科技 # 6-8K # 安卓开发工程师 :1\",\"郑州盛联易网络科技 # 5-6K # 安卓工程师 :1\",\"神灯网络 # 9-14K # Andriod安卓高级工程师 :1\",\"郑州梅地亚 # 5-8K # 安卓程序员 :1\",\"河南行星智能电子... # 5-8K # 安卓开发工程师Andriod :1\",\"聚格 # 6-9K # 高薪急聘安卓开发工程师 :1\",\"报国梦电子商务 # 5-10K # 安卓开发工程师 :1\",\"博海电子 # 8-10K # 安卓开发工程师 :1\",\"河南安信科技 # 6-8K # 安卓开发工程师 :1\",\"河南鑫安利 # 6-10K # 安卓开发工程师 :1\",\"陀螺 # 6-10K # 安卓前端研发工程师 :1\",\"河南君创 # 6-8K # 安卓开发工程师 :1\",\"简信软件 # 6-10K # 安卓开发 :1\",\"郑州动态网络科技 # 6-10K # 安卓工程师 :1\",\"懂体验 # 4-9K # 高薪安卓开发工程师 :1\",\"佳维 # 8-12K # Android开发工程师 :1\",\"河南没问题 # 8-10K # 安卓 :1\",\"释码云识别技术研究院 # 3-7K·13薪 # Android/Linux驱动工程师（实习） :1\",\"官膳食 # 7-12K # Android开发工程师 :1\",\"众惠物联 # 6-9K # 安卓leader :1\",\"天纵科技 # 8-13K # 安卓开发工程师 :1\",\"易豪电子商务 # 5-10K # Android :1\",\"万企 # 6-8K # 安卓 :1\",\"云控物联网 # 5-8K # 安卓开发工程师 :1\",\"仁慧企业管理 # 6-10K # Android开发工程师 :1\",\"云彩科技 # 5-10K # 初级中级安卓apk工程师 :1\",\"郑州势为 # 4-6K # 安卓android开发工程师 :1\",\"聚购网络科技 # 8-13K # 安卓开发工程师 :1\",\"牧客科技 # 12-18K # 安卓逆向开发 :1\",\"郑州鼎臣网络科技 # 10-15K # 安卓逆向工程师 :1\",\"中机创 # 7-9K # Android开发工程师 :1\",\"德一集团 # 6-11K # 安卓开发工程师 :1\",\"郑州卓见软件科技 # 11-20K·13薪 # Android开发 产品公司 :1\",\"郑州珑凌科技 # 16-25K # 资深android开发 :1\",\"国源科技 # 8-10K # 中级Android研发工程师（中级） :1\",\"中时能源 # 5-10K # 安卓工程师 :1\",\"光合 # 7-12K # 安卓开发工程师 :1\",\"医美圈 # 6-10K # 安卓开发 :1\",\"新远方商翼 # 5-10K # 安卓开发工程师 :1\",\"因特嘉 # 5-6K # 安卓开发工程师 :1\",\"河南众聚电子商务 # 6-8K # 安卓 :1\",\"金区有限公司 # 8-12K # Android开发 :1\",\"河南星火燎原 # 8-13K # 安卓开发工程师 :1\",\"郑州时空软件 # 4-6K # 安卓开发工程师 :1\",\"东方潜能 # 3-8K # 安卓工程师 :1\",\"中机智云 # 6-9K # Android开发工程师 :1\",\"美达中国-深圳美达 # 7-12K·14薪 # 安卓开发工程师 :1\",\"象过河软件 # 6-9K # Android开发工程师 :1\",\"智云全景 # 5-6K # Android开发工程师 :1\",\"君衡教育 # 20-35K # 高级安卓开发工程师 :1\",\"屹通信息科技 # 7-10K # Android开发（全国出差） :1\",\"海克企业管理 # 3-5K # 安卓开发 :1\",\"简云科技 # 5-10K # 安卓开发工程师 :1\",\"诚毅物联网 # 5-10K # 安卓工程师 :1\",\"应之运 # 7-8K # 安卓开发 :1\",\"郑州佰谷信息 # 6-7K # 安卓开发工程师 :1\",\"十一人网络科技 # 5-6K # Android开发 :1\",\"点卡木 # 7-10K # 安卓开发 :1\",\"飞渡 # 7-10K # 安卓开发工程师 :1\",\"河南万邦 # 6-11K # IOS、安卓前端研发工程师 :1\",\"新天科技 # 5-10K # Android开发工程师 :1\",\"良仓科技 # 6-8K # Android应用开发工程师 :1\",\"视博电子 # 4-8K # Android开发工程师 :1\",\"UIOT超级智慧家 # 8-13K # 安卓开发工程师 :1\",\"中原金科 # 7-11K # APP开发工程师-安卓方向 :1\",\"郑州蓓蕾 # 4-8K # 安卓APP开发工程师 :1\",\"河南中钢网 # 7-12K # Android开发工程师 :1\",\"很快科技 # 5-10K # Android开发工程师 :1\",\"英苑企业管理咨询 # 8-12K # 安卓高级研发工程师 :1\",\"常春藤软件 # 5-8K # 高级Android开发工程师 :1\",\"锅圈供应链 # 8-13K·13薪 # Android开发工程师 :1\",\"沸点网络 # 10-11K # Android开发工程师 :1\",\"中原银行 # 16-20K·15薪 # 移动端开发 :1\",\"郑州中原报业传媒有限公司 # 10K-15K # 安卓开发工程师 :1\",\"北京东蓝数码科技有限公司 # 5K-10K # 安卓开发工程师 :1\",\"河南米伦科技有限公司 # 6K-8K # 安卓开发工程师 :1\",\"河南晨飞安全技术有限公司 # 6K-10K # 安卓开发工程师 双休 :1\",\"郑州万豪网络技术有限公司 # 6K-8K # 安卓开发 :1\",\"河南金途科技集团股份有限公司 # 8K-10K # 安卓工程师 :1\",\"河南莲菜网络科技有限公司 # 6K-8K # 安卓开发工程师 :1\",\"河南省百年利人企业孵化器有限公司 # 5K-8K # 安卓开发工程师 :1\",\"深圳邦益基金管理有限公司 # 10K-12K # 安卓开发工程师 :1\",\"河南知途游道信息科技有限公司 # 6K-8K :1\",\"湖南中南智能装备有限公司 # 15K-20K :45\",\"中科软科技股份有限公司 # 6K-8K :18\",\"广州国测规划信息技术有限公司 # 8K-10K :10\",\"北京赏心悦目软件有限公司 # 8K-13K :10\",\"正星科技股份有限公司 # 6K-8K :9\",\"河南因特嘉软件工程有限公司 # 6K-8K :9\",\"北京华仁颐居信息科技有限公司 # 6K-10K :9\",\"东方通信股份有限公司 # 7K-13K :9\",\"卫华集团有限公司 # 8K-15K :9\",\"河南亿兴科技股份有限公司 # 6K-10K :9\",\"河南商蒙软件科技有限公司 # 4K-7K :9\",\"郑州中业科技股份有限公司 # 9K-13K :9\",\"河南恒视软件科技有限公司 # 8K-10K :9\",\"郑州诚毅物联网技术有限公司 # 6K-8K :9\",\"郑州闪创网络科技有限公司 # 5K-10K :3\",\"河南八六三软件股份有限公司 # 8K-10K :3\",\"超级智慧家(上海)物联网科技有限公司 # 8K-15K :3\",\"新开普电子股份有限公司 # 薪资面议 :3\",\"郑州榕盛信息技术有限公司 # 3K-5K :3\",\"绿色三谷 # 15-20K :3\",\"安徽省刀锋网络科技有限公司 # 15K-20K :2\",\"河南犇犇网络科技有限公司 # 7K-10K :2\",\"北京森泰英睿传媒科技有限公司河南分公司 # 6K-8K :2\",\"北京芯盾集团有限公司 # 8K-12K :2\",\"郑州信大先进技术研究院 # 7K-12K :2\",\"郑州天迈科技股份有限公司 # 10K-15K :2\",\"北京云海航创网络科技有限公司郑州分公司 # 10K-15K :2\",\"郑州张一绝餐饮企业管理咨询有限公司 # 4K-8K :2\",\"芯盾网安 # 8-13K :2\",\"集优科技 # 8-12K :2\",\"河南威漫信息科技有限公司 # 10K-15K :1\",\"豪弘电子商务集团有限公司 # 6K-8K :1\",\"郑州现代外语学校 # 6K-9K :1\",\"河南澳乐康科技有限公司 # 8K-13K :1\",\"河南康派智能技术有限公司 # 8K-12K :1\",\"郑州华骏技术有限公司 # 5K-6K :1\",\"牧原食品股份有限公司 # 10K-15K :1\",\"郑州致博思远企业管理咨询有限公司 # 6K-12K :1\",\"河南华鼎供应链管理有限公司 # 6K-11K :1\",\"上海屹通信息科技发展有限公司 # 6K-8K :1\",\"河南天星教育传媒股份有限公司 # 8K-12K :1\",\"北京合众伟奇科技有限公司 # 7K-12K :1\",\"天博电子信息科技有限公司 # 5K-8K :1\",\"郑州聚格软件科技有限公司 # 8K-10K :1\",\"郑州诺云网络科技有限公司 # 6K-8K :1\",\"河南中裕广恒科技股份有限公司 # 6K-8K :1\",\"河南省遥感测绘院 # 8K-10K :1\",\"UU跑腿 # 7K-10K :1\",\"郑州软通合力计算机技术有限公司 # 8K-10K :1\",\"中科九洲科技股份有限公司 # 6K-8K :1\",\"中融鑫集团有限公司 # 4K-6K :1\",\"河南永硕实业发展有限公司 # 6K-8K :1\",\"河南云安溯源科技有限公司 # 7K-10K :1\",\"深圳市芬析仪器制造有限公司 # 6K-8K :1\",\"河南枞宁信息科技有限公司 # 15K-30K :1\",\"天宇正清科技有限公司 # 8K-15K :1\",\"北京上标国际知识产权代理有限公司 # 4K-6K :1\",\"南京麦思伦信息科技有限公司 # 10K-18K :1\",\"新天科技股份有限公司 # 6K-12K :1\",\"郑州梦之源电子科技有限公司 # 6K-8K :1\",\"北京超图软件股份有限公司 # 5K-9K :1\",\"浪潮集团有限公司 # 6K-12K :1\",\"郑州星海科技有限公司 # 6K-10K :1\",\"河南塔姆网络科技有限公司 # 10K-15K :1\",\"河南省晨罡实业有限公司 # 8K-12K :1\",\"新商育科技有限公司 # 10K-20K :1\",\"河南九域腾龙信息工程有限公司 # 8K-10K :1\",\"河南智森物联网科技有限公司 # 6K-12K :1\",\"河南今迈实业发展有限公司 # 5K-10K :1\",\"河南合众伟奇云智科技有限公司 # 7K-12K :1\",\"郑州灵慧软件科技有限公司 # 6K-10K :1\",\"河南品辰电子科技有限公司 # 4K-6K :1\",\"郑州郑大信息技术有限公司 # 8K-10K :1\",\"河南智联时空信息科技有限公司 # 6K-10K :1\",\"郑州乙丙丁软件科技有限公司 # 6K-8K :1\",\"郑州信泽华计算机技术开发有限公司 # 6K-8K :1\",\"郑州点都科技有限公司 # 6K-10K :1\",\"北京农信通科技有限责任公司 # 6K-8K :1\",\"河南云煤网网络科技有限责任公司 # 5K-8K :1\",\"河南智宽科技有限公司 # 6K-8K :1\",\"郑州好聚点科技有限公司 # 8K-15K :1\",\"华测电子认证有限责任公司 # 6K-8K :1\",\"郑州优易达电子科技有限公司 # 6K-8K :1\",\"中机智云有限公司 # 6K-9K :1\",\"河南万维科技开发有限公司 # 3K-5K :1\",\"郑州珑凌科技有限公司 # 15K-20K :1\",\"北京杰山科技有限公司 # 4K-8K :1\",\"北京琥珀创想科技有限公司 # 7K-12K :1\",\"郑州闪电云信息技术有限公司 # 9K-11K :1\",\"漯河江山天安新型建材有限公司 # 6K-12K :1\",\"南京迈特望科技股份有限公司 # 15K-20K :1\",\"辅仁药业集团有限公司 # 6K-8K :1\",\"郑州博税信息技术有限公司 # 8K-10K :1\",\"河南有个圈网络科技有限公司 # 8K-10K :1\",\"郑州程序猫信息技术有限公司 # 8K-10K :1\",\"北京企服嘉通技术服务有限公司 # 6K-11K :1\",\"厦门特力通通信工程有限公司 # 10K-20K :1\",\"龙湾科技(北京)有限公司 # 8K-15K :1\",\"河南果盛教育科技有限公司 # 6K-9K :1\",\"中公教育 # 薪资面议 :1\",\"郑州掌尚信息技术有限公司 # 6K-10K :1\",\"河南陀螺信息技术有限公司 # 6K-8K :1\",\"河南知行进化电子商务有限公司 # 5K-10K :1\",\"郑州立信软件科技有限公司 # 7K-9K :1\",\"河南新远方商翼电子科技有限公司 # 5K-10K :1\",\"郑州威科姆科技股份有限公司 # 6K-8K :1\",\"北京广大泰祥自动化技术有限公司鹤壁分公司 # 8K-10K :1\",\"富士康科技集团郑州科技园 # 10K-17K :1\",\"河南投资集团有限公司 # 9K-16K :1\",\"郑州中软高科信息技术有限公司 # 8K-15K :1\",\"中原银行股份有限公司 # 薪资面议 :1\",\"河南易众拍卖行有限公司 # 6K-9K :1\",\"河南凯仕网络科技有限公司 # 8K-10K :1\",\"郑州深蓝电子有限公司 # 6K-12K :1\",\"河南省电子规划研究院有限责任公司 # 10K-15K :1\",\"郑州软盟通信技术有限公司 # 7K-12K :1\",\"郑州远洋电子科技有限公司 # 8K-10K :1\",\"河南格瑞恩电子科技有限公司 # 5K-10K :1\",\"郑州瑞孚智能设备有限公司 # 7K-12K :1\",\"红星机器 # 6-11K :1\",\"澳乐康 # 8-13K :1\",\"合众伟奇 # 7-12K·14薪 :1\",\"博聪教育 # 8-12K :1\",\"闪创科技 # 6-10K :1\",\"七网科技 # 6-8K :1\",\"八角科技 # 8-12K :1\",\"卓瑞姆 # 7-9K :1\",\"北京物通 # 8-12K :1\",\"畅木 # 3-8K :1\",\"河南花样年通讯 # 8-12K :1\",\"阿里巴巴集团 # 20-40K·16薪 :1\",\"修齐治平 # 7-12K :1\",\"河南双汇投资发展... # 8-13K :1\",\"富士康 # 8-12K :1\",\"启步科技 # 9-11K :1\",\"杭州优效科技 # 5-10K :1\",\"天迈科技 # 10-15K :1\",\"智联时空 # 6-10K :1\",\"大张旗鼓文化传播 # 8-13K :1\",\"郑州燚轩科技 # 12-15K :1\",\"聚鑫鼎 # 5-10K :1\",\"八六三软件 # 6-10K :1\",\"犇犇科技 # 8-10K :1\",\"慕速物联 # 5-8K :1\",\"中原申威 # 10-12K :1\",\"微聊 # 6-11K :1\",\"刀锋互娱 # 11-18K :1\",\"吾言文化传播有限公司 # 4-8K :1\",\"立信科技 # 5-10K·13薪 :1\",\"河南塔姆 # 10-15K :1\",\"博信科技 # 4-8K :1\",\"蓝信科技 # 8-13K·14薪 :1\",\"厚普科技 # 5-8K :1\",\"栩和 # 7-10K :1\",\"迪确良品 # 4-6K :1\",\"华韩软件 # 150-200元/天 :1\",\"承易启慧 # 6-9K :1\",\"呐吼科技 # 15-25K :1\",\"私塾国际学府 # 8-13K :1\",\"河南微盟文化传播 # 6-8K :1\",\"庚凡科技 # 6-7K :1\",\"远洋科技 # 5-10K :1\",\"慧鼎科技 # 8-10K :1\",\"河南宇信 # 10-11K :1\",\"汇智丰 # 5-9K :1\",\"北京微训科技有限公司 # 5-8K :1\",\"维飞科技 # 7-10K :1\",\"优碧科技 # 9-14K :1\",\"扬宸公司 # 7-10K :1\",\"美朵科技 # 10-15K :1\",\"闻秋科技 # 10-11K :1\",\"微镖 # 8-9K :1\",\"中州智惠物流 # 9-14K :1\",\"杭州云榭科技有限... # 6-11K :1\",\"江湖工匠 # 7-8K :1\",\"深蓝汽贸 # 10-20K :1\",\"河南酱八爷 # 5-10K :1\",\"奥创百科集团 # 7-12K :1\",\"慢跑实验室 # 5-10K :1\",\"河南橙石网络科技 # 6-9K :1\",\"郑州广之达 # 8-12K :1\",\"蜂店网络 # 5-8K :1\",\"云海航创网络科技 # 9-14K :1\",\"迅众科技 # 5-10K :1\",\"聚时 # 4-5K :1\",\"福睿智能科技 # 6-9K :1\",\"开元创启 # 4-9K :1\",\"米伦科技 # 6-11K :1\",\"万众邦 # 4-6K :1\",\"金擎科技 # 8-13K :1\",\"郑州正拓科技 # 8-13K·13薪 :1\",\"欣宜嘉 # 4-9K·13薪 :1\",\"曼德 # 5-8K :1\",\"河南涛雷软件科技 # 7-14K :1\",\"斯迈欧网络科技 # 4-9K :1\",\"明天工贸 # 8-13K :1\",\"优潮 # 8-13K :1\",\"铭商缘网络科技 # 5-10K :1\",\"爱怡家 # 4-9K :1\",\"云企汇网络科技 # 6-8K :1\",\"咱的店 # 10-15K :1\",\"JYATECH # 7-8K :1\",\"山西乾森网络科技 # 5-10K :1\",\"河南咏赞软件 # 4-6K :1\",\"三业达 # 2-5K :1\",\"郑州小苗软件 # 5-10K :1\",\"天晟科技 # 4-8K :1\",\"鼓点软件 # 6-12K :1\",\"承金泰投资有限公司 # 4-8K :1\",\"河南纤原网络 # 7-8K :1\",\"卡车团 # 4-8K :1\",\"亚瑞材料 # 6-12K :1\",\"格乐电子 # 5-8K :1\",\"无锡精英堂 # 7-14K :1\",\"河南债无债互联网科技 # 5-7K :1\",\"采知企业孵化器 # 6-12K :1\"]";
        List<String> o = GsonGetter.getInstance().getGson().fromJson(lastData, new TypeToken<List<String>>() {
        }.getType());
        return o == null ? new ArrayList<>() : o;
    }

    /*zune: 统计文字出现的次数**/
    private void statisticsTime(List<String> companyCounts) {
        Disposable subscribe = Observable.just(companyCounts).map(new Function<List<String>, Map<String, Integer>>() {
            @Override
            public Map<String, Integer> apply(List<String> strings) throws Exception {
                long time = System.currentTimeMillis();
                Map<String, Integer> temp = new HashMap<>();
                int minLength = 2;
                int maxLength = 10;
                for (String data : strings) {
                    for (int i = 0; i < data.length(); i++) {
                        if (i == minLength - 1) {
                            String tempStr = data.substring(0, i + 1);
                            temp.put(tempStr, (temp.get(tempStr) == null ? 1 : temp.get(tempStr) + 1));
                        } else if (i > minLength - 1 && i < maxLength) {
                            int length = i - minLength + 1;
                            for (int j = 0; j < length; j++) {
                                String tempStr = data.substring(j, i + 1);
                                temp.put(tempStr, (temp.get(tempStr) == null ? 1 : temp.get(tempStr) + 1));
                            }
                        } else if (i >= maxLength) {
                            int length = maxLength - minLength;
                            for (int j = 0; j < length; j++) {
                                String tempStr = data.substring(i - maxLength + j, i + 1);
                                temp.put(tempStr, (temp.get(tempStr) == null ? 1 : temp.get(tempStr) + 1));
                            }
                        }
                    }
                }
                Log.i("zune", "time = " + (System.currentTimeMillis() - time));
                return temp;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringIntegerMap -> {
                    List<KeyWords> keyWords = new ArrayList<>();
                    for (String s : stringIntegerMap.keySet()) {
                        if (stringIntegerMap.get(s) == null || stringIntegerMap.get(s) < 5) {
                            continue;
                        }
                        KeyWords keyWord = new KeyWords();
                        keyWord.keyWord = s;
                        keyWord.time = stringIntegerMap.get(s);
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
                });
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
        JsoupUtils.getInstance().setImageDir(getExternalFilesDir("img").getPath() + separator);
        JsoupUtils.getInstance().setUnableImagePath(getExternalFilesDir("img").getPath() + separator + "unableImg.txt");
        JsoupUtils.getInstance().setVideoPath(getExternalFilesDir("img").getPath() + separator + "invisibleVideo.txt");
        JsoupUtils.getInstance().setOnResultListener(new JsoupUtils.OnResultListener() {
            @Override
            public void onResult(String url) {
                ToastUtils.showShort(url);
            }
        });
        new Thread() {
            @Override
            public void run() {
                super.run();
                JsoupUtils.getInstance().getContent(null, "http://www.udp2p.com");
            }
        }.start();
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
        clearHolder.getResults().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                clearHolder.dismiss();
                if (index == 0) {
                    String path = (String) clearHolder.getResults().getAdapter().getItem(index);
                    List<ContactBean> contactBeans = startParseXsl(path);
                    List<String> contacts = new ArrayList<>();
                    for (ContactBean contactBean : contactBeans) {
                        contacts.add(contactBean.name + " : " + contactBean.phone);
                    }
                    clearHolder.stopLoad(contacts, false);
                    clearHolder.getResults().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
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
            map = ExcelManager.getInstance().analyzeXlsx(fileName);
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
        clearHolder.getResults().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                if (index == clearHolder.getResults().getAdapter().getCount() - 1) {
                    startFileChoose();
                } else if (index >= 2) {
                    ReadTxtActivity.start(MainActivity.this, saveBean, index - 2);
                } else {
                    String item = (String) clearHolder.getResults().getAdapter().getItem(index);
                    ReadTxtActivity.start(MainActivity.this, item);
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
                clearHolder.getResults().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
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
