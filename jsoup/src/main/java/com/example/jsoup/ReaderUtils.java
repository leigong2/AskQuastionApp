package com.example.jsoup;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.bean.InfoData;
import com.example.jsoup.jsoup.JsoupUtils;
import com.example.jsoup.thread.CustomThreadPoolExecutor;
import com.mysql.cj.util.StringUtils;
import com.spreada.utils.chinese.ZHConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;

public class ReaderUtils {
    private static Set<String> urls = new HashSet<>();
    private static String baseUrl;
    private static CustomThreadPoolExecutor sPool = new CustomThreadPoolExecutor(1000);

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
        for (int i = 0; i < 42; i++) {
            sPool.execute(new MyRunnable(new HrefData(url + "/page/" + i, null, null)) {
                @Override
                public void run(HrefData hrefData) {
                    List<String> urls = new ArrayList<>();
                    Document document = read(hrefData.href);
                    if (document == null) {
                        return;
                    }
                    Elements elements = document.getElementsByTag("h2");
                    for (Element element : elements) {
                        String href = null;
                        String title = null;
                        for (int i = 0; i < element.childNodeSize(); i++) {
                            Node node = element.childNode(i);
                            if (node instanceof Element) {
                                for (Attribute attribute : node.attributes()) {
                                    String value = attribute.getValue();
                                    if ("href".equals(attribute.getKey())) {
                                        href = value;
                                    }
                                }
                                for (Node childNode : node.childNodes()) {
                                    if (childNode instanceof TextNode) {
                                        title = ((TextNode) childNode).getWholeText();
                                    }
                                }
                            }
                        }
                        if (title == null || StringUtils.isNullOrEmpty(title.trim())) {
                            continue;
                        }
                        if (href == null || StringUtils.isNullOrEmpty(href.trim())) {
                            continue;
                        }
                        if (urls.contains(href)) {
                            continue;
                        }
                        urls.add(href);
                    }
                    for (String url : urls) {
                        getContent(url);
                    }
                }
            });
        }
//        List<HrefData> primaryHref = getPrimaryHref(document);
//        for (HrefData s : primaryHref) {
//            sPool.execute(new MyRunnable(s) {
//                @Override
//                public void run(HrefData hrefData) {
//                    Document primaryDocument = read(hrefData.href);
//                    if (primaryDocument == null) {
//                        return;
//                    }
//                    List<String> secondaryHref = getSecondaryHref(new ArrayList<>(), hrefData.href, primaryDocument, 1);
//                    for (String secondaryUrl : secondaryHref) {
//                        getContent(secondaryUrl);
//                    }
//                }
//            });
//        }
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

    private static List<HrefData> getPrimaryHref(Document document) {
        Elements elements = document.getElementsByTag("li");
        List<HrefData> hrefData = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for (Element e : elements) {
            for (int i = 0; i < e.childNodeSize(); i++) {
                Node child = e.childNode(i);
                String href = child.attr("href");
                String s = null;
                if (child instanceof Element) {
                    List<Node> nodes = child.childNodes();
                    if (nodes != null) {
                        for (Node node : nodes) {
                            if (node instanceof TextNode) {
                                s = ((TextNode) node).getWholeText();
                            }
                        }
                    }
                }
                if (s == null || StringUtils.isNullOrEmpty(s.trim())) {
                    continue;
                }
                if (s.contains("【")) {
                    continue;
                }
                if (urls.contains(href)) {
                    continue;
                }
                if ("关于".equals(s) || "翻译".equals(s) || "More".equals(s) || "翻译文章相关说明".equals(s)) {
                    continue;
                }
                urls.add(href);
                hrefData.add(new HrefData(href, s, s));
            }
        }
        return hrefData;
    }

