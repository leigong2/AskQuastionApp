package com.example.android.askquastionapp.bean;

public class HrefData {
    public String href;
    public String title;
    public String text;

    public HrefData() {
    }

    public HrefData(String href, String title, String text) {
        this.href = href;
        this.title = title;
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
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
