package com.example.jsoup.jsoup;


import com.example.jsoup.bean.VideoBean;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsoupUtils {
    private static JsoupUtils jsoupUtils;
    private static String videoPath = "D:\\user\\zune\\invisibleVideo.txt";
    private static String m3u8Path = "D:\\user\\zune\\invisibleM3U8Video.txt";
    private static String unableImagePath = "D:\\user\\zune\\unableImg.txt";
    private static String enableImagePath = "D:\\user\\zune\\enableImg.txt";
    private static String musicPath = "D:\\user\\zune\\music.txt";
    private static String dbDir = "D:\\user\\zune\\db";
    public static String musicDBPath = "D:\\user\\zune\\db\\music_db.db";
    public static String movieDBPath = "D:\\user\\zune\\db\\movie_db.db";
    public static String videoDBPath = "D:\\user\\zune\\db\\video_db.db";
    public static String avDBPath = "D:\\user\\zune\\db\\av_db.db";
    public static Set<String> srcUrls = new HashSet<>();

    private JsoupUtils() {
        File file = new File(dbDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("video_type", "");
        List<VideoBean> m = SqliteUtils.getInstance(movieDBPath).queryData("video_bean", map, VideoBean.class, 1);
        for (VideoBean bean : m) {
            srcUrls.add(bean.video_url);
        }
        System.out.println("src = " + srcUrls.size());
    }

    public static JsoupUtils getInstance() {
        if (jsoupUtils == null) {
            jsoupUtils = new JsoupUtils();
        }
        return jsoupUtils;
    }

    private static File sM3u8File;
    private static FileWriter sM3u8Fos;
    private static File sVideoFile;
    private static FileWriter sVideoFos;
    private static File sUnableImageFile;
    private static FileWriter sUnableImageFos;
    private static File sEnableImageFile;
    private static FileWriter sEnableImageFos;
    private static File sMusicFile;
    private static FileWriter sMusicFos;

    public static void writeToTxt(String path, String imageUrl) throws Exception {
        if (path.equals(m3u8Path)) {
            if (sM3u8File == null) {
                sM3u8File = new File(path);
                if (!sM3u8File.exists()) {
                    sM3u8File.createNewFile();
                }
                sM3u8Fos = new FileWriter(sM3u8File, true);
            }
            sM3u8Fos.write(imageUrl + "\n");
        } else if (path.equals(videoPath)) {
            if (sVideoFile == null) {
                sVideoFile = new File(path);
                if (!sVideoFile.exists()) {
                    sVideoFile.createNewFile();
                }
                sVideoFos = new FileWriter(sVideoFile, true);
            }
            sVideoFos.write(imageUrl + "\n");
        } else if (path.equals(unableImagePath)) {
            if (sUnableImageFile == null) {
                sUnableImageFile = new File(path);
                if (!sUnableImageFile.exists()) {
                    sUnableImageFile.createNewFile();
                }
                sUnableImageFos = new FileWriter(sUnableImageFile, true);
            }
            sUnableImageFos.write(imageUrl + "\n");
        } else if (path.equals(enableImagePath)) {
            if (sEnableImageFile == null) {
                sEnableImageFile = new File(path);
                if (!sEnableImageFile.exists()) {
                    sEnableImageFile.createNewFile();
                }
                sEnableImageFos = new FileWriter(sEnableImageFile, true);
            }
            sEnableImageFos.write(imageUrl + "\n");
        } else if (path.equals(musicPath)) {
            if (sMusicFile == null) {
                sMusicFile = new File(path);
                if (!sMusicFile.exists()) {
                    sMusicFile.createNewFile();
                }
                sMusicFos = new FileWriter(sMusicFile, true);
            }
            sMusicFos.write(imageUrl + "\n");
        }
    }

}
