package com.example.android.askquastionapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.besar.BesarActivity;
import com.example.android.askquastionapp.clean.ClearHolder;
import com.example.android.askquastionapp.contacts.ContactBean;
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
import com.example.android.askquastionapp.utils.DocumentsFileUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.SaveUtils;
import com.example.android.askquastionapp.video.ListenMusicActivity;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.wxapi.ShareDialog;
import com.example.android.askquastionapp.xmlparse.ExcelManager;
import com.example.jsoup.jsoup.JsoupUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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
    public static List<String> sExtSdCardPaths = new ArrayList<>();

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
        findViewById(R.id.read_txt).setOnClickListener(v -> startReadTxt());
        findViewById(R.id.fenbei).setOnClickListener(v -> starFenbei());
        findViewById(R.id.map).setOnClickListener((v) -> LocationActivity.start(MainActivity.this));
        findViewById(R.id.besar).setOnClickListener(v -> BesarActivity.start(MainActivity.this));
        findViewById(R.id.math).setOnClickListener(v -> MathFunActivity.start(MainActivity.this));
        findViewById(R.id.constants).setOnClickListener(v -> getConstants());
        findViewById(R.id.delete_dir).setOnClickListener(v -> deleteDir());
        findViewById(R.id.share).setOnClickListener(v -> share());
        findViewById(R.id.parse_xsl).setOnClickListener(v -> parseXsl());
        findViewById(R.id.parse_url).setOnClickListener(v -> parseUrl());
        findViewById(R.id.normal_video).setOnClickListener(v -> {
            if (new File(movieFile).exists()) {
                WatchVideoActivity.start(MainActivity.this, movieFile);
            } else {
                gzipFiles();
            }
        });
        findViewById(R.id.av_video).setOnClickListener(v -> {
            if (new File(avFile).exists()) {
                WatchVideoActivity.start(MainActivity.this, avFile);
            } else {
                gzipFiles();
            }
        });
        findViewById(R.id.single_music).setOnClickListener(v -> {
            if (new File(musicFile).exists()) {
                ListenMusicActivity.start(MainActivity.this, musicFile);
            } else {
                gzipFiles();
            }
        });
        findViewById(R.id.replace_bitmap).setOnClickListener(v -> replaceBitmap());
        findViewById(R.id.look_pic).setOnClickListener(v -> lookPic());
        findViewById(R.id.reader).setOnClickListener(v -> ReaderListActivity.start(MainActivity.this));
        findViewById(R.id.js_save).setOnClickListener(v -> saveJs());
        findViewById(R.id.js_read).setOnClickListener(v -> readJs());
        findViewById(R.id.android_q).setOnClickListener(v -> startNotifycationQ());
        ClearUtils.getInstance().getAppProcessName(this);
        findViewById(R.id.src_image).setOnClickListener(v -> startLoadImg());
        findViewById(R.id.sd_card).setOnClickListener(v -> sdCard());
    }

    private void sdCard() {
        if (!DocumentsFileUtils.getInstance().hasPermissions(this)) {
            return;
        }
        readSdCard();
        writeToSdCard();
    }

    private void writeToSdCard() {
        if (DocumentsFileUtils.getInstance().rootPath == null) {
            return;
        }
        File file = new File(DocumentsFileUtils.getInstance().rootPath[DocumentsFileUtils.getInstance().rootPath.length - 1] + separator + "测试.txt");
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
        List<String> strings = new ArrayList<>();
        for (String s : DocumentsFileUtils.getInstance().rootPath) {
            DocumentFile document = DocumentsFileUtils.getInstance().getUriDocumentFile(s);
            File file = DocumentsFileUtils.getInstance().documentToFile(document);
            for (File listFile : file.listFiles()) {
                strings.add("存储设备是：" + s + "; 路径是：" + listFile.getPath());
            }
        }
        if (clearHolder == null) {
            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
        }
        clearHolder.stopLoad(strings, false);
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
        PictureActivity.start(this, imageDir);
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
}
