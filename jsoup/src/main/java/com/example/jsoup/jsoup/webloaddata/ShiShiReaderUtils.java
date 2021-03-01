package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.bean.HrefData;
import com.mysql.cj.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;

public class ShiShiReaderUtils extends BaseWebLoadUtils {

    public static void load(String url) {
        Document document = read(url);
        if (document == null) {
            return;
        }
        for (int i = 120; i < 200; i++) {
            sPool.execute(new BaseRunnable(new HrefData(url + i, null, null)) {
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
