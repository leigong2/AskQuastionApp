package com.example.android.askquastionapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ClearUtils {
    private static ClearUtils utils;
    private boolean isDeleting;
    private static Set<String> packageNames = new HashSet<>();

    private ClearUtils() {
    }

    public static ClearUtils getInstance() {
        if (utils == null) {
            synchronized (ClearUtils.class) {
                if (utils == null) {
                    utils = new ClearUtils();
                }
            }
        }
        return utils;
    }

    public  void getAppProcessName(Context context) {
        //当前应用pid
        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i <apps.size() ; i++) {
            String name = apps.get(i).activityInfo.packageName;
            packageNames.add(name);
        }
    }

    public void getFiles(final String path, Observer<List<String>> observer) {
        Observable<List<String>> observable = Observable.just(1).map(new Function<Integer, List<String>>() {
            @Override
            public List<String> apply(Integer integer) throws Exception {
                List<String> temp = new ArrayList<>();
                getPath(temp, path);
                return temp;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        if (observer != null) {
            observable.subscribe(observer);
        }
    }

    private void getPath(List<String> temp, String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            if (file.listFiles().length > 0) {
                for (int i = 0; i < file.listFiles().length; i++) {
                    File child = file.listFiles()[i];
                    getPath(temp, child.getPath());
                }
            } else {
                temp.add(file.getPath());
            }
        }
        if (file.exists() && file.isFile()) {
            temp.add(file.getPath());
        }
    }

    public void delete(final String path, Observer<List<String>> observer) {
        Observable<List<String>> observable = Observable.just(1).map(new Function<Integer, List<String>>() {
            @Override
            public List<String> apply(Integer integer) throws Exception {
                List<String> temp = new ArrayList<>();
                deleteFileDir(new File(path), temp, false);
                Log.e("zune", String.format("\"不知不觉删除了%s个文件\"", temp.size()));
                SystemClock.sleep(1000);
                return temp;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        if (observer != null) {
            observable.subscribe(observer);
        }
    }

    public void delete(String path) {
        if (isDeleting) {
            return;
        }
        isDeleting = true;
        Observable.just(1).map(new Function<Integer, List<String>>() {
            @Override
            public List<String> apply(Integer integer) throws Exception {
                List<String> temp = new ArrayList<>();
                deleteFileDir(new File(path), temp, true);
                SystemClock.sleep(1000);
                return temp;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<String> strings) {
                        Log.e("zune", String.format("\"不知不觉删除了%s个文件\"", strings.size()));
                        strings.clear();
                        isDeleting = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void deleteFileDir(File file, List<String> temp, boolean focus) {
        if (focus && file.exists()) {
            deleteAll(file, temp);
            return;
        }
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                file.delete();
            } else {
                List<String> noDelete = new ArrayList<>();
                noDelete.add("Android");
                noDelete.add("Bing");
                noDelete.add("DCIM");
                noDelete.add("Documents");
                noDelete.add("Download");
                noDelete.add("Picture");
                noDelete.add("Pictures");
                noDelete.add("Music");
                noDelete.add("QQBrowser");
                noDelete.add("Telegram");
                noDelete.add("tencent");
                for (int i = 0; i < files.length; i++) {
                    File tempFile = files[i];
                    String tempFileName = tempFile.getName();
                    if (noDelete.contains(tempFileName)) {
                        if ("Android".equals(tempFileName)) {
                            File[] listFiles = tempFile.listFiles();
                            if (listFiles != null) {
                                for (File listFile : listFiles) {
                                    if (!"data".equals(listFile.getName())) {
                                        delete(listFile.getPath());
                                    } else {
                                        File[] datasFile = listFile.listFiles();
                                        if (datasFile != null) {
                                            for (File datas : datasFile) {
                                                if (!packageNames.contains(datas.getName())) {
                                                    delete(datas.getPath());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    deleteAll(tempFile, temp);
                }
            }
        }
    }

    private void deleteAll(File file, List<String> temp) {
        if (file.exists() && file.isFile()) {
            boolean delete = file.delete();
            temp.add(file.getAbsolutePath());
        } else if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                boolean delete = file.delete();
                temp.add(file.getAbsolutePath());
            }
            for (int i = 0; i < files.length; i++) {
                deleteAll(files[i], temp);
            }
            boolean delete = file.delete();
            temp.add(file.getAbsolutePath());
        }
    }
}