    private static List<String> getSecondaryHref(List<String> urls, String url, Document document, int page) {
        if (document == null) {
            return urls;
        }
        Elements elements = document.getElementsByTag("h2");
        for (Element element : elements) {
            String href = null;
            String title = null;
            for (int i = 0; i < element.childNodeSize(); i++) {
                Node node = element.childNode(i);
                if (node instanceof Element) {
                    for (Attribute attribute : node.attributes()) {
                        String value = attribute.getValue();
                        if ("href".equals(attribute.getKey())) {
                            href = value;
                        }
                    }
                    for (Node childNode : node.childNodes()) {
                        if (childNode instanceof TextNode) {
                            title = ((TextNode) childNode).getWholeText();
                        }
                    }
                }
            }
            if (title == null || StringUtils.isNullOrEmpty(title.trim())) {
                continue;
            }
            if (href == null || StringUtils.isNullOrEmpty(href.trim())) {
                continue;
            }
            if (urls.contains(href)) {
                continue;
            }
            urls.add(href);
        }
        if (urls.isEmpty()) {
            return urls;
        }
        Document nextDocument = read(url + "page/" + (page + 1));
        System.out.println(url + "page/" + (page + 1));
        getSecondaryHref(urls, url, nextDocument, page + 1);
        return urls;
    }

    public static void getContent(String url) {
        sPool.execute(new JsoupUtils.MyRunnable(null, url) {
            @Override
            public void run(InfoData infoData, String url) {
                Document document = read(url);
                if (document == null) {
                    return;
                }
                Elements h1 = document.getElementsByTag("h1");
                String title = null;
                for (Element element : h1) {
                    for (int i = 0; i < element.childNodeSize(); i++) {
                        Node node = element.childNode(i);
                        if (node instanceof TextNode) {
                            title = ((TextNode) node).getWholeText();
                        }
                    }
                }
                if (title == null || StringUtils.isNullOrEmpty(title.trim())) {
                    return;
                }
                Elements p = document.getElementsByTag("p");
                StringBuilder sb = new StringBuilder();
                sb.append(title).append("\n");
                for (Element element : p) {
                    for (int i = 0; i < element.childNodeSize(); i++) {
                        Node node = element.childNode(i);
                        if (node instanceof TextNode) {
                            String wholeText = ((TextNode) node).getWholeText();
                            if (wholeText == null || StringUtils.isNullOrEmpty(wholeText.trim())) {
                                continue;
                            }
                            sb.append(wholeText).append("\n");
                        }
                    }
                }
                writeToLocal(title, sb.toString());
            }
        });
    }

    private static void writeToLocal(String title, String text) {
        try {
            File dir = new File("D:\\user\\zune\\text");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, title.replaceAll("/", "").replaceAll(" ", "") + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            String[] split = text.split("\n");
            for (String s : split) {
                if (s == null || StringUtils.isNullOrEmpty(s.trim())) {
                    continue;
                }
                if ("Fill in your details below or click an icon to log in:".equals(s.trim())) {
                    continue;
                }
                if ("您正在使用您的 WordPress.com 账号评论。".equals(s.trim())) {
                    continue;
                }
                if ("您正在使用您的 Google 账号评论。".equals(s.trim())) {
                    continue;
                }
                if ("您正在使用您的 Twitter 账号评论。".equals(s.trim())) {
                    continue;
                }
                if ("您正在使用您的 Facebook 账号评论。".equals(s.trim())) {
                    continue;
                }
                if ("Connecting to %s".equals(s.trim())) {
                    continue;
                }
                fw.write(s.trim());
                fw.write("\n");
            }
            fw.close();
        } catch (Exception e) {
            System.out.println("createNewFile error" + title);
        }
    }

