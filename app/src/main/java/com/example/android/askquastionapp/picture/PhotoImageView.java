package com.example.android.askquastionapp.picture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.utils.SimpleObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private FillingValueAnimator filingAnimator;
    private int orientation = LinearLayout.HORIZONTAL;

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
        colorPaint.setStrokeWidth(1f);
        colorPaint.setStyle(Paint.Style.STROKE);
    }

    private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            /*zune：这个坐标是相对屏幕的坐标，缩放的时候，注意要转换为相对画布的坐标**/
            float cx = detector.getFocusX();
            float cy = detector.getFocusY();
            float scale = detector.getScaleFactor();
            PhotoImageView.this.onScale(cx, cy, scale);
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

    private void onScale(float cx, float cy, float scale) {
        float oldScale = scaleFactor;
        scaleFactor *= scale;
        scaleFactor = scaleFactor < 1f ? 1f : scaleFactor > 40 ? 40 : scaleFactor;
        if (scaleFactor > 40) {
            return;
        }
        updateScaleViewRect(cx, cy, oldScale, scaleFactor);
        splitCanvasRect();
        postInvalidate();
    }

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

    /*zune：设置画布的边界**/
    private void updateViewRect(float left, float top, float right, float bottom) {
        if (mImageWidth < measuredWidth && mImageHeight < measuredHeight) {
            dispatchSingleViewRect(left, top, right, bottom);
            return;
        }
        float minX = (1 - scaleFactor) * measuredWidth + getMarginLeft();
        float maxX = scaleFactor * measuredWidth - getMarginLeft();
        float minY = (1 - scaleFactor) * measuredHeight + getMarginTop();
        float maxY = scaleFactor * measuredHeight - getMarginTop();
        float screenScale;
        /*zune：对于宽图，各边界的处理**/
        if (orientation == LinearLayout.HORIZONTAL) {
            if (left < minX) {
                left = minX;
                right = measuredWidth;
            }
            if (right > maxX) {
                right = maxX;
                left = 0;
            }
            screenScale = measuredHeight / (1f * measuredWidth * mImageHeight / mImageWidth);
            if (scaleFactor < screenScale) {
                /*zune：当高度还没有手机屏幕高，就固定死上下距离**/
                top = (-measuredHeight * scaleFactor + measuredHeight) / 2;
                bottom = measuredHeight * scaleFactor - top;
            } else {
                if (top < minY) {
                    top = minY;
                    bottom = measuredHeight + getMarginTop();
                }
                if (bottom > maxY) {
                    bottom = maxY;
                    top = -getMarginTop();
                }
            }
        } else {
            /*zune：对于长图，各边界的处理**/
            if (top < minY) {
                top = minY;
                bottom = measuredHeight;
            }
            if (bottom > maxY) {
                bottom = maxY;
                top = 0;
            }
            screenScale = measuredWidth / (1f * measuredHeight * mImageWidth / mImageHeight);
            if (scaleFactor < screenScale) {
                /*zune：当宽度还没有手机屏幕宽，就固定死左右距离**/
                left = (-measuredWidth * scaleFactor + measuredWidth) / 2;
                right = measuredWidth * scaleFactor - left;
            } else {
                if (left < minX) {
                    left = minX;
                    right = measuredWidth + getMarginLeft();
                }
                if (right > maxX) {
                    right = maxX;
                    left = -getMarginLeft();
                }
            }
        }
        mViewRect.set(left, top, right, bottom);
    }

    private void dispatchSingleViewRect(float left, float top, float right, float bottom) {
        if (orientation == LinearLayout.HORIZONTAL) {
            float baseScale = measuredHeight / (1f * measuredWidth * mImageHeight / mImageWidth);
            float offsetTop = measuredHeight * (1 - scaleFactor) / 2f;
            if (scaleFactor < baseScale) {
                top = offsetTop;
                bottom = top + scaleFactor * mImageHeight;
            }
            if (left < (1 - scaleFactor) * measuredWidth) {
                left = (1 - scaleFactor) * measuredWidth;
                right = measuredWidth;
            }
            if (right > scaleFactor * measuredWidth) {
                right = scaleFactor * measuredWidth;
                left = 0;
            }
        } else {
            float baseScale = measuredWidth / (1f * measuredHeight * mImageWidth / mImageHeight);
            if (scaleFactor < baseScale) {
                left = measuredWidth * (1 - scaleFactor) / 2f;
                right = left + scaleFactor * mImageWidth;
            }
            if (top < (1 - scaleFactor) * measuredHeight) {
                top = (1 - scaleFactor) * measuredHeight;
                bottom = measuredHeight;
            }
            if (bottom > scaleFactor * measuredHeight) {
                bottom = scaleFactor * measuredHeight;
                top = 0;
            }
        }
        mViewRect.set(left, top, right, bottom);
    }

    private GestureDetector moveGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent currentEvent, MotionEvent motionEvent, float scrollX, float scrollY) {
            float left = mViewRect.left - scrollX;
            float top = mViewRect.top - scrollY;
            float right = mViewRect.right - scrollX;
            float bottom = mViewRect.bottom - scrollY;
            updateViewRect(left, top, right, bottom);
            splitCanvasRect();
            postInvalidate();
            return false;
        }

        private boolean isScaling;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isScaling) {
                return super.onDoubleTap(e);
            }
            isScaling = true;
            float x = e.getX();
            float y = e.getY();
            /*zune：超过8倍,再缩小**/
            ValueAnimator scaleAnimator = ValueAnimator.ofFloat(scaleFactor, scaleFactor >= 8 ? 1 / scaleFactor : scaleFactor * (float) Math.sqrt(8));
            scaleAnimator.setDuration(400);
            scaleAnimator.setInterpolator(new LinearInterpolator());
            scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    onScale(x, y, value / scaleFactor);
                }
            });
            scaleAnimator.start();
            BaseApplication.getInstance().getHandler().postDelayed(() -> isScaling = false, scaleAnimator.getDuration());
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent currentEvent, MotionEvent motionEvent, float velocityX, float velocityY) {
            //velocityX, 每秒向x轴移动的像素；velocityY, 每秒向y轴移动的速度
            final long duration = getSplineFlingDuration((float) Math.hypot(velocityX, velocityY));
            if (filingAnimator == null) {
                filingAnimator = new FillingValueAnimator();
                filingAnimator.setFloatValues(0, 1f);
                filingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = (float) valueAnimator.getAnimatedValue();
                        /*zune：时间 * 速度 / 4大概是移动距离**/
                        float curDisX = -filingAnimator.getDuration() * value * filingAnimator.velocityX / 1000 / 4;
                        float curDisY = -filingAnimator.getDuration() * value * filingAnimator.velocityY / 1000 / 4;
                        if (filingAnimator.lastX == 0 && filingAnimator.lastY == 0) {
                            filingAnimator.lastX = curDisX;
                            filingAnimator.lastY = curDisY;
                            return;
                        }
                        float dx = curDisX - filingAnimator.lastX;
                        float dy = curDisY - filingAnimator.lastY;
                        filingAnimator.lastX = curDisX;
                        filingAnimator.lastY = curDisY;
                        onScroll(filingAnimator.currentEvent, filingAnimator.motionEvent, dx, dy);
                    }
                });
            } else if (filingAnimator.isRunning()) {
                return false;
            }
            filingAnimator.lastX = 0;
            filingAnimator.lastY = 0;
            filingAnimator.velocityX = velocityX;
            filingAnimator.velocityY = velocityY;
            filingAnimator.currentEvent = currentEvent;
            filingAnimator.motionEvent = motionEvent;
            filingAnimator.setDuration(duration);
            filingAnimator.setInterpolator(new LinearInterpolator());
            filingAnimator.start();
            return false;
        }

        /*zune：获取惯性滑动的时长的一个公式**/
        private int getSplineFlingDuration(float velocity) {
            final double l = Math.log(0.35f * Math.abs(velocity) / (ViewConfiguration.getScrollFriction() * SensorManager.GRAVITY_EARTH * 39.37f * getResources().getDisplayMetrics().density * 160.0f * 0.84f));
            final double decelMinusOne = (float) (Math.log(0.78) / Math.log(0.9)) - 1.0;
            return (int) (1000.0 * Math.exp(l / decelMinusOne));
        }
    });

    public void setFile(File file) {
        try {
            setImageResource(new FileInputStream(file), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setImageResource(InputStream inputStream) {
        setImageResource(inputStream, null);
    }

    public void setImageResource(InputStream inputStream, File file) {
        Observable.just(inputStream).map(new Function<InputStream, Integer>() {
            @Override
            public Integer apply(InputStream inputStream) throws Exception {
                mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, tmpOptions);
                if (tmpOptions.outHeight == -1 || tmpOptions.outWidth == -1) {
                    if (file == null) {
                        return 1;
                    }
                    ExifInterface exifInterface = new ExifInterface(file.getPath());
                    mImageHeight = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH
                            , ExifInterface.ORIENTATION_NORMAL);
                    mImageWidth = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH
                            , ExifInterface.ORIENTATION_NORMAL);
                } else {
                    mImageWidth = tmpOptions.outWidth;
                    mImageHeight = tmpOptions.outHeight;
                }
                if (1f * mImageWidth / measuredWidth >= 1f * mImageHeight / measuredHeight) {
                    orientation = LinearLayout.HORIZONTAL;
                } else {
                    orientation = LinearLayout.VERTICAL;
                }
                splitImageRect();
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageRequest) {
            return true;
        }
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

    private boolean imageRequest;

    /*zune：将图片分解为碎片**/
    private void splitImageRect() {
        if (mDecoder == null || measuredWidth == 0 || imageRequest) {
            return;
        }
        imageRequest = true;
        Observable.just(1).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                /*zune：绘制图片网格的列表，将图片分割为多个碎片**/
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                if (orientation == LinearLayout.HORIZONTAL) {
                    options.inSampleSize = (int) (1 / scaleFactor * mImageWidth / measuredWidth);
                } else {
                    options.inSampleSize = (int) (1 / scaleFactor * mImageHeight / measuredHeight);
                }
                girdBitmaps.clear();
                mBitmapRectList.clear();
                float imageHeight = mImageHeight;
                float imageWidth = mImageWidth;
                if (imageHeight == 0 || imageWidth == 0) {
                    return 1;
                }
                float width = measuredWidth;
                float height = measuredHeight;
                int hCount = (int) (imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1));
                int vCount = (int) (imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1));
                if (hCount == 1 && vCount == 1) {
                    dispatchSingleImage();
                    return -1;
                }
                for (int i = 0; i < hCount; i++) {
                    for (int j = 0; j < vCount; j++) {
                        Rect rect = new Rect();
                        rect.left = (int) (1f * i * width);
                        rect.right = (int) Math.min(rect.left + width, imageWidth);
                        rect.top = (int) (1f * j * height);
                        rect.bottom = (int) Math.min(rect.top + height, imageHeight);
                        Bitmap bitmap = mDecoder.decodeRegion(rect, options);
                        girdBitmaps.add(bitmap);
                        mBitmapRectList.add(rect);
                        if (onProgressCallBack != null) {
                            onProgressCallBack.onPosition(1f * girdBitmaps.size() / (hCount * vCount));
                        }
                    }
                }
                imageRequest = false;
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer, Integer>(1, false) {
                    @Override
                    public void onNext(Integer integer, Integer integer2) {
                        if (integer == -1) {
                            splitCanvasRect();
                            return;
                        }
                        if (orientation == LinearLayout.HORIZONTAL) {
                            if (mImageHeight < measuredHeight) {
                                /*zune：如果是超宽图，初始化的时候，默认将高度放大到屏幕高度**/
                                onScale(0, measuredHeight / 2f, 1f * measuredHeight / mImageHeight * mImageWidth / measuredWidth);
                                return;
                            }
                        } else {
                            if (mImageWidth < measuredWidth) {
                                /*zune：如果是超长图，初始化的时候，默认将宽度放大到屏幕宽度**/
                                onScale(measuredWidth / 2f, 0, 1f * measuredWidth / mImageWidth * mImageHeight / measuredHeight);
                                return;
                            }
                        }
                        splitCanvasRect();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    private void dispatchSingleImage() {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = mImageWidth;
        rect.top = 0;
        rect.bottom = mImageHeight;
        Bitmap bitmap = mDecoder.decodeRegion(rect, options);
        girdBitmaps.add(bitmap);
        mBitmapRectList.add(rect);
        if (onProgressCallBack != null) {
            onProgressCallBack.onPosition(1);
        }
        imageRequest = false;
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
        Bitmap tempBitmap = girdBitmaps.get(0);
        int normalBitmapWidth = tempBitmap.getWidth();
        int normalBitmapHeight = tempBitmap.getHeight();
        if (orientation == LinearLayout.VERTICAL) {
            width = Math.min(measuredWidth * getRadio(), 1f * height * normalBitmapWidth / normalBitmapHeight);
        }
        if (orientation == LinearLayout.HORIZONTAL) {
            height = Math.min(measuredHeight * getRadio(), 1f * width * normalBitmapHeight / normalBitmapWidth);
        }
        int hCount = (int) (imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1));
        int vCount = (int) (imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1));
        if (hCount == 1 && vCount == 1) {
            dispatchSingleCanvas();
            return;
        }
        for (int i = 0; i < hCount; i++) {
            for (int j = 0; j < vCount; j++) {
                RectF rect = new RectF();
                rect.left = i * width + getMarginLeft() + mViewRect.left;
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
                if (bitmapWidth < normalBitmapWidth) {
                    rect.right = rect.left + width * bitmapWidth / normalBitmapWidth;
                }
                int bitmapHeight = bitmap.getHeight();
                if (bitmapHeight < normalBitmapHeight) {
                    rect.bottom = rect.top + height * bitmapHeight / normalBitmapHeight;
                }
                mCanvasRectList.add(rect);
            }
        }
        /*zune：将可视化的区域填充上bitmap，延迟200ms是为了不让卡顿感强烈**/
        List<Rect> visibleRect = new ArrayList<>();
        List<Integer> visibleCanvasRectF = new ArrayList<>();
        for (int i = 0; i < mCanvasRectList.size(); i++) {
            if (checkIsVisible(mCanvasRectList.get(i))) {
                Rect rect = mBitmapRectList.get(i);
                visibleRect.add(rect);
                visibleCanvasRectF.add(i);
            }
        }
        loadingHandler.removeCallbacksAndMessages(null);
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                decodeRealBitmap(visibleRect, visibleCanvasRectF);
            }
        }, 200);
    }

    private void dispatchSingleCanvas() {
        RectF rect = new RectF();
        rect.left = mViewRect.left;
        rect.right = rect.left + measuredWidth * scaleFactor;
        rect.top = mViewRect.top;
        rect.bottom = rect.top + measuredHeight * scaleFactor;
        if (orientation == LinearLayout.HORIZONTAL) {
            rect.top = mViewRect.top + (measuredHeight - 1f * mImageHeight / mImageWidth * measuredWidth) / 2f * scaleFactor;
            rect.bottom = rect.top + 1f * mImageHeight / mImageWidth * measuredWidth * scaleFactor;
        }
        if (orientation == LinearLayout.VERTICAL) {
            rect.left = mViewRect.left + (measuredWidth - 1f * mImageWidth / mImageHeight * measuredHeight) / 2f * scaleFactor;
            rect.right = rect.left + 1f * mImageWidth / mImageHeight * measuredHeight * scaleFactor;
        }
        mCanvasRectList.add(rect);
        postInvalidate();
    }

    private Handler loadingHandler = new Handler();


    private float getMarginLeft() {
        /*zune：整体视图view，在不缩放的情况下相对屏幕左边的距离(计算网格从哪里开始绘制)**/
        if (orientation == LinearLayout.VERTICAL && mImageHeight > measuredHeight) {
            return (measuredWidth * scaleFactor - 1f * measuredHeight * scaleFactor * mImageWidth / mImageHeight) / 2;
        } else {
            return 0;
        }
    }

    private float getMarginTop() {
        /*zune：整体视图view，在不缩放的情况下相对屏幕上方的距离(计算网格从哪里开始绘制)**/
        if (orientation == LinearLayout.HORIZONTAL && mImageWidth > measuredWidth) {
            return (measuredHeight * scaleFactor - 1f * measuredWidth * scaleFactor * mImageHeight / mImageWidth) / 2;
        } else {
            return 0;
        }
    }

    private float getRadio() {
        /*zune：图片缩小比例，和缩放比例不同，这个是指view可视矩形宽度与图片宽度的比率**/
        if (orientation == LinearLayout.HORIZONTAL) {
            return mViewRect.width() / mImageWidth;
        } else {
            return mViewRect.height() / mImageHeight;
        }
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
        if (girdBitmaps.isEmpty() || mCanvasRectList.isEmpty()) {
            return;
        }
        for (int i = 0; i < girdBitmaps.size(); i++) {
            RectF rectF = mCanvasRectList.get(i);
            if (!checkIsVisible(rectF)) {
                continue;
            }
            canvas.drawBitmap(girdBitmaps.get(i), null, rectF, null);
            canvas.drawRect(rectF, colorPaint);
            Iterator<Integer> iterator = realBitmap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer position = iterator.next();
                if (i == position) {
                    Bitmap bitmap = realBitmap.get(position);
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, mCanvasRectList.get(i), null);
                    }
                    break;
                }
            }
        }
        recycleBitmap();
    }

    /*zune：回收不可见的图片资源**/
    private void recycleBitmap() {
        for (int i = 0; i < mCanvasRectList.size(); i++) {
            Iterator<Integer> iterator = realBitmap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer position = iterator.next();
                if (i == position) {
                    Bitmap bitmap = realBitmap.get(position);
                    if (bitmap != null && !checkIsVisible(mCanvasRectList.get(i))) {
                        bitmap.recycle();
                        bitmap = null;
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }

    /*zune：加载真正的大图碎片**/
    private void decodeRealBitmap(List<Rect> visibleRect, List<Integer> visiblePosition) {
        Observable.just(visibleRect).map(new Function<List<Rect>, Integer>() {
            @Override
            public Integer apply(List<Rect> rects) throws Exception {
                for (int i = 0; i < rects.size(); i++) {
                    options.inSampleSize = (int) (1 / scaleFactor * mImageWidth / measuredWidth);
                    Bitmap bitmap = mDecoder.decodeRegion(rects.get(i), options);
                    realBitmap.put(visiblePosition.get(i), bitmap);
                }
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<Integer, Integer>(1, false) {
            @Override
            public void onNext(Integer i, Integer j) {
                postInvalidate();
            }
        });
    }

    /*zune：检查画布是否超过了可视区域**/
    private boolean checkIsVisible(RectF rectF) {
        if (rectF.right < 0 || rectF.bottom < 0 || rectF.left > measuredWidth || rectF.top > measuredHeight) {
            return false;
        }
        return true;
    }

    /*zune：画布的矩形区域**/
    private List<RectF> mCanvasRectList = new ArrayList<>();

    /*zune：对应画布的图片碎片的矩形区域**/
    private List<Rect> mBitmapRectList = new ArrayList<>();

    /*zune：视图的矩形(也就是说，初始是整个可视区域，随着缩放进行，整个图片扩展到可视区域之外的矩形，这个矩形是相对屏幕的坐标)**/
    private RectF mViewRect = new RectF();

    /*zune：网格碎片的bitmap列表，其实可以把不可见的也加上，这样计算起来更加方便**/
    private List<Bitmap> girdBitmaps = new ArrayList<>();

    /*zune：真正的大图碎片，保存起来，每个碎片，对应一个矩形区域**/
    private Map<Integer, Bitmap> realBitmap = new HashMap<>();

    public static class FillingValueAnimator extends ValueAnimator {
        float velocityX, velocityY;
        MotionEvent currentEvent, motionEvent;
        float lastX = 0, lastY = 0;
    }

    public interface OnProgressCallBack {
        void onPosition(float progress);
    }

    private OnProgressCallBack onProgressCallBack;

    public void setOnProgressCallBack(OnProgressCallBack onProgressCallBack) {
        this.onProgressCallBack = onProgressCallBack;
    }
}
