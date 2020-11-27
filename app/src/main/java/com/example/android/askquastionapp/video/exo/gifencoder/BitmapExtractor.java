package com.example.android.askquastionapp.video.exo.gifencoder;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.example.android.askquastionapp.BaseApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.N;


public class BitmapExtractor {

    private static final int US_OF_S = 1000 * 1000;

    private List<Bitmap> bitmaps = new ArrayList<>();
    private int width = 0;
    private int height = 0;
    private int begin = 0;
    private int end = 0;
    private int fps = 5;

    public List<Bitmap> createBitmaps(Uri mUri, String videoPath) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Uri uri = null;
            File file = new File(videoPath);
            if (mUri == null) {
                try {
                    if (Build.VERSION.SDK_INT < N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(BaseApplication.getInstance(), "com.guochao.faceshow.fileprovider", file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (uri == null) {
                    uri = Uri.parse(videoPath);
                }
            } else {
                uri = mUri;
            }
            mmr.setDataSource(BaseApplication.getInstance(), uri);
            double inc = US_OF_S / fps;
            for (double i = begin * US_OF_S; i < end * US_OF_S; i += inc) {
                Bitmap frame = mmr.getFrameAtTime((long) i, MediaMetadataRetriever.OPTION_CLOSEST);
                if (frame != null) {
                    bitmaps.add(scale(frame));
                }
            }
            mmr.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmaps;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setScope(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public void setFPS(int fps) {
        this.fps = fps;
    }

    private Bitmap scale(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap,
                width > 0 ? width : bitmap.getWidth(),
                height > 0 ? height : bitmap.getHeight(),
                true);
    }
}
