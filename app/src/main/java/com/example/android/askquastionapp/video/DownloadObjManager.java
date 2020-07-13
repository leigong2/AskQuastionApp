package com.example.android.askquastionapp.video;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Notification.PRIORITY_MAX;
import static android.os.Build.VERSION_CODES.O;

public class DownloadObjManager {
    private static final String CHANNEL_ONE_ID = "download";
    private static DownloadObjManager sDownloadManager;

    private DownloadObjManager() {
    }

    public static DownloadObjManager getInstance() {
        if (sDownloadManager == null) {
            sDownloadManager = new DownloadObjManager();
        }
        return sDownloadManager;
    }

    public void startDownload(String url, String path, MainActivity.CallBack callBack) {
        if (new File(path).exists()) {
            new File(path).delete();
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) {
                    return;
                }
                float total = response.body().contentLength();
                if (total <= 0) {
                    onFailure(call, new IOException(""));
                }
                InputStream is = response.body().byteStream();
                FileOutputStream fos = new FileOutputStream(new File(path));
                int len = 0;
                byte[] buffer = new byte[1024];
                while (-1 != (len = is.read(buffer))) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
                is.close();
                if (callBack != null) {
                    callBack.onCallBack();
                }
            }
        });
    }

    public void startDownWithPosition(String url, String path) {
        if (url == null || !url.startsWith("http")) {
            return;
        }
        final File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            private int positionId = getPositionId(notifyIds);

            private int getPositionId(List<Integer> notifyIds) {
                if (notifyIds.isEmpty()) {
                    notifyIds.add(0, 0);
                    return 0;
                }
                for (int i = 0; i < notifyIds.size(); i++) {
                    if (i < notifyIds.get(i)) {
                        notifyIds.add(i, i);
                        return i;
                    }
                }
                int size = notifyIds.size();
                notifyIds.add(size);
                return size;
            }

            private float curPosition;

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.body() == null) {
                        return;
                    }
                    float total = response.body().contentLength();
                    if (total <= 0) {
                        onFailure(call, new IOException(""));
                    }
                    float curData = 0;
                    InputStream is = response.body().byteStream();
                    FileOutputStream fos = new FileOutputStream(file);
                    showNotify(BaseApplication.getInstance(), file.getName(), curPosition, positionId, String.format("(%sM/%sM)", 0, keepTwo((double) (total / 1024 / 1024))));
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    float temp = 0;
                    while (-1 != (len = is.read(buffer))) {
                        fos.write(buffer, 0, len);
                        curData += len;
                        temp += len;
                        float position = curData / total;
                        if (position < curPosition + 0.01f && temp < 1024 * 1024f) {
                            continue;
                        }
                        temp = 0;
                        if (position > curPosition) {
                            curPosition = (int) (position * 10000) / 10000f;
                        }
                        if (position == 1) {
                            curPosition = 1;
                        }
                        showNotify(BaseApplication.getInstance(), file.getName(), curPosition, positionId, String.format("(%sM/%sM)"
                                ,  keepTwo((double) (curData / 1024 / 1024)), keepTwo((double) (total / 1024 / 1024))));
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    showNotify(BaseApplication.getInstance(), file.getName(), curPosition, positionId, "(下载完毕)");
                    curPosition = 0;
                }
            }
        });
    }

    private String keepTwo(Double d) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance(new Locale("en", "US"));
        df.applyPattern("######0.##");
        return df.format(d);
    }

    private Notification notification;
    private NotificationManager notificationManager;
    private List<Integer> notifyIds = new ArrayList<>();

    private void showNotify(Context context, String name, float curProgress, int position, String append) {
        if (notification == null || curProgress == 0) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // 获取remoteViews（参数一：包名；参数二：布局资源）
            RemoteViews remoteViews;
            // 设置自定义的Notification内容
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= O) {
                builder = new Notification.Builder(context.getApplicationContext(), CHANNEL_ONE_ID);
                NotificationChannel channel = new NotificationChannel(CHANNEL_ONE_ID, "AskQuastionApp", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(CHANNEL_ONE_ID);
                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.download_controller);
                builder.setCustomHeadsUpContentView(remoteViews);
            } else {
                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.download_controller);
                builder = new Notification.Builder(context.getApplicationContext());
            }
            remoteViews.setTextViewText(R.id.tv_name, name);
            remoteViews.setTextViewText(R.id.tv_content, "正在下载...     " + (int) (curProgress * 10000) / 100f + "%" + append);
            Intent intent = new Intent();
            notification = builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContent(remoteViews)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(PRIORITY_MAX)
                    .build();
            notification.defaults = Notification.DEFAULT_SOUND;//设置为默认的声音
            notificationManager.notify(position, notification);
        } else {
            notification.contentView.setTextViewText(R.id.tv_name, name);
            notification.contentView.setTextViewText(R.id.tv_content, "正在下载...     " + (int) (curProgress * 10000) / 100f + "%" + append);
            notificationManager.notify(position, notification);
            if (curProgress == 1) {
                notificationManager.cancel(position);
                for (int i = 0; i < notifyIds.size(); i++) {
                    if (notifyIds.get(i) == position) {
                        notifyIds.remove(i);
                        break;
                    }
                }
            }
        }
    }
}
