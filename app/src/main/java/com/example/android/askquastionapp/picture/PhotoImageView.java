package com.example.android.askquastionapp.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
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

    public PhotoImageView(Context context) {
        super(context);
        setSampleSize();
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSampleSize();
    }

    public PhotoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSampleSize();
    }

    private void setSampleSize() {
        options.inSampleSize = 8;
    }

    private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = scaleFactor < 1f ? (float) 1f : scaleFactor > 5 ? 5 : scaleFactor;
            LogUtils.i("zune: ", "scale = " + scaleFactor);
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
                invalidate();
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
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
        mViewRect.set(0, 0, measuredWidth, (int) (1f * measuredWidth * mImageHeight / mImageWidth));
        mImageRect.set(0, 0, mImageWidth, mImageHeight);
        splitImageRect();
        splitCanvasRect();
    }

    private void splitImageRect() {
        mImageRectList.clear();
        int imageHeight = this.mImageHeight;
        int imageWidth = this.mImageWidth;
        if (imageHeight == 0 || imageWidth == 0) {
            return;
        }
        int measuredHeight = (int) (1f * measuredWidth * imageHeight / imageWidth);
        int width = (int) (measuredWidth);
        int height = (int) (measuredHeight);
        int hCount = imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1);
        int vCount = imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1);
        for (int i = 0; i < hCount; i++) {
            for (int j = 0; j < vCount; j++) {
                Rect rect = new Rect();
                rect.left = (int) (1f * i * width);
                rect.right = Math.min(rect.left + width, imageWidth);
                rect.top = (int) (1f * j * height);
                rect.bottom = Math.min(rect.top + height, imageHeight);
                mImageRectList.add(rect);
            }
        }
    }

    private void splitCanvasRect() {
        mCanvasRectList.clear();
        int imageHeight = this.mImageHeight;
        int imageWidth = this.mImageWidth;
        if (imageHeight == 0 || imageWidth == 0) {
            return;
        }
        float measuredHeight = 1f * measuredWidth * imageHeight / imageWidth;
        float width = measuredWidth * getRadio();
        float height = measuredHeight * getRadio();
        int hCount = imageWidth / measuredWidth + (imageWidth % measuredWidth == 0 ? 0 : 1);
        int vCount = (int) (imageHeight / measuredHeight + (imageHeight % measuredHeight == 0 ? 0 : 1));
        for (int i = 0; i < hCount; i++) {
            for (int j = 0; j < vCount; j++) {
                RectF rect = new RectF();
                rect.left = 1f * i * width;
                rect.right = Math.min(rect.left + width, imageWidth * getRadio());
                rect.top = 1f * j * height;
                rect.bottom = Math.min(rect.top + height, imageHeight * getRadio());
                mCanvasRectList.add(rect);
            }
        }
    }

    private float getRadio() {
        return 1f * measuredWidth / mImageWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDecoder == null) {
            return;
        }
        resizeImageBitmap(canvas);
    }

    private void resizeImageBitmap(Canvas canvas) {
        options.inSampleSize = 8;
        for (int i = 0; i < mImageRectList.size(); i++) {
            Bitmap bitmap = mDecoder.decodeRegion(mImageRectList.get(i), options);
            RectF rectF = mCanvasRectList.get(i);
            Rect rect = fromRectF(rectF);
            canvas.drawBitmap(bitmap, null, rect, null);
        }
    }

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

    private Rect mImageRect = new Rect();
}
