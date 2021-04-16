package com.example.android.askquastionapp.read;


import android.content.Context;
import android.graphics.Paint;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.R;

import java.util.ArrayList;
import java.util.List;

public class ReadFragmentUtil {

    private final List<String> mTextsString = new ArrayList<>();
    private int mCurrentIndex;

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
        int maxLines = (int) (totalHeight / lineHeight) - 1;
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

    public String getPreString() {
        if (mTextsString.isEmpty()) {
            return "";
        }
        if (mCurrentIndex > 0) {
            return mTextsString.get(--mCurrentIndex);
        } else {
            return mTextsString.get(0);
        }
    }

    public String getNextString() {
        if (++mCurrentIndex < mTextsString.size()) {
            return mTextsString.get(mCurrentIndex);
        }
        return "";
    }

    public boolean isFirst() {
        return mCurrentIndex == 0;
    }

    public void raise(String text) {
        mTextsString.add(text);
        mCurrentIndex = mTextsString.size() - 1;
    }

    public long getCurrentLength() {
        long currentLength = 0;
        for (int i = 0; i < mCurrentIndex; i++) {
            currentLength += mTextsString.get(i).length();
        }
        return currentLength;
    }

    public String startSearchText(String result) {
        for (int i = 0; i < mTextsString.size(); i++) {
            String s = mTextsString.get(i);
            if (s.contains(result)) {
                mCurrentIndex = i;
                return s;
            }
        }
        return "";
    }
}
