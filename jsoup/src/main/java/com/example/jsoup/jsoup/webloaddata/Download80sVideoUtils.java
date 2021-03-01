package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.InfoData;
import com.example.jsoup.bean.VideoBean;
import com.example.jsoup.jsoup.SqliteUtils;
import com.mysql.cj.util.StringUtils;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.jsoup.jsoup.SqliteUtils.checkUrlEnable;
import static java.util.Locale.US;

public class Download80sVideoUtils extends BaseWebLoadUtils {
    private static Set<String> srcUrls = new HashSet<>();

    private static List<String> videoTitles = new ArrayList<>();
    private static List<String> videoUrl = new ArrayList<>();
    public static String videoDBPath = "D:\\user\\zune\\db\\video_db.db";

    public static void load(String url) {
        Document read = read(url);
        if (read == null) {
            return;
        }
        List<HrefData> hrefs = getHrefs(read);
        for (HrefData href : hrefs) {
            getMp4Video(null, href);
        }
        getCurrentVideos(read);
    }

    private static void getCurrentVideos(Document document) {
        Elements elements = document.getElementsByTag("div");
        String title = document.title();
        for (Element element : elements) {
            Attributes attributes = element.attributes();
            String s = attributes.get("class");
            if ("videoUiWrapper thumbnail".equals(s)) {
                Elements divs = element.getElementsByTag("div");
                for (Element div : divs) {
                    List<Node> dataNodes = div.childNodes();
                    for (Node d : dataNodes) {
                        List<Node> nodes = d.childNodes();
                        for (Node node : nodes) {
                            if (node instanceof Element) {
                                Attributes attr = node.attributes();
                                String src = attr.get("src");
                                if (!StringUtils.isNullOrEmpty(src)) {
                                    System.out.println(src);
                                    HrefData href = new HrefData(src, title, title);
                                    insertToDb(null, href);
                                }
                            }
                        }
                    }
                }
            }
        }
        List<HrefData> hrefs = getHrefs(document);
        for (HrefData href : hrefs) {
            Document read = read(baseUrl + "/" + href.href);
            if (read == null) {
                continue;
            }
            getCurrentVideos(read);
        }
    }

    private static void getMp4Video(InfoData infoData, HrefData href) {
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
            insertToDb(infoData, href);
        }
    }

    private synchronized static void insertToDb(InfoData infoData, HrefData href) {
        try {
            if (href.title == null || href.title.isEmpty()) {
                String[] title = href.href.split(".mp4")[0].split("/");
                href.title = title[title.length - 1];
            } else {
                href.title = href.title.split(getGbk("高清mp4"))[0];
            }
            if (checkUrlEnable(href.href)) {
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
                    } else {
                        video.setVideo_add_time(new SimpleDateFormat("yyyy-MM-dd", US).format(new Date(System.currentTimeMillis())));
                    }
                    System.out.println(video.toString());
                    SqliteUtils.getInstance(videoDBPath).insertData("video_bean", video);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getGbk(String string) {
        try {
            return new String(string.getBytes("GBK"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }
}
