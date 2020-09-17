package com.example.android.askquastionapp.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.example.android.askquastionapp.BaseApplication;

import maximsblog.blogspot.com.jlatexmath.core.AjLatexMath;
import maximsblog.blogspot.com.jlatexmath.core.Insets;
import maximsblog.blogspot.com.jlatexmath.core.TeXConstants;
import maximsblog.blogspot.com.jlatexmath.core.TeXFormula;
import maximsblog.blogspot.com.jlatexmath.core.TeXIcon;

public class LatexToSpannedUtils {
    public static Bitmap mathToBitmap(float textSize, String math) {
        int w = BaseApplication.getInstance().getResources().getDisplayMetrics().widthPixels;
        TeXFormula formula = new TeXFormula(math);
        TeXIcon icon = formula.new TeXIconBuilder()
                .setStyle(TeXConstants.STYLE_DISPLAY)
                .setSize(textSize)
                .setWidth(TeXConstants.UNIT_PIXEL, w, TeXConstants.ALIGN_LEFT)
                .setIsMaxWidth(true)
                .setInterLineSpacing(TeXConstants.UNIT_PIXEL,
                        AjLatexMath.getLeading(textSize)).build();
        icon.setInsets(new Insets(5, 5, 5, 5));
        Bitmap image = Bitmap.createBitmap(icon.getIconWidth(), icon.getIconHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas g2 = new Canvas(image);
        g2.drawColor(Color.WHITE);
        icon.paintIcon(g2, 0, 0);
        return scaleBitmapAndKeepRation(image, icon.getIconHeight(), icon.getIconWidth());
    }

    private static Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp,
                                           int reqHeightInPixels, int reqWidthInPixels) {
        Bitmap bitmap = Bitmap.createBitmap(reqWidthInPixels,
                reqHeightInPixels, Bitmap.Config.ARGB_8888);
        Canvas g = new Canvas(bitmap);
        g.drawBitmap(targetBmp, 0, 0, null);
        targetBmp.recycle();
        return bitmap;
    }

}
