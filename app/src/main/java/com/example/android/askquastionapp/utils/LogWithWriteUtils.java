package com.example.android.askquastionapp.utils;

import android.util.Log;

import com.example.android.askquastionapp.BaseApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogWithWriteUtils {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void i(String tag, String msg) {
        writeLog(msg, tag);
        Log.i(tag, msg);
    };

    private static void writeLog(String tag, String msg) {
        BufferedWriter bw = null;
        try {
            File fileDir = new File(BaseApplication.getInstance().getExternalCacheDir(), "log");
            File file = new File(fileDir, simpleDateFormat.format(new Date(System.currentTimeMillis())));
            if (!file.exists()) {
                file.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(tag + ": " + msg);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
