package com.example.android.askquastionapp.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.view.WindowManager;

import com.blankj.utilcode.util.SPUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.bean.ImageTag;
import com.example.android.askquastionapp.reader.ReaderActivity;
import com.example.android.askquastionapp.video.DownloadObjManager;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by leigong2 on 2018-03-18 018.
 */

public class SetWrapperUtil {
    private List<ImageTag> images;
    private static SetWrapperUtil util = new SetWrapperUtil();

    public static SetWrapperUtil getInstanse() {
        return util;
    }

    private void setWallpaper(Bitmap bitmap) {
        WallpaperManager manager = WallpaperManager.getInstance(BaseApplication.getInstance());
        try {
            WindowManager wm = (WindowManager) BaseApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
                bitmap = resizeBitmap(bitmap);
            }
            Rect rect = new Rect(0, 0, width, height);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.setBitmap(bitmap, rect, false, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
            } else {
                manager.setBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createImages() {
        images = GsonGetter.getInstance().getGson().fromJson(SPUtils.getInstance().getString("image", "[]"), new TypeToken<List<ImageTag>>() {
        }.getType());
        if (!needUpdate()) {
            setImages();
            return;
        }
        getImages();
    }

    /*zune： 判断壁纸是否需要更新*/
    private boolean needUpdate() {
        if (images.size() < 8) {
            return true;
        }
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
        return false;
    }

    /*zune： 判断指定的那天，有没有本地的bing图片**/
    private File hasImage(int nextInt) {
        File fileDir = new File(getSavePicPath() + "/Bing");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String fileName = getName(nextInt) + ".jpg";
        return new File(fileDir, fileName);
    }

    @NotNull
    private String getSavePicPath() {
        return DocumentsFileUtils.getInstance().getExtSDCardPath(BaseApplication.getInstance());
    }

    /*zune： 根据距离今天的天数， 获取那时的网络bing图片**/
    private void getImages() {
        Observable.just(1).map(new Function<Integer, List<ImageTag>>() {
            @Override
            public List<ImageTag> apply(Integer integer) throws Exception {
                List<ImageTag> imageUrls = new ArrayList<>();
                int unDownloadSize = getUnDownloadSize();
                for (int index = 0; index < unDownloadSize; index++) {
                    String path = "http://cn.bing.com/HPImageArchive.aspx?idx=" + index + "&n=1";
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
                                if (contains(images, imageUrl)) {
                                    continue;
                                }
                                imageUrls.add(new ImageTag(imageUrl, imageUrls.size()));
                                File fileDir = new File(getSavePicPath() + "/Bing");
                                if (!fileDir.exists()) {
                                    fileDir.mkdirs();
                                }
                                DownloadObjManager.getInstance().startDownload(imageUrl, new File(fileDir, getName(index) + ".jpg").getPath());
                            }
                        }
                    }
                }
                return imageUrls;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ImageTag>, Integer>(1, false) {
                    @Override
                    public void onNext(List<ImageTag> imageUrls, Integer integer) {
                        for (ImageTag image : images) {
                            image.position += imageUrls.size();
                        }
                        for (int i = imageUrls.size() - 1; i >= 0; i--) {
                            images.add(0, imageUrls.get(i));
                            if (images.size() > 8) {
                                images.remove(images.size() - 1);
                            }
                        }
                        SPUtils.getInstance().put("image", GsonGetter.getInstance().getGson().toJson(images));
                        setImages();
                    }
                });
    }

    private int getUnDownloadSize() {
        File fileDir = new File(getSavePicPath() + "/Bing");
        File[] files = fileDir.listFiles();
        if (!fileDir.exists() || files == null) {
            return 8;
        }
        String today = getName(0);
        long todayTime = getDataTime(today);
        int downloadCount = 0;
        for (File file : files) {
            String name = file.getName().replaceAll(".jpg", "");
            long dataTime = getDataTime(name);
            if (todayTime - dataTime >= 0 && todayTime - dataTime < 8 * 3600 * 24) {
                downloadCount++;
            }
        }
        return 8 - downloadCount;
    }

    public long getDataTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date parse = sdf.parse(time);
            if (parse != null) {
                return parse.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    private boolean contains(List<ImageTag> images, String imageUrl) {
        for (ImageTag image : images) {
            if (image.url != null && image.url.equalsIgnoreCase(imageUrl)) {
                return true;
            }
        }
        return false;
    }

    /*zune： 随机选择的8天图片，获取一张，并转换为bitmap， 设置壁纸**/
    private void setImages() {
        if (images.size() == 0) {
            return;
        }
        SPUtils.getInstance().put("mills", System.currentTimeMillis());
        // 通过网络地址创建URL对象
        Random random = new Random();
        int nextInt = random.nextInt(images.size());
        LogUtils.i("zune: ", "nextInt = " + nextInt);
        File file = hasImage(images.get(nextInt).position);
        if (file.exists()) {
            setWallpaper(BitmapFactory.decodeFile(file.getPath()));
            return;
        }
        DownloadObjManager.getInstance().startDownload(images.get(nextInt).url, file.getPath(), new MainActivity.CallBack() {
            @Override
            public void onCallBack() {
                setWallpaper(BitmapFactory.decodeFile(file.getPath()));
            }
        });
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

    /*zune： 根据索引，获取指定的具体是哪一天**/
    private String getName(int nextInt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String format = sdf.format(new Date(System.currentTimeMillis() - nextInt * 24 * 1000 * 3600L));
        return format;
    }
}