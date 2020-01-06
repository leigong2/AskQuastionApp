package com.example.jsoup.thread;

import com.example.jsoup.jsoup.JsoupUtils;

public class SimpleThread extends Thread {
    private String url;

    public SimpleThread(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        super.run();
//        JsoupUtils.getInstance().setJustGif(false);
        JsoupUtils.getInstance().getContent(null, url);
    }
}
