package com.example.android.askquastionapp.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;

import androidx.core.content.FileProvider;

import java.io.File;

public class DownloadApkUtils {
    private File saveFile;
    private long downloadId = 0;
    DownloadReceiver mReceiver = new DownloadReceiver();
    private static DownloadApkUtils sDownloadApkUtils;

    private DownloadApkUtils() {
    }

    public static DownloadApkUtils getInstance() {
        if (sDownloadApkUtils == null) {
            synchronized (DownloadApkUtils.class) {
                if (sDownloadApkUtils == null) {
                    sDownloadApkUtils = new DownloadApkUtils();
                }
            }
        }
        return sDownloadApkUtils;
    }

    public void startDownload(Context context, String url, String path) {
        registerReceiver(context);
        initFile(context, path);
        if (downloadId != 0) {  //根据任务ID判断是否存在相同的下载任务，如果有则清除
            clearCurrentTask(context, downloadId);
        }
        downloadId = downLoadApk(context, url, path);
    }

    private void initFile(Context context, String path) {
        if (saveFile == null) {
            saveFile = new File(context.getExternalCacheDir(), path);
        }
    }

    public long downLoadApk(Context context, String url, String path) {
        // 得到系统的下载管理
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        // 以下两行代码可以让下载的apk文件被直接安装而不用使用Fileprovider,系统7.0或者以上才启动。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder localBuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(localBuilder.build());
        }
        DownloadManager.Request requestApk = new DownloadManager.Request(uri);
        // 设置在什么网络下下载
        requestApk.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 下载中和下载完后都显示通知栏
        requestApk.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (saveFile.exists()) {    //判断文件是否存在，存在的话先删除
            saveFile.delete();
        }
        requestApk.setDestinationUri(Uri.fromFile(saveFile));
        // 表示允许MediaScanner扫描到这个文件，默认不允许。
        requestApk.allowScanningByMediaScanner();
        // 设置下载中通知栏的提示消息
        requestApk.setTitle("正在下载...");
        // 设置设置下载中通知栏提示的介绍
        requestApk.setDescription(path);

        // 7.0以上的系统适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestApk.setRequiresDeviceIdle(false);
            requestApk.setRequiresCharging(false);
        }
        // 启动下载,该方法返回系统为当前下载请求分配的一个唯一的ID
        return manager.enqueue(requestApk);
    }

    /**
     * 下载前先移除前一个任务，防止重复下载
     *
     * @param downloadId
     */
    public void clearCurrentTask(Context mContext, long downloadId) {
        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            dm.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public void installApk(Context context) {
        downloadId = 0;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            String[] command = {"chmod", "777", saveFile.getAbsolutePath()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "com.example.android.askquastionapp.FileProvider", saveFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(saveFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        unRegisterReceiver(context);
    }

    public void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction("android.intent.action.DOWNLOAD_SERVICE");
        context.registerReceiver(mReceiver, filter);
    }

    public void unRegisterReceiver(Context context) {
        context.unregisterReceiver(mReceiver);
    }

    public static class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                DownloadApkUtils.getInstance().installApk(context);
            } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
                // 如果还未完成下载，用户点击Notification ，跳转到下载中心
                Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(viewDownloadIntent);
            }
        }
    }
}
