package com.example.android.askquastionapp.bean;

public class VideoBean {
    public int page;
    public String video_url;
    public String video_name;
    public String video_type;
    public String video_add_time;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

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
        return "VideoBean{" +
                "page=" + page +
                ", video_url='" + video_url + '\'' +
                ", video_name='" + video_name + '\'' +
                ", video_type='" + video_type + '\'' +
                ", video_add_time='" + video_add_time + '\'' +
                '}';
    }
}
