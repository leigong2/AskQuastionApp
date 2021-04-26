package com.example.jsoup.jsoup.webloaddata;

import com.example.jsoup.bean.HrefData;
import com.example.jsoup.jsoup.SqliteUtils;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XiqumeUtils extends BaseWebLoadUtils {
    protected static String baseUrl = "http://xiqu.99uz.com";

    private static List<HrefData> mLists = new ArrayList<>();

    public static void load() {
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                List<HrefData> lists = SqliteUtils.getInstance("D:\\user\\zune\\db\\xiqu_play_url.db").queryAllData("xiqu_play_url", HrefData.class);
                Collections.sort(lists, new Comparator<HrefData>() {
                    @Override
                    public int compare(HrefData h1, HrefData h2) {
                        return h1.getTitle().compareTo(h2.getTitle());
                    }
                });
                for (int i = 0; i < 1; i++) {
                    HrefData hrefData = lists.get(i);
                    Document read = read("https://www.00394.net/play/1720-2040.html");
                    if (read == null) {
                        continue;
                    }
                    HrefData hrefs = getVideoFrame(read, hrefData.getTitle(), hrefData.getText());
                    startDownLoad(hrefs.href);
                }
                System.out.println("完成: " + mLists.size());
            }
        });
    }

    private static void startDownLoad(String url) {
        //1.创建一个okhttpclient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request.Builder对象，设置参数，请求方式如果是Get，就不用设置，默认就是Get
        Request request = new Request.Builder()
                .url(url)
                .build();
        //3.创建一个Call对象，参数是request对象，发送请求
        Call call = okHttpClient.newCall(request);
        //4.异步请求，请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源，转换成我们想要的类型
                byte[] pictureBt = response.body().bytes();
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                File file = new File("D:\\user\\zune\\img\\down\\test.mp4");
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                    }
                    fos.flush();
                    System.out.println("下载完成");
                    //下载完成
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static HrefData getVideoFrame(Document document, String title, String text) {
        Elements elements = document.getElementsByTag("script");
        for (Element element : elements) {
            if (element == null) {
                continue;
            }
            for (int i = 0; i < element.childNodeSize(); i++) {
                Node node = element.childNode(i);
                if (node instanceof DataNode) {
                    String wholeData = ((DataNode) node).getWholeData();
                    if (wholeData != null && wholeData.contains("playM3u8")) {
                        String[] playM3u8s = wholeData.split("playM3u8");
                        String playM3u8 = playM3u8s[playM3u8s.length - 1];
                        String substring = playM3u8.substring(1, playM3u8.length() - 1);
                        return new HrefData(substring, substring, substring);
                    }
                }
            }
        }
        String string = elements.toString();
        String src = elements.attr("src");
        return new HrefData(src, title, text);
    }

    /**
     * 获取页面中所有的a标签
     *
     * @return
     */
    public static List<HrefData> getHrefs(Document document) {
        Elements elements = document.getElementsByTag("li");
        List<HrefData> datas = new ArrayList<>();
        for (Element element : elements) {
            Elements attr = element.getElementsByTag("a");
            String href = attr.attr("href");
            Elements em = element.getElementsByTag("em");
            if (em != null && em.size() > 0) {
                continue;
            }
            if (!href.startsWith("/yuju/")) {
                continue;
            }
            HrefData hrefData = new HrefData(href, attr.attr("title"), attr.text());
            datas.add(hrefData);
        }
        return datas;
    }
}
