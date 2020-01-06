package com.example.jsoup;

import com.example.jsoup.bean.VideoBean;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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


    // 读取文件指定行。
    public static List<VideoBean> readAppointedLineNumber(File sourceFile)
            throws IOException {
        List<VideoBean> temp = new ArrayList<>();
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        int lines = 0;
        VideoBean bean = new VideoBean();
        while (s != null) {
            s = reader.readLine();
            if (lines % 2 == 0) {
                bean.setVideo_name(s);
            } else {
                bean.setVideo_url(s);
                temp.add(bean);
                bean = new VideoBean();
            }
            lines++;
        }
        reader.close();
        in.close();
        return temp;
    }

    private static Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");

    public static void getFileName(String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            List<String> strings = Arrays.asList(dir.list());
            for (String string : strings) {
                String[] split = string.split("\\.");
                for (int i = string.length() - (split[split.length - 1].length() + 2); i >= 0; i--) {
                    if (pattern.matcher(String.valueOf(string.charAt(i))).matches()) {
                        continue;
                    }
                    String substring = string.substring(0, i + 1);
                    File file = new File(dir, substring);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    boolean b = renameTo(file, dir.getPath() + File.separator + string, string);
                    break;
                }
            }
        }
    }

    private static boolean renameTo(File fileDir, String filePath, String fileName) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return file.renameTo(new File(fileDir, fileName));
    }

    public static void redo(String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    for (File listFile : file.listFiles()) {
                        boolean b = renameTo(dir, listFile.getPath(), listFile.getName());
                        System.out.println(b);
                        if (b) {
                            file.delete();
                        }
                    }
                }
            }
        }
    }
}
