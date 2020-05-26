package com.example.jsoup.jsoup;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.ImgData;
import com.example.jsoup.thread.CustomThreadPoolExecutor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.ip;
import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;
import static com.example.jsoup.jsoup.JsoupUtils.getHrefs;
import static com.example.jsoup.jsoup.JsoupUtils.getImgs;

public class GetGifDownloader {

    private static CustomThreadPoolExecutor sPool;
    private int secondaryIndex;
    private static String imageDir = "D:\\user\\zune\\img";

    public static void getGif() {
        File fileDir = new File("D:\\user\\zune\\img");
        Collections.addAll(imageName, fileDir.list());
        if (sPool == null) {
            sPool = new CustomThreadPoolExecutor(100);
        }
        for (int i = 43; i <= 50; i++) {
            for (int j = 1; j < 100; j++) {
                String url;
                if (j == 1) {
                    url = String.format("http://www.5160088.com/html/part/index%s.html", i);
                } else {
                    url = String.format("http://www.5160088.com/html/part/index%s_%s.html", i, j);
                }
                boolean rightUrl = getRightUrl(url);
                if (!rightUrl) {
                    break;
                }
            }
        }
    }

    private static boolean getRightUrl(String url) {
        Document document = getDocument(url);
        List<HrefData> hrefs = getHrefs(document);
        boolean isRight = false;
        for (HrefData href : hrefs) {
            if (href.href.startsWith("/html/article/index")) {
                String rightUrl = "http://www.5160088.com" + href.href;
                //System.out.println(url + "-------------------" + rightUrl);
                parseUrl(rightUrl);
                isRight = true;
            }
        }
        return isRight;
    }

    private static Document getDocument(String url) {
        Document document = null;
        String[] split = ip[(int) (ip.length * Math.random())].split(":");
        String userAgent = userAgents[(int) (userAgents.length * Math.random())];
        String session = "BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJWQ0Y2Y4ZDI1Y2FlZTYyMTZkMzI4ZTZmNGQ4YzEzOTMxBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMVI1Zlp5enVNV2FSV3hWblYzbzlvVzAra3FRQmhIYmxxS1VabC84ZTlEODg9BjsARg%3D%3D--e1511a32fa20a96b7edd158462dc171bd56de578; Hm_lvt_0cf76c77469e965d2957f0553e6ecf59=1575606394; Hm_lpvt_0cf76c77469e965d2957f0553e6ecf59=1575613261";
        try {
            int i = (int) (Math.random() * 1000);////做一个随机延时，防止网站屏蔽
            while (i != 0) {
                i--;
            }
            if (url.startsWith("https")) {
                trustAllHttpsCertificates();
            }
            document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .data("query", "Java")
                    .userAgent(userAgent)
                    .timeout(300000).get();
        } catch (Exception e) {
            try {
                if (url.startsWith("https")) {
                    trustAllHttpsCertificates();
                }
                document = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .data("query", "Java")
                        .userAgent(userAgent)
                        .timeout(300000).post();
            } catch (Exception e1) {
            }
        }
        return document;
    }

    private static void parseUrl(String url) {
        Document document = getDocument(url);
        if (document != null) {
            List<ImgData> imgs = getImgs(document);
            String[] titlesTemp = document.title().split("\\\\");
            String[] titles;
            if (titlesTemp.length == 1) {
                titles = document.title().split("/");
            } else {
                titles = titlesTemp;
            }
            for (int i = 0; i < imgs.size(); i++) {
                System.out.println(imgs.get(i));
                if (!imgs.get(i).src.endsWith("gif")) {
                    //continue;
                }
                if (!imgs.get(i).alt.contains("妖")
                        && !imgs.get(i).alt.contains("拳")
                        && !imgs.get(i).alt.contains("喉咙")
                        && !imgs.get(i).alt.contains("重")
                        && !imgs.get(i).alt.contains("屁眼")
                        && !imgs.get(i).alt.contains("人彘")
                        && !imgs.get(i).alt.contains("男同")
                        && !imgs.get(i).alt.contains("豚")  ) {
//                    break;
                }
                String s = titles.length == 0 ? String.valueOf(System.currentTimeMillis()) : titles[0].split("】")[0] + "】";
                if (imageName.contains(s.replaceAll(" ", ""))) {
                    break;
                }
                ImgData img = imgs.get(i);
                String[] splitImg = img.src.split("\\.");
                String end = "jpg";
                if (splitImg.length == 1) {
                    if (img.src.endsWith("png")) {
                        end = "png";
                    } else if (img.src.endsWith("gif")) {
                        end = "gif";
                    }
                }
                img.alt  = s.replaceAll(" ", "") + i + "." + (splitImg.length == 0 ? end : splitImg[splitImg.length - 1].split("&")[0]);
                if (img.alt.contains("搞笑")) {
                    continue;
                }
                checkDownload(img);
            }
        }
    }

