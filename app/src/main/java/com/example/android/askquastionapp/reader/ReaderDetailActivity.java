package com.example.android.askquastionapp.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ReaderDetailActivity extends AppCompatActivity {
    String[] userAgents = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",};
    String[] ip = {
            "223.198.25.148:9999"
    };
    private EditText editText;

    public static void start(Context context, String url, String title) {
        Intent intent = new Intent(context, ReaderDetailActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_detail);
        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        editText = findViewById(R.id.read_pager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            editText.setBackground(null);
        }
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        editText.setLineSpacing(0, 1);
        editText.setCursorVisible(false);
        editText.setClickable(true);
        editText.setTextIsSelectable(true);
        editText.setIncludeFontPadding(false);
        int padding = SizeUtils.dp2px(10);
        editText.setPadding(padding, 0, padding, padding);
        editText.setTextColor(Color.BLACK);
        loadData(url);
    }

    private void loadData(String url) {
        Observable.just(url).map(new Function<String, Document>() {
            @Override
            public Document apply(String url) throws Exception {
                Document document = null;
                String userAgent = userAgents[(int) (userAgents.length * Math.random())];
                try {
                    int i = (int) (Math.random() * 1000);////做一个随机延时，防止网站屏蔽
                    while (i != 0) {
                        i--;
                    }
                    document = Jsoup.connect(url)
                            .userAgent(userAgent)
                            .timeout(30000).get();
                } catch (Exception e) {
                    try {
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
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Document>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Document document) {
                        StringBuilder artBody = getArtBody(document);
                        editText.setText(artBody.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
            if (!TextUtils.isEmpty(s) && s.equals("artbody")) {
                for (int i = 0; i < element.childNodeSize(); i++) {
                    Elements ps = element.getElementsByTag("p");
                    if (ps.isEmpty()) {
                        Node node = element.childNode(i);
                        String temp = node.attributes().get("#text");
                        if (!TextUtils.isEmpty(temp.trim())) {
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
                            if (!TextUtils.isEmpty(node.attributes().get("#text").trim())) {
                                sb.append(node.attributes().get("#text"));
                            }
                        } else {
                            if (!TextUtils.isEmpty(stringBuilder.toString())) {
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

}
