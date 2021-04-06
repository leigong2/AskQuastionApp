package com.example.android.askquastionapp.scan;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.android.askquastionapp.utils.ToastUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.scan.zxing.android.CaptureActivityHandler;
import com.example.android.askquastionapp.scan.zxing.android.InactivityTimer;
import com.example.android.askquastionapp.scan.zxing.bean.ZxingConfig;
import com.example.android.askquastionapp.scan.zxing.camear.CameraManager;
import com.example.android.askquastionapp.scan.zxing.common.Constant;
import com.example.android.askquastionapp.scan.zxing.view.ViewfinderView;
import com.example.android.askquastionapp.utils.BrowserUtils;

public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView previewView;
    private ViewfinderView viewFinderView;
    private ZxingConfig config;
    private CameraManager cameraManager;
    private SurfaceHolder surfaceHolder;
    private InactivityTimer inactivityTimer;
    private boolean hasSurface;
    private CaptureActivityHandler handler;

    public static void start(Activity activity, int requestCodeScan) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        ZxingConfig config = new ZxingConfig();
        //是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        config.setFullScreenScan(false);
        config.setShake(false);
        config.setFrameLineColor(R.color.colorTranslate1);
        config.setReactColor(R.color.colorPrimary);
        config.setScanLineColor(R.color.colorPrimary);
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        activity.startActivityForResult(intent, requestCodeScan);
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);
        previewView = findViewById(R.id.preview_view);
        viewFinderView = findViewById(R.id.viewfinder_view);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inactivityTimer.onResume();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, config, viewFinderView, cameraManager);
                handler.setCallBack(new CaptureActivityHandler.CallBack() {
                    @Override
                    public void onScanResult(String s) {
                        if (s == null) {
                            ToastUtils.showShort("扫描失败");
                            return;
                        } else {
                            ToastUtils.showShort("扫描成功，扫描结果已返回剪切板");
                        }
                        ClipboardManager clipboardManager = (ClipboardManager) BaseApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, s));
                        if (clipboardManager.getPrimaryClip() != null && clipboardManager.hasPrimaryClip()) {
                            clipboardManager.getPrimaryClip().getItemAt(0).getText();
                        }
                        if (s.startsWith("http")) {
                            BrowserUtils.goToBrowser(CaptureActivity.this, s);
                        }
                    }
                });
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        if (getIntent().getExtras() != null) {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        }
        if (config == null) {
            config = new ZxingConfig();
        }
        viewFinderView.setZxingConfig(config);
        cameraManager = new CameraManager(getApplication(), config);
        viewFinderView.setCameraManager(cameraManager);
        surfaceHolder = previewView.getHolder();
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        inactivityTimer.onDestry();
        viewFinderView.stopAnimator();
        cameraManager.closeDriver();
        super.onDestroy();
    }
}
