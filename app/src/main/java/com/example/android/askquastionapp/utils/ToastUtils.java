package com.example.android.askquastionapp.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.lang.reflect.Field;

public class ToastUtils {

    //之前显示的内容
    private static String oldMsg;
    //第一次时间
    private static long oneTime = 0;
    //第二次时间
    private static long twoTime = 0;

    public static Toast showToast(Context context, int resId, int duration) {
        if (context == null) {
            context = BaseApplication.getInstance();
        }
        return showToast(context, context.getString(resId), duration);
    }

    public static Toast showToast(Context context, int resId) {
        if (context == null) {
            context = BaseApplication.getInstance();
        }
        return showToast(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    public static Toast showToast(Context context, CharSequence msg) {
        return showToast(context, msg, Toast.LENGTH_SHORT);
    }

    /**
     * 显示一个在中间的toast
     *
     * @param context
     * @param iconId
     * @param msg
     * @return
     */
    public static Toast showCenterToast(Context context, @DrawableRes int iconId, @NonNull CharSequence msg) {
        if (context == null) {
            context = BaseApplication.getInstance();
        }
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_center_toast, null);
        TextView textView = view.findViewById(android.R.id.message);
        textView.setText(msg);
        ImageView imageView = view.findViewById(R.id.icon);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(iconId);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        textView.setGravity(Gravity.CENTER);
        toast.setGravity(Gravity.CENTER, 0, 0);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            hook(toast);
        }
        try {
            toast.show();
        } catch (Exception ignore) {
        }
        return toast;
    }

    public static Toast showToast(final Context context, final CharSequence msg, final int duration) {
        if (isMainThread()) {
            return realShowToast(context, duration, msg);
        } else {
            BaseApplication.getInstance().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    realShowToast(context, duration, msg);
                }
            });
            return null;
        }
    }

    public static void showShort(String msg) {
        showToast(BaseApplication.getInstance(), msg, Toast.LENGTH_SHORT);
    }

    public static void showLong(String msg) {
        showToast(BaseApplication.getInstance(), msg, Toast.LENGTH_LONG);
    }

    /**
     * Return whether the thread is the main thread.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static Toast realShowToast(Context context, final int duration, final CharSequence message) {
        if (context == null) {
            context = BaseApplication.getInstance();
        }
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.transient_notification, null);
        TextView textView = view.findViewById(android.R.id.message);
        textView.setText(message);
        toast.setDuration(duration);
        toast.setView(view);
        textView.setGravity(Gravity.CENTER);
        int height = BaseApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
        toast.setGravity(Gravity.BOTTOM, 0, (int) (height * 13.0 / 100));
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            hook(toast);
        }
        try {
            toast.show();
        } catch (Exception ignore) {
        }
        return toast;
    }

    private static Toast realShowToast(Context context, final int duration, final CharSequence message, @DrawableRes int drawableId) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.transient_notification, null);
        TextView textView = view.findViewById(android.R.id.message);
        if (drawableId != 0) {
            TextDrawableUtil.addDrawableTop(textView, drawableId);
            textView.getLayoutParams().width = DensityUtil.dp2px(200);
            textView.setPadding(DensityUtil.dp2px(33), DensityUtil.dp2px(24)
                    , DensityUtil.dp2px(33), DensityUtil.dp2px(24));
            textView.setCompoundDrawablePadding(DensityUtil.dp2px(16));
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        textView.setText(message);
        toast.setDuration(duration);
        toast.setView(view);
        textView.setGravity(Gravity.CENTER);
        if (drawableId == 0) {
            int height = BaseApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
            toast.setGravity(Gravity.TOP, 0, (int) (height * 13.0 / 100));
        } else {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            hook(toast);
        }
        try {
            toast.show();
        } catch (Exception ignore) {
        }
        return toast;
    }

    /**
     * fix a bug while show toast on api level 25
     *
     * @See ｛Link https://blog.csdn.net/mylike_45/article/details/89637861｝
     */
    private static Field sField_TN;
    private static Field sField_TN_Handler;

    static {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            try {
                sField_TN = Toast.class.getDeclaredField("mTN");
                sField_TN.setAccessible(true);

                sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
                sField_TN_Handler.setAccessible(true);
            } catch (Exception e) {
            }
        }
    }

    private static void hook(Toast toast) {
        try {
            Object tn = sField_TN.get(toast);
            Handler preHandler = (Handler) sField_TN_Handler.get(tn);
            sField_TN_Handler.set(tn, new SafelyHandlerWarpper(preHandler));
        } catch (Exception e) {
        }
    }

    private static class SafelyHandlerWarpper extends Handler {

        private Handler impl;

        public SafelyHandlerWarpper(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                impl.handleMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    /* fix end */

    public static void showLiveToast(Context context, @DrawableRes int drawableId, @StringRes int resId) {
        if (isMainThread()) {
            showLiveToast(context, drawableId, context.getString(resId), Toast.LENGTH_LONG);
        } else {
            BaseApplication.getInstance().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    showLiveToast(context, drawableId, context.getString(resId), Toast.LENGTH_LONG);
                }
            });
        }
    }

    public static void showLiveToast(Context context, int drawableId, String text) {
        if (isMainThread()) {
            showLiveToast(context, drawableId, text, Toast.LENGTH_LONG);
        } else {
            BaseApplication.getInstance().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    showLiveToast(context, drawableId, text, Toast.LENGTH_LONG);
                }
            });
        }
    }

    private static void showLiveToast(Context context, int drawableId, String text, int duration) {
        realShowToast(context, duration, text, drawableId);
    }
}
