package com.example.jsoup;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.thread.CustomThreadPoolExecutor;
import com.mysql.cj.util.StringUtils;
import com.spreada.utils.chinese.ZHConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;

public class ShiShiReaderUtils {
    private static Set<String> urls = new HashSet<>();
    private static String baseUrl;
    private static CustomThreadPoolExecutor sPool = new CustomThreadPoolExecutor(1000);
    private static ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);

    private static Document read(String url) {
        if (urls.contains(url)) {
            return null;
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
        urls.add(url.startsWith("http") ? url : baseUrl + url);
        Document document = null;
        String userAgent = userAgents[(int) (userAgents.length * Math.random())];
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
        return document;
    }

    public static void load(String url) {
        Document document = read(url);
        if (document == null) {
            return;
        }
        for (int i = 120; i <200; i++) {
            sPool.execute(new MyRunnable(new HrefData(url + i, null, null)) {
                @Override
                public void run(HrefData hrefData) {
                    Document document = read(hrefData.href);
                    if (document == null) {
                        return;
                    }
                    findUrl(document);
                }
            });
        }
    }

    private static void findUrl(Document document) {
        Elements elements = document.getElementsByTag("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("?id=")) {
                getRealUrl(element, href);
            }
        }
    }

    private static void getRealUrl(Element element, String href) {
        Document read = read(href);
        if (read == null) {
            return;
        }
        String nodeText = getNodeText(element);
        Elements reads = read.getElementsByTag("a");
        for (Element elementRead : reads) {
            String hrefRead = elementRead.attr("href");
            String nodeTextRead = getNodeText(elementRead);
            if ("点击阅读".equals(nodeTextRead)) {
                Document readDetail = read(hrefRead);
                if (readDetail == null) {
                    continue;
                }
                Elements elementsDetails = readDetail.getElementsByTag("a");
                for (Element elementsDetail : elementsDetails) {
                    String hrefDetail = elementsDetail.attr("href");
                    String nodeTextDetail = getNodeText(elementsDetail);
                    if (nodeText != null && nodeText.equals(nodeTextDetail) && hrefDetail != null && !hrefDetail.startsWith("http")) {
                        String s = hrefRead.replaceAll("index.html", "") + hrefDetail;
                        getContent(s);
                    }
                }
            }
        }
    }

    private static String getNodeText(Element elementsDetail) {
        for (int j = 0; j < elementsDetail.childNodeSize(); j++) {
            Node nodeDetail = elementsDetail.childNode(j);
            if (nodeDetail instanceof TextNode) {
                return ((TextNode) nodeDetail).getWholeText();
            }
        }
        return null;
    }

    public abstract static class MyRunnable implements Runnable {
        private HrefData hrefData;

        public MyRunnable(HrefData hrefData) {
            this.hrefData = hrefData;
        }

        public abstract void run(HrefData hrefData);

        @Override
        public void run() {
            run(hrefData);
        }
    }

    public static void getContent(String url) {
        Document document = read(url);
        if (document == null) {
            return;
        }
        Elements div = document.getElementsByTag("div");
        String title = null;
        StringBuilder sb = new StringBuilder();
        for (Element element : div) {
            String id = element.attr("id");
            if ("title".equals(id)) {
                title = ((TextNode) element.childNode(0)).getWholeText();
            }
            if ("content".equals(id)) {
                for (int i = 0; i < element.childNodeSize(); i++) {
                    Node node = element.childNode(i);
                    if (node instanceof TextNode) {
                        sb.append(((TextNode) node).getWholeText());
                    }
                }
            }
        }
        if (title == null) {
            String s = sb.toString().split("\r\n")[0];
            title = s.length() > 20 ? s.substring(0, 20) : s;
        }
        writeToLocal(title, sb.toString());
    }

    private static void writeToLocal(String title, String text) {
        try {
            File dir = new File("D:\\user\\zune\\text");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, converter.convert(title).replaceAll("/", "").replaceAll(" ", "").replaceAll("\\r", "") + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            String[] split = text.split("\n");
            for (String s : split) {
                if (s == null || StringUtils.isNullOrEmpty(s.trim())) {
                    continue;
                }
                fw.write(converter.convert(s).trim().replaceAll("\\r", "").replaceAll("\u00a0", ""));
                fw.write("\n");
            }
            fw.close();
        } catch (Exception e) {
            System.out.println("createNewFile error" + title);
        }
    }

    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}
