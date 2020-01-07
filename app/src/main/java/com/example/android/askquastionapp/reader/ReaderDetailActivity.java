package com.example.android.askquastionapp.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.R;
import com.mysql.cj.util.StringUtils;

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

import static com.example.jsoup.jsoup.Content.userAgents;
import static com.example.jsoup.jsoup.HttpsUrlValidator.trustAllHttpsCertificates;

public class ReaderDetailActivity extends AppCompatActivity {
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
            if (!StringUtils.isNullOrEmpty(s) && s.equals("artbody")) {
                for (int i = 0; i < element.childNodeSize(); i++) {
                    Elements ps = element.getElementsByTag("p");
                    if (ps.isEmpty()) {
                        Node node = element.childNode(i);
                        String temp = node.attributes().get("#text");
                        if (!StringUtils.isNullOrEmpty(temp.trim())) {
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
                            if (!StringUtils.isNullOrEmpty(node.attributes().get("#text").trim())) {
                                sb.append(node.attributes().get("#text"));
                            }
                        } else {
                            if (!StringUtils.isNullOrEmpty(stringBuilder.toString())) {
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
