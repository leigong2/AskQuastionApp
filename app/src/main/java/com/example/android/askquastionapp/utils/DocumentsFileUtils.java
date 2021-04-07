package com.example.android.askquastionapp.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringDef;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.android.askquastionapp.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class DocumentsFileUtils {

    //https://www.jianshu.com/p/f3fcf033be5c
    public static final String VIDEO_TYPE = "video/mp4";
    public static final String TXT_TYPE = "text/plain";
    public static final String WORD_TYPE = "application/msword";
    public static final String EXCEL_TYPE = "text/xml";
    public static final String VOICE_TYPE = "audio/x-mpeg";
    public static final String IMAGE_TYPE = "image/jpeg";

    @StringDef({VIDEO_TYPE, TXT_TYPE, WORD_TYPE, EXCEL_TYPE, VOICE_TYPE, IMAGE_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NormalMimeType {
    }

    public static String mUriPath;

    private static final String TAG = DocumentsFileUtils.class.getSimpleName();

    public static final int OPEN_DOCUMENT_TREE_CODE = 8000;

    private static List<String> sExtSdCardPaths = new ArrayList<>();

    private DocumentsFileUtils() {
        init();
    }

    private void init() {
        if (!hasPermissions(BaseApplication.getInstance())) {
            return;
        }
        rootPath = getExtSDCardPath(BaseApplication.getInstance());
    }

    public @Nullable String[] rootPath;

    private static DocumentsFileUtils sUtils;

    public static DocumentsFileUtils getInstance() {
        if (sUtils == null) {
            synchronized (DocumentsFileUtils.class) {
                if (sUtils == null) {
                    sUtils = new DocumentsFileUtils();
                }
            }
        } else if (sUtils.rootPath == null) {
            sUtils.init();
        }
        return sUtils;
    }

    public void cleanCache() {
        sExtSdCardPaths.clear();
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String[] getExtSdCardPaths(Context context) {
        if (sExtSdCardPaths.size() > 0) {
            return sExtSdCardPaths.toArray(new String[0]);
        } else {
            sExtSdCardPaths.toArray(getExtSDCardPath(context));
        }
        return getExtSDCardPath(context);
        /*for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    sExtSdCardPaths.add(path);
                }
            }
        }
        if (sExtSdCardPaths.isEmpty()) sExtSdCardPaths.add("/storage/sdcard1");
        return sExtSdCardPaths.toArray(new String[0]);*/
    }

    /**
     * zune: 获取路所有存储器路径,一般情况下position = 0是内置存储,position= 1是外置存储
     **/
    public String[] getExtSDCardPath(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context
                .STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            return (String[]) invoke;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD
     * card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isOnExtSdCard(final File file, Context c) {
        return getExtSdCardFolder(file, c) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5).
     * If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    public DocumentFile fileToDocument(final File file, final boolean isDirectory, Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file);
        }
        String baseFolder = getExtSdCardFolder(file, context);
        Log.i(TAG, "lum_ baseFolder " + baseFolder);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }
        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relativePath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString(baseFolder,
                null);
        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }
        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);
            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }
        return document;
    }

    public File documentToFile(DocumentFile file) {
        if (TextUtils.isEmpty(mUriPath)) {
            return null;
        }
        if (rootPath == null) {
            return null;
        }
        try {
            String decode = URLDecoder.decode(file.getUri().toString().replace(mUriPath, ""), "utf-8");
            String[] split = decode.split(":");
            return new File(rootPath[rootPath.length - 1] + File.separator + split[split.length - 1]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new File(rootPath[rootPath.length - 1] + File.separator + file.getUri().toString().replace(mUriPath, ""));
    }

    public boolean mkdirs(Context context, File dir) {
        boolean res = dir.mkdirs();
        if (!res) {
            if (DocumentsFileUtils.getInstance().isOnExtSdCard(dir, context)) {
                DocumentFile documentFile = DocumentsFileUtils.getInstance().fileToDocument(dir, true, context);
                res = documentFile != null && documentFile.canWrite();
            }
        }
        return res;
    }

    public boolean delete(Context context, File file) {
        boolean ret = file.delete();
        if (!ret && DocumentsFileUtils.getInstance().isOnExtSdCard(file, context)) {
            DocumentFile f = DocumentsFileUtils.getInstance().fileToDocument(file, false, context);
            if (f != null) {
                ret = f.delete();
            }
        }
        return ret;
    }

    private boolean canWrite(File file) {
        boolean res = file.exists() && file.canWrite();
        if (!res && !file.exists()) {
            try {
                if (!file.isDirectory()) {
                    res = file.createNewFile() && file.delete();
                } else {
                    res = file.mkdirs() && file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public boolean canWrite(Context context, File file) {
        boolean res = canWrite(file);
        if (!res && DocumentsFileUtils.getInstance().isOnExtSdCard(file, context)) {
            DocumentFile documentFile = DocumentsFileUtils.getInstance().fileToDocument(file, true, context);
            res = documentFile != null && documentFile.canWrite();
        }
        return res;
    }

    public boolean renameTo(Context context, DocumentFile srcDoc, DocumentFile destDoc) {
        boolean res = false;
        if (srcDoc != null && destDoc != null) {
            try {
                if (destDoc.getParentFile() != null) {
                    if (destDoc.getParentFile().equals(destDoc.getParentFile())) {
                        if (destDoc.getName() != null) {
                            res = srcDoc.renameTo(destDoc.getName());
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (srcDoc.getParentFile() != null) {
                            res = DocumentsContract.moveDocument(context.getContentResolver(),
                                    srcDoc.getUri(),
                                    srcDoc.getParentFile().getUri(),
                                    destDoc.getUri()) != null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public boolean renameTo(Context context, File src, File dest) {
        boolean res = src.renameTo(dest);
        if (!res && isOnExtSdCard(dest, context)) {
            DocumentFile srcDoc;
            if (isOnExtSdCard(src, context)) {
                srcDoc = fileToDocument(src, false, context);
            } else {
                srcDoc = DocumentFile.fromFile(src);
            }
            DocumentFile destDoc = fileToDocument(dest.getParentFile(), true, context);
            if (srcDoc != null && destDoc != null) {
                try {
                    if (src.getParent().equals(dest.getParent())) {
                        res = srcDoc.renameTo(dest.getName());
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        res = DocumentsContract.moveDocument(context.getContentResolver(),
                                srcDoc.getUri(),
                                srcDoc.getParentFile().getUri(),
                                destDoc.getUri()) != null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    public InputStream getInputStream(Context context, File destFile) {
        InputStream in = null;
        try {
            if (!canWrite(destFile) && isOnExtSdCard(destFile, context)) {
                DocumentFile file = DocumentsFileUtils.getInstance().fileToDocument(destFile, false, context);
                if (file != null && file.canWrite()) {
                    in = context.getContentResolver().openInputStream(file.getUri());
                }
            } else {
                in = new FileInputStream(destFile);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return in;
    }

    public OutputStream getOutputStream(Context context, File destFile) {
        OutputStream out = null;
        try {
            if (!canWrite(destFile) && isOnExtSdCard(destFile, context)) {
                DocumentFile file = DocumentsFileUtils.getInstance().fileToDocument(destFile, false, context);
                if (file != null && file.canWrite()) {
                    out = context.getContentResolver().openOutputStream(file.getUri());
                }
            } else {
                out = new FileOutputStream(destFile);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    public boolean saveTreeUri(Context context, String rootPath, Uri uri) {
        DocumentFile file = DocumentFile.fromTreeUri(context, uri);
        if (file != null && file.canWrite()) {
            SharedPreferences perf = PreferenceManager.getDefaultSharedPreferences(context);
            perf.edit().putString(rootPath, uri.toString()).apply();
            Log.e(TAG, "save uri" + rootPath);
            return true;
        } else {
            Log.e(TAG, "no write permission: " + rootPath);
        }
        return false;
    }

    public boolean checkWritableRootPath(Context context, String rootPath) {
        File root = new File(rootPath);
        if (!root.canWrite()) {
            if (DocumentsFileUtils.getInstance().isOnExtSdCard(root, context)) {
                Log.i(TAG, "lum_ isOnExtSdCard");
                DocumentFile documentFile = DocumentsFileUtils.getInstance().fileToDocument(root, true, context);
                return documentFile == null || !documentFile.canWrite();
            } else {
                Log.i(TAG, "lum_2 get perf");
                SharedPreferences perf = PreferenceManager.getDefaultSharedPreferences(context);

                String documentUri = perf.getString(rootPath, "");

                if (documentUri == null || documentUri.isEmpty()) {
                    return true;
                } else {
                    DocumentFile file = DocumentFile.fromTreeUri(context, Uri.parse(documentUri));
                    return !(file != null && file.canWrite());
                }
            }
        }
        return false;
    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储路径，true返回外置SD卡路径
     * @return
     */
    private String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public boolean hasUriTree(String rootPath) {
        String as = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).getString(rootPath, null);
        if (!TextUtils.isEmpty(as)) {
            return true;
        }
        return false;
    }

    public DocumentFile getUriDocumentFile(String rootPath) {
        String as = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).getString(rootPath,
                null);
        Uri treeUri = null;
        if (!TextUtils.isEmpty(as)) {
            treeUri = Uri.parse(as);
        }
        if (treeUri == null) {
            return null;
        }
        return DocumentFile.fromTreeUri(BaseApplication.getInstance(), treeUri);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean showOpenDocumentTree(Activity activity) {
        if (!hasPermissions(activity)) {
            return false;
        }
        if (rootPath == null) {
            return false;
        }
        if (hasUriTree(rootPath[rootPath.length - 1])) {
            DocumentFile uriDocumentFile = getUriDocumentFile(rootPath[rootPath.length - 1]);
            mUriPath = uriDocumentFile.getUri().toString();
            return false;
        }
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = activity.getSystemService(StorageManager.class);
            StorageVolume volume = sm.getStorageVolume(new File(rootPath[rootPath.length - 1]));
            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }
        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        activity.startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean showOpenDocumentTreeIgnore(Activity activity) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = activity.getSystemService(StorageManager.class);
            StorageVolume volume = sm.getStorageVolume(new File(rootPath[rootPath.length - 1]));
            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }
        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        activity.startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
        return true;
    }

    public void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OPEN_DOCUMENT_TREE_CODE:
                if (rootPath == null) {
                    return;
                }
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    saveTreeUri(context, rootPath[rootPath.length - 1], uri);
                    mUriPath = uri.toString();
                    Log.i(TAG, "lum_uri ： " + uri);
                }
                break;
            default:
                break;
        }
    }

    public boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(activity);
                return false;
            }
        }
        return true;
    }

    public boolean hasPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    //复制文件
    public static void copyFile(Context context, File oldfile, DocumentFile newDocumentFile, @NormalMimeType String mimeType, String path) {
        try {
            if (!oldfile.exists() || !oldfile.isFile()) {
                return;
            }
            newDocumentFile.createFile(mimeType, path);
            FileInputStream fin = new FileInputStream(oldfile);//输入流
            OutputStream fout = context.getContentResolver().openOutputStream(newDocumentFile.getUri());;//输出流
            byte[] b = new byte[1024];
            while ((fin.read(b)) != -1) {//读取到末尾 返回-1 否则返回读取的字节个数
                fout.write(b);
            }
            fin.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*zune：获取文件夹里面的文件，并排序**/
    public static List<DocumentFile> sortFileWithLastModify(DocumentFile dir, @IntRange(from = 0, to = 2) int sortType) {
        DocumentFile[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        List<Long> tempLastModifies = new ArrayList<>();
        long[] lastModifies = new long[files.length];
        for (int i = 0; i < files.length; i++) {
            if (sortType == 1) {
                lastModifies[i] = files[i].length();
                tempLastModifies.add(files[i].length());
            } else {
                lastModifies[i] = files[i].lastModified();
                tempLastModifies.add(files[i].lastModified());
            }
        }
        if (sortType > 0) {
            for (int i = 0; i < lastModifies.length; i++) {
                for (int j = i + 1; j <= lastModifies.length - 1; j++) {
                    if (lastModifies[i] > lastModifies[j]) {
                        long temp = lastModifies[i];
                        lastModifies[i] = lastModifies[j];
                        lastModifies[j] = temp;
                    }
                }
            }
        }
        List<DocumentFile> sortFiles = new ArrayList<>();
        for (int i = 0; i < lastModifies.length; i++) {
            int firstIndex = tempLastModifies.indexOf(lastModifies[i]);
            DocumentFile file = files[firstIndex];
            while (sortFiles.contains(file)) {
                firstIndex += (tempLastModifies.subList(firstIndex + 1, tempLastModifies.size()).indexOf(lastModifies[i]) + 1);
                if (firstIndex == -1 || firstIndex >= files.length) {
                    break;
                }
                file = files[firstIndex];
            }
            sortFiles.add(file);
        }
        return sortFiles;
    }
}
