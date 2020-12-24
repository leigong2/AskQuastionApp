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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.android.askquastionapp.utils.SimpleObserver;

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

    private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            /*zune：这个坐标是相对屏幕的坐标，缩放的时候，注意要转换为相对画布的坐标**/
            float cx = detector.getFocusX();
            float cy = detector.getFocusY();
            float oldScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = scaleFactor < 1f ? 1f : scaleFactor > 20 ? 20 : scaleFactor;
            if (scaleFactor >= 20) {
                return true;
            }
            updateScaleViewRect(cx, cy, oldScale, scaleFactor);
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

    /*zune：* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                                                                      l------------a-----o-l------------------
     *
     * L                                                                  --------------------------A-------------O----x------------------------------------
     *
     * L------------------------------------------------------------------j-------------O------------------X---------------------------------------------------------------
     *
     * left = -(x * scale  - x)
     *
     * left = -(x - mView.left) / oldScale * scale - x
     *
     *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  ***/
    private void updateScaleViewRect(float cx, float cy, float oldScale, float scaleFactor) {
        /*zune：-(x - mView.left) / oldScale * scale - x  **/
        float left = -((cx - mViewRect.left) / oldScale * scaleFactor - cx);
        float top = -((cy - mViewRect.top) / oldScale * scaleFactor - cy);
        float right = left + measuredWidth * scaleFactor;
        float bottom = top + measuredHeight * scaleFactor;
        updateViewRect(left, top, right, bottom);
    }

    private GestureDetector moveGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {

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

    private void updateViewRect(float left, float top, float right, float bottom) {
        mViewRect.set(left, top, right, bottom);
    }

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
        Observable.just(inputStream).map(new Function<InputStream, Integer>() {
            @Override
            public Integer apply(InputStream inputStream) throws Exception {
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, new Rect(), tmpOptions);
                mImageWidth = tmpOptions.outWidth;
                mImageHeight = tmpOptions.outHeight;
                mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                splitImageRect();
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
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
        updateViewRect(0, 0, measuredWidth, measuredHeight);
        splitImageRect();
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
                        splitCanvasRect();
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
                rect.left = i * width + mViewRect.left;
                rect.right = rect.left + width;
                rect.top = j * height + getMarginTop() + mViewRect.top;
                rect.bottom = rect.top + height;
                if (girdBitmaps.size() <= mCanvasRectList.size()) {
                    continue;
                }
                Bitmap bitmap = girdBitmaps.get(mCanvasRectList.size());
                if (bitmap == null) {
                    continue;
                }
                int bitmapWidth = bitmap.getWidth();
                int normalBitmapWidth = girdBitmaps.get(0).getWidth();
                if (bitmapWidth < normalBitmapWidth) {
                    rect.right = rect.left + width * bitmapWidth / normalBitmapWidth;
                }
                int bitmapHeight = bitmap.getHeight();
                int normalBitmapHeight = girdBitmaps.get(0).getHeight();
                if (bitmapHeight < normalBitmapHeight) {
                    rect.bottom = rect.top + height * bitmapHeight / normalBitmapHeight;
                }
                mCanvasRectList.add(rect);
            }
        }
    }

    private float getMarginTop() {
        /*zune：整体视图view，在不缩放的情况下相对屏幕上方的距离(计算网格从哪里开始绘制)**/
        return (measuredHeight * scaleFactor - 1f * measuredWidth * scaleFactor * mImageHeight / mImageWidth) / 2;
    }

    private float getRadio() {
        /*zune：图片缩小比例，和缩放比例不同，这个是指view可视矩形宽度与图片宽度的比率**/
        return mViewRect.width() / mImageWidth;
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

    /*zune：画布的矩形区域**/
    private List<RectF> mCanvasRectList = new ArrayList<>();

    /*zune：视图的矩形(也就是说，初始是整个可视区域，随着缩放进行，整个图片扩展到可视区域之外的矩形，这个矩形是相对屏幕的坐标)**/
    private RectF mViewRect = new RectF();

    /*zune：网格碎片的bitmap列表，其实可以把不可见的也加上，这样计算起来更加方便**/
    private List<Bitmap> girdBitmaps = new ArrayList<>();
}
