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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.utils.SimpleObserver;

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
        scaleFactor = scaleFactor < 1f ? 1f : scaleFactor > 20 ? 20 : scaleFactor;
        if (scaleFactor >= 20) {
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

    private void updateViewRect(float left, float top, float right, float bottom) {
        float minX = -scaleFactor * measuredWidth + measuredWidth;
        float maxX = scaleFactor * measuredWidth;
        float minY = (1 - scaleFactor) * measuredHeight + getMarginTop();
        float maxY = scaleFactor * measuredHeight - getMarginTop();
        if (left < minX) {
            left = minX;
            right = measuredWidth;
        }
        if (right > maxX) {
            right = maxX;
            left = 0;
        }
        if (scaleFactor < measuredHeight / (1f * measuredWidth * mImageHeight / mImageWidth)) {
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
                options.inSampleSize = (int) (1 / scaleFactor * mImageWidth / measuredWidth);
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
        Bitmap tempBitmap = girdBitmaps.get(0);
        int normalBitmapWidth = tempBitmap.getWidth();
        int normalBitmapHeight = tempBitmap.getHeight();
        float height = Math.min(measuredHeight * getRadio(), 1f * width * normalBitmapHeight / normalBitmapWidth);
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

    private Handler loadingHandler = new Handler();

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
        for (int i = 0; i < girdBitmaps.size(); i++) {
            RectF rectF = mCanvasRectList.get(i);
            if (!checkIsVisible(rectF)) {
                continue;
            }
            canvas.drawBitmap(girdBitmaps.get(i), null, rectF, null);
            canvas.drawRect(rectF, colorPaint);
            for (Integer position : realBitmap.keySet()) {
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
//            ConcurrentModificationException
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

    /*zune：回收所有图片资源**/
    private void clearBitmaps() {
        Iterator<Integer> iterator = realBitmap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer position = iterator.next();
            Bitmap bitmap = realBitmap.get(position);
            if (bitmap != null) {
                bitmap = null;
            }
        }
        realBitmap.clear();
    }

    /*zune：加载真正的大图碎片**/
    private void decodeRealBitmap(List<Rect> visibleRect, List<Integer> visiblePosition) {
        Observable.just(visibleRect).map(new Function<List<Rect>, Integer>() {
            @Override
            public Integer apply(List<Rect> rects) throws Exception {
                clearBitmaps();
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

    public class FillingValueAnimator extends ValueAnimator {
        float velocityX, velocityY;
        MotionEvent currentEvent, motionEvent;
        float lastX = 0, lastY = 0;
    }
}
