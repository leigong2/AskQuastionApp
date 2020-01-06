package com.example.android.askquastionapp.utils;

import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.jsoup.GsonGetter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SaveUtils {
    public static void save(SaveBean saveBean) {
        SPUtils.getInstance().put("save", GsonGetter.getInstance().getGson().toJson(saveBean));
    }

    public static SaveBean get() {
        String json = SPUtils.getInstance().getString("save");
        return GsonGetter.getInstance().getGson().fromJson(json, SaveBean.class);
    }

    public static class SaveBean implements Serializable{
        /**zune: 所有已保存的文件**/
        public List<Save> saves;
        /**zune: 文件是否被初始化了**/
        public boolean inited;
        public static class Save implements Serializable {
            /**zune: 文件路径**/
            public String path;
            /**zune: 文件所处页码位置**/
            public int position;
            /**zune: 文件总页码**/
            public int total;
        }
    }

    public static void putCache(String path, List<String> strings) {
        FileWriter fw = null;
        try {
            File rootFile = BaseApplication.getInstance().getCacheDir();
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }
            File dir = new File(rootFile.getAbsolutePath(), path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (int i = 0; i < strings.size(); i++) {
                File file = new File(dir, String.valueOf(i));
                if (!file.exists()) {
                    file.createNewFile();
                }
                fw = new FileWriter(file);
                fw.write(strings.get(i));
                fw.flush();
                fw.close();
            }
        } catch (Exception ignore) {
            Log.i("zune", "写入错误" + ignore);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<String> getCache(String path) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        try {
            File rootFile = BaseApplication.getInstance().getCacheDir();
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }
            File dir = new File(rootFile.getAbsolutePath(), path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] files = getFiles(dir);
            for (int i = 0; i < files.length; i++) {
                fr = new FileReader(files[i]);
                int length;
                char[] a = new char[16];
                sb.setLength(0);
                while ((length = fr.read(a)) != -1) {
                    sb.append(new String(a, 0, length));
                }
                result.add(sb.toString());
            }
        } catch (Exception ignore) {
            Log.i("zune", "ignore e = " + ignore);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String getCache(String path, int page) {
        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        try {
            File rootFile = BaseApplication.getInstance().getCacheDir();
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }
            File dir = new File(rootFile.getAbsolutePath(), path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, String.valueOf(page));
            if (!file.exists()) {
                file.createNewFile();
            }
            fr = new FileReader(file);
            int length;
            char[] a = new char[16];
            sb.setLength(0);
            while ((length = fr.read(a)) != -1) {
                sb.append(new String(a, 0, length));
            }
        } catch (Exception ignore) {
            Log.i("zune", "ignore e = " + ignore);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private static File[] getFiles(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < i; j++) {
                if (getFilePosition(files[i]) < getFilePosition(files[j])) {
                    File temp = files[i];
                    files[i] = files[j];
                    files[j] = temp;
                }
            }
        }
        return files;
    }

    private static int getFilePosition(File file) {
        String[] split = file.getPath().split(File.separator);
        String filePosition = split[split.length - 1];
        return Integer.parseInt(filePosition);
    }
}
