package com.example.android.askquastionapp.bean;


import androidx.annotation.Nullable;

/**
 * Created by leigong2 on 2018-03-17 017.
 */

public class ImageTag implements Comparable<ImageTag> {
    public String url;
    public Integer position;

    public ImageTag(String url, Integer position) {
        this.url = url;
        this.position = position;
    }

    @Override
    public int compareTo(@Nullable ImageTag o) {
        if (position != null && o != null && o.position != null) {
            return position.compareTo(o.position);
        } else {
            return 0;
        }
    }
}
