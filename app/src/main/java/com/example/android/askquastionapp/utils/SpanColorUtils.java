package com.example.android.askquastionapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ReplacementSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by 王志龙 on 2018/10/25 025.
 */
public class SpanColorUtils {
    public static void setSpannerString(TextView textView, String keyWords, int color) {
        String content = textView.getText().toString().trim();
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        /**zune: 匹配所有**/
/*        Set<Integer> indexStart = new HashSet<>();
        if (keyWords != null) {
            indexStart = getCharIndex(content, keyWords);
        }
        if (indexStart.size() > 0) {
            for (Integer integer : indexStart) {
                builder.setSpan(new ForegroundColorSpan(color)
                        , integer, integer + keyWords.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        } else {
            builder.setSpan(new ForegroundColorSpan(color)
                    , 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }*/
        /**zune: 匹配第一个**/
        int indexStart = -1;
        if (keyWords != null) {
            indexStart = getFirstCharIndex(content, keyWords);
        }
        if (indexStart >= 0) {
            builder.setSpan(new ForegroundColorSpan(color)
                    , indexStart, indexStart + keyWords.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            builder.setSpan(new ForegroundColorSpan(color)
                    , 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        textView.setText(builder);
    }

    /**zune: 形如...我是一条鱼儿xxxx, 匹配前面是6个字符 + ...
     * @param extraLength  匹配字段前面有多少个字符
     * **/
    public static void setCutSpannerString(TextView textView, String keyWords, int color, int extraLength) {
        CharSequence content = textView.getText();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int indexStart = -1;
        if (keyWords != null) {
            indexStart = getFirstCharIndex(content.toString(), keyWords);
        }
        if (indexStart >= 0) {
            String extra = "...";
            if (indexStart > extra.length() + extraLength + 1) {
                CharSequence substring = content.subSequence(indexStart - extraLength, content.length());
                builder.append(extra).append(substring);
                indexStart -= (content.length() - substring.length() - extra.length());
            } else {
                builder.append(content);
            }
            builder.setSpan(new ForegroundColorSpan(color)
                    , indexStart, indexStart + keyWords.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            builder.append(content);
            builder.setSpan(new ForegroundColorSpan(color)
                    , 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        textView.setText(builder);
    }

    public static SpannableStringBuilder append(SpannableStringBuilder spannable, ImageSpan imageSpan) {
        String tag = imageSpan.toString();
        spannable.append(tag);
        spannable.setSpan(imageSpan, spannable.length() - tag.length(), spannable.length()
                , Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static SpannableStringBuilder append(SpannableStringBuilder spannable, SpannableStringBuilder builder) {
        String tag = builder.toString();
        spannable.append(tag);
        spannable.setSpan(builder, spannable.length() - tag.length(), spannable.length()
                , Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private static Set<Integer> getCharIndex(String content, String keyWords) {
        Set<Integer> charsIndex = new HashSet<>();
        if (!content.contains(keyWords) && !content.contains(keyWords.toLowerCase()) && !content.contains(keyWords.toUpperCase())) {
            return charsIndex;
        }
        String toUpperCase = keyWords.toUpperCase();
        String toLowerCase = keyWords.toLowerCase();
        int indexUp = content.indexOf(toUpperCase);
        while (indexUp >= 0) {
            charsIndex.add(indexUp);
            indexUp = content.indexOf(toUpperCase, indexUp + 1);
        }
        int indexLow = content.indexOf(toLowerCase);
        while (indexLow >= 0) {
            charsIndex.add(indexLow);
            indexLow = content.indexOf(toUpperCase, indexLow + 1);
        }
        return charsIndex;
    }

    public static Integer getFirstCharIndex(String content, String keyWords) {
        int charsIndex = -1;
        if (!content.toUpperCase().contains(keyWords.toUpperCase())) {
            return charsIndex;
        }
        String toUpperCase = keyWords.toUpperCase();
        return content.toUpperCase().indexOf(toUpperCase);
    }

    /**
     * zune: 忽略大小写的时候用到
     **/
    private static boolean containsChar(String content, String keyWords) {
        if (keyWords.length() == 1 && keyWords.matches("[A-Za-z]")) {
            String toUpperCase = keyWords.toUpperCase();
            return content.toUpperCase().contains(toUpperCase);
        }
        return false;
    }

    public static void setSpannerSize(TextView textView, String keyWords, int textSize) {
        String content = textView.getText().toString().trim();
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        int indexStart = 0;
        int indexEnd = 0;
        if (content.contains(keyWords)) {
            indexStart = content.indexOf(keyWords);
            indexEnd = indexStart + keyWords.length();
        }
        if (indexEnd > 0) {
            builder.setSpan(new AbsoluteSizeSpan(textSize)
                    , indexStart, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            builder.setSpan(new AbsoluteSizeSpan(textSize)
                    , 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        textView.setText(builder);
    }

    public static void appendSpannerRes(TextView textView, int resId) {
        textView.append("  ");
        CharSequence content = textView.getText();
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Drawable drawable = textView.getContext().getResources().getDrawable(resId);
        int minimumWidth = drawable.getMinimumWidth();
        int minimumHeight = drawable.getMinimumHeight();
        drawable.setBounds(0, 0, minimumWidth, minimumHeight);
        builder.setSpan(new VerticalCenterSpan(drawable), content.length() - 1, content.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
        textView.append(" ");
    }

    public static void appendSpannerDrawable(TextView textView, Drawable drawable) {
        textView.append("  ");
        CharSequence content = textView.getText();
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        int minimumWidth = drawable.getMinimumWidth();
        int minimumHeight = drawable.getMinimumHeight();
        textView.measure(0, 0);
        float measuredHeight = textView.getMeasuredHeight() - textView.getCompoundPaddingTop() - textView.getCompoundPaddingBottom();
        drawable.setBounds(0, 0, (int) (minimumWidth * measuredHeight / minimumHeight), (int) measuredHeight);
        builder.setSpan(new VerticalCenterSpan(drawable), content.length() - 1, content.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
        textView.append(" ");
    }

    public static SpannableStringBuilder appendSpanner(TextView textView, Drawable drawable, SpannableStringBuilder builder) {
        builder.append("  ");
        int minimumWidth = drawable.getMinimumWidth();
        int minimumHeight = drawable.getMinimumHeight();
        textView.measure(0, 0);
        float measuredHeight = textView.getMeasuredHeight() - textView.getCompoundPaddingTop() - textView.getCompoundPaddingBottom();
        drawable.setBounds(0, 0, (int) (minimumWidth * measuredHeight / minimumHeight), (int) measuredHeight);
        builder.setSpan(new VerticalCenterSpan(drawable), builder.length() - 1, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(" ");
        return builder;
    }

    public static SpannableStringBuilder appendSpanner(Context context, float textSize, Drawable drawable, SpannableStringBuilder builder) {
        builder.append("  ");
        int minimumWidth = drawable.getMinimumWidth();
        int minimumHeight = drawable.getMinimumHeight();
        TextView textView = new TextView(context);
        textView.setTextSize(textSize);
        textView.measure(0, 0);
        float measuredHeight = textView.getMeasuredHeight() - textView.getCompoundPaddingTop() - textView.getCompoundPaddingBottom();
        drawable.setBounds(0, 0, (int) (minimumWidth * measuredHeight / minimumHeight), (int) measuredHeight);
        builder.setSpan(new VerticalCenterSpan(drawable), builder.length() - 1, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(" ");
        return builder;
    }

    /**
     * 使TextView中不同大小字体垂直居中
     */
    public static class VerticalCenterSpan extends ImageSpan {
        public VerticalCenterSpan(Drawable drawable) {
            super(drawable);
        }

        public int getSize(Paint paint, CharSequence text, int start, int end,
                           Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                //获得文字、图片高度
                int fontHeight = fmPaint.bottom - fmPaint.top;
                int drHeight = rect.bottom - rect.top;
                //对于这段算法LZ表示也不解，正常逻辑应该同draw中的计算一样但是显示的结果不居中，经过几次调试之后才发现这么算才会居中
                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fm.ascent = -bottom;
                fm.top = -bottom;
                fm.bottom = top;
                fm.descent = top;
            }
            return rect.right;
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            canvas.save();
            int transY = 0;
            //获得将要显示的文本高度-图片高度除2等居中位置+top(换行情况)
            transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
            //偏移画布后开始绘制
            canvas.translate(x, transY);
            //Canvas: trying to use a recycled bitmap android.graphics.Bitmap  throw java.lang.RuntimeException
            try {
                b.draw(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.restore();
        }
    }

    public static class VerticalTextSpan extends ReplacementSpan {

        private float textSize;
        private Typeface typeFace;

        public VerticalTextSpan(float textSize) {
            this.textSize = textSize;
        }

        public VerticalTextSpan(Typeface typeFace, float textSize) {
            this.textSize = textSize;
            this.typeFace = typeFace;
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fontMetricsInt) {
            text = text.subSequence(start, end);
            Paint p = getCustomTextPaint(paint);
            return (int) p.measureText(text.toString());
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
            text = text.subSequence(start, end);
            Paint p = getCustomTextPaint(paint);
            Paint.FontMetricsInt fm = p.getFontMetricsInt();
            //此处重新计算y坐标，使字体居中
            canvas.drawText(text.toString(), x, y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2), p);
        }

        private TextPaint getCustomTextPaint(Paint srcPaint) {
            TextPaint paint = new TextPaint(srcPaint);
            if (typeFace != null) {
                paint.setTypeface(typeFace);
            }
            paint.setTextSize(textSize);   //设定字体大小, sp转换为px
            return paint;
        }
    }
}
