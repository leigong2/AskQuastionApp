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
import java.util.List;

public class TelegramUtils {
    private static List<String> lists = new ArrayList<>();

    public static void renameFiles(String fileDirPath) {
        lists.clear();
        File fileDir = new File(fileDirPath);
        for (File file : fileDir.listFiles()) {
            if (!file.getName().equalsIgnoreCase("5_6134375340744638650.txt")) {
//                continue;
            }
            if (file.getName().startsWith("1_") || file.getName().startsWith("5_")) {
                try {
                    String s = FileUtil.codeString(file.getPath());
                    System.out.println(s + "............" + file.getName());
//                    FileUtil.encodeFileToUtf8("gbk", s, file.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(lists);
    }

    private static void renameFile(File file) {
        try {
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(file),  "gbk"));
            File temp = new File(file.getParentFile(), "temp.txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp, false), "gbk"));
            String s = null;
            while ((s = fr.readLine()) != null) {
                String replaceAll = s.replaceAll("\t", "").replaceAll(" ", "").replaceAll("　", "");
                if (replaceAll.trim().length() > 0) {
                    if (replaceAll.length() > 15) {
                        s = replaceAll.substring(0, 15);
                    }
                    break;
                }
            }
            if (s != null) {
                lists.add(s);
            }
            bw.write(s + "\n");
            bw.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
