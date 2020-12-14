package com.example.android.askquastionapp.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.jsoup.GsonGetter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static android.content.Context.CAMERA_SERVICE;

public class CameraV2Manager {
    private static CameraV2Manager sCameraV2Manager;
    private Handler mHandler;
    private TextureView mTextureView;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewBuilder;
    private MediaRecorder mMediaRecorder;
    private CameraCaptureSession mSession;
    private boolean isFont;

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
        File file = new File(BaseApplication.getInstance().getExternalCacheDir(), System.currentTimeMillis() + ".mp4");

        //Call this only before setOutputFormat().
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        //Call this after setAudioSource()/setVideoSource() but before prepare().
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        //Call this after setOutputFormat() and before prepare().
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        //Must be called after setVideoSource(). Call this after setOutFormat() but before prepare().
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);//设置比特率 一般是 1*分辨率 到 10*分辨率 之间波动。比特率越大视频越清晰但是视频文件也越大。
        mMediaRecorder.setVideoFrameRate(16);//设置帧数 选择 16即可， 过大帧数也会让视频文件更大当然也会更流畅，但是没有多少实际提升。人眼极限也就30帧了。

        //Must be called after setVideoSource(). Call this after setOutFormat() but before prepare()., 默认相机是横屏打开，所以视频宽度是屏幕高度，视频高度是屏幕宽度
        Size matchingSize = getMatchingSize();
        mMediaRecorder.setVideoSize(matchingSize.getWidth(), matchingSize.getHeight());

        mMediaRecorder.setOrientationHint(90); // 输出旋转90度，保持竖屏录制

        //Call this after setOutputFormat() but before prepare().
        mMediaRecorder.setPreviewDisplay(new Surface(mTextureView.getSurfaceTexture()));

        //Call this after setOutputFormat() but before prepare().
        mMediaRecorder.setOutputFile(file.getAbsolutePath());

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                // 发生错误，停止录制
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        });
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算需要的使用的摄像头分辨率
     *
     * @return
     */
    private Size getMatchingSize() {
        int deviceWidth = ScreenUtils.getScreenWidth();
        int deviceHeight = ScreenUtils.getScreenHeight();
        try {
            CameraManager cameraManager = (CameraManager) mTextureView.getContext().getSystemService(CAMERA_SERVICE);
            String cameraId = getCameraId(cameraManager);
            if (cameraId == null) {
                return null;
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap == null) {
                return null;
            }
            Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            int offSize = deviceWidth + deviceHeight;
            Size resultSize = null;
            /*zune：找出与给定的相机分辨率最接近的那一个**/
            for (Size size : sizes) {
                int width = size.getWidth();
                int height = size.getHeight();
                int offBig = Math.max(deviceWidth, deviceHeight) - Math.max(width, height);
                int offSmall = Math.min(deviceWidth, deviceHeight) - Math.min(width, height);
                if (offBig < 0 || offSmall < 0) {
                    /*zune：比手机分辨率大的直接舍弃**/
                    continue;
                }
                if (offSize > offBig + offSmall) {
                    /*zune：发现更合适的分辨率，就替换之**/
                    resultSize = size;
                    offSize = offBig + offSmall;
                }
            }
            return resultSize;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return new Size(deviceWidth, deviceHeight);
    }

    private String getCameraId(CameraManager cameraManager) {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();//获取可用相机列表
            for (String cameraId : cameraIdList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);//得到当前id的摄像头描述特征
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING); //获取摄像头的方向特征信息
                if (!isFont && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) { //这里选择了后摄像头
                    return cameraId;
                }
                if (isFont && facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
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

    public void switchCamera() {
        if (mTextureView != null && mTextureView.isAvailable()) {
            isFont = !isFont;
            if (mCamera != null) {
                mCamera.close();
            }
            if (mSession != null) {
                mSession.close();
            }
            openCamera();
        }
    }

    public void startRecord() {
        try {
            mSession.stopRepeating();//停止预览，准备切换到录制视频
            mSession.close();//关闭预览的会话，需要重新创建录制视频的会话
            mSession = null;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        initRecordConfig();
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        Size matchingSize = getMatchingSize();
        surfaceTexture.setDefaultBufferSize(matchingSize.getWidth(), matchingSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        Surface recorderSurface = mMediaRecorder.getSurface();//从获取录制视频需要的Surface
        try {
            mPreviewBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewBuilder.addTarget(previewSurface);
            mPreviewBuilder.addTarget(recorderSurface);
            //请注意这里设置了Arrays.asList(previewSurface,recorderSurface) 2个Surface，很好理解录制视频也需要有画面预览，第一个是预览的Surface，第二个是录制视频使用的Surface
            mCamera.createCaptureSession(Arrays.asList(previewSurface, recorderSurface), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();
    }

    public void pauseRecord() {

    }

    public void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
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
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        }
    }

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            LogUtils.i("zune: ", "onSurfaceTextureAvailable = " + surfaceTexture);
            openCamera();
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

    private void openCamera() {
        //CameraManaer 摄像头管理器，用于检测摄像头，打开系统摄像头
        CameraManager cameraManager = (CameraManager) mTextureView.getContext().getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = getCameraId(cameraManager);
            if (cameraId == null) {
                return;
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);//获取某个相机(摄像头特性)
            cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);//检查支持
            if (ActivityCompat.checkSelfPermission(mTextureView.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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
            mSession = session;
            LogUtils.i("zune: ", "相机创建成功");
            try {
                mSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//拍照
                mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//返回结果
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
