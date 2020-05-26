package com.example.jsoup.jsoup;


import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.HData;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.ImgData;
import com.example.jsoup.bean.InfoData;
import com.example.jsoup.bean.MusicBean;
import com.example.jsoup.bean.VideoBean;
import com.example.jsoup.thread.CustomThreadPoolExecutor;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TestUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.jsoup.jsoup.Content.ip;
import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;
import static com.example.jsoup.jsoup.SqliteUtils.checkUrlEnable;

public class JsoupUtils {
    private static JsoupUtils jsoupUtils;
    private CustomThreadPoolExecutor sPool;
    private static String videoPath = "D:\\user\\zune\\invisibleVideo.txt";
    private static String m3u8Path = "D:\\user\\zune\\invisibleM3U8Video.txt";
    private static String imageDir = "D:\\user\\zune\\img";
    private static String unableImagePath = "D:\\user\\zune\\unableImg.txt";
    private boolean justGif = true;
    private static String enableImagePath = "D:\\user\\zune\\enableImg.txt";
    private static String musicPath = "D:\\user\\zune\\music.txt";
    private static String dbDir = "D:\\user\\zune\\db";
    private static String musicDBPath = "D:\\user\\zune\\db\\music_db.db";
    public static String movieDBPath = "D:\\user\\zune\\db\\movie_db.db";
    public static String videoDBPath = "D:\\user\\zune\\db\\video_db.db";
    public static String avDBPath = "D:\\user\\zune\\db\\av_db.db";
    private boolean sStop;
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
        urls.clear();
        return jsoupUtils;
    }

    public void setJustGif(boolean justGif) {
        this.justGif = justGif;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public void setUnableImagePath(String unableImagePath) {
        this.unableImagePath = unableImagePath;
    }

    private synchronized CustomThreadPoolExecutor getThreadExecutor() {
        if (sPool == null || sPool.isShutdown()) {
            sPool = new CustomThreadPoolExecutor(1000);
        }
        return sPool;
    }

    private static String baseUrl;
    /*zune: 必须在子线程中执行**/
    private static Set<String> urls = new HashSet<>();

    private int count = 0;

    public void getContent(InfoData infoData, String url) {
        if (sStop) {
            return;
        }
        if (urls.contains(url)) {
            return;
        }
        if (baseUrl == null) {
            String[] split = url.split("/");
            if (split.length >= 3) {
                baseUrl = url.startsWith("https") ? "https://" + split[2] : "http://" + split[2];
            }
            if (baseUrl == null) {
                baseUrl = url;
            }
        }
        if (url.startsWith("http") && !url.startsWith(baseUrl)) {
            return;
        }
        urls.add(url.startsWith("http") ? url : baseUrl + url);
        Document document = null;
        String[] splitIp = ip[(int) (ip.length * Math.random())].split(":");
        String userAgent = userAgents[(int) (userAgents.length * Math.random())];
        String session = "Hm_lvt_e55ff7844747a41e412fd2b38266f729=1574478858,1575279638,1575332776,1576215605; UM_distinctid=16e9640c55172a-03d372c96ca269-4c302b7a-1fa400-16e9640c5523e3; CNZZDATA1275110260=1105678920-1574476527-https%253A%252F%252Fwww.baidu.com%252F%7C1576213844; bdshare_firstime=1574478857680; Hm_lvt_e4476baf9a1725eedfe34c443331f6cf=1574479437,1575279643,1575332777,1576215906; Hm_lpvt_e55ff7844747a41e412fd2b38266f729=1576216455; Hm_lpvt_e4476baf9a1725eedfe34c443331f6cf=1576216455";
        try {
            int i = (int) (Math.random() * 1000);////做一个随机延时，防止网站屏蔽
            while (i != 0) {
                i--;
            }
            if (url.startsWith("https")) {
                trustAllHttpsCertificates();
            }
            document = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(30000).get();
        } catch (Exception e) {
            try {
                if (url.startsWith("https")) {
                    trustAllHttpsCertificates();
                }
                document = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .timeout(30000).post();
                System.out.println("正常：" + url);
            } catch (Exception e1) {
                System.out.println("异常：" + url + "..........." + e1);
            }
        }
        List<String> temp = new ArrayList<>();
        if (document != null) {
//            StringBuilder artBody = getArtBody(document);
//            System.out.println(artBody);
            List<HrefData> hrefs = getHrefs(document);
            for (HrefData href : hrefs) {
                if (href.href == null || baseUrl.equals(href.href) || (!href.href.startsWith("/") && !href.href.startsWith("http"))) {
                    continue;
                }
                if (urls.contains(baseUrl + href.href)) {
                    continue;
                }
//                getMp4Video(infoData, href);
                if (count > 0) {
                    return;
                }
//                getMp3Url(href);
                temp.add(href.href.startsWith("/") ? baseUrl + href.href : href.href);
            }
            List<ImgData> imgs = getImgs(document);
            for (int i = 0; i < imgs.size(); i++) {
                ImgData imgData = imgs.get(i);
                String[] split = imgData.src.split("\\.");
                String[] titlesTemp = document.title().split("\\\\");
                String[] titles;
                if (titlesTemp.length == 1) {
                    titles = document.title().split("/");
                } else {
                    titles = titlesTemp;
                }
                String end = "jpg";
                if (split.length == 1) {
                    if (imgData.src.endsWith("png")) {
                        end = "png";
                    } else if (imgData.src.endsWith("gif")) {
                        end = "gif";
                    }
                }
                boolean isTitle = (titles.length == 0 ? String.valueOf(System.currentTimeMillis()) : titles[0]).trim().length() == 4;
                if (isTitle) {
                    continue;
                }
                String imageName = (titles.length == 0 ? String.valueOf(System.currentTimeMillis()) : titles[0]) + i
                        + "." + (split.length == 0 ? end : split[split.length - 1]);
                if (imageName.contains(getGbk("正在播放"))) {
                    continue;
                }
                if (justGif && ((imageName.endsWith("gif") || imageName.endsWith("GIF")))) {
                    downLoadImg(imgData.src.startsWith("http") ? imgData.src : baseUrl + imgData.src
                            , imageName);
                } else if (!justGif) {
                    downLoadImg(imgData.src.startsWith("http") ? imgData.src : baseUrl + imgData.src
                            , imageName);
                }
            }
//            List<String> videos = getM3U8Videos(document);
//            if (!videos.isEmpty()) {
//                for (String video : videos) {
//                    try {
//                        writeToTxt(m3u8Path, video);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
        }
        String s = dispatchNextUrl(url);
        if (checkUrlEnable(s)) {
            getContent(infoData, s);
        }
        /*CustomThreadPoolExecutor threadPool = getThreadExecutor();
        threadPool.execute(new MyRunnable(getInfoData(document), dispatchNextUrl(url)) {
                               @Override
                               public void run(InfoData infoData, String url) {
                                   getContent(infoData, url);
                               }
                           }*/
        for (String ss : temp) {
            CustomThreadPoolExecutor threadPool = getThreadExecutor();
            threadPool.execute(new MyRunnable(getInfoData(document), ss) {
                @Override
                public void run(InfoData infoData, String url) {
                    getContent(infoData, url);
                }
            });
        }
    }

    public void getHomePage(Document document) {
        if (document != null) {
            System.out.println(document);
        }
        Elements elements = document.getElementsByTag("div");
        StringBuilder title = new StringBuilder();
        for (Element element : elements) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("weizhi".equalsIgnoreCase(s)) {
                Elements as = element.getElementsByTag("a");
                for (Element a : as) {
                    for (int i = 0; i < a.childNodeSize(); i++) {
                        Node node = a.childNode(i);
                        title.append(node).append(":");
                    }
                }
                System.out.println(title.deleteCharAt(title.length() - 1));
            }
        }
        Elements ul = document.getElementsByTag("ul");
        for (Element element : ul) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("p7".equalsIgnoreCase(s)) {
                Elements hrefs = element.getElementsByTag("a");
                for (Element a : hrefs) {
                    //实体对象构造方法，代码省略
                    HrefData hrefData = new HrefData(a.attr("href"), a.attr("title"), a.text());
                    hrefData.title = title.toString();
                    System.out.println(hrefData);
                }
            }
        }
    }

    private String dispatchNextUrl(String url) {
        String[] split = url.split("\\.");
        if (split.length < 2) {
            return null;
        }
        String[] temp = split[split.length - 2].split("-");
        if (temp.length == 1) {
            return null;
        }
        String s = temp[temp.length - 1];
        int position = -1;
        try {
            position = Integer.parseInt(s);
        } catch (Exception ignored) {
        }
        StringBuilder sb = new StringBuilder();
        if (position > 0) {
            for (int i = 0; i < split.length; i++) {
                if (i != split.length - 2) {
                    sb.append(split[i]).append(".");
                    continue;
                }
                for (int j = 0; j < temp.length; j++) {
                    if (j != temp.length - 1) {
                        sb.append(temp[j]).append("-");
                        continue;
                    }
                    sb.append(position).append("_2");
                }
                sb.append(".");
            }
        } else {
            temp = split[split.length - 2].split("_");
            String tempPosition = temp[temp.length - 1];
            int realPosition = -1;
            try {
                realPosition = Integer.parseInt(tempPosition) + 1;
            } catch (Exception ignored) {
            }
            for (int i = 0; i < split.length; i++) {
                if (i != split.length - 2) {
                    sb.append(split[i]).append(".");
                    continue;
                }
                for (int j = 0; j < temp.length; j++) {
                    if (j != temp.length - 1) {
                        sb.append(temp[j]).append("_");
                        continue;
                    }
                    sb.append(realPosition);
                }
                sb.append(".");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private StringBuilder getArtBody(Document document) {
        StringBuilder sb = new StringBuilder();
        Elements elements = document.getElementsByTag("div");
        for (Element element : elements) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("weizhi".equalsIgnoreCase(s)) {
                Elements as = element.getElementsByTag("a");
                StringBuilder title = new StringBuilder();
                for (Element a : as) {
                    for (int i = 0; i < a.childNodeSize(); i++) {
                        Node node = a.childNode(i);
                        title.append(node).append(":");
                    }
                }
                System.out.println(title.deleteCharAt(title.length() - 1));
            }
            if ("artinfo".equalsIgnoreCase(s)) {
                for (int i = 0; i < element.childNodeSize(); i++) {
                    Node node = element.childNode(i);
                    String author = node.attributes().get("#text").trim();
                    System.out.println(author);
                }
            }
            if (!StringUtils.isNullOrEmpty(s) && s.equals("artbody")) {
                for (int i = 0; i < element.childNodeSize(); i++) {
                    Elements ps = element.getElementsByTag("p");
                    if (ps.isEmpty()) {
                        Node node = element.childNode(i);
                        String temp = node.attributes().get("#text");
                        if (!StringUtils.isNullOrEmpty(temp.trim())) {
                            sb.append(temp);
                            break;
                        }
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Element p : ps) {
                            for (int j = 0; j < p.childNodeSize(); j++) {
                                Node node = p.childNode(j);
                                String temp = node.attributes().get("#text");
                                stringBuilder.append(temp);
                            }
                        }
                        if (stringBuilder.toString().trim().isEmpty()) {
                            Node node = element.childNode(i);
                            if (!StringUtils.isNullOrEmpty(node.attributes().get("#text").trim())) {
                                sb.append(node.attributes().get("#text"));
                            }
                        } else {
                            if (!StringUtils.isNullOrEmpty(stringBuilder.toString())) {
                                sb.append(stringBuilder);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return sb;
    }

    private List<String> voiceUrls = new ArrayList<>();
    private List<String> voiceEnableUrls = new ArrayList<>();

    private void getMp3Url(HrefData href) {
        /*zune: 对应 baseUrl = http://www.9ku.com**/
        if (href.href.contains("/play/")) {
            String replace = href.href.split("/play/")[1].replace(".htm", "");
            String url = String.format("%s/html/playjs/%s/%s.js HTTP/1.1", baseUrl, ((int) (Math.random() * 1000 + 1)),
                    replace);
            if (!voiceUrls.contains(replace)) {
                voiceUrls.add(replace);
                try {
                    String json = requestUrl(url);
                    MusicBean musicBean = GsonGetter.getInstance().getGson().fromJson(json, MusicBean.class);
                    if (!"".equals(musicBean.wma) && !voiceEnableUrls.contains(musicBean.wma) && checkUrlEnable(musicBean.wma)) {
                        musicBean.page = voiceEnableUrls.size() / 10;
                        boolean b = SqliteUtils.getInstance(musicDBPath).insertData("music_bean", musicBean);
                        System.out.println("voiceEnableUrls.size() = " + voiceEnableUrls.size() + ", insert = " + b);
                        voiceEnableUrls.add(musicBean.wma);
                    }
//                    writeToTxt(musicPath, json + ",");
                } catch (Exception e) {
                }
            }
        }
    }

    private String requestUrl(String voiceUrl) throws Exception {
        if (voiceUrl.startsWith("https")) {
            trustAllHttpsCertificates();
        }
        Document document = Jsoup.connect(voiceUrl)
                .data("query", "Java")
                .userAgent(userAgents[(int) (userAgents.length * Math.random())])
                .cookie("auth", "token")
                .timeout(5000000).get();
        Elements allElements = document.getElementsByTag("body");
        if (allElements != null) {
            for (Element element : allElements) {
                List<Node> nodes = element.childNodes();
                if (nodes != null) {
                    for (Node node : nodes) {
                        return node.toString().replaceAll("\\(", "").replaceAll("\\)", "");
                    }
                }
            }
        }
        return "";
    }

    private List<String> videoTitles = new ArrayList<>();
    private List<String> videoUrl = new ArrayList<>();

    private void getMp4Video(InfoData infoData, HrefData href) {
        String http = href.href.startsWith("http") ? href.href : baseUrl + href.href;
        if (infoData != null && (srcUrls.contains(http)
                || "大陆剧".equals(infoData.getVideo_type())
                || "港台剧".equals(infoData.getVideo_type())
                || "日韩剧".equals(infoData.getVideo_type())
                || "分享:".equals(infoData.getVideo_type())
                || "纪录片".equals(infoData.getVideo_type())
                || "欧美剧".equals(infoData.getVideo_type()))) {
            System.out.println("ignore = " + http);
            return;
        }
        /*zune: 对于baseUrl = https://www.2344ww.com
        & http://www.8080s.net
        * **/
        if (getGbk("<-- HTTP下载 -->").equals(href.text) || getGbk("本地下载").equals(href.text)
                || "<-- HTTP下载 -->".equals(href.text) || "本地下载".equals(href.text)) {
            try {
                if (href.title == null || href.title.isEmpty()) {
                    String[] title = href.href.split(".mp4")[0].split("/");
                    href.title = title[title.length - 1];
                } else {
                    href.title = href.title.split(getGbk("高清mp4"))[0];
                }
                if (checkUrlEnable(href.href)) {
                    synchronized (this) {
                        if (!videoUrl.contains(href.href)) {
                            videoUrl.add(href.href);
                            videoTitles.add(href.title);
                            VideoBean video = new VideoBean();
                            int size = videoUrl.size();
                            video.setPage(size - 1);
                            video.setVideo_name(href.title);
                            video.setVideo_url(href.href);
                            if (infoData != null) {
                                video.setVideo_type(infoData.getVideo_type());
                                if (infoData.getVideo_add_time() == null) {
                                    String[] split = href.title.split("[()]");
                                    if (split.length > 1) {
                                        infoData.setVideo_add_time(split[1]);
                                    }
                                }
                                video.setVideo_add_time(infoData.getVideo_add_time());
                            }
                            System.out.println(video.toString());
                            SqliteUtils.getInstance(videoDBPath).insertData("video_bean", video);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getGbk(String string) {
        try {
            return new String(string.getBytes("GBK"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    /*zune: 对应baseUrl = http://www.udp2p.com**/
    private List<String> getM3U8Videos(Document document) {
        Elements allElements = document.getElementsByTag("script");
        List<String> videoUrls = new ArrayList<>();
        if (allElements != null) {
            for (Element element : allElements) {
                List<Node> nodes = element.childNodes();
                if (nodes != null) {
                    for (Node node : nodes) {
                        String[] split = node.toString().split("\"");
                        for (String s : split) {
                            if ((s.startsWith("http") && s.endsWith(getGbk("m3u8")))
                                    || (s.startsWith("http") && s.endsWith("m3u8"))) {
                                String e = s.replaceAll("\\\\", "/");
                                videoUrls.add(e);
                            }
                        }
                    }
                }
            }
        }
        return videoUrls;
    }

    List<String> imageUrls = new ArrayList<>();

    public void downLoadImg(String downloadUrl, String imageName) {
        if (sStop) {
            return;
        }
        if (imageUrls.contains(downloadUrl)) {
            System.out.println("zune downloadUrl =  " + downloadUrl);
        } else {
            imageUrls.add(downloadUrl);
            downloadPicture(downloadUrl, imageDir, imageName);
        }
    }

    /**
     * 获取页面中所有的a标签
     *
     * @return
     */
    public static List<HrefData> getHrefs(Document document) {
        Elements attr = document.getElementsByTag("a");
        List<HrefData> hrefDataList = new ArrayList<HrefData>();
        for (Element element : attr) {
            //实体对象构造方法，代码省略
            HrefData hrefData = new HrefData(element.attr("href"), element.attr("title"), element.text());
            Element head = document.head();
            Elements elementsByTag = head.getElementsByTag("title");
            if (elementsByTag.size() > 0 && elementsByTag.get(0).childNodeSize() > 0) {
                Node title = elementsByTag.get(0).childNode(0);
                hrefData.title = title.toString();
            }
            hrefDataList.add(hrefData);
        }
        return hrefDataList;
    }

    /**
     * 获取页面中所有的dd标签
     *
     * @return
     */
    public InfoData getInfoData(Document document) {
        InfoData infoData = new InfoData();
        Elements attr = document.getElementsByTag("dd");
        for (Element element : attr) {
            //实体对象构造方法，代码省略
            String text = element.text();
            if (text.startsWith("分类")) {
                String[] split = text.split("：");
                if (split.length > 1) {
                    infoData.setVideo_type(split[1]);
                }
            }
            if (text.startsWith("时间")) {
                String[] split = text.split("：");
                if (split.length > 1) {
                    infoData.setVideo_add_time(split[1]);
                }
            }
        }
        if (infoData.getVideo_add_time() == null && infoData.getVideo_type() == null) {
            return null;
        }
        return infoData;
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
            if (s.startsWith("哈哈哈")) {
                System.out.println(s);
            }
            ImgData imgData = new ImgData(src, s.split("】")[0] + "】" + i);//实体对象构造方法，代码省略
            imgDataList.add(imgData);
        }
        return imgDataList;
    }

    /**
     * 获取页面中的h标签
     *
     * @return
     */
    public List<HData> getHs(Document document) {
        List<HData> hDataList = new ArrayList<HData>();
        Elements attr1 = document.getElementsByTag("h1");
        for (Element element : attr1) {
            HData hData = new HData("h1", element.text());//实体对象构造方法，代码省略
            hDataList.add(hData);
        }
        Elements attr2 = document.getElementsByTag("h2");
        for (Element element : attr2) {
            HData hData = new HData("h2", element.text());
            hDataList.add(hData);
        }
        return hDataList;
    }

    //链接url下载图片
    private void downloadPicture(String imageUrl, String dir, String imageName) {
        long time = System.currentTimeMillis();
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(fileDir, imageName.replaceAll("\\\\", "").replaceAll("/", "")
                .replaceAll(":", "").replaceAll("\\*", "").replaceAll("\\?", "")
                .replaceAll("\\\"", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("|", "").replaceAll("\\|", "").replaceAll(" ", ""));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("zune: createNewFile file = " + file.getPath());
            }
        } else {
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
            http.setRequestProperty("user-agent", userAgents[(int) (userAgents.length * Math.random())]);
            http.setRequestMethod("GET");
            http.setInstanceFollowRedirects(false);
            http.connect();
            responseCode = http.getResponseCode();
            if (responseCode != 200) {
                file.delete();
                imageUrls.remove(imageUrl);
                return;
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
            if (imageUrls.size() >= 1000) {
                sStop = true;
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

    private void writeToTxt(String path, String imageUrl) throws Exception {
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

    public abstract class MyRunnable implements Runnable {
        String url;
        InfoData infoData;

        public MyRunnable(InfoData infoData, String url) {
            this.url = url;
            this.infoData = infoData;
        }

        @Override
        public void run() {
            run(infoData, url);
        }

        public abstract void run(InfoData infoData, String url);
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GB2312
                String s = encode;
                return s; //是的话，返回“GB2312“，以下代码同理
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是ISO-8859-1
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是UTF-8
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GBK
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return ""; //如果都不是，说明输入的内容不属于常见的编码格式。
    }

    public interface OnResultListener {
        void onResult(String url);
    }

    private OnResultListener onResultListener;

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }
}
