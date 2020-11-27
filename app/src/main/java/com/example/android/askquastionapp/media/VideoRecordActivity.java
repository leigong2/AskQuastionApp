package com.example.android.askquastionapp.media;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.jsoup.GsonGetter;

import java.io.File;
import java.util.Arrays;

public class VideoRecordActivity extends AppCompatActivity {

    private static final String TAG = "zune: ";
    private TextureView textureView;
    private MediaRecorder mMediaRecorder;
    private HandlerThread mThreadHandler;
    private Handler mHandler;
    private CaptureRequest.Builder mPreviewBuilder;

    public static void start(Context context, boolean showButton) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        intent.putExtra("showButton", showButton);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        textureView = findViewById(R.id.texture_view);
        boolean showButton = getIntent().getBooleanExtra("showButton", false);
        findViewById(R.id.start_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.pause_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.stop_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.restart_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.start_record).setOnClickListener(v -> startRecord());
        findViewById(R.id.pause_record).setOnClickListener(v -> pauseRecord());
        findViewById(R.id.stop_record).setOnClickListener(v -> stopRecord());
        findViewById(R.id.restart_record).setOnClickListener(v -> restartRecord());
        mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
        textureView.setSurfaceTextureListener(textureListener);
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.e(TAG, "可用");
            //CameraManaer 摄像头管理器，用于检测摄像头，打开系统摄像头
            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            try {
                String[] CameraIdList = cameraManager.getCameraIdList();//获取可用相机列表
                Log.e(TAG, "可用相机的个数是:" + CameraIdList.length);
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(CameraIdList[0]);//获取某个相机(摄像头特性)
                cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);//检查支持

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraManager.openCamera(CameraIdList[0], mCameraDeviceStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.e(TAG, "改变" + "i = " + i + "i1 = " + i1);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e(TAG, "释放");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            Log.e(TAG, "更新");
        }
    };
    private CameraDevice mCamera;
    //CameraDeviceandroid.hardware.Camera也就是Camera1的Camera
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                mCamera = camera;
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
        }
    };

    /**
     * @param camera
     * @throws CameraAccessException 开始预览
     */
    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        Surface surface = new Surface(texture);
        try {
            //CameraRequest表示一次捕获请求，用来对z照片的各种参数设置，比如对焦模式、曝光模式等。CameraRequest.Builder用来生成CameraRequest对象
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        camera.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
    }

    //CameraCaptureSession 这个对象控制摄像头的预览或者拍照
    //setRepeatingRequest()开启预览，capture()拍照
    //StateCallback监听CameraCaptureSession的创建
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.e(TAG, "相机创建成功！");
            try {
                session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//拍照
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//返回结果
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "这里异常");
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e(TAG, "相机创建失败！");
        }
    };

    //CameraCaptureSession.CaptureCallback监听拍照过程
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            Log.e(TAG, "这里接受到数据" + GsonGetter.getInstance().getGson().toJson(result));
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {

        }
    };

    private void initRecordConfig() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        //创建视频保存的文件地址
        File file = new File(BaseApplication.getInstance().getExternalCacheDir(), System.currentTimeMillis() + ".mp");

        //Call this only before setOutputFormat().
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        //Call this after setAudioSource()/setVideoSource() but before prepare().
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        //Call this after setOutputFormat() and before prepare().
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        //Must be called after setVideoSource(). Call this after setOutFormat() but before prepare().
        mMediaRecorder.setVideoSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        //Must be called after setVideoSource(). Call this after setOutFormat() but before prepare().
        mMediaRecorder.setVideoFrameRate(4);

        mMediaRecorder.setPreviewDisplay(new Surface(textureView.getSurfaceTexture()));

        //Call this after setOutputFormat() but before prepare().
        mMediaRecorder.setOutputFile(file.getPath());

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                // 发生错误，停止录制
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        });

        //Call this method before prepare().
        mMediaRecorder.setVideoEncodingBitRate(1024 * 1024);

        int rotation = Surface.ROTATION_0;
        mMediaRecorder.setOrientationHint(rotation);
    }

    private void startRecord() {

    }

    private void pauseRecord() {

    }

    private void stopRecord() {

    }

    private void restartRecord() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.close();
        }
    }
}
