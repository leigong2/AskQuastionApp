package com.example.android.askquastionapp.media;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PictureCheckManager {
    private static PictureCheckManager sPictureCheckManager;

    private PictureCheckManager() {
    }

    public static PictureCheckManager getInstance() {
        if (sPictureCheckManager == null) {
            synchronized (PictureCheckManager.class) {
                if (sPictureCheckManager == null) {
                    sPictureCheckManager = new PictureCheckManager();
                }
            }
        }
        return sPictureCheckManager;
    }

    public List<MediaData> getAllPictures(Context context) {
        long startTime = System.currentTimeMillis();
        List<MediaData> result = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return result;
        }
        while (cursor.moveToNext()) {
            MediaData mediaData = new MediaData();
            mediaData.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            result.add(mediaData);
        }
        cursor.close();
        LogUtils.i("zune: ", "cursor count = " + result.size() + ", time = " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        Map<String, List<MediaData>> allPictures = getAllPictures(Environment.getExternalStorageDirectory());
        LogUtils.i("zune: ", "allPictures count = " + allPictures.size() + ", time = " + (System.currentTimeMillis() - startTime));
        return result;
    }

    public Map<String, List<MediaData>> getAllPictures(File dir) {
        long startTime = System.currentTimeMillis();
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllPictures(file));
                }
            } else if (file.length() > 1024 * 100 && isImageFile(file)) {
                MediaData mediaData = new MediaData();
                mediaData.path = file.getPath();
                String parent = file.getParent();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                group.add(mediaData);
                resultMap.put(parent, group);
            }
        }
        LogUtils.i("zune: ", "allPictures count = " + resultMap.size() + ", time = " + (System.currentTimeMillis() - startTime));
        ToastUtils.showLong("allPictures count = " + resultMap.size() + ", time = " + (System.currentTimeMillis() - startTime));
        return resultMap;
    }

    private boolean isImageFile(File file) {
        String name = file.getName();
        //获取拓展名
        String fileEnd = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        return fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif") || fileEnd.equals("jpeg") || fileEnd.equals("bmp");
    }

    public List<MediaData> getPrivatePictures() {
        File dir = new File(Environment.getExternalStorageDirectory(), "Android");
        return getPrivate(new ArrayList<>(), dir);
    }

    private List<MediaData> getPrivate(List<MediaData> result, File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    result.addAll(getPrivate(result, file));
                }
            } else if (file.length() > 1024 * 100 && isImageFile(file)) {
                MediaData mediaData = new MediaData();
                mediaData.path = file.getPath();
                result.add(mediaData);
            }
        }
        return result;
    }

    public List<MediaData> getTencentPictures() {
        List<MediaData> result = new ArrayList<>();
        return result;
    }

    public static class MediaData {
        public String path;
        public long size;
        public String type;
        public int mediaType;
        public String folder;
    }
}