    private static List<String> imageUrls = new ArrayList<>();
    private static Set<String> imageName = new HashSet<>();

    private static void checkDownload(ImgData img) {
        if (!imageUrls.contains(img.src)) {
            imageUrls.add(img.src);
            sPool.execute(new MyRunnable(img) {
                @Override
                void run(ImgData img) {
                    downloadPicture(img.src, imageDir + "\\" + img.alt.split("】")[0] + "】", img.alt);
                }
            });
        }
    }

    //链接url下载图片
    private static void downloadPicture(String imageUrl, String dir, String imageName) {
        System.out.println(imageName + "..................." + imageUrl);
        long time = System.currentTimeMillis();
        URL url;
        int responseCode = 0;
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(fileDir, imageName.replaceAll("\\\\", "").replaceAll("/", "")
                .replaceAll(":", "").replaceAll("\\*", "").replaceAll("\\?", "")
                .replaceAll("\\\"", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("|", "").replaceAll("\\|", "").replaceAll(" ", ""));
        try {
            url = new URL(imageUrl);
            if (imageUrl.startsWith("https")) {
                trustAllHttpsCertificates();
            }
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(2000000);
            http.setReadTimeout(2000000);
            http.setRequestProperty("user-agent", userAgents[(int) (userAgents.length * Math.random())]);
            http.setRequestMethod("GET");
            http.setInstanceFollowRedirects(false);
            http.connect();
            responseCode = http.getResponseCode();
            if (responseCode != 200) {
                return;
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream is = http.getInputStream();
            OutputStream os = new FileOutputStream(file);
            byte by[] = new byte[1024];
            int len = 0;
            while ((len = is.read(by, 0, by.length)) != -1) {
                os.write(by, 0, len);
            }
            os.close();
            is.close();
            if (file.length() < 100 * 1024) {
                file.delete();
            }
        } catch (Exception e) {
            if (!"Connection timed out: connect".equals(e.getMessage())
                    && !"imgroom.net".equals(e.getMessage())
                    && !"item.slide.com".equals(e.getMessage())
                    && !"bbs.crsky.com".equals(e.getMessage())
                    && !"cdn.imagehost.click".equals(e.getMessage())
                    && !"sadpanda.us".equals(e.getMessage())
                    && !"Address already in use: connect".equals(e.getMessage())
                    && !"preview.filesonic.com".equals(e.getMessage())
                    && !"Unexpected end of file from server".equals(e.getMessage())
                    && !"i.minus.com".equals(e.getMessage())
                    && !"i.min.us".equals(e.getMessage())
                    && !"i0.uyl.me".equals(e.getMessage())
                    && !"Connection reset".equals(e.getMessage())) {
                System.out.println("zune: downloadImg: " + imageUrl
                        + ", e = " + e.getMessage() + ", 共耗时：" + (System.currentTimeMillis() - time) + "毫秒" + "responseCode = " + responseCode);
            } else {
                System.out.println("zune: downloadImg: " + imageUrl
                        + ", e = " + e.getMessage() + ", 共耗时：" + (System.currentTimeMillis() - time) + "毫秒" + "responseCode = " + responseCode);
            }
            if (file.exists()) {
                file.delete();
            }
        }
    }


    public static abstract class MyRunnable implements Runnable {
        ImgData url;

        public MyRunnable(ImgData url) {
            this.url = url;
        }

        @Override
        public void run() {
            run(url);
        }

        abstract void run(ImgData position);
    }
}
