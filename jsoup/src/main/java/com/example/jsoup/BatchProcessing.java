package com.example.jsoup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BatchProcessing {
    static File descFile = new File("D:\\user\\zune\\xmlTranslate\\xml2xml.bat");

    public static void createFiles(String dir) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(descFile));
            File rootDir = new File(dir);
            File[] fileDirs = rootDir.listFiles();
            if (fileDirs == null) {
                return;
            }
            for (File fileDir : fileDirs) {
                if (fileDir.isDirectory()) {
                    File[] files = fileDir.listFiles();
                    if (files == null) {
                        break;
                    }
                    for (File file : files) {
                        if (!file.getPath().endsWith(".xml")) {
                            continue;
                        }
                        String replaceFilePath = fileDir.getName() + File.separator + file.getName().replaceAll(".xml", ".txt");
                        String replaceFileDescPath = fileDir.getPath() + File.separator + file.getName();
                        String s = "java -jar AXMLPrinter2.jar " + fileDir.getName() + File.separator + file.getName() + ">" + replaceFilePath + "\n";
                        System.out.println(s);
                        bw.write(s);
//                        file.delete();
//                        File replaceFile = new File(replaceFilePath);
//                        replaceFile.renameTo(new File(replaceFileDescPath));
                    }
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
