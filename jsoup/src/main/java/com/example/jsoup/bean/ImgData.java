package com.example.jsoup.bean;

public class ImgData {
    public String src;
    public String alt;

    public ImgData(String src, String alt) {
        this.src = src;
        this.alt = alt;
    }

    @Override
    public String toString() {
        return "ImgData{" +
                "src='" + src + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }
}
