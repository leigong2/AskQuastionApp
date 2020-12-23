package com.example.android.askquastionapp.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.jsoup.GsonGetter;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PhotoImageView extends View {

    private int mImageWidth;
    private int mImageHeight;
    private int measuredWidth;
    private int measuredHeight;
    private BitmapRegionDecoder mDecoder;
    private static final BitmapFactory.Options options = new BitmapFactory.Options();
    private float scaleFactor = 1f;
    private Paint colorPaint;

    public PhotoImageView(Context context) {
        super(context);
        init();
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(Color.RED);
        colorPaint.setStrokeWidth(3f);
        colorPaint.setStyle(Paint.Style.STROKE);
    }

    private float mCurX;
    private float mCurY;

    private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            /*zune：这个坐标是相对屏幕的坐标，缩放的时候，注意要转换为相对画布的坐标**/
            float cx = detector.getFocusX();
            float cy = detector.getFocusY();
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = scaleFactor < 1f ? 1f : scaleFactor > 20 ? 20 : scaleFactor;
            if (scaleFactor >= 20) {
                return true;
            }
            mCurX = cx;
            mCurY = cy;
            LogUtils.i("zune: ", "scale = " + scaleFactor + ", cx = " + cx + ", cy = " + cy);
            updateViewRect(cx, cy, scaleFactor);
            splitCanvasRect();
            postInvalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    });

    private GestureDetector moveGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        public boolean onMove(GestureDetector detector) {
//            int moveX = (int) detector.getMoveX();
//            int moveY = (int) detector.getMoveY();
//            if (mImageWidth * scaleFactor > getMeasuredWidth()) {
//                mRect.left -= moveX / scaleFactor;
//                if (mRect.left < 0) {
//                    mRect.left = 0;
//                }
//                mRect.right = (int) (mRect.left + getMeasuredWidth());
//                if (mRect.right > mImageWidth * scaleFactor) {
//                    mRect.right = (int) (mImageWidth * scaleFactor);
//                    mRect.left = (int) (mImageWidth * scaleFactor - getMeasuredWidth());
//                }
//            }
//            if (mImageHeight * scaleFactor > getMeasuredHeight()) {
//                mRect.top -= moveY / scaleFactor;
//                if (mRect.top < 0) {
//                    mRect.top = 0;
//                }
//                mRect.bottom = (int) (mRect.top + getMeasuredHeight());
//                if (mRect.bottom > mImageHeight * scaleFactor) {
//                    mRect.bottom = (int) (mImageHeight * scaleFactor);
//                    mRect.top = (int) (mImageHeight * scaleFactor - getMeasuredHeight());
//                }
//            }
//            postInvalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent currentEvent, MotionEvent motionEvent, float scrollX, float scrollY) {
            LogUtils.i("zune: ", "scrollX = " + scrollX + ", scrollY = " + scrollY);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent currentEvent, MotionEvent motionEvent, float velocityX, float velocityY) {
            return false;
        }
    });

    public void setImageResource(int resource) {
        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
        tmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resource);
        mImageWidth = tmpOptions.outWidth;
        mImageHeight = tmpOptions.outHeight;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        try {
            InputStream inputStream = getResources().openRawResource(resource);
            mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setImageResource(InputStream inputStream) {
        Observable.just(inputStream).map(new Function<InputStream, BitmapRegionDecoder>() {
            @Override
            public BitmapRegionDecoder apply(InputStream inputStream) throws Exception {
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, new Rect(), tmpOptions);
                mImageWidth = tmpOptions.outWidth;
                mImageHeight = tmpOptions.outHeight;
                return BitmapRegionDecoder.newInstance(inputStream, false);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<BitmapRegionDecoder, Integer>(1, false) {
            @Override
            public void onNext(BitmapRegionDecoder bitmapRegionDecoder, Integer integer) {
                mDecoder = bitmapRegionDecoder;
                splitImageRect();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        moveGestureDetector.onTouchEvent(event);   //平移
        scaleGestureDetector.onTouchEvent(event);    //双指缩放
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*zune：测量view宽高，目的，拿到view的最大显示位置：初始化图片网格，画布网格**/
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
        mViewRect.set(0, 0, measuredWidth, measuredHeight);
        splitImageRect();
        splitCanvasRect();
    }

    private void updateViewRect(float cx, float cy, float scaleFactor) {
        /*zune Todo 缩放后，重新设置可见的区域 **/
        float absX = (cx - mViewRect.left) / scaleFactor;
        float absY = (cy - mViewRect.top) / scaleFactor;
        float leftScale = absX * (1 - scaleFactor);
        float topScale = absY * (1 - scaleFactor);
        mViewRect.set((int) leftScale, (int) topScale, (int) (leftScale + (measuredWidth * scaleFactor)), (int) (topScale + scaleFactor * measuredWidth * mImageHeight / mImageWidth));
        LogUtils.i("zune: ", "cx = " + cx + ", cy = " + cy + ", scale = " + scaleFactor + ", viewRect = " + GsonGetter.getInstance().getGson().toJson(mViewRect));
    }

    private void splitImageRect() {
        if (mDecoder == null || measuredWidth == 0) {
            return;
        }
        Observable.just(1).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                /*zune：绘制图片网格的列表，将图片分割为多个碎片**/
                options.inSampleSize = (int) (1 / scaleFactor * mImageWidth / measuredWidth);
                mImageRectList.clear();
                girdBitmaps.clear();
                float imageHeight = mImageHeight;
                float imageWidth = mImageWidth;
                if (imageHeight == 0 || imageWidth == 0) {
                    return 1;
                }
                float width = measuredWidth;
                float height = measuredHeight;
                int hCount = (int) (imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1));
                int vCount = (int) (imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1));
                for (int i = 0; i < hCount; i++) {
                    for (int j = 0; j < vCount; j++) {
                        Rect rect = new Rect();
                        rect.left = (int) (1f * i * width);
                        rect.right = (int) Math.min(rect.left + width, imageWidth);
                        rect.top = (int) (1f * j * height);
                        rect.bottom = (int) Math.min(rect.top + height, imageHeight);
                        mImageRectList.add(rect);
                        Bitmap bitmap = mDecoder.decodeRegion(rect, options);
                        girdBitmaps.add(bitmap);
                    }
                }
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer, Integer>(1, false) {
                    @Override
                    public void onNext(Integer integer, Integer integer2) {
                        invalidate();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    private void splitCanvasRect() {
        /*zune：绘制画布的网格，将画布分成与网格对应数量的碎片，画布是可以缩放的，因此，网格的大小也是变化的**/
        mCanvasRectList.clear();
        float imageHeight = this.mImageHeight;
        float imageWidth = this.mImageWidth;
        if (imageHeight == 0 || imageWidth == 0) {
            return;
        }
        float width = measuredWidth * getRadio();
        float height = measuredHeight * getRadio();
        int hCount = (int) (imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1));
        int vCount = (int) (imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1));
        for (int i = 0; i < hCount; i++) {
            for (int j = 0; j < vCount; j++) {
                RectF rect = new RectF();
                rect.left = 1f * i * width + mViewRect.left;
                rect.right = Math.min(rect.left + width, imageWidth * getRadio());
                rect.top = 1f * j * height + mViewRect.top + getMarginTop();
                rect.bottom = Math.min(rect.top + height, imageHeight * getRadio() + getMarginTop());
                mCanvasRectList.add(rect);
            }
        }
    }

    private float getMarginTop() {
        /*zune：整体视图view，在不缩放的情况下相对屏幕上方的距离(计算网格从哪里开始绘制)**/
        return (measuredHeight - 1f * measuredWidth * mImageHeight / mImageWidth) / 2;
    }

    private float getRadio() {
        /*zune：图片缩小比例，和缩放比例不同，这个是指view可视矩形宽度与图片宽度的比率**/
        return 1f * mViewRect.width() / mImageWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDecoder == null) {
            return;
        }
        drawImageBitmap(canvas);
    }

    /*zune：核心的绘制业务逻辑，绘制之前先计算采样率，然后过滤掉不可视的矩形画布，最后将bitmap存入容器中，并根据矩形绘制出对应的图片**/
    private void drawImageBitmap(Canvas canvas) {
        colorPaint.setStyle(Paint.Style.STROKE);
        colorPaint.setColor(Color.RED);
        for (int i = 0; i < girdBitmaps.size(); i++) {
            RectF rectF = mCanvasRectList.get(i);
            if (!checkIsVisible(rectF)) {
                continue;
            }
            Rect rect = fromRectF(rectF);
            canvas.drawBitmap(girdBitmaps.get(i), null, rect, null);
        }
    }

    /*zune：检查画布是否超过了可视区域**/
    private boolean checkIsVisible(RectF rectF) {
        if (rectF.right < 0 || rectF.bottom < 0 || rectF.left > measuredWidth || rectF.top > measuredHeight) {
            return false;
        }
        return true;
    }

    /*zune：将画布矩形区域转换为可绘制的矩形**/
    private Rect fromRectF(@NotNull RectF rectF) {
        int left = (int) rectF.left;
        int top = (int) rectF.top;
        int right = (int) rectF.right;
        int bottom = (int) rectF.bottom;
        return new Rect(left, top, right, bottom);
    }

    private List<Rect> mImageRectList = new ArrayList<>();

    private List<RectF> mCanvasRectList = new ArrayList<>();

    private Rect mViewRect = new Rect();

    private List<Bitmap> girdBitmaps = new ArrayList<>();
}
