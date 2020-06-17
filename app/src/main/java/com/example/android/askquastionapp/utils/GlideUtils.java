package com.example.android.askquastionapp.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static java.io.File.separator;

public class GlideUtils {
    private static GlideUtils sGlideUtils;
    private final SafeKeyGenerator mSafeKeyGenerator;

    private GlideUtils() {
        mSafeKeyGenerator = new SafeKeyGenerator();
    }

    public static GlideUtils getInstance() {
        if (sGlideUtils == null) {
            synchronized (GlideUtils.class) {
                if (sGlideUtils == null) {
                    sGlideUtils = new GlideUtils();
                }
            }
        }
        return sGlideUtils;
    }

    public File getLocalCache(Context context, String url) {
        /*File file = context.getExternalFilesDir("Glide_cache");
        if (file == null) {
            file = new File(context.getFilesDir(), "Glide_cache");
        }*/
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        String safeKey = mSafeKeyGenerator.getSafeKey(originalKey);
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(CustomGlideModule.directory,
                    1, 1, CustomGlideModule.CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File putLocalCache(String url, byte[] bitmap) {
        try {
            OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
            String safeKey = mSafeKeyGenerator.getSafeKey(originalKey);
            File cache = new File(CustomGlideModule.directory, safeKey + ".0");
            OutputStream out = new FileOutputStream(cache);
            InputStream is = new ByteArrayInputStream(bitmap);
            byte[] buff = new byte[1024];
            int len;
            while((len=is.read(buff))!=-1){
                out.write(buff, 0, len);
            }
            is.close();
            out.close();
            return cache;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class OriginalKey implements Key {

        private final String id;
        private final Key signature;

        public OriginalKey(String id, Key signature) {
            this.id = id;
            this.signature = signature;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            try {
                messageDigest.update(id.getBytes(STRING_CHARSET_NAME));
                signature.updateDiskCacheKey(messageDigest);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OriginalKey that = (OriginalKey) o;

            if (!id.equals(that.id)) {
                return false;
            }
            if (!signature.equals(that.signature)) {
                return false;
            }

            return true;
        }
    }

    public static class SafeKeyGenerator {
        private final LruCache<Key, String> loadIdToSafeHash = new LruCache<Key, String>(1000);

        public String getSafeKey(Key key) {
            String safeKey;
            synchronized (loadIdToSafeHash) {
                safeKey = loadIdToSafeHash.get(key);
            }
            if (safeKey == null) {
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    key.updateDiskCacheKey(messageDigest);
                    safeKey = Util.sha256BytesToHex(messageDigest.digest());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }

    public void loadUrlWithoutDefault(String url, View view) {
        loadUrl(url, view, true, false, true, -1);
    }

    public void loadUrl(String url, View view, boolean control, boolean isPath) {
        loadUrl(url, view, control, isPath, false, -1);
    }

    public void loadUrl(String url, View view, boolean control, boolean isPath, int position) {
        loadUrl(url, view, control, isPath, false, position);
    }

    private void loadUrl(String url, View view, boolean control, boolean isPath, boolean withoutDefault, int position) {
        File localCache = GlideUtils.getInstance().getLocalCache(view.getContext(), url);
        if (localCache != null) {
            setFileToView(localCache, view, control);
            return;
        }
        if (control && !withoutDefault) {
            loadDefaultIcon(view, true);
        }
        Observable.just(url).map(new Function<String, File>() {
            @Override
            public File apply(String url) throws Exception {
                Object model = isPath ? new File(url) : url;
                if (url.endsWith("gif")) {
                    if (android.os.Build.VERSION.SDK_INT >= 17) {
                        return Glide.with(view.getContext())
                                .asFile()
                                .load(model)
                                .apply(new RequestOptions().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                                .submit()
                                .get();
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= 17) {
                        File file = Glide.with(view.getContext())
                                .asFile()
                                .load(model)
                                .apply(new RequestOptions().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                                .submit()
                                .get();
                        return reSaveFile(file);
                    }
                }
                return null;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<File, View>(view, control) {
            @Override
            public void onNext(File file, View view) {
                if (file == null) {
                    loadDefaultIcon(view, true);
                }
                setFileToView(file, view, control);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                loadUrlFromHttp(url, view, control, isPath, withoutDefault, position);
            }
        });
    }

    private void loadDefaultIcon(View view, boolean control) {
        if (view instanceof GifImageView) {
            try {
                GifDrawable gifFromUri = new GifDrawable(view.getResources(), R.mipmap.place_loading);
                ((GifImageView) view).setImageDrawable(gifFromUri);
                view.getLayoutParams().height = (int) ((float) gifFromUri.getIntrinsicHeight() / gifFromUri.getIntrinsicWidth() * ScreenUtils.getScreenWidth());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (view instanceof ImageView) {
            Drawable drawable = view.getResources().getDrawable(R.mipmap.place_loading);
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bm = bd.getBitmap();
            if (bm != null) {
                ((ImageView) view).setImageBitmap(bm);
                view.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
            }
        } else {
            Drawable drawable = view.getResources().getDrawable(R.mipmap.place_loading);
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bm = bd.getBitmap();
            if (bm != null) {
                view.setBackground(bd);
                if (control) {
                    view.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
                }
            }
        }
    }

    /*zune: 如果图片加载不到的话，用http强制加载**/
    private void loadUrlFromHttp(String url, View view, boolean control, boolean isPath, boolean withoutDefault, int position) {
        //1.创建一个okhttpclient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request.Builder对象，设置参数，请求方式如果是Get，就不用设置，默认就是Get
        Request request = new Request.Builder()
                .url(url)
                .build();
        //3.创建一个Call对象，参数是request对象，发送请求
        Call call = okHttpClient.newCall(request);
        //4.异步请求，请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("zune", "run: onFailure = " + e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源，转换成我们想要的类型
                byte[] pictureBt = response.body().bytes();
                File file = putLocalCache(url, pictureBt);
                BaseApplication.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        File localCache = GlideUtils.getInstance().getLocalCache(BaseApplication.getInstance(), url);
                        if (localCache == null) {
                            setFileToView(file, view, control);
                        } else {
                            setFileToView(localCache, view, control);
                        }
                    }
                });
            }
        });
    }

    public void setFileToView(File file, View view, boolean control) {
        Context context = view.getContext();
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        if (view instanceof GifImageView) {
            try {
                GifDrawable gifFromUri = new GifDrawable(file);
                ((GifImageView) view).setImageDrawable(gifFromUri);
                if (control) {
                    view.getLayoutParams().height = (int) ((float) gifFromUri.getIntrinsicHeight() / gifFromUri.getIntrinsicWidth() * ScreenUtils.getScreenWidth());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (view instanceof ImageView) {
            Bitmap bm = BitmapFactory.decodeFile(file.getPath());
            if (bm != null) {
                ((ImageView) view).setImageBitmap(bm);
                if (control) {
                    view.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
                }
            }
        } else {
            Bitmap bm = BitmapFactory.decodeFile(file.getPath());
            if (bm != null) {
                view.setBackground(new BitmapDrawable(bm));
                if (control) {
                    view.getLayoutParams().height = (int) ((float) bm.getHeight() / bm.getWidth() * ScreenUtils.getScreenWidth());
                }
            }
        }
    }

    private File reSaveFile(File src) {
        if (src == null) {
            return null;
        }
        long length = src.length();
        if (!src.exists()) {
            return src;
        }
        String srcName = src.getName();
        String parent = src.getParent();
        File tempDir = new File(CustomGlideModule.directory + separator + "temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File temp = new File(tempDir, src.getName());
        if (!temp.exists()) {
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //实现压缩，并重新生成BitMap对象
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(src.getAbsolutePath(), newOpts);
        float w = newOpts.outWidth;
        int scaleWidth = (int) (w / ScreenUtils.getScreenWidth() + 1);
        if (scaleWidth <= 1) {
            scaleWidth = 1;
        }
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        newOpts.inSampleSize = scaleWidth;// 设置缩放比例, 以宽度为基准
        newOpts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(src.getAbsolutePath(), newOpts);
        try {
            FileOutputStream out = new FileOutputStream(temp);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            //已将压缩图片保存为temp文件
            out.flush();
            out.close();
            bos.flush();
            bos.close();
            boolean delete = src.delete();
            File dest = new File(parent, srcName);
            boolean b = temp.renameTo(dest);
            /*Log.i("zune:", "resaveFile: delete = " + delete + ", rename = " + b + ", 原bitmap w = " + w + ", 压缩后bitmap w = " + bitmap.getWidth()
                    + "压缩前的length = " + length
                    + "srcLength = " + dest.length());*/
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return src;
    }

}
