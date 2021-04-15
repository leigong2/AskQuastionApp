package com.example.jsoup.reader;

import com.example.jsoup.FileUtil;
import com.example.jsoup.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MergeTxtUtils {
    public static void reMergeTxt(File fileDir) {
        if (fileDir == null || !fileDir.exists()) {
            return;
        }
        File[] files = fileDir.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
//                reMergeTxt(file);
            } else {
                readAndWriteTxt(file, ++count);
//                dismissFileToParent(file);
            }
        }
    }

    private static void dismissFileToParent(File file) {
        if (!file.getName().equalsIgnoreCase("2021-03-01_14-04-39.txt")) {
            return;
        }
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String s = null;
            BufferedWriter bw = null;
            File temp = null;
            List<String> titles = new ArrayList<>();
            while ((s = fileReader.readLine()) != null) {
                if (checkExtra(s) > 0) {
                    System.out.println(s);
                    String title = s.substring(s.indexOf("卷") + 1);
                    title = com.mysql.cj.util.StringUtils.isNullOrEmpty(title) ? s : title;
                    if (titles.contains(title)) {
                        title = title + 1;
                    }
                    titles.add(title);
                    temp = new File(file.getParentFile(), FileUtil.replaceUnableStr(title).split("第")[0] + ".txt");
                    boolean newFile = temp.createNewFile();
                    if (bw != null) {
                        bw.close();
                    }
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp)));
                    bw.write(title + "\n");
                } else if (bw != null) {
                    bw.write(StringUtils.convertToSimplifiedChinese(s) + "\n");
                }
            }
            fileReader.close();
            if (bw != null) {
                bw.close();
            }
//            file.delete();
//            temp.renameTo(file);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static int count = 0;
    private static int fileCount = 1;

    private static void readAndWriteTxt(File file, int count) {
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            File temp = new File(file.getParentFile(), "中短篇(" + fileCount + ").txt");
            if (temp.length() > 10 * 1024 * 1024) {
                count = 1;
                temp = new File(file.getParentFile(), "中短篇(" + ++fileCount + ").txt");
            }
            boolean newFile = temp.createNewFile();
            System.out.println("createFile: " + newFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp, true)));
//            String title = "第" + count + "卷 " + file.getName().replaceAll("\\.txt", "") + "\n";
//            bw.write(StringUtils.convertToSimplifiedChinese(title));
            String s = null;
            boolean ready = false;
            while ((s = fileReader.readLine()) != null) {
                if (!com.mysql.cj.util.StringUtils.isNullOrEmpty(s) && !ready) {
                    ready = true;
                    bw.write(StringUtils.convertToSimplifiedChinese("第" + count + "卷 " + s + "\n"));
                } else {
                    bw.write(StringUtils.convertToSimplifiedChinese(s) + "\n");
                }
            }
            fileReader.close();
            bw.close();
//            file.delete();
//            temp.renameTo(file);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static int checkExtra(String s) {
        int i = s.indexOf("第") - s.indexOf("卷");
        if (s.contains("第") && s.contains("卷") && i < -1 && i > -7) {
            return 1;
        }
        return -1;
    }
}
