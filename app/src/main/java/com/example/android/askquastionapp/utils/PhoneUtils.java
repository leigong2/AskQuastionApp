package com.example.android.askquastionapp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

public class PhoneUtils {

    @SuppressLint("MissingPermission")
    public static boolean isFeatures(Context context) {
        //判断是否是模拟器
        String serial = Build.SERIAL;
        return
                /**zune: 软件判断，判断系统相关**/
                Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.toLowerCase().contains("vbox")
                        || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || "unknown".equalsIgnoreCase(serial)
                        || "android".equalsIgnoreCase(serial)
                        || Build.MODEL.contains("Android SDK built for x86")
                        || Build.MANUFACTURER.contains("Genymotion")
                        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                        || "google_sdk".equals(Build.PRODUCT)
                        || ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                        .getNetworkOperatorName().toLowerCase().equals("android")
                        /**zune: 硬件判断， 传感器，cpu，摄像头**/
                        || ((SensorManager) context.getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_LIGHT) == null
                        || readCpuInfo().contains("amd") || readCpuInfo().contains("intel")
                        || !hasCamera();
    }

    private static boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查设备是否有摄像头
     *
     * @return true, 有相机；false,无相机
     */
    public static boolean hasCamera() {
        return hasBackFacingCamera() || hasFrontFacingCamera();
    }

    /**
     * 检查设备是否有后置摄像头
     *
     * @returntrue,有后置摄像头；false,后置摄像头
     */
    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    /**
     * 检查设备是否有前置摄像头
     *
     * @returntrue,有前置摄像头；false,前置摄像头
     */
    public static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_BACK = 1;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    /*
     *作者:赵星海
     *时间:2019/2/21 17:58
     *用途:根据CPU是否为电脑来判断是否为模拟器(子方法)
     *返回:String
     */
    public static String readCpuInfo() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (Exception ignored) {
        }
        return result;
    }

    public static boolean isLocalChina() {
        String systemCountry;
        String systemLanguage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleListCompat listCompat = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            systemCountry = listCompat.get(0).getCountry();
            systemLanguage = listCompat.get(0).getLanguage();
        } else {
            systemCountry = Locale.getDefault().getCountry();
            systemLanguage = Locale.getDefault().getLanguage();
        }
        if (TextUtils.isEmpty(systemCountry)) {
            return "CN".equals(systemLanguage) || "cn".equals(systemLanguage)
                    || "ZH".equals(systemLanguage) || "zh".equals(systemLanguage);
        } else {
            return "ZH".equals(systemCountry) || "zh".equals(systemCountry)
                    || "CN".equals(systemCountry) || "cn".equals(systemCountry);
        }
    }

    public static String getLocalCountry() {
        String systemLanguage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleListCompat listCompat = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            systemLanguage = listCompat.get(0).getCountry();
        } else {
            systemLanguage = Locale.getDefault().getCountry();
        }
        return systemLanguage;
    }

    /**
     * 判断是否是刘海屏
     *
     * @return
     */
    public static boolean hasNotchScreen(Activity activity) {
        if (getInt("ro.miui.notch", activity) == 1 || hasNotchAtHuawei(activity) || hasNotchAtOPPO(activity)
                || hasNotchAtVivo(activity) || isAndroidP(activity) != null) {
            return true;
        }
        return false;
    }

    /**
     * Android P 刘海屏判断
     *
     * @param activity
     * @return
     */
    public static DisplayCutout isAndroidP(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (decorView != null && android.os.Build.VERSION.SDK_INT >= 28) {
            WindowInsets windowInsets = decorView.getRootWindowInsets();
            if (windowInsets != null)
                return windowInsets.getDisplayCutout();
        }
        return null;
    }

    /**
     * 小米刘海屏判断.
     *
     * @return 0 if it is not notch ; return 1 means notch
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static int getInt(String key, Activity activity) {
        int result = 0;
        if (isXiaomi()) {
            try {
                ClassLoader classLoader = activity.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
                //参数类型
                @SuppressWarnings("rawtypes")
                Class[] paramTypes = new Class[2];
                paramTypes[0] = String.class;
                paramTypes[1] = int.class;
                Method getInt = SystemProperties.getMethod("getInt", paramTypes);
                //参数
                Object[] params = new Object[2];
                params[0] = new String(key);
                params[1] = new Integer(0);
                result = (Integer) getInt.invoke(SystemProperties, params);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 是否是小米手机
    public static boolean isXiaomi() {
        return "Xiaomi".equals(Build.MANUFACTURER);
    }

    //是否是华为
    public static boolean isHuaWei() {
        //emuiApiLevel>0 即华为系统
        int emuiApiLevel = 0;
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getDeclaredMethod("get", new Class[]{String.class});
            emuiApiLevel = Integer.parseInt((String) method.invoke(cls, new Object[]{"ro.build.hw_emui_api_level"}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emuiApiLevel>0;
    }

    /**
     * 华为刘海屏判断
     *
     * @return
     */
    public static boolean hasNotchAtHuawei(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class HwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            LogUtils.i("zune:", "hasNotchAtHuawei ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            LogUtils.i("zune:", "hasNotchAtHuawei NoSuchMethodException");
        } catch (Exception e) {
            LogUtils.i("zune:", "hasNotchAtHuawei Exception");
        } finally {
            return ret;
        }
    }

    public static final int VIVO_NOTCH = 0x00000020;//是否有刘海
    public static final int VIVO_FILLET = 0x00000008;//是否有圆角

    /**
     * VIVO刘海屏判断
     *
     * @return
     */
    public static boolean hasNotchAtVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            LogUtils.i("zune:", "hasNotchAtVivo ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            LogUtils.i("zune:", "hasNotchAtVivo NoSuchMethodException");
        } catch (Exception e) {
            LogUtils.i("zune:", "hasNotchAtVivo Exception");
        } finally {
            return ret;
        }
    }

    /**
     * OPPO刘海屏判断
     *
     * @return
     */
    public static boolean hasNotchAtOPPO(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 判断摄像头是否可用
     * 主要针对6.0 的魅族，因为6.0的魅族，你拒绝了摄像头权限，它也会给你走onGranted
     * 以后有更好的方法的话可适当替换
     * https://blog.csdn.net/jm_beizi/article/details/51728495
     *
     * @return
     */
    public static boolean isCameraCanUse(Context context) {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
            // 对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
        }
        return canUse;
    }


    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return true;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return true;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess != null && appProcess.processName != null && appProcess.processName.equals(context.getPackageName())) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            }
        }
        return false;
    }
}
