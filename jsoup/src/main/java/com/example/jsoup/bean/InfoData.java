package com.example.jsoup.bean;

public class InfoData {
    private String video_type;
    private String video_add_time;

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

    public String getVideo_add_time() {
        return video_add_time;
    }

    public void setVideo_add_time(String video_add_time) {
        this.video_add_time = video_add_time;
    }

    @Override
    public String toString() {
        return "InfoData{" +
                "video_type='" + video_type + '\'' +
                ", video_add_time='" + video_add_time + '\'' +
                '}';
    }
}
