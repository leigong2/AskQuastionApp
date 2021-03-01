package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.bean.HrefData;
import com.mysql.cj.util.StringUtils;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class ArtReadUtils extends BaseWebLoadUtils {

    public static void load(String url) {
        Document read = read(url);
        if (read == null) {
            return;
        }
        getHomePage(read);
        String s = dispatchNextUrl(url);
        Document document = read(s);
        if (document == null) {
            return;
        }
        getArtBody(document);
    }

    private static void getHomePage(Document document) {
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

    private static StringBuilder getArtBody(Document document) {
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
                            }
                        }
                    }
                }
            }
        }
        return sb;
    }
}
