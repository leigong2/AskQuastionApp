package com.example.jsoup.thread;

import com.example.jsoup.bean.MusicBean;
import com.example.jsoup.jsoup.JsoupUtils;
import com.example.jsoup.jsoup.SqliteUtils;

import java.util.List;

import static com.example.jsoup.jsoup.JsoupUtils.musicDBPath;

public class SimpleThread extends Thread {
    private String url;

    public SimpleThread(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        super.run();
//        JsoupUtils.getInstance().setJustGif(false);
        List<MusicBean> musicBeans = SqliteUtils.getInstance(musicDBPath).queryAllData("music_bean", MusicBean.class);
        for (MusicBean musicBean : musicBeans) {
            JsoupUtils.getInstance().voiceEnableUrls.add(musicBean.wma);
        }
        JsoupUtils.getInstance().getContent(null, url);
    }
}
