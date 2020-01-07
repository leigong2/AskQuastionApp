package com.example.jsoup;

import com.example.jsoup.bean.VideoBean;
import com.example.jsoup.jsoup.GetGifDownloader;
import com.example.jsoup.jsoup.HlsDownloader;
import com.example.jsoup.jsoup.JsoupUtils;
import com.example.jsoup.jsoup.SqliteUtils;
import com.example.jsoup.thread.SimpleThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.jsoup.jsoup.JsoupUtils.avDBPath;
import static com.example.jsoup.jsoup.JsoupUtils.movieDBPath;
import static com.example.jsoup.jsoup.JsoupUtils.videoDBPath;

public class MyClass {
    public static void main(String[] args) {
        //http://www.mgy5.com/  http://www.rerere888.com/arttype/2.html
//        HlsDownloader.getInstance().download("https://www.2344ww.com/vod/html16/24564_down_0.html", "D:\\user\\zune\\img", "test");
//        SimpleThread simpleThread = new SimpleThread("https://jobs.zhaopin.com/");
//        SimpleThread simpleThread = new SimpleThread("https://www.2344ww.com");
        SimpleThread simpleThread = new SimpleThread("http://www.rensheng5.com/gushihui/mjgs/id-179122_2.html");
        simpleThread.start();
//        GetGifDownloader.getGif();
//        FileUtil.getFileName("D:\\user\\zune\\img");

        /*for (int i = 0; i < 74470; i++) {
            Map<String, Object> oldMap = new HashMap<>();
            oldMap.put("page", i);
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("id", i);
            int count = SqliteUtils.getInstance(movieDBPath).updateData("video_bean", oldMap, newMap);
            System.out.println(count);
        }*/
        /*Map<String, Object> map = new HashMap<>();
        map.put("video_type", "");
        List<VideoBean> m = SqliteUtils.getInstance(movieDBPath).queryData("video_bean", map, VideoBean.class, 1);
        System.out.println(m);*/
        /*File file = new File("D:\\user\\zune\\db\\movie_db.gz");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File src = new File("D:\\user\\zune\\db\\movie_db.db");
        File[] srcFiles = new File[] {src};
        GzipUtils.zipFiles(srcFiles, file);*/

        /*Map<String, Object> map = new HashMap<>();
        map.put("page", 0);
        List<VideoBean> m = SqliteUtils.getInstance(avDBPath).queryData("video_bean", map, VideoBean.class, 10);
        System.out.println(m);*/
//        HlsDownloader.getInstance().download("http://42.51.43.168/20190128/03_1.m3u8", "D:\\user\\zune\\video", "测试");
    }
}
