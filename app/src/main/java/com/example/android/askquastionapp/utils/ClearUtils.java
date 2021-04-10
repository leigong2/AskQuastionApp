package com.example.android.askquastionapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.views.ListDialog;

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

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.Q;
import static com.example.android.askquastionapp.utils.DocumentsFileUtils.OPEN_DOCUMENT_TREE_CODE;

public class ClearUtils {
    private static ClearUtils utils;
    private boolean isDeleting;
    private static Set<String> packageNames = new HashSet<>();
    public static final boolean requestLegacyExternalStorage = true;

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

    public void getAppProcessName(Context context) {
        //当前应用pid
        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
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

    public void delete(LifecycleOwner lifecycle, final String path, Observer<List<String>> observer) {
        if (Build.VERSION.SDK_INT == Q && !ClearUtils.requestLegacyExternalStorage) {
            Uri currentTreeUri = DocumentsFileUtils.getInstance().getCurrentTreeUri();
            if (currentTreeUri == null) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                if (lifecycle instanceof Activity) {
                    ((Activity) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
                } else if (lifecycle instanceof Fragment) {
                    ((Fragment) lifecycle).startActivityForResult(intent, OPEN_DOCUMENT_TREE_CODE);
                }
            } else {
                Context context = lifecycle instanceof Activity ? (Activity) lifecycle : ((Fragment) lifecycle).getContext();
                if (context == null) {
                    context = BaseApplication.getInstance().getBaseContext();
                }
                deleteFileDirQ(context, DocumentFile.fromTreeUri(context, currentTreeUri));
            }
            return;
        }
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

    public void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode != OPEN_DOCUMENT_TREE_CODE || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uriDir = data.getData();
        if (uriDir == null) {
            return;
        }
        deleteFileDirQ(context, DocumentFile.fromTreeUri(context, uriDir));
    }

    private void deleteFileDirQ(Context context, DocumentFile documentFile) {
        ListDialog<ListDialog.BaseData> listDialog = ListDialog.showDialog((FragmentActivity) context, true);
        if (documentFile == null) {
            return;
        }
        Observable.just(documentFile).map(new Function<DocumentFile, List<String>>() {
            @Override
            public List<String> apply(DocumentFile documentFile) throws Exception {
                List<String> temp = new ArrayList<>();
                deleteFileQ(documentFile, temp);
                LogUtils.i("zune: ", "不知不觉删除了：" + temp.size());
                return temp;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>, ListDialog<ListDialog.BaseData>>(listDialog, false) {
                    @Override
                    public void onNext(List<String> strings, ListDialog<ListDialog.BaseData> listDialog) {
                        List<ListDialog.BaseData> datas = new ArrayList<>();
                        for (String s : strings) {
                            ListDialog.BaseData data = new ListDialog.BaseData(s);
                            datas.add(data);
                        }
                        ToastUtils.showShort("删除完成");
                        listDialog.showWithData(datas, false);
                    }
                });
    }

    private void deleteFileQ(@Nullable DocumentFile documentFile, List<String> temp) {
        if (documentFile == null) {
            return;
        }
        List<String> noDelete = new ArrayList<>();
        noDelete.add("Android");
        addExtra(noDelete);
        noDelete.add("Bing");
        noDelete.add("DCIM");
        noDelete.add("Documents");
        noDelete.add("Download");
        noDelete.add("Downloads");
        noDelete.add("Picture");
        noDelete.add("Pictures");
        noDelete.add("Music");
        noDelete.add("Video");
        noDelete.add("QQBrowser");
        noDelete.add("Telegram");
        noDelete.add("tencent");
        if (documentFile.isDirectory()) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            if (documentFiles.length == 0) {
                documentFile.delete();
                String[] path = documentFile.getUri().getPath().split(":");
                temp.add(path[path.length - 1]);
                boolean delete = documentFile.delete();
                LogUtils.i("zune: ", path[path.length - 1] + "：delete = " + delete);
                return;
            }
            for (DocumentFile file : documentFiles) {
                if (file.isDirectory() && !noDelete.contains(file.getName())) {
                    if (file.listFiles().length == 0) {
                        file.delete();
                        String[] path = file.getUri().getPath().split(":");
                        temp.add(path[path.length - 1]);
                        boolean delete = file.delete();
                        deleteFileQ(file.getParentFile(), temp);
                        LogUtils.i("zune: ", path[path.length - 1] + "：delete = " + delete);
                    } else {
                        deleteFileQ(file, temp);
                    }
                } else if (file.isFile()) {
                    String[] path = file.getUri().getPath().split(":");
                    temp.add(path[path.length - 1]);
                    boolean delete = file.delete();
                    deleteFileQ(file.getParentFile(), temp);
                    LogUtils.i("zune: ", path[path.length - 1] + "：delete = " + delete);
                }
            }
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
                addExtra(noDelete);
                noDelete.add("Bing");
                noDelete.add("DCIM");
                noDelete.add("Documents");
                noDelete.add("Download");
                noDelete.add("Picture");
                noDelete.add("Pictures");
                noDelete.add("Music");
                noDelete.add("Video");
                noDelete.add("QQBrowser");
                noDelete.add("Telegram");
                noDelete.add("tencent");
                for (int i = 0; i < files.length; i++) {
                    File tempFile = files[i];
                    String tempFileName = tempFile.getName();
                    if (noDelete.contains(tempFileName)) {
                        if ("tencent".equalsIgnoreCase(tempFileName)) {
                            continue;
                        }
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
                            continue;
                        }
                        File[] tempList = tempFile.listFiles();
                        if (tempList != null) {
                            for (File listFile : tempList) {
                                if (listFile.isHidden()) {
                                    delete(listFile.getPath());
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

    private void addExtra(List<String> noDeleteFiles) {
        if (PhoneUtils.isXiaomi()) {
            noDeleteFiles.add("MIUI");
        }
        if (PhoneUtils.isHuaWei()) {
            noDeleteFiles.add("Huawei");
        }
    }
}
