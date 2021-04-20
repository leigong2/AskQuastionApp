package com.example.android.askquastionapp.reader;

import android.content.Context;

import com.example.android.askquastionapp.views.CommonDialog;

import java.io.File;

public class TxtUtils {
    public static void renameTxt(Context context, String src, String desc, File dir) throws Throwable {
        if (dir.isFile()) {
            if (dir.getName().endsWith(src)) {
                boolean rename = dir.renameTo(new File(dir.getPath().replaceAll(src, desc)));
                if (!rename) {
                    new CommonDialog(context).setContent("重命名失败：" + "\n file : " + dir.getPath()).show();
                }
            }
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            new CommonDialog(context).setContent("重命名失败：" + "\n file : null").show();
            return;
        }
        for (File file : files) {
            renameTxt(context, src, desc, file);
        }
    }
}
