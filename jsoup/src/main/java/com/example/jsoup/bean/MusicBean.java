package com.example.jsoup.bean;

public class MusicBean {
    /*{
    "gsid": "2209",
    "gspic": "http://aliyunimg.9ku.com/pic/gstx/1/2209.jpg",
    "id": "511416",
    "mname": "爱神总是敲错门",
    "page": 8227,
    "singer": "张含韵",
    "status": "20",
    "wma": "http://mp3.9ku.com/hot/2013/05-14/511416.mp3",
    "zjid": "0",
    "zjname": "",
    "zjpic": ""
}*/
    public int page;
    public String id;
    public String gspic;
    public String gsid;
    public String status;
    public String zjid;
    public String zjname;
    public String zjpic;
    public String singer;
    public String mname;
    public String wma;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGspic() {
        return gspic;
    }

    public void setGspic(String gspic) {
        this.gspic = gspic;
    }

    public String getGsid() {
        return gsid;
    }

    public void setGsid(String gsid) {
        this.gsid = gsid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZjid() {
        return zjid;
    }

    public void setZjid(String zjid) {
        this.zjid = zjid;
    }

    public String getZjname() {
        return zjname;
    }

    public void setZjname(String zjname) {
        this.zjname = zjname;
    }

    public String getZjpic() {
        return zjpic;
    }

    public void setZjpic(String zjpic) {
        this.zjpic = zjpic;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getWma() {
        return wma;
    }

    public void setWma(String wma) {
        this.wma = wma;
    }

    @Override
    public String toString() {
        return "MusicBean{" +
                "page=" + page +
                ", id='" + id + '\'' +
                ", gspic='" + gspic + '\'' +
                ", gsid='" + gsid + '\'' +
                ", status='" + status + '\'' +
                ", zjid='" + zjid + '\'' +
                ", zjname='" + zjname + '\'' +
                ", zjpic='" + zjpic + '\'' +
                ", singer='" + singer + '\'' +
                ", mname='" + mname + '\'' +
                ", wma='" + wma + '\'' +
                '}';
    }
}
