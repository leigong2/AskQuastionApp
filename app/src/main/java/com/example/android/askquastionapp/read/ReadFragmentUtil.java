package com.example.android.askquastionapp.read;


import android.content.Context;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.SaveUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadFragmentUtil {

    public List<String> mFilesString;

    public void onNext(int resId, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_left_out,
                        R.anim.slide_left_in,
                        R.anim.slide_right_out
                ).replace(resId, fragment)
                .commit();
    }

    public void onPre(int resId, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_left_in,
                        R.anim.slide_right_out,
                        R.anim.slide_right_in,
                        R.anim.slide_left_out
                ).replace(resId, fragment)
                .commit();
    }

    public void save(SaveUtils.SaveBean.Save curData) {
        SaveUtils.SaveBean saveBean = SaveUtils.get();
        if (saveBean == null) {
            saveBean = new SaveUtils.SaveBean();
        }
        if (saveBean.saves == null) {
            saveBean.saves = new ArrayList<>();
        }
        saveBean.inited = true;
        boolean contained = false;
        for (SaveUtils.SaveBean.Save temp : saveBean.saves) {
            if (curData.path.equals(temp.path)) {
                temp.position = curData.position;
                contained = true;
                break;
            }
        }
        if (!contained) {
            SaveUtils.SaveBean.Save save = new SaveUtils.SaveBean.Save();
            save.path = curData.path;
            save.position = curData.position;
            saveBean.saves.add(save);
        }
        SaveUtils.save(saveBean);
    }

    private float progress;
    public int init(EditText editText, String path) {
        mFilesString = new ArrayList<>();
        FileReader fr = null;
        long total = 0;
        try {
            File file = new File(path);
            total = file.length();
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long curLength = 0;
        Log.i("zune", "读取开始");
        while (true) {
            StringBuilder s = new StringBuilder();
            try {
                int length;
                char[] a = new char[16];
                //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
                //当文件读取到结尾时返回 -1,循环结束。
                while ((length = fr.read(a)) != -1) {
                    s.append(new String(a, 0, length));
                    curLength += 16L;
                    if (total > 0) {
                        progress = curLength * 100f / total;
                    }
                    if (checkNext(editText, s)) {
                        break;
                    }
                    if (onLoadingListener != null) {
                        onLoadingListener.onLoading(progress);
                    }
                }
                if (s.length() > 0) {
                    mFilesString.add(s.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(s)) {
                break;
            }
        }
        Log.i("zune", "读取结束");
        SaveUtils.putCache(path, mFilesString);
        return mFilesString.size();
    }

    public boolean checkNext(EditText editText, StringBuilder sb) {
        int screenWidth = ScreenUtils.getScreenWidth();
        String temp = sb.toString().replaceAll("\n", "");
        int offCount = sb.toString().length() - temp.length();
        float measureWidth = editText.getPaint().measureText(sb.toString());
        int currentLines = (int) (measureWidth / (screenWidth - getRightPadding(editText)));
        Paint.FontMetrics fm = editText.getPaint().getFontMetrics();
        int lineHeight = (int) Math.ceil(fm.descent - fm.top) + 2;
        int statusBarHeight = getStatusBarHeight(editText.getContext());
        float totalHeight = ScreenUtils.getScreenHeight() - statusBarHeight;
        int maxLines = (int) (totalHeight / lineHeight) - 3;
        return currentLines >= maxLines - offCount;
    }

    private float getRightPadding(EditText editText) {
        int screenWidth = ScreenUtils.getScreenWidth();
        String temp = "我";
        float singleWidth = editText.getPaint().measureText(temp);
        int count = (int) (screenWidth / singleWidth);
        return screenWidth - singleWidth * count;
    }
    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public interface OnLoadingListener {
        void onLoading(float percent);
    }

    OnLoadingListener onLoadingListener;

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }
}