    /**
     * Merge txt. 将一个文件夹下所有的txt合并为一个
     *
     * @param dirPath  the dir path, 原文件夹；
     * @param descPath the desc path, 目标文件夹；
     */
    public static void mergeTxt(String dirPath, String descPath) {
        sPool.execute(new MyRunnable(new HrefData(dirPath, dirPath, dirPath)) {
            @Override
            public void run(HrefData hrefData) {
                File descDir = new File(descPath);
                if (!descDir.exists()) {
                    descDir.mkdirs();
                }
                File descFile = new File(descDir, new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date(System.currentTimeMillis())) + ".txt");
                if (!descFile.exists()) {
                    try {
                        descFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    File fileDir = new File(hrefData.text);
                    if (!fileDir.exists()) {
                        return;
                    }
                    File[] temp = fileDir.listFiles();
                    if (temp == null) {
                        return;
                    }
                    List<File> files = new ArrayList<>();
                    Collections.addAll(files, temp);
                    Collections.sort(files, (f1, f2) -> (int) (f2.length() - f1.length()));
                    BufferedWriter fw = new BufferedWriter(new FileWriter(descFile, false));
                    int count = 1;
                    BufferedReader fr = null;
                    long size = 0;
                    for (File file : files) {
                        if (!file.exists() || file.isDirectory() || !file.getPath().endsWith(".txt")) {
                            continue;
                        }
                        fw.write("第" + count + "卷" + file.getName().replaceAll(".txt", ""));
                        fw.newLine();
                        fr = new BufferedReader(new FileReader(file));
                        while (true) {
                            String s = fr.readLine();
                            if (s == null) {
                                break;
                            }
                            fw.write(s);
                            size += s.getBytes().length;
                            fw.newLine();
                        }
                        if (size > 10 * 1024 * 1024) {
                            fw.close();
                            size = 0;
                            count = 0;
                            int number = Integer.parseInt(String.valueOf(descFile.getName().charAt(descFile.getName().indexOf(".txt") - 1)));
                            int next = number + 1 >= 10 ? 0 : number + 1;
                            String s = descFile.getName().replaceAll(number + ".txt", next + ".txt");
                            descFile = new File(descDir, s);
                            if (!descFile.exists()) {
                                try {
                                    descFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            fw = new BufferedWriter(new FileWriter(descFile));
                        }
                        count++;
                    }
                    if (fr != null) {
                        fr.close();
                    }
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Rename txt. 重命名文件夹下的所有txt文件
     *
     * @param srcDir the dir path
     */
    public static void renameTxt(String srcDir) {
        sPool.execute(new MyRunnable(new HrefData(srcDir, srcDir, srcDir)) {
            @Override
            public void run(HrefData hrefData) {
                File dir = new File(hrefData.text);
                File[] files = dir.listFiles();
                if (files == null) {
                    return;
                }
                for (File file : files) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                        String title = null;
                        while (true) {
                            title = br.readLine();
                            if (StringUtils.isNullOrEmpty(title.trim())) {
                                continue;
                            }
                            if ((title.endsWith(")") && title.startsWith("(")) || (title.endsWith("）") && title.startsWith("（"))) {
                                continue;
                            }
                            title = replaceUnknown(title);
                            break;
                        }
                        if (StringUtils.isNullOrEmpty(title.trim())) {
                            continue;
                        }
                        if (title.trim().length() > 20) {
                            title = title.substring(0, 20);
                        }
                        br.close();
                        File valueFile = new File(hrefData.text, title.trim() + ".txt");
                        boolean b = file.renameTo(valueFile);
                        System.out.println(file.getPath() + "......" + b);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static String replaceUnknown(String title) {
        StringBuilder sb = new StringBuilder();
        String s = title.split("-")[0];
        for (int i = 0; i < s.length(); i++) {
            char c = title.charAt(i);
            if (isChinese(c)) {
                sb.append(c);
            }
            if (Character.isDigit(c)) {
                sb.append(c);
            }
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                sb.append(c);
            }
            if (c == '【' || c == '】' || c == '[' || c == ']' || c == '(' || c == ')' || c == '（' || c == '）') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    /**
     * Convert to simple chinese.  将文件夹内所有文件转换成简体
     *
     * @param srcPath the src path
     */
    public static void convertToSimpleChinese(String srcPath) {
        ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
        try {
            File dir = new File(srcPath);
            if (!dir.isDirectory()) {
                return;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                File temp = new File(srcPath, "temp");
                if (!temp.exists()) {
                    temp.createNewFile();
                }
                BufferedReader br = new BufferedReader(new FileReader(file));
                BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
                while (true) {
                    String s = br.readLine();
                    if (s == null) {
                        break;
                    }
                    String convert = converter.convert(s);
                    bw.write(convert);
                    bw.newLine();
                }
                br.close();
                bw.close();
                boolean delete = file.delete();
                boolean rename = temp.renameTo(file);
                System.out.println(delete + "....." + rename);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
