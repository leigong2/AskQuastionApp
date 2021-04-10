package com.example.android.askquastionapp.utils;

import com.google.gson.Gson;

public class GsonGetter {
    private static GsonGetter gsonGetter;
    private Gson gson;

    private GsonGetter() {
    }

    public static GsonGetter getInstance() {
        if (gsonGetter == null) {
            gsonGetter = new GsonGetter();
        }
        return gsonGetter;
    }

    public Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
