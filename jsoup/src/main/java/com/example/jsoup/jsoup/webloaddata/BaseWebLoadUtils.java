package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.thread.CustomThreadPoolExecutor;
import com.spreada.utils.chinese.ZHConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;

public class BaseWebLoadUtils {

    protected static Set<String> urls = new HashSet<>();
    protected static String baseUrl;
    protected static CustomThreadPoolExecutor sPool = new CustomThreadPoolExecutor(1000);
    protected static ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);

    protected static Document read(String url) {
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
            if (!hrefData.href.startsWith("/video-") && !hrefData.href.startsWith("v.php?v=")) {
                continue;
            }
            hrefDataList.add(hrefData);
        }
        return hrefDataList;
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

    protected static String dispatchNextUrl(String url) {
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

    protected static String getNodeText(Element elementsDetail) {
        for (int j = 0; j < elementsDetail.childNodeSize(); j++) {
            Node nodeDetail = elementsDetail.childNode(j);
            if (nodeDetail instanceof TextNode) {
                return ((TextNode) nodeDetail).getWholeText();
            }
        }
        return null;
    }

    public abstract static class BaseRunnable implements Runnable {
        private HrefData hrefData;
        private String url;

        public BaseRunnable(HrefData hrefData) {
            this.hrefData = hrefData;
        }

        public BaseRunnable(String url) {
            this.url = url;
        }

        public void run(HrefData hrefData) {
        };

        public void run(String url) {
        };

        @Override
        public void run() {
            run(hrefData);
            run(url);
        }
    }
}
