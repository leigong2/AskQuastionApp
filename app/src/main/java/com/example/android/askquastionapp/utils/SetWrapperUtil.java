package com.example.android.askquastionapp.utils;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.blankj.utilcode.util.SPUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.bean.ImageTag;
import com.example.android.askquastionapp.reader.ReaderActivity;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by leigong2 on 2018-03-18 018.
 */

public class SetWrapperUtil {
    private List<ImageTag> images;
    private static SetWrapperUtil util = new SetWrapperUtil();

    private SetWrapperUtil() {
    }

    public static SetWrapperUtil getInstanse() {
        return util;
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1001) {
            } else if (msg.what == 1002) {
                //Todo 加载完毕
                removeImage();
                Collections.sort(images);
                for (int i = 0; i < images.size(); i++) {
                    Log.i("zune: ", "position = " + images.get(i).position + ", url = "
                            + images.get(i).url);
                }
                SPUtils.getInstance().put("image", GsonGetter.getInstance().getGson().toJson(images));
                setImages();
            } else if (msg.what == 1003) {
                Bitmap bitmap = (Bitmap) msg.obj;
                if (bitmap == null) {
                    return;
                }
                WallpaperManager manager = WallpaperManager.getInstance(BaseApplication.getInstance());
                try {
                    WindowManager wm = (WindowManager) BaseApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
                    int width = wm.getDefaultDisplay().getWidth();
                    int height = wm.getDefaultDisplay().getHeight();
                    if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
                        bitmap = resizeBitmap(bitmap);
                    }
                    if (width > height) {
                        return;
                    }
                    Log.i("zune: ", "设置壁纸的size = " + bitmap.getWidth() + ".." + bitmap.getHeight());
                    Rect rect = new Rect(0, 0, width, height);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, rect, false
                                , WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                    } else {
                        manager.setBitmap(bitmap);
                    }
                    Log.i("zune: ", "设置好壁纸了");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void createImages() {
        images = GsonGetter.getInstance().getGson().fromJson(SPUtils.getInstance().getString("image"), new TypeToken<List<ImageTag>>() {
        }.getType());
        if (images == null) {
            images = new ArrayList<>();
        }
        if (needUpdate() || images.size() < 8) {
            images.clear();
            for (int i = 0; i < 8; i++) {
                getImages(i);
            }
        } else {
            removeImage();
            Collections.sort(images);
            for (int i = 0; i < images.size(); i++) {
                Log.i("zune: ", "position = " + images.get(i).position + ", url = "
                        + images.get(i).url);
            }
            setImages();
        }
    }

