package com.example.android.askquastionapp.utils;


import com.example.android.askquastionapp.bean.HrefData;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class BaseWebLoadUtils {
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
}
