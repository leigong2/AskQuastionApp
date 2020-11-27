package com.example.android.askquastionapp.video.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.BaseApplication;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Camera2DelegateImp implements Camera2Delegate, SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private CaptureRequest mCaptureRequest;
    private MediaRecorder mMediaRecorder;
    private CameraDevice mCamera;

    public void setSurfaceView(SurfaceView mSurfaceView) {
        this.mSurfaceView = mSurfaceView;
        mSurfaceView.getHolder().addCallback(this);
        BaseApplication.getInstance().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpMediaRecorder();
            }
        }, 1000);
    }

    private void setUpMediaRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        //创建视频保存的文件地址
        File file = new File(BaseApplication.getInstance().getExternalCacheDir(), System.currentTimeMillis() + ".mp4");

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

        //Call this after setOutputFormat() but before prepare().
        mMediaRecorder.setOutputFile(file.getPath());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
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
        prepare();
    }

    private void prepare() {
        try {
            //This method must be called after setting up the desired audio and video sources, encoders, file format, etc., but before start()
            mMediaRecorder.prepare();
            Log.e("zune", "prepare: " + true);
        } catch (IOException e) {
            Log.e("zune", "error prepare video record:" + e.getMessage());
        }
        if (callBack != null) {
            callBack.onCallBack();
        }
    }

    public interface CallBack {
        void onCallBack();
    }

    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public void startPreview(@NotNull Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    try {
                        Camera2DelegateImp.this.onOpened(camera);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, BaseApplication.getInstance().getHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //session callback
    private CameraCaptureSession.StateCallback videoSessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d("zune ", " session onConfigured id:" + session.getDevice().getId());
            try {
                session.setRepeatingRequest(mCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                        super.onCaptureProgressed(session, request, partialResult);
                        Log.i("zune ", "onCaptureProgressed: " + partialResult);
                    }

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Log.i("zune ", "onCaptureCompleted: " + result);
                    }
                }, BaseApplication.getInstance().getHandler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            try {
                mMediaRecorder.start();
                onRecordStarted(true);
            } catch (RuntimeException e) {
                onRecordStarted(false);
                Log.e("zune ", "start record failed msg:" + e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d("zune ", "create session fail id:" + session.getDevice().getId());
        }
    };

    //session callback
    private CameraCaptureSession.StateCallback previewSessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d("zune ", " session onConfigured id:" + session.getDevice().getId());
            try {
                session.setRepeatingRequest(mCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                        super.onCaptureProgressed(session, request, partialResult);
                        Log.i("zune ", "onCaptureProgressed: " + partialResult);
                    }

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Log.i("zune ", "onCaptureCompleted: " + result);
                    }
                }, BaseApplication.getInstance().getHandler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d("zune ", "create session fail id:" + session.getDevice().getId());
        }
    };

    private void onOpened(@NotNull CameraDevice camera) throws CameraAccessException {
        mCamera = camera;
        Log.d("zune", "createCameraCaptureSession");
        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        Surface surface = mSurfaceView.getHolder().getSurface();
        builder.addTarget(surface);
        mCaptureRequest = builder.build();
        camera.createCaptureSession(Arrays.asList(surface), previewSessionStateCb, BaseApplication.getInstance().getHandler());
    }

    private void onStartRecord(@NotNull CameraDevice camera) throws CameraAccessException {
        Log.d("zune", "createCameraCaptureSession");
        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        Surface surface = mSurfaceView.getHolder().getSurface();
        builder.addTarget(surface);
        mCaptureRequest = builder.build();
        Surface recorderSurface = mMediaRecorder.getSurface();
        camera.createCaptureSession(Arrays.asList(surface), videoSessionStateCb, BaseApplication.getInstance().getHandler());
    }

    public void startRecord(Context context) {
        try {
            onStartRecord(mCamera);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecordStarted(boolean started) {

    }

    @Override
    public void onRecordStoped(Uri uri) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("zune ", "surfaceCreated: ");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
