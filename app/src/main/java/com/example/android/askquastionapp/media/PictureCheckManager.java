package com.example.android.askquastionapp.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.utils.DocumentsFileUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.LogUtils;
import com.example.android.askquastionapp.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.example.android.askquastionapp.utils.DocumentsFileUtils.OPEN_DOCUMENT_TREE_CODE;
import static com.example.android.askquastionapp.utils.FileUtil.isImageFile;

public class PictureCheckManager {
    private static PictureCheckManager sPictureCheckManager;
    private int fileSize = 1024 * 30;

    private Handler mHandler;
    private int mediaType;

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

    private int sortType = 2;  //0.默认，1.大小，2.修改时间

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public int getSortType() {
        return sortType;
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
        String storagePath = DocumentsFileUtils.getInstance().getExtSDCardPath(BaseApplication.getInstance());
        List<File> files = new ArrayList<>();
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        File[] neiFiles = dir.listFiles();
        if (neiFiles != null) {
            Collections.addAll(files, neiFiles);
        }
        if (storagePath != null) {
            File[] waiFiles = new File(storagePath).listFiles();
            if (waiFiles != null) {
                Collections.addAll(files, waiFiles);
            }
        }
        for (File file : files) {
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
            File[] listFiles = file.listFiles();
            if (file.isDirectory() && listFiles != null) {
                if (listFiles.length > 0) {
                    resultMap.putAll(getAllVideos(handler, file, mediaType));
                }
            } else if (!file.isDirectory() && file.length() > fileSize && isVideoFile(file)) {
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
        String storagePath = DocumentsFileUtils.getInstance().getExtSDCardPath(BaseApplication.getInstance());
        List<File> files = new ArrayList<>();
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        File[] neiFiles = dir.listFiles();
        if (neiFiles != null) {
            Collections.addAll(files, neiFiles);
        }
        if (storagePath != null) {
            File[] waiFiles = new File(storagePath).listFiles();
            if (waiFiles != null) {
                Collections.addAll(files, waiFiles);
            }
        }
        for (File file : files) {
            String pathName = file.getPath().replaceAll(dir.getPath(), "");
            if (pathName.startsWith("/Android")) {
                resultMap.putAll(getAllPictures(handler, new File(dir, "/Android/data/org.telegram.messenger/cache"), mediaType));
                continue;
            }
            if (pathName.startsWith("/.")) {
                continue;
            }
            if (pathName.startsWith("/tencent")) {
                continue;
            }
            File[] listFiles = file.listFiles();
            if (file.isDirectory() && listFiles != null) {
                if (listFiles.length > 0) {
                    resultMap.putAll(getAllPictures(handler, file, mediaType));
                }
            } else {
                if (file.length() > fileSize && isImageFile(file)) {
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
                        MediaData parentMedia = new MediaData();
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
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getAllPictures(Handler handler, File dir, int mediaType) {
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        List<File> files = FileUtil.sortFileWithLastModify(dir, sortType);
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getPath().contains("/.")) {
                    continue;
                }
                File[] filesList = file.listFiles();
                if (filesList != null && filesList.length > 0) {
                    resultMap.putAll(getAllPictures(handler, file, mediaType));
                }
            } else if (file.length() > fileSize && isImageFile(file)) {
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
            } else if (file.length() > fileSize && isVideoFile(file)) {
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

    public static <T> boolean isVideoFile(T file) {
        String name = file instanceof File ? ((File) file).getName() : ((DocumentFile) file).getName();
        String path = file instanceof File ? ((File) file).getPath() : ((DocumentFile) file).getUri().getPath();
        if (name == null) {
            return false;
        }
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
        File[] files = dir.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File[] files1 = file.listFiles();
                if (files1 != null && files1.length > 0) {
                    result.addAll(getPrivate(result, file));
                }
            } else if (file.length() > fileSize && isImageFile(file)) {
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

    public void getQNormalPictures(LifecycleOwner lifecycle, Handler handler, int mediaType) {
        mHandler = handler;
        this.mediaType = mediaType;
        Uri currentTreeUri = DocumentsFileUtils.getInstance().getCurrentTreeUri();
        if (currentTreeUri == null) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            if (lifecycle instanceof Activity) {
                ((Activity) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
            } else if (lifecycle instanceof Fragment) {
                ((Fragment) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
            }
        } else {
            getNormalPicturesFromUri(currentTreeUri, mHandler, mediaType);
        }
    }

    public void getQNormalVideos(LifecycleOwner lifecycle, Handler handler, int mediaType) {
        mHandler = handler;
        this.mediaType = mediaType;
        Uri currentTreeUri = DocumentsFileUtils.getInstance().getCurrentTreeUri();
        if (currentTreeUri == null) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            if (lifecycle instanceof Activity) {
                ((Activity) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
            } else if (lifecycle instanceof Fragment) {
                ((Fragment) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
            }
        } else {
            getNormalVideosFromUri(currentTreeUri, mHandler, mediaType);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != OPEN_DOCUMENT_TREE_CODE || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uriDir = data.getData();
        if (uriDir == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mediaType == 1) {
                    getNormalVideosFromUri(uriDir, mHandler, mediaType);
                } else {
                    getNormalPicturesFromUri(uriDir, mHandler, mediaType);
                }
            }
        }.start();
    }

    private Map<String, List<MediaData>> getNormalPicturesFromUri(Uri uriDir, Handler handler, int mediaType) {
        DocumentFile dir = DocumentFile.fromTreeUri(BaseApplication.getInstance(), uriDir);
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        if (dir.listFiles() == null) {
            return resultMap;
        }
        for (DocumentFile file : dir.listFiles()) {
            String path = file.getUri().getPath();
            if (path == null) {
                continue;
            }
            String dirPath = dir.getUri().getPath();
            if (dirPath == null) {
                continue;
            }
            String pathName = path.replaceAll(dirPath, "");
            if (pathName.startsWith("Android")) {
//                resultMap.putAll(getAllQPictures(new DocumentFile(dir, "/Android/data/org.telegram.messenger/cache"), handler, mediaType));
                continue;
            }
            if (pathName.startsWith("tencent")) {
                continue;
            }
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllQPictures(file, handler, mediaType));
                }
            } else {
                if (file.length() > fileSize && isImageFile(file)) {
                    MediaData mediaData = new MediaData();
                    mediaData.path = file.getUri().getPath();
                    mediaData.pathUri = file.getUri();
                    String parent = file.getParentFile() == null ? "" : file.getParentFile().getName();
                    List<MediaData> group = resultMap.get(parent);
                    if (group == null) {
                        group = new ArrayList<>();
                    }
                    group.add(mediaData);
                    if (!resultMap.containsKey(parent)) {
                        Message msg = new Message();
                        msg.what = mediaType;
                        mediaData.folder = parent;
                        mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                        MediaData parentMedia = new MediaData();
                        parentMedia.folder = parent;
                        parentMedia.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                        msg.obj = parentMedia;
                        handler.sendMessage(msg);
                    }
                    resultMap.put(parent, group);
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    msg.obj = mediaData;
                    handler.sendMessage(msg);
                }
            }
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getNormalVideosFromUri(Uri uriDir, Handler handler, int mediaType) {
        DocumentFile dir = DocumentFile.fromTreeUri(BaseApplication.getInstance(), uriDir);
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        if (dir.listFiles() == null) {
            return resultMap;
        }
        for (DocumentFile file : dir.listFiles()) {
            String path = file.getUri().getPath();
            if (path == null) {
                continue;
            }
            String dirPath = dir.getUri().getPath();
            if (dirPath == null) {
                continue;
            }
            String pathName = path.replaceAll(dirPath, "");
            if (pathName.startsWith("Android")) {
//                resultMap.putAll(getAllQPictures(new DocumentFile(dir, "/Android/data/org.telegram.messenger/cache"), handler, mediaType));
                continue;
            }
            if (pathName.startsWith("tencent")) {
                continue;
            }
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    resultMap.putAll(getAllQVideos(file, handler, mediaType));
                }
            } else {
                if (file.length() > fileSize && isImageFile(file)) {
                    MediaData mediaData = new MediaData();
                    mediaData.path = file.getUri().getPath();
                    mediaData.pathUri = file.getUri();
                    String parent = file.getParentFile() == null ? "" : file.getParentFile().getName();
                    List<MediaData> group = resultMap.get(parent);
                    if (group == null) {
                        group = new ArrayList<>();
                    }
                    group.add(mediaData);
                    if (!resultMap.containsKey(parent)) {
                        Message msg = new Message();
                        msg.what = mediaType;
                        mediaData.folder = parent;
                        mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                        MediaData parentMedia = new MediaData();
                        parentMedia.folder = parent;
                        parentMedia.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                        msg.obj = parentMedia;
                        handler.sendMessage(msg);
                    }
                    resultMap.put(parent, group);
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    msg.obj = mediaData;
                    handler.sendMessage(msg);
                }
            }
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getAllQPictures(DocumentFile documentDir, Handler handler, int mediaType) {
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        List<DocumentFile> files = DocumentsFileUtils.sortFileWithLastModify(documentDir, sortType);
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                if (file.getUri().getPath() != null && file.getUri().getPath().contains("/.")) {
                    LogUtils.i("zune: ", "........dir path = " + file.getUri().getPath());
                    continue;
                }
                if (file.listFiles() != null && file.listFiles().length > 0) {
                    LogUtils.i("zune: ", "dir path = " + file.getUri().getPath());
                    resultMap.putAll(getAllQPictures(file, handler, mediaType));
                }
            } else if (file.length() > fileSize && isImageFile(file)) {
                LogUtils.i("zune: ", "image path = " + file.getUri().getPath());
                MediaData mediaData = new MediaData();
                mediaData.path = file.getUri().getPath();
                mediaData.pathUri = file.getUri();
                String parent = file.getParentFile() == null ? "" : file.getParentFile().getName();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    parentMedia.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                group.add(mediaData);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                msg.obj = mediaData;
                handler.sendMessage(msg);
                resultMap.put(parent, group);
            } else {
                LogUtils.i("zune: ", "file path = " + file.getUri().getPath());
            }
        }
        return resultMap;
    }

    private Map<String, List<MediaData>> getAllQVideos(DocumentFile documentDir, Handler handler, int mediaType) {
        Map<String, List<MediaData>> resultMap = new HashMap<>();
        List<DocumentFile> files = DocumentsFileUtils.sortFileWithLastModify(documentDir, sortType);
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                if (file.getUri().getPath() != null && file.getUri().getPath().contains("/.")) {
                    LogUtils.i("zune: ", "........dir path = " + file.getUri().getPath());
                    continue;
                }
                if (file.listFiles() != null && file.listFiles().length > 0) {
                    LogUtils.i("zune: ", "dir path = " + file.getUri().getPath());
                    resultMap.putAll(getAllQVideos(file, handler, mediaType));
                }
            } else if (file.length() > fileSize && isVideoFile(file)) {
                LogUtils.i("zune: ", "image path = " + file.getUri().getPath());
                MediaData mediaData = new MediaData();
                mediaData.mediaType = 1;
                mediaData.path = file.getUri().getPath();
                mediaData.pathUri = file.getUri();
                String parent = file.getParentFile() == null ? "" : file.getParentFile().getName();
                List<MediaData> group = resultMap.get(parent);
                if (group == null) {
                    group = new ArrayList<>();
                }
                if (!resultMap.containsKey(parent)) {
                    Message msg = new Message();
                    msg.what = mediaType;
                    mediaData.folder = parent;
                    mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    PictureCheckManager.MediaData parentMedia = new PictureCheckManager.MediaData();
                    parentMedia.folder = parent;
                    parentMedia.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                    msg.obj = parentMedia;
                    handler.sendMessage(msg);
                }
                group.add(mediaData);
                Message msg = new Message();
                msg.what = mediaType;
                mediaData.folder = parent;
                mediaData.folderUri = file.getParentFile() == null ? null : file.getParentFile().getUri();
                msg.obj = mediaData;
                handler.sendMessage(msg);
                resultMap.put(parent, group);
            } else {
                LogUtils.i("zune: ", "file path = " + file.getUri().getPath());
            }
        }
        return resultMap;
    }

    public static class MediaData {
        public String path;
        public long size;
        public String type;
        public int mediaType;//0-图片，1-视频
        public String folder;
        public Uri pathUri;
        public Uri folderUri;

        public String getName() {
            if (path == null) {
                return pathUri == null ? "" : pathUri.getPath();
            }
            String[] split = path.split(File.pathSeparator);
            return split[split.length - 1];
        }
    }
}
