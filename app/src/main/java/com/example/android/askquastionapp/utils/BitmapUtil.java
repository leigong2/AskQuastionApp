package com.example.android.askquastionapp.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BitmapUtil {
    /**
     * 将bitmap中的某种颜色值替换成新的颜色
     *
     * @param oldBitmap
     * @param oldColor
     * @param newColor
     * @return
     */
    public static Bitmap replaceBitmapColor(Bitmap oldBitmap, String oldColor, String newColor) {
        if (oldBitmap == null) {
            return null;
        }
        //相关说明可参考 http://xys289187120.blog.51cto.com/3361352/657590/
        Bitmap bitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //循环获得bitmap所有像素点
        int mBitmapWidth = bitmap.getWidth();
        int mBitmapHeight = bitmap.getHeight();
        int mArrayColorLengh = mBitmapWidth * mBitmapHeight;
        int[] mArrayColor = new int[mArrayColorLengh];
        int count = 0;
        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWidth; j++) {
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                int color = bitmap.getPixel(j, i);
                String formatColor = String.format("#%X", color);
                if (formatColor.equals(oldColor)) {
                    bitmap.setPixel(j, i, Color.parseColor(newColor));
                }
            }
        }
        return bitmap;
    }
}
