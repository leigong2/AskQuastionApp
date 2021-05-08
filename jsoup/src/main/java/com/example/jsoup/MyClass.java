package com.example.jsoup;

import com.example.jsoup.jsoup.GzipUtils;
import com.example.jsoup.jsoup.webloaddata.XiqumeUtils;
import com.example.jsoup.reader.MergeTxtUtils;
import com.example.jsoup.reader.TelegramUtils;

import java.io.File;

public class MyClass {
    public static boolean sStop;

    public static void main(String[] args) {
        //http://www.mgy5.com/  http://www.rerere888.com/arttype/2.html
//        HlsDownloader.getInstance().download("https://www.2344ww.com/vod/html16/24564_down_0.html", "D:\\user\\zune\\img", "test");
//        SimpleThread simpleThread = new SimpleThread("https://jobs.zhaopin.com/");
//        SimpleThread simpleThread = new SimpleThread("http://www.9ku.com/music/t_new.htm");
//        SimpleThread simpleThread = new SimpleThread("http://www.nuu22.com");
//        SimpleThread simpleThread = new SimpleThread("http://www.rensheng5.com/gushihui/mjgs/id-179122_2.html");
//        SimpleThread simpleThread = new SimpleThread("https://www.zybang.com/question/96d3380ee3dd6a9a761e00d72c03f30d.html");
//        SimpleThread simpleThread = new SimpleThread("http://www.rerere10.com/index.php/arttype/26-2.html");
//        SimpleThread simpleThread = new SimpleThread("https://100fj.xyz/search.php?key=%E5%B1%81%E7%9C%BC");
//        SimpleThread simpleThread = new SimpleThread("http://www.kandegang.cn/video/kengwang/1140.html");
//        SimpleThread simpleThread = new SimpleThread("https://spritiualne.wordpress.com/2021/01/24/%e5%a8%bc%e5%a6%93%e6%94%b9%e9%80%a0%e7%89%88/");
//        simpleThread.start();
//        DownloadPictureUtils.loadImg("https://588ku.com/ycpng/13031081.html");
//        http://www.365jjwy.com/
//        https://nenai.xyz/vodsearch.html
//        http://wap.qiliuxs.com/
//        http://www.555avs.com/
//        https://www.4hudd09.com
//        http://0341.xyz/
//        https://www.kuaibo555.com/
//        https://tieniu.tv
//        https://567863.net/
//        http://www.mm6111.com
//        http://www.ccpc31.com
//        http://www.wyxun.com/
//        http://www.118jie.com/ccls_38/
//        https://www.geise.cc/
//        http://ayjyf.com/
//        https://www.pcc68.com/
//        http://www.fvhthnjr0i3v.buzz/



        XiqumeUtils.load();
//        GetGifDownloader.getFileDetail();
        /*UiUtil.getInstance().showDialog("图片加载器", new UiUtil.CallBack() {
            @Override
            public void onChooseFileDir(String path) {
                System.out.println("path = " + path);
                if (!StringUtils.isNullOrEmpty(path)) {
                    GetGifDownloader.imageDir = path;
                }
            }

            @Override
            public void onStatusChange(boolean start, String path) {
                System.out.println(start ? "开始下载了" : "停止下载了");
                if (!StringUtils.isNullOrEmpty(path)) {
                    GetGifDownloader.imageDir = path;
                }
                if (start) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            sStop = false;
                            GetGifDownloader.getGif();
                        }
                    }.start();
                } else {
                    stopEvery();
                }
            }
        });*/
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

    public static void stopEvery() {
        sStop = true;
    }
}
