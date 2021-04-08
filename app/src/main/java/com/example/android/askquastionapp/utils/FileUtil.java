package com.example.android.askquastionapp.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by DB_BOY on 2019/6/24.</br>
 * String DOC = "application/msword";
 * String XLS = "application/vnd.ms-excel";
 * String PPT = "application/vnd.ms-powerpoint";
 * String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
 * String XLSX = "application/x-excel";
 * String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
 * String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
 * String PDF = "application/pdf";
 * String MP4 = "video/mp4";
 * String M3U8 = "application/x-mpegURL";
 */
public class FileUtil {
    public static String readFileSize(String path) {
        return readableFileSize(new File(path).length());
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    public static String getPath(Context context, Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            //一些三方的文件浏览器会进入到这个方法中，例如ES
            //QQ文件管理器不在此列
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                String id = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && isNokiaDocument(uri)) {
            String path = uri.getPath();
            if (path != null && path.startsWith("/shared_files")) {
                path = path.replaceFirst("/shared_files", "");
            }
            return path;
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore (and general)
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isQQMediaDocument(uri)) {
                String path = uri.getPath();
                File fileDir = Environment.getExternalStorageDirectory();
                File file = new File(fileDir, path.substring("/QQBrowser".length(), path.length()));
                return file.exists() ? file.toString() : null;
            }

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return uri.getPath();
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        return getDataColumn(context, contentUri, null, null);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isNokiaDocument(Uri uri) {
        return "com.fihtdc.filemanager.provider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * 使用第三方qq文件管理器打开
     *
     * @param uri
     * @return
     */
    public static boolean isQQMediaDocument(Uri uri) {
        return "com.tencent.mtt.fileprovider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static File assetsToFile(Context context, String assetsName) {
        try {
            InputStream in = context.getResources().getAssets().open(assetsName);
            FileOutputStream fos = null;
            try {
                byte[] data = new byte[1024];
                int offset = 0;
                File file = new File(context.getFilesDir(), assetsName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                while ((offset = in.read(data)) > -1) {
                    fos.write(data, 0, offset);
                }
                return file;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                in.close();
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 读取文件指定行。
    public static String readAppointedLineNumber(File sourceFile, int lineNumber)
            throws IOException {
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        int lines = 0;
        while (s != null) {
            s = reader.readLine();
            if (lines == lineNumber) {
                break;
            }
            lines++;
        }
        reader.close();
        in.close();
        return s;
    }

    //复制文件
    public static void copyFile(File oldfile, File newfile) {
        try {
            if (!oldfile.exists() || !oldfile.isFile()) {
                return;
            }
            if (newfile.exists()) {//新文件路径下有同名文件
                return;
            } else {
                newfile.createNewFile();
            }
            FileInputStream fin = new FileInputStream(oldfile);//输入流
            FileOutputStream fout = new FileOutputStream(newfile, true);//输出流
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

    public static List<File> sortFileWithLastModify(File dir, @IntRange(from = 0, to = 2) int sortType) {
        File[] files = dir.listFiles();
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
        List<File> sortFiles = new ArrayList<>();
        for (int i = 0; i < lastModifies.length; i++) {
            int firstIndex = tempLastModifies.indexOf(lastModifies[i]);
            File file = files[firstIndex];
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

    public static <T> boolean isImageFile(T file) {
        String name = file instanceof File ? ((File) file).getName() : ((DocumentFile) file).getName();
        String path = file instanceof File ? ((File) file).getPath() : ((DocumentFile) file).getUri().getPath();
        if (name == null) {
            return false;
        }
        //获取拓展名
        String fileEnd = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        String fileHeader = getFileHeader(path);
        if (fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif") || fileEnd.equals("jpeg") || fileEnd.equals("bmp") || fileEnd.equals("webp")) {
            return true;
        }
        if ("FFD8FF".equalsIgnoreCase(fileHeader) || "89504E47".equalsIgnoreCase(fileHeader) || "47494638".equalsIgnoreCase(fileHeader)
                || "49492A00".equalsIgnoreCase(fileHeader) || "424D".equalsIgnoreCase(fileHeader)) {
            return true;
        }
        return false;
    }

    /**
     * 根据文件路径获取文件头信息
     *
     * @param filePath 文件路径
     * @return 文件头信息
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[4];
            /*
             * int read() 从此输入流中读取一个数据字节。 int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */
            int read = is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception ignore) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        }
        return value;
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (byte b : src) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        System.out.println(builder.toString());
        return builder.toString();
    }

    public static String getFileSize(File file) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        if (file.length() > 1024 * 1024) {
            String string = decimalFormat.format(file.length() / 1024f / 1024f);//返回字符串
            return string + "M";
        } else if (file.length() > 1024) {
            String string = decimalFormat.format(file.length() / 1024f);//返回字符串
            return string + "K";
        }
        return file.length() + "B";
    }

    public static Uri getCurrentUri(Context activity, String filePath) {
        if (activity == null) {
            return null;
        }
        File file = new File(filePath);
        if (Build.VERSION.SDK_INT < N) {
            return Uri.fromFile(file);
        } else if (Build.VERSION.SDK_INT < 29) {
            return FileProvider.getUriForFile(activity, activity.getPackageName() + ".FileProvider", file);
        } else {
            Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (file.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static File uriToFileApiQ(Uri uri, Context context) {
        File file = null;
        if(uri == null || uri.getPath() == null) {
            return null;
        }
        //android10以上转换
        if (uri.getScheme() != null && uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getExternalCacheDir().getAbsolutePath(), "temp");
                FileOutputStream fos = new FileOutputStream(cache);
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
