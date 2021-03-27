package com.example.android.askquastionapp.picture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

abstract class AbstractPhotoImageView extends View {

    private int mImageWidth;
    private int mImageHeight;
    private int measuredWidth;
    private int measuredHeight;
    private BitmapRegionDecoder mDecoder;
    private final BitmapFactory.Options options = new BitmapFactory.Options();
    private float scaleFactor = 1f;
    private Paint colorPaint;
    private FillingValueAnimator filingAnimator; // 惯性动画
    private int orientation = LinearLayout.HORIZONTAL;
    private TextPaint mTextPaint;  //画文字的画笔

    public AbstractPhotoImageView(Context context) {
        super(context);
        init();
    }

    public AbstractPhotoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbstractPhotoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(Color.RED);
        int dp_1 = DensityUtil.dp2px(1f);
        PathEffect effects = new DashPathEffect(new float[]{dp_1 * 5, dp_1 * 5, dp_1 * 5, dp_1 * 5}, dp_1);
        colorPaint.setPathEffect(effects);
        colorPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(DensityUtil.dp2px(16));
    }

    private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            getParent().requestDisallowInterceptTouchEvent(true);
            /*zune：这个坐标是相对屏幕的坐标，缩放的时候，注意要转换为相对画布的坐标**/
            float cx = detector.getFocusX();
            float cy = detector.getFocusY();
            float scale = detector.getScaleFactor();
            AbstractPhotoImageView.this.onScale(cx, cy, scale);
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
    private boolean[] limitRect = new boolean[]{false, false, false, false};

    private void updateScaleViewRect(float cx, float cy, float oldScale, float scaleFactor) {
        /*zune：-(x - mView.left) / oldScale * scale - x  **/
        float left = -((cx - mViewRect.left) / oldScale * scaleFactor - cx);
        float top = -((cy - mViewRect.top) / oldScale * scaleFactor - cy);
        float right = left + measuredWidth * scaleFactor;
        float bottom = top + measuredHeight * scaleFactor;
        updateViewRect(left, top, right, bottom, false);
    }

    /*zune：设置画布的边界**/
    private void updateViewRect(float left, float top, float right, float bottom, boolean byScroll) {
        for (int i = 0; i < 4; i++) {
            limitRect[i] = false;
        }
        if (mImageWidth < measuredWidth && mImageHeight < measuredHeight) {
            dispatchSingleViewRect(left, top, right, bottom, byScroll);
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
                limitRect[2] = byScroll;
            }
            if (right > maxX) {
                right = maxX;
                left = 0;
                limitRect[0] = byScroll;
            }
            screenScale = measuredHeight / (1f * measuredWidth * mImageHeight / mImageWidth);
            if (scaleFactor < screenScale) {
                /*zune：当高度还没有手机屏幕高，就固定死上下距离**/
                top = (-measuredHeight * scaleFactor + measuredHeight) / 2;
                bottom = measuredHeight * scaleFactor - top;
                limitRect[1] = byScroll;
                limitRect[3] = byScroll;
            } else {
                if (top < minY) {
                    top = minY;
                    bottom = measuredHeight + getMarginTop();
                    limitRect[3] = byScroll;
                }
                if (bottom > maxY) {
                    bottom = maxY;
                    top = -getMarginTop();
                    limitRect[1] = byScroll;
                }
            }
        } else {
            /*zune：对于长图，各边界的处理**/
            if (top < minY) {
                top = minY;
                bottom = measuredHeight;
                limitRect[3] = byScroll;
            }
            if (bottom > maxY) {
                bottom = maxY;
                top = 0;
                limitRect[1] = byScroll;
            }
            screenScale = measuredWidth / (1f * measuredHeight * mImageWidth / mImageHeight);
            if (scaleFactor < screenScale) {
                /*zune：当宽度还没有手机屏幕宽，就固定死左右距离**/
                left = (-measuredWidth * scaleFactor + measuredWidth) / 2;
                right = measuredWidth * scaleFactor - left;
                limitRect[0] = byScroll;
                limitRect[2] = byScroll;
            } else {
                if (left < minX) {
                    left = minX;
                    right = measuredWidth + getMarginLeft();
                    limitRect[2] = byScroll;
                }
                if (right > maxX) {
                    right = maxX;
                    left = -getMarginLeft();
                    limitRect[0] = byScroll;
                }
            }
        }
        mViewRect.set(left, top, right, bottom);
    }

    /*zune：画布碎片和图片碎片只有一张的情况，设置视图边界**/
    private void dispatchSingleViewRect(float left, float top, float right, float bottom, boolean byScroll) {
        if (orientation == LinearLayout.HORIZONTAL) {
            /*zune：基准缩放标准是指的是针对横图，图片高度刚好缩放到可视高度的缩放量**/
            float baseScale = measuredHeight / (1f * measuredWidth * mImageHeight / mImageWidth);
            float offsetTop = measuredHeight * (scaleFactor - 1) / 2f;
            if (scaleFactor < baseScale) {
                top = -offsetTop;
                bottom = top + scaleFactor * mImageHeight;
                limitRect[1] = byScroll;
                limitRect[3] = byScroll;
            } else {
                float nowImageWidth = scaleFactor * measuredWidth;
                float nowImageHeight = nowImageWidth * mImageHeight / mImageWidth;
                float offsetY = (nowImageHeight - measuredHeight) / 2;
                float minTop = -offsetTop - offsetY;
                float maxTop = -offsetTop + offsetY;
                if (top < minTop) {
                    top = minTop;
                    limitRect[3] = byScroll;
                }
                if (top > maxTop) {
                    top = maxTop;
                    limitRect[1] = byScroll;
                }
                bottom = top + scaleFactor * measuredHeight;
            }
            if (left < (1 - scaleFactor) * measuredWidth) {
                left = (1 - scaleFactor) * measuredWidth;
                right = measuredWidth;
                limitRect[2] = byScroll;
            }
            if (right > scaleFactor * measuredWidth) {
                right = scaleFactor * measuredWidth;
                left = 0;
                limitRect[0] = byScroll;
            }
        } else {
            float baseScale = measuredWidth / (1f * measuredHeight * mImageWidth / mImageHeight);
            float offsetLeft = measuredWidth * (scaleFactor - 1) / 2f;
            if (scaleFactor < baseScale) {
                left = -offsetLeft;
                right = left + scaleFactor * mImageWidth;
                limitRect[0] = byScroll;
                limitRect[2] = byScroll;
            } else {
                float nowImageHeight = scaleFactor * measuredHeight;
                float nowImageWidth = nowImageHeight * mImageWidth / mImageHeight;
                float offsetX = (nowImageWidth - measuredWidth) / 2;
                float minLeft = -offsetLeft - offsetX;
                float maxLeft = -offsetLeft + offsetX;
                if (left < minLeft) {
                    left = minLeft;
                    limitRect[2] = byScroll;
                }
                if (left > maxLeft) {
                    left = maxLeft;
                    limitRect[0] = byScroll;
                }
                right = left + scaleFactor * measuredHeight;
            }
            if (top < (1 - scaleFactor) * measuredHeight) {
                top = (1 - scaleFactor) * measuredHeight;
                bottom = measuredHeight;
                limitRect[3] = byScroll;
            }
            if (bottom > scaleFactor * measuredHeight) {
                bottom = scaleFactor * measuredHeight;
                top = 0;
                limitRect[1] = byScroll;
            }
        }
        mViewRect.set(left, top, right, bottom);
    }

    protected boolean isDismiss;

    private GestureDetector moveGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(AbstractPhotoImageView.this);
            }
        }

        private boolean onScroll(MotionEvent currentEvent, MotionEvent motionEvent, float scrollX, float scrollY, boolean byScroll) {
            if (isDismiss) {
                return false;
            }
            float left = mViewRect.left - scrollX;
            float top = mViewRect.top - scrollY;
            float right = mViewRect.right - scrollX;
            float bottom = mViewRect.bottom - scrollY;
            boolean isScrollHorizontal = Math.abs(scrollX) >= Math.abs(scrollY);
            int dp_30 = DensityUtil.dp2px(30);
            if (limitRect[0] && scrollX < -dp_30 && isScrollHorizontal) {
                onGestureScroll(PhotoImageView.GESTURE_SCROLL_LEFT);
                return false;
            }
            if (limitRect[2] && scrollX > dp_30 && isScrollHorizontal) {
                onGestureScroll(PhotoImageView.GESTURE_SCROLL_RIGHT);
                return false;
            }
            if (limitRect[1] && scrollY < -dp_30 && !isScrollHorizontal) {
                onGestureScroll(PhotoImageView.GESTURE_SCROLL_TOP);
                return false;
            }
            if (limitRect[3] && scrollY > dp_30 && !isScrollHorizontal) {
                onGestureScroll(PhotoImageView.GESTURE_SCROLL_BOTTOM);
                return false;
            }
            updateViewRect(left, top, right, bottom, true);
            splitCanvasRect();
            postInvalidate();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent currentEvent, MotionEvent motionEvent, float scrollX, float scrollY) {
            return onScroll(currentEvent, motionEvent, scrollX, scrollY, true);
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
                        if (filingAnimator == null) {
                            return;
                        }
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
                        onScroll(filingAnimator.currentEvent, filingAnimator.motionEvent, dx, dy, false);
                    }
                });
            } else if (filingAnimator.isRunning()) {
                filingAnimator.cancel();
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

    public void setFile(File file, boolean changeSrcFile) {
        srcFile = file;
        Observable.just(file).map(new Function<File, File>() {
            @Override
            public File apply(File srcFile) throws Exception {
                File file = null;
                if (mCurRotation == 0 && changeSrcFile) {
                    mCurRotation = getRotation(srcFile);
                }
                if (mCurRotation == 0 && changeSrcFile) {
                    return srcFile;
                }
                Message msg = new Message();
                msg.what = 1002;
                loadingHandler.sendMessage(msg);
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                Bitmap image = BitmapFactory.decodeStream(BaseApplication.getInstance().getContentResolver().openInputStream(FileUtil.getUriFromFile(getContext(), srcFile)), null, newOpts);
                if (mCurRotation % 360 != 0) {
                    image = rotate(image, mCurRotation);
                }
                // 把压缩后的数据baos存放到ByteArrayInputStream中
                BufferedOutputStream bos = null;
                try {
                    File parentFile = srcFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    String[] split = srcFile.getPath().split("\\.");
                    file = new File(getContext().getExternalCacheDir(), "temp." + split[split.length - 1]);
                    file.deleteOnExit();
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    boolean b = false;
                    String newPath = file.getPath();
                    if (newPath.toLowerCase().endsWith(".png")) {
                        b = image.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    } else if (newPath.toLowerCase().endsWith(".jpg") || newPath.toLowerCase().endsWith(".jpeg")) {
                        b = image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    }
                    bos.close();
                    if (b && split.length > 1 && changeSrcFile) {
                        boolean delete = srcFile.delete();
                        boolean renameTo = file.renameTo(new File(split[0] + "." + split[1]));
                    }
                } catch (Exception ignore) {
                } finally {
                    try {
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (image != null) {
                        image.recycle();
                    }
                }
                return changeSrcFile ? srcFile : file;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<File, Integer>(1, false) {
                    @Override
                    public void onNext(File file, Integer integer2) {
                        if (mInputStream != null) {
                            float oldScale = scaleFactor;
                            scaleFactor = 1;
                            updateScaleViewRect(0, 0, oldScale, scaleFactor);
                            postInvalidate();
                        }
                        try {
                            setImageResource(new FileInputStream(file), file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private int getRotation(File file) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            InputStream inputStream = BaseApplication.getInstance().getContentResolver().openInputStream(FileUtil.getUriFromFile(getContext(), file));
            ExifInterface exifInterface = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exifInterface = new ExifInterface(inputStream);
            } else {
                exifInterface = new ExifInterface(file.getPath());
            }
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    public void setImageResource(InputStream inputStream) {
        setImageResource(inputStream, null);
    }

    private InputStream mInputStream;

    public void setImageResource(InputStream inputStream, File file) {
        release();
        mInputStream = inputStream;
        Observable.just(inputStream).map(new Function<InputStream, Integer>() {
            @Override
            public Integer apply(InputStream inputStream) throws Exception {
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                if (file == null) {
                    BitmapFactory.decodeStream(inputStream, null, tmpOptions);
                } else {
                    if (file.getAbsolutePath().endsWith(".gif")) {
                        loadingText = "暂不支持此类型";
                        postInvalidate();
                        return 1;
                    }
                    BitmapFactory.decodeFile(file.getAbsolutePath(), tmpOptions);
                }
                mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                if (tmpOptions.outHeight == -1 || tmpOptions.outWidth == -1) {
                    return 1;
                } else {
                    mImageWidth = tmpOptions.outWidth;
                    mImageHeight = tmpOptions.outHeight;
                }
                splitImageRect();
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    public InputStream getImageResource() {
        return mInputStream;
    }

    public Bitmap getImageBitmap() {
        if (mImageWidth == 0 || mImageHeight == 0 || mDecoder == null || measuredWidth == 0 || measuredHeight == 0 || srcFile == null) {
            return null;
        }
        Bitmap bitmap = getLayoutCache();
        if (bitmap != null) {
            return bitmap;
        }
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            InputStream inputStream = new FileInputStream(srcFile);
            ExifInterface exifInterface = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exifInterface = new ExifInterface(inputStream);
            } else {
                exifInterface = new ExifInterface(srcFile.getPath());
            }
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }

            Rect rect = new Rect();
            rect.left = 0;
            rect.right = mImageWidth;
            rect.top = 0;
            rect.bottom = mImageHeight;
            /*zune：绘制图片网格的列表，将图片分割为多个碎片**/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = (int) (1f * Math.max(mImageWidth, mImageHeight) / Math.max(measuredWidth, measuredHeight));
            return rotate(mDecoder.decodeRegion(rect, options), degree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getLayoutCache() {
        ViewGroup v = (ViewGroup) getParent();
        if (v.getWidth() <= 0 || v.getHeight() <= 0) {
            return null;
        }
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    private Bitmap rotate(Bitmap bitmap, int degree) {
        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmp;
        }
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageRequest) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
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
        updateViewRect(0, 0, measuredWidth, measuredHeight, false);
        splitImageRect();
    }

    private boolean imageRequest;

    /*zune：将图片分解为碎片**/
    private void splitImageRect() {
        if (mDecoder == null || measuredWidth == 0 || imageRequest) {
            return;
        }
        float widthWeight = 1f * mImageWidth / measuredWidth;
        float heightWeight = 1f * mImageHeight / measuredHeight;
        if (widthWeight >= heightWeight) {
            orientation = LinearLayout.HORIZONTAL;
        } else {
            orientation = LinearLayout.VERTICAL;
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
                    imageRequest = false;
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
                        if (loadingHandler != null) {
                            float progress = 1f * girdBitmaps.size() / (hCount * vCount);
                            Message msg = new Message();
                            msg.what = 1001;
                            msg.obj = progress;
                            loadingHandler.sendMessage(msg);
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
//                                onScale(0, measuredHeight / 2f, 1f * measuredHeight / mImageHeight * mImageWidth / measuredWidth);
                                /*zune：超宽图不处理了**/
//                                return;
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

    /*zune：画布碎片和图片碎片只有一张的情况，设置图片碎片**/
    private void dispatchSingleImage() {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = mImageWidth;
        rect.top = 0;
        rect.bottom = mImageHeight;
        Bitmap bitmap = mDecoder.decodeRegion(rect, options);
        girdBitmaps.add(bitmap);
        mBitmapRectList.add(rect);
        imageRequest = false;
        loadingText = null;
        float newScale = 1f * mImageHeight / mImageWidth / (1f * measuredHeight / measuredWidth);
        if (newScale > 1) {
            scaleFactor = newScale;
            updateScaleViewRect(measuredWidth / 2f, 0, 1, newScale);
            postInvalidate();
        }
    }

    private boolean canvasResetting;

    private void splitCanvasRect() {
        if (girdBitmaps.isEmpty()) {
            return;
        }
        canvasResetting = true;
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
        canvasResetting = false;
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                decodeRealBitmap(visibleRect, visibleCanvasRectF);
            }
        }, 200);
    }

    /*zune：画布碎片和图片碎片只有一张的情况，设置画布碎片**/
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
        canvasResetting = false;
        postInvalidate();
    }

    private Handler loadingHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                float progress = (float) msg.obj;
                DecimalFormat df = new DecimalFormat("######0.00");
                loadingText = df.format(progress * 100) + "%";
                postInvalidate();
                if (progress == 1) {
                    loadingText = null;
                }
            } else if (msg.what == 1002) {
                loadingText = "正在摆正角度";
                postInvalidate();
            }
        }
    };

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
        if (!TextUtils.isEmpty(loadingText)) {
            float width = mTextPaint.measureText(loadingText);
            canvas.drawText(loadingText, measuredWidth / 2f - width / 2f, measuredHeight / 2f, mTextPaint);
            return;
        }
        if (mDecoder == null) {
            return;
        }
        try {
            drawImageBitmap(canvas);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /*zune：核心的绘制业务逻辑，绘制之前先计算采样率，然后过滤掉不可视的矩形画布，最后将bitmap存入容器中，并根据矩形绘制出对应的图片**/
    private synchronized void drawImageBitmap(Canvas canvas) throws Throwable {
        if (girdBitmaps.isEmpty() || mCanvasRectList.isEmpty() || canvasResetting) {
            return;
        }
        for (int i = 0; i < girdBitmaps.size(); i++) {
            if (mCanvasRectList.size() < i) {
                break;
            }
            RectF rectF = mCanvasRectList.get(i);
            if (!checkIsVisible(rectF)) {
                continue;
            }
            if (!canvasResetting && !imageRequest) {
                canvas.drawBitmap(girdBitmaps.get(i), null, rectF, null);
                canvas.drawRect(rectF, colorPaint);
            }
            Iterator<Integer> iterator = realBitmap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer position = iterator.next();
                if (i == position) {
                    Bitmap bitmap = realBitmap.get(position);
                    if (bitmap != null) {
                        if (!canvasResetting && !imageRequest) {
                            canvas.drawBitmap(bitmap, null, mCanvasRectList.get(i), null);
                        }
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

    public void release() {
        mCurRotation = 0;
        setAlpha(1);
        setScaleX(1);
        setScaleY(1);
        setTranslationY(0);
        scaleFactor = 1f;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        realBitmap.clear();
        girdBitmaps.clear();
        mBitmapRectList.clear();
        mCanvasRectList.clear();
        mDecoder = null;
        imageRequest = false;
        mInputStream = null;
        for (int i = 0; i < 4; i++) {
            limitRect[i] = false;
        }
        if (filingAnimator != null) {
            filingAnimator.cancel();
        }
        filingAnimator = null;
        isDismiss = false;
        setBackgroundColor(Color.BLACK);
        loadingHandler.removeCallbacksAndMessages(null);
    }

    private int mCurRotation;
    private File srcFile;

    public void setCurRotation(int rotation, boolean changeSrcFile) {
        mCurRotation = rotation;
        setFile(srcFile, changeSrcFile);
    }

    public static class FillingValueAnimator extends ValueAnimator {
        float velocityX, velocityY;
        MotionEvent currentEvent, motionEvent;
        float lastX = 0, lastY = 0;
    }

    public interface OnLimitCallBack {
        void onDismiss();

        default void onLeftLimit() {
        }

        default void onRightLimit() {
        }
    }

    public void dismiss() {
        setVisibility(GONE);
        setAlpha(1);
        setScaleX(1);
        setScaleY(1);
        setTranslationY(0);
        isDismiss = false;
        for (int i = 0; i < 4; i++) {
            limitRect[i] = false;
        }
    }

    private String loadingText;

    protected abstract void onGestureScroll(@PhotoImageView.GestureScrollType int type);

    private OnLongClickListener onLongClickListener;

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }
}