    /*zune： 去重**/
    private void removeImage() {
        if (images.size() == 8) {
            return;
        }
        List<ImageTag> temp = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            if (!contains(temp, images.get(i))) {
                temp.add(images.get(i));
            }
        }
        Log.i("zune: ", "temp size = " + temp.size());
        images.clear();
        images.addAll(temp);
    }

    /*zune： 判断集合中是否有该元素**/
    private boolean contains(List<ImageTag> temp, ImageTag imageTag) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).position == imageTag.position) {
                return true;
            }
        }
        return false;
    }

    /*zune： 判断壁纸是否需要更新*/
    private boolean needUpdate() {
        long date = SPUtils.getInstance().getLong("mills", -1L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.CHINA);
        Date dates = new Date(date);
        String format = sdf.format(dates);
        String[] formats = format.split("-");

        String now = sdf.format(new Date(System.currentTimeMillis()));
        String[] nows = now.split("-");
        if (Integer.parseInt(formats[0]) < Integer.parseInt(nows[0])) {
            return true;
        }
        if (Integer.parseInt(formats[1]) < Integer.parseInt(nows[1])) {
            return true;
        }
        if (Integer.parseInt(formats[2]) < Integer.parseInt(nows[2])) {
            return true;
        }
        if (Integer.parseInt(formats[3]) < Integer.parseInt(nows[3])) {
            return true;
        }
        return false;
    }

    /*zune： 判断指定的那天，有没有本地的bing图片**/
    private File hasImage(int nextInt) {
        String SAVE_PIC_PATH = getSavePicPath();//保存到SD卡
        String SAVE_REAL_PATH = SAVE_PIC_PATH + "/Bing";//保存的确切位置
        Log.i("zune: ", "保存图片 = " + SAVE_REAL_PATH);
        File fileDir = new File(SAVE_REAL_PATH);
        String fileName = getName(nextInt) + ".jpg";
        File file = new File(fileDir, fileName);
        return file;
    }

    @NotNull
    private String getSavePicPath() {
        return DocumentsFileUtils.getInstance().getExtSDCardPath(BaseApplication.getInstance());
    }

    /*zune： 根据距离今天的天数， 获取那时的网络bing图片**/
    private void getImages(final int i) {
        new Thread() {
            @Override
            public void run() {
                String path = "http://cn.bing.com/HPImageArchive.aspx?idx=" + i + "&n=1";
                try {
                    Document document = Jsoup.connect(path)
                            .userAgent(ReaderActivity.userAgents[(int) (ReaderActivity.userAgents.length * Math.random())])
                            .timeout(30000).post();
                    Elements elements = document.getElementsByTag("url");
                    String imageUrl = null;
                    for (Element element : elements) {
                        for (int j = 0; j < element.childNodeSize(); j++) {
                            Node node = element.childNode(j);
                            if (node instanceof TextNode) {
                                String wholeText = ((TextNode) node).getWholeText();
                                String temp = "http://cn.bing.com" + wholeText;
                                imageUrl = temp.replace("1920x1080", "1080x1920");
                            }
                        }
                    }
                    ImageTag imageTag = new ImageTag();
                    imageTag.url = imageUrl;
                    imageTag.position = i;
                    images.add(imageTag);
                } catch (Exception e) {
                    Log.i("zune: ", "getImages e = " + e);
                }
                if (images.size() == 8) {
                    handler.sendEmptyMessage(1002);
                }
            }
        }.start();
    }

    /*zune： 随机选择的8天图片，获取一张，并转换为bitmap， 设置壁纸**/
    private void setImages() {
        Log.i("zune: ", "images size = " + images.size());
        if (images.size() == 0) {
            return;
        }
        SPUtils.getInstance().put("mills", System.currentTimeMillis());
        new Thread() {
            @Override
            public void run() {
                try {
                    // 通过网络地址创建URL对象
                    Random random = new Random();
                    int nextInt = random.nextInt(images.size());
                    Log.i("zune: ", "nextInt = " + nextInt);
                    File file = hasImage(images.get(nextInt).position);
                    if (file != null) {
                        Bitmap image = getBitmap(file);
                        if (file.exists() && image != null) {
                            Message msg = new Message();
                            msg.what = 1003;
                            msg.obj = image;
                            handler.sendMessage(msg);
                            return;
                        }
                    }
                    URL url = new URL(images.get(nextInt).url);
                    // 根据URL
                    // 打开连接，URL.openConnection函数会根据URL的类型，返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // 设定URL的请求类别，有POST、GET 两类
                    conn.setRequestMethod("GET");
                    //设置从主机读取数据超时（单位：毫秒）
                    conn.setConnectTimeout(5000);
                    //设置连接主机超时（单位：毫秒）
                    conn.setReadTimeout(5000);
                    InputStream inputStream = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        Log.i("zune: ", "网络resize之前的size = " + bitmap.getWidth() + ".." + bitmap.getHeight());
                        Bitmap resize = resizeBitmap(bitmap);
                        Log.i("zune: ", "网络resize之后的size = " + resize.getWidth() + ".." + resize.getHeight());
                        saveBitmap(resize, images.get(nextInt).position);
                        Message msg = new Message();
                        msg.what = 1003;
                        msg.obj = resize;
                        handler.sendMessage(msg);
                    } else {
                        dispatchLocal();
                    }
                } catch (Exception e) {
                    Log.i("zune: ", "setImages e = " + e);
                    dispatchLocal();
                }
            }
        }.start();
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        WindowManager wm = (WindowManager) BaseApplication.getInstance()
                .getSystemService(Context.WINDOW_SERVICE);
        float width = wm.getDefaultDisplay().getWidth();
        float height = wm.getDefaultDisplay().getHeight();
        float bitmapHeight = bitmap.getHeight();
        float bitmapWidth = bitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(width / bitmapWidth, height / bitmapHeight); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth()
                , bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    /*zune： 从本地bing图片中，随机选择一张，设置壁纸**/
    private void dispatchLocal() {
        String SAVE_PIC_PATH = getSavePicPath();//保存到SD卡
        String SAVE_REAL_PATH = SAVE_PIC_PATH + "/Bing";//保存的确切位置
        Log.i("zune: ", "保存图片 = " + SAVE_REAL_PATH);
        File fileDir = new File(SAVE_REAL_PATH);
        File[] files = fileDir.listFiles();
        if (files != null && files.length > 0) {
            Random random = new Random();
            int nextInt = random.nextInt(files.length);
            File file = files[nextInt];
            Bitmap image = getBitmap(file);
            if (file.exists() && image != null) {
                Message msg = new Message();
                msg.what = 1003;
                msg.obj = image;
                handler.sendMessage(msg);
                return;
            }
        } else {
        }
    }

    /*zune： file转换bitmap**/
    private Bitmap getBitmap(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            if (bitmap != null) {
                Log.i("zune: ", "本地resize之前的size = " + bitmap.getWidth() + ".." + bitmap.getHeight());
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*zune： 将指定那一天的bitmap，保存到本地**/
    private void saveBitmap(Bitmap bitmap, int nextInt) {
        String SAVE_PIC_PATH = getSavePicPath();//保存到SD卡
        String SAVE_REAL_PATH = SAVE_PIC_PATH + "/Bing";//保存的确切位置
        Log.i("zune: ", "保存图片 = " + SAVE_REAL_PATH);
        File fileDir = new File(SAVE_REAL_PATH);
        if (!fileDir.exists()) {
            boolean mkdir = fileDir.mkdirs();
            Log.i("zune: ", "mkDir = " + mkdir);
        }
        String fileName = getName(nextInt) + ".jpg";
        File file = new File(fileDir, fileName);
        try {
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                Log.i("zune: ", "newFile = " + newFile);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("zune: ", "file e = " + e);
        }

        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        BaseApplication.getInstance().sendBroadcast(intent);
    }

    /*zune： 根据索引，获取指定的具体是哪一天**/
    private String getName(int nextInt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String format = sdf.format(new Date(System.currentTimeMillis() - nextInt * 24 * 1000 * 3600L));
        return format;
    }
}