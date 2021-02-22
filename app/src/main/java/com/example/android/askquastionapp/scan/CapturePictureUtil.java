package com.example.android.askquastionapp.scan;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.scan.zxing.decode.encode.CodeCreator;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CapturePictureUtil {
    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> allFormats = new ArrayList<>();
        allFormats.add(BarcodeFormat.AZTEC);
        allFormats.add(BarcodeFormat.CODABAR);
        allFormats.add(BarcodeFormat.CODE_39);
        allFormats.add(BarcodeFormat.CODE_93);
        allFormats.add(BarcodeFormat.CODE_128);
        allFormats.add(BarcodeFormat.DATA_MATRIX);
        allFormats.add(BarcodeFormat.EAN_8);
        allFormats.add(BarcodeFormat.EAN_13);
        allFormats.add(BarcodeFormat.ITF);
        allFormats.add(BarcodeFormat.MAXICODE);
        allFormats.add(BarcodeFormat.PDF_417);
        allFormats.add(BarcodeFormat.QR_CODE);
        allFormats.add(BarcodeFormat.RSS_14);
        allFormats.add(BarcodeFormat.RSS_EXPANDED);
        allFormats.add(BarcodeFormat.UPC_A);
        allFormats.add(BarcodeFormat.UPC_E);
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
        HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    public static void parseQCodeInFile(String path, OnResultListener onResultListener) {
        parseQCodeInBitmap(fileToBitmap(path), onResultListener);
    }

    public static void parseQCodeInputStream(InputStream ins, OnResultListener onResultListener) {
        parseQCodeInBitmap(inputStringToBitmap(ins), onResultListener);
    }

    public static Bitmap inputStringToBitmap(InputStream ins) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ins, null, options);
            int sampleSize = options.outHeight / 400;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(ins, null, options);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap fileToBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = options.outHeight / 400;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            return null;
        }
    }

    public static void parseQCodeInBitmap(Bitmap bitmap, OnResultListener onResultListener) {
        if (bitmap == null) {
            if (onResultListener != null) {
                onResultListener.onResult(null);
            }
            return;
        }
        Observable.just(bitmap).map(new Function<Bitmap, String>() {
            @Override
            public String apply(Bitmap bitmap) throws Exception {
                for (int i = 0; i < 4; i++) {
                    String s = bitmap2Result(rotate(bitmap, i * 90));
                    if (s != null) {
                        return s;
                    }
                }
                return null;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String, OnResultListener>(onResultListener, false) {
                    @Override
                    public void onNext(String s, OnResultListener onResultListener) {
                        if (onResultListener != null) {
                            onResultListener.onResult(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (onResultListener != null) {
                            onResultListener.onResult(null);
                        }
                    }
                });
    }

    @Nullable
    private static String bitmap2Result(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Result result = null;
        RGBLuminanceSource source = null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            source = new RGBLuminanceSource(width, height, pixels);
            result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
            return result.getText();
        } catch (Throwable e) {
            e.printStackTrace();
            if (source != null) {
                try {
                    result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                    return result.getText();
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Bitmap rotate(Bitmap bitmap, int degree) {
        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmp;
        }
        return bitmap;
    }

    public static Bitmap getQCodeBitmap(String s) {
        return CodeCreator.createQRCode(s, 400, 400, BitmapFactory.decodeResource(BaseApplication.getInstance()
                .getResources(), R.mipmap.ic_launcher, null));
    }

    /**
     * 保存bitmap图片到本地
     *
     * @param context 上下文
     * @param bmp     bitmap图片
     * @param dirName 要保存到的文件路径
     * @return 2成功 -1失败
     */
    public static int saveImageToGallery(Context context, Bitmap bmp, String dirName) {
        //android 10 需要 用 MediaStore来存
        String fileName = "QCode_"
                + new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.getDefault()).format(new Date(System.currentTimeMillis())) + ".png";
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put("relative_path", "DCIM/Camera");
            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = BaseApplication.getInstance().getContentResolver();
            Uri insertUri = resolver.insert(external, values);
            if (insertUri != null) {
                try (BufferedOutputStream outputStream = new BufferedOutputStream(resolver.openOutputStream(insertUri))) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
            return 2;
        }
        //生成路径
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File appDir = new File(root, dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        //获取文件
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
//            //通知系统相册刷新
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),fileName,null);
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                    Uri.fromFile(new File(file.getPath()))));
            return 2;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
//                deleteFile(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public interface OnResultListener {
        void onResult(String s);
    }
}
