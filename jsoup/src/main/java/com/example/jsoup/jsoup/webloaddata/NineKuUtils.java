package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.GsonGetter;
import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.MusicBean;
import com.example.jsoup.jsoup.SqliteUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;
import static com.example.jsoup.jsoup.JsoupUtils.writeToTxt;
import static com.example.jsoup.jsoup.SqliteUtils.checkUrlEnable;

public class NineKuUtils extends BaseWebLoadUtils {

    private static List<String> voiceUrls = new ArrayList<>();
    private static Set<String> voiceEnableUrls = new HashSet<>();
    private static String musicDBPath = "D:\\user\\zune\\db\\music_db.db";
    private static String musicPath = "D:\\user\\zune\\music.txt";

    public static void load(String url) {
        Document document = read(url);
        if (document == null) {
            return;
        }
        List<HrefData> hrefs = getHrefs(document);
        for (HrefData href : hrefs) {
            if (href.href == null || baseUrl.equals(href.href) || (!href.href.startsWith("/") && !href.href.startsWith("http"))) {
                continue;
            }
            if (urls.contains(baseUrl + href.href)) {
                continue;
            }
            getMp3Url(href);
        }
    }

    private static void getMp3Url(HrefData href) {
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
                    writeToTxt(musicPath, json + ",");
                } catch (Exception ignore) {
                }
            }
        }
    }

    private static String requestUrl(String voiceUrl) throws Exception {
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
}
