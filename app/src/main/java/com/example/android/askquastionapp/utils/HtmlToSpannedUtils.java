package com.example.android.askquastionapp.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.android.askquastionapp.views.VerticalCenterSpan;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.io.File;
import java.io.IOException;

public class HtmlToSpannedUtils {

    public static void loadHtmlContent(TextView textView, String content) {
        final int[] srcCount = {0};
        Spanned spanned = Html.fromHtml(content, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                ++srcCount[0];
                Glide.with(textView).asFile().load(source).addListener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        srcCount[0]--;
                        if (srcCount[0] == 0) {
                            setResourceData(textView, content);
                        }
                        return false;
                    }
                }).submit();
                return null;
            }
        }, null);
        textView.setText(spanned);
    }

    private static void setResourceData(TextView textView, String testTitle) {
        Spanned spanned = Html.fromHtml(testTitle, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                try {
                    File localCache = GlideUtils.getInstance().getLocalCache(textView.getContext(), source);
                    Uri uri = Uri.fromFile(localCache);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(textView.getContext().getContentResolver(), uri);
                    BitmapDrawable drawable = new BitmapDrawable(textView.getContext().getResources(), bitmap);
                    drawable.setFilterBitmap(true);
                    // 必须设为图片的边际,不然TextView显示不出图片
                    int minimumWidth = drawable.getIntrinsicWidth();
                    int minimumHeight = drawable.getIntrinsicHeight();
                    float dpi = DensityUtil.dp2px(1f);
                    drawable.setBounds(0, 0, (int) (minimumWidth * dpi), (int) (minimumHeight * dpi));
                    return drawable;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, (opening, tag, output, xmlReader) -> {
        });
        if (spanned instanceof SpannableStringBuilder) {
            ImageSpan[] imageSpans = spanned.getSpans(0, spanned.length(), ImageSpan.class);
            for (ImageSpan imageSpan : imageSpans) {
                int start = spanned.getSpanStart(imageSpan);
                int end = spanned.getSpanEnd(imageSpan);
                Drawable d = imageSpan.getDrawable();
                VerticalCenterSpan newImageSpan = new VerticalCenterSpan(d);
                ((SpannableStringBuilder) spanned).setSpan(newImageSpan, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                ((SpannableStringBuilder) spanned).removeSpan(imageSpan);
            }
        }
        textView.setText(spanned);
    }
}
