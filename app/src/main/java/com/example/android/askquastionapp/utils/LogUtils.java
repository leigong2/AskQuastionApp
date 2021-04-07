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

public class LogUtils {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void i(String tag, String msg) {
        //writeLog(msg, tag);
        Log.i(tag, createMessage(msg));
    }

    private static String getMethodNames(StackTraceElement[] sElements) {
        String className = sElements[2].getFileName();
        String methodName = sElements[2].getMethodName();
        int lineNumber = sElements[2].getLineNumber();
        return "(" + className + ":" + lineNumber + ")";
    }

    private static String createMessage(String msg) {
        String name = getMethodNames(new Throwable().getStackTrace());
        return name + msg;
    }

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
