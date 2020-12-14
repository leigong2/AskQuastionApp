package com.example.android.askquastionapp.media;

import android.Manifest;
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
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.jsoup.GsonGetter;

import java.io.File;
import java.util.Arrays;

import static android.content.Context.CAMERA_SERVICE;

public class CameraV2Manager {
    private static CameraV2Manager sCameraV2Manager;
    private Handler mHandler;
    private TextureView mTextureView;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewBuilder;
    private MediaRecorder mMediaRecorder;

    private CameraV2Manager() {
    }

    public static CameraV2Manager getInstance() {
        if (sCameraV2Manager == null) {
            sCameraV2Manager = new CameraV2Manager();
        }
        return sCameraV2Manager;
    }

    public void init(@NonNull TextureView textureView) {
        mHandler = new Handler(Looper.getMainLooper());
        mTextureView = textureView;
        textureView.setSurfaceTextureListener(mTextureListener);
    }

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

        mMediaRecorder.setPreviewDisplay(new Surface(mTextureView.getSurfaceTexture()));

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

    /**
     * @param camera
     * @throws CameraAccessException 开始预览
     */
    private void startPreview(CameraDevice camera) throws CameraAccessException {
        if (mTextureView == null) {
            return;
        }
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
        Surface surface = new Surface(texture);
        try {
            //CameraRequest表示一次捕获请求，用来对z照片的各种参数设置，比如对焦模式、曝光模式等。CameraRequest.Builder用来生成CameraRequest对象
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        camera.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
    }

    public void startRecord() {

    }

    public void pauseRecord() {

    }

    public void stopRecord() {

    }

    public void restartRecord() {

    }

    public void release() {
        if (mCamera != null) {
            mCamera.close();
        }
        if (mHandler != null) {
            mHandler.removeMessages(0);
        }
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            if (surfaceTexture != null) {
                surfaceTexture.release();
            }
            mTextureView = null;
        }
    }

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            LogUtils.i("zune: ", "onSurfaceTextureAvailable = " + surfaceTexture);
            //CameraManaer 摄像头管理器，用于检测摄像头，打开系统摄像头
            CameraManager cameraManager = (CameraManager) mTextureView.getContext().getSystemService(CAMERA_SERVICE);
            try {
                String[] CameraIdList = cameraManager.getCameraIdList();//获取可用相机列表
                LogUtils.i("zune: ", "可用相机的个数是 = " + CameraIdList.length);
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(CameraIdList[0]);//获取某个相机(摄像头特性)
                cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);//检查支持

                if (ActivityCompat.checkSelfPermission(mTextureView.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraManager.openCamera(CameraIdList[0], mCameraDeviceStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            LogUtils.i("zune: ", "改变大小 width = " + width + ", height = " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            LogUtils.i("zune: ", "onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            LogUtils.i("zune: ", "onSurfaceTextureUpdated");
        }
    };

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
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

    //CameraCaptureSession 这个对象控制摄像头的预览或者拍照
    //setRepeatingRequest()开启预览，capture()拍照
    //StateCallback监听CameraCaptureSession的创建
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            LogUtils.i("zune: ", "相机创建成功");
            try {
                session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//拍照
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//返回结果
            } catch (CameraAccessException e) {
                e.printStackTrace();
                LogUtils.i("zune: ", "CameraAccessException = " + GsonGetter.getInstance().getGson().toJson(e));
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            LogUtils.i("zune: ", "onConfigureFailed = " + GsonGetter.getInstance().getGson().toJson(session));
        }
    };

    //CameraCaptureSession.CaptureCallback监听拍照过程
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            LogUtils.i("zune: ", "onCaptureCompleted, CaptureRequest = " + GsonGetter.getInstance().getGson().toJson(request));
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            LogUtils.i("zune: ", "onCaptureProgressed, CaptureRequest = " + GsonGetter.getInstance().getGson().toJson(request));
        }
    };
}
