package com.example.jsoup.bean;

public class HrefData {
    public String href;
    public String title;
    public String text;

    public HrefData(String href, String title, String text) {
        this.href = href;
        this.title = title;
        this.text = text;
    }

    @Override
    public String toString() {
        return "HrefData{" +
                "href='" + href + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
