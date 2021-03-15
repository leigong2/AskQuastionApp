package com.example.android.askquastionapp.media;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

        getAllPictures(null, 0);
        return result;
    }

    public Map<String, List<MediaData>> getAllPictures(Handler handler, int mediaType) {
        long startTime = System.currentTimeMillis();
        Map<String, List<MediaData>> allPictures = getAllPictures(handler, Environment.getExternalStorageDirectory(), mediaType);
        LogUtils.i("zune: ", "allPictures count = " + allPictures.size() + ", time = " + (System.currentTimeMillis() - startTime));
        ToastUtils.showLong("allPictures count = " + allPictures.size() + ", time = " + (System.currentTimeMillis() - startTime));
        return allPictures;
    }

    public Map<String, List<MediaData>> getNormalVideos(Handler handler, int mediaType) {
        File dir = Environment.getExternalStorageDirectory();
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        if (dir.listFiles() == null) {
            return resultMap;
        }
        List<File> arrayList = Arrays.asList(dir.listFiles());
        Collections.sort(arrayList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (!f1.isDirectory()) {
                    return -1;
                }
                if (!f2.isDirectory()) {
                    return 1;
                }
                if (f1.listFiles() == null) {
                    return -1;
                }
                if (f2.listFiles() == null) {
                    return 1;
                }
                return f1.listFiles().length - f2.listFiles().length;
            }
        });
        for (File file : arrayList) {
            if (file == null) {
                continue;
            }
            String pathName = file.getPath().replaceAll(dir.getPath(), "");
            if (pathName.startsWith("/.")) {
                continue;
            }
            if (pathName.startsWith("/Android")) {
                resultMap.putAll(getAllVideos(handler, new File(dir, "/Android/data/com.xunlei.downloadprovider/files/ThunderDownload"), mediaType));
                resultMap.putAll(getAllVideos(handler, new File(dir, "/Android/data/com.tencent.mm/MicroMsg/a7b426e55418f94695aa9f4c039faf7e/video"), mediaType));
                continue;
            }
            if (pathName.startsWith("/tencent")) {
                continue;
            }
            if (file.isDirectory() && file.listFiles() != null) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllVideos(handler, file, mediaType));
                }
            } else if (!file.isDirectory() && file.length() > 1024 * 100 && isVideoFile(file)) {
                MediaData mediaData = new MediaData();
                mediaData.path = file.getPath();
                mediaData.mediaType = 1;
                String parent = file.getParent();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                group.add(mediaData);
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                resultMap.put(parent, group);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                msg.obj = mediaData;
                handler.sendMessage(msg);
                resultMap.put(parent, group);
            }
        }
        return resultMap;
    }

    public Map<String, List<MediaData>> getNormalPictures(Handler handler, int mediaType) {
        File dir = Environment.getExternalStorageDirectory();
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        if (dir.listFiles() == null) {
            return resultMap;
        }
        List<File> arrayList = Arrays.asList(dir.listFiles());
        Collections.sort(arrayList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (!f1.isDirectory()) {
                    return -1;
                }
                if (!f2.isDirectory()) {
                    return 1;
                }
                if (f1.listFiles() == null) {
                    return -1;
                }
                if (f2.listFiles() == null) {
                    return 1;
                }
                return f1.listFiles().length - f2.listFiles().length;
            }
        });
        for (File file : arrayList) {
            String pathName = file.getPath().replaceAll(dir.getPath(), "");
            if (pathName.startsWith("/Android")) {
                resultMap.putAll(getAllPictures(handler, new File(dir, "/Android/data/org.telegram.messenger/cache"), mediaType));
                continue;
            }
            if (pathName.startsWith("/tencent")) {
                continue;
            }
            if (pathName.startsWith("/.")) {
                continue;
            }
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllPictures(handler, file, mediaType));
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
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                resultMap.put(parent, group);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                msg.obj = mediaData;
                handler.sendMessage(msg);
            }
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getAllPictures(Handler handler, File dir, int mediaType) {
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllPictures(handler, file, mediaType));
                }
            } else if (file.length() > 1024 * 100 && isImageFile(file)) {
                MediaData mediaData = new MediaData();
                mediaData.path = file.getPath();
                String parent = file.getParent();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                group.add(mediaData);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                msg.obj = mediaData;
                handler.sendMessage(msg);
                resultMap.put(parent, group);
            }
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getAllVideos(Handler handler, File dir, int mediaType) {
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        if (dir.listFiles() == null) {
            return resultMap;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (file.listFiles() != null && file.listFiles().length > 0) {
                    resultMap.putAll(getAllVideos(handler, file, mediaType));
                }
            } else if (file.length() > 1024 * 100 && isVideoFile(file)) {
                MediaData mediaData = new MediaData();
                mediaData.path = file.getPath();
                mediaData.mediaType = 1;
                String parent = file.getParent();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                group.add(mediaData);
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                resultMap.put(parent, group);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                msg.obj = mediaData;
                handler.sendMessage(msg);
                resultMap.put(parent, group);
            }
        }
        return resultMap;
    }

    private boolean isImageFile(File file) {
        String name = file.getName();
        //获取拓展名
        String fileEnd = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        return fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif") || fileEnd.equals("jpeg") || fileEnd.equals("bmp") || fileEnd.equals("webp");
    }

    private boolean isVideoFile(File file) {
        String name = file.getName();
        //获取拓展名
        String fileEnd = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        return fileEnd.equalsIgnoreCase("mp4") || fileEnd.equalsIgnoreCase("MOV")
                || fileEnd.equalsIgnoreCase("MPEG4") || fileEnd.equalsIgnoreCase("avi")
                || fileEnd.equalsIgnoreCase("WMV") || fileEnd.equalsIgnoreCase("avi")
                || fileEnd.equalsIgnoreCase("ts") || fileEnd.equalsIgnoreCase("FLV")
                || fileEnd.equalsIgnoreCase("F4V") || fileEnd.equalsIgnoreCase("RM")
                || fileEnd.equalsIgnoreCase("RMVB") || fileEnd.equalsIgnoreCase("3GP");
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
        public int mediaType;//0-图片，1-视频
        public String folder;
    }
}
