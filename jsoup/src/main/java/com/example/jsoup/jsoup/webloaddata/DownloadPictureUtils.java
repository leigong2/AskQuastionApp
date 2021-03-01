package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.MyClass;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.ImgData;
import com.example.jsoup.thread.CustomThreadPoolExecutor;
import com.mysql.cj.util.StringUtils;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;
import static com.example.jsoup.jsoup.JsoupUtils.writeToTxt;

public class DownloadPictureUtils extends BaseWebLoadUtils {

    private static String enableImagePath = "D:\\user\\zune\\enableImg.txt";
    private static String unableImagePath = "D:\\user\\zune\\unableImg.txt";
    private static String imageDir = "D:\\user\\zune\\img";
    private static List<String> imageUrls = new ArrayList<>();
    private static Set<String> imageName = new HashSet<>();

    public static void getImg() {
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                File fileDir = new File(imageDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
//        http://www.rerere10.com/index.php/arttype/26-1667.html
//        http://822jjj.com/html/article/index26321.html
                for (int i = 2; i <= 64; i++) {
                    String url = String.format("http://99pbec.xyz/wvov-13-%s.asp", i);
                    Document document = read(url);
                    if (document == null) {
                        continue;
                    }
                    Elements elements = document.getElementsByTag("a");
                    for (Element element : elements) {
                        String nodeText = getNodeText(element);
                        String href = element.attr("href");
                        if (href != null && href.contains("qqji-")) {
                            parseUrl( "http://99pbec.xyz" + href, nodeText);
                        }
                    }
                }
            }
        });
    }

    public static void getGif() {
        File fileDir = new File(imageDir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (fileDir.list() != null && fileDir.list().length > 0) {
            Collections.addAll(imageName, fileDir.list());
        }
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
        if (MyClass.sStop) {
            return false;
        }
        Document document = read(url);
        List<HrefData> hrefs = getHrefs(document);
        boolean isRight = false;
        for (HrefData href : hrefs) {
            if (href.href.startsWith("/html/article/index")) {
                String rightUrl = "http://www.5160088.com" + href.href;
                //System.out.println(url + "-------------------" + rightUrl);
                parseUrl(rightUrl, href.title);
                isRight = true;
            }
        }
        return isRight;
    }

    private static void parseUrl(String url, String title) {
        Document document = read(url);
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
                        && !imgs.get(i).alt.contains("豚")) {
//                    break;
                }
                String s = titles.length == 0 ? String.valueOf(System.currentTimeMillis()) : titles[0].split("】")[0].split("]")[0] + "]";
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
                img.alt = s.replaceAll(" ", "") + i + "." + (splitImg.length == 0 ? end : splitImg[splitImg.length - 1].split("&")[0]);
                if (title != null) {
                    img.alt = title + "." + end;
                }
                if (img.alt.contains("搞笑")) {
                    continue;
                }
                checkDownload(img);
            }
        }
    }

    private static void checkDownload(ImgData img) {
        if (!imageUrls.contains(img.src)) {
            imageUrls.add(img.src);
            sPool.execute(new BaseRunnable(new HrefData(null, img.src, img.alt)) {
                @Override
                public void run(HrefData hrefData) {
                    downloadPicture(hrefData.title, imageDir + "\\" + hrefData.text.split("】")[0].split("]")[0] + "]", hrefData.text, null);
                }
            });
        }
    }

    /**
     * 获取页面中所有img标签
     *
     * @return
     */
    public static List<ImgData> getImgs(Document document) {
        Element head = document.head();
        Elements title = head.getElementsByTag("meta");
        String imageTitle = null;
        for (Element element : title) {
            Attributes attributes = element.attributes();
            for (int i = 0; i < attributes.size(); i++) {
                if (attributes.asList().size() > 1 && "keywords".equals(attributes.asList().get(0).getValue()) && StringUtils.isNullOrEmpty(imageTitle)) {
                    imageTitle = attributes.asList().get(1).getValue().split("】")[0] + "】";
                    break;
                }
                if (attributes.asList().size() > 1 && "description".equals(attributes.asList().get(0).getValue()) && StringUtils.isNullOrEmpty(imageTitle)) {
                    imageTitle = attributes.asList().get(1).getValue().split("】")[0] + "】";
                    break;
                }
            }
        }
        if (StringUtils.isNullOrEmpty(imageTitle)) {
            imageTitle = document.title().split("】")[0] + "】";
        }
        Elements attr = document.getElementsByTag("img");
        List<ImgData> imgDataList = new ArrayList<ImgData>();
        for (int i = 0; i < attr.size(); i++) {
            Element element = attr.get(i);
            String src = element.attr("src");
            String s = StringUtils.isNullOrEmpty(element.attr("alt")) ? imageTitle : element.attr("alt");
            ImgData imgData = new ImgData(src, s.split("】")[0] + "】" + i);//实体对象构造方法，代码省略
            imgDataList.add(imgData);
        }
        return imgDataList;
    }

    public void downLoadImg(String downloadUrl, String imageName, OnResultListener onResultListener) {
        if (imageUrls.contains(downloadUrl)) {
            System.out.println("zune downloadUrl =  " + downloadUrl);
        } else {
            imageUrls.add(downloadUrl);
            downloadPicture(downloadUrl, imageDir, imageName, onResultListener);
        }
    }

    //链接url下载图片
    private static void downloadPicture(String imageUrl, String dir, String imageName, OnResultListener onResultListener) {
        long time = System.currentTimeMillis();
        File fileDir = new File(dir);
        File file = new File(fileDir, imageName.replaceAll("\\\\", "").replaceAll("/", "")
                .replaceAll(":", "").replaceAll("\\*", "").replaceAll("\\?", "")
                .replaceAll("\\\"", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("|", "").replaceAll("\\|", "").replaceAll(" ", ""));
        if (file.exists()) {
            return;
        }
        URL url;
        int responseCode = 0;
        try {
            url = new URL(imageUrl);
            if (imageUrl.startsWith("https")) {
                trustAllHttpsCertificates();
            }
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(50000000);
            http.setReadTimeout(50000000);
            http.setRequestMethod("GET");
            http.setInstanceFollowRedirects(false);
            http.setRequestProperty("referer", "http://99pbec.xyz");
            http.connect();
            responseCode = http.getResponseCode();
            if (responseCode != 200) {
                return;
            }
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("zune: createNewFile file = " + file.getPath());
                }
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
                imageUrls.remove(imageUrl);
            } else if (onResultListener != null) {
                onResultListener.onResult(imageUrl);
            } else {
                writeToTxt(enableImagePath, imageUrl);
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
                System.out.println("zune: downloadImg: " + imageUrl + " file = " + file.getPath() + ", e = " + e.getMessage() + ", 共耗时：" + (System.currentTimeMillis() - time) + "毫秒" + "responseCode = " + responseCode);
            }
            file.delete();
            imageUrls.remove(imageUrl);
            try {
                writeToTxt(unableImagePath, imageUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public interface OnResultListener {
        void onResult(String url);
    }

}
