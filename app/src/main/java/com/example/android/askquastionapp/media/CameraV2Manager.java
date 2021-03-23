package com.example.android.askquastionapp.media;

import android.Manifest;
import android.app.Activity;
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
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.jsoup.GsonGetter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
    private ImageReader mImageReader;
    private Surface mPreviewSurface;

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
        File file = new File(Environment.getExternalStorageDirectory() + String.format("/DCIM/Camera/%s.mp4", getNameByTime()));

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
        Size matchingSize = getMatchingSize(false);
        if (matchingSize != null) {
            mMediaRecorder.setVideoSize(matchingSize.getWidth(), matchingSize.getHeight());
        }

        mMediaRecorder.setOrientationHint(isFont ? 270 : 90); // 输出旋转90度，保持竖屏录制

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
    private Size getMatchingSize(boolean byCamera) {
        try {
            CameraManager cameraManager = (CameraManager) mTextureView.getContext().getSystemService(CAMERA_SERVICE);
            String cameraId = getCameraId(cameraManager);
            if (cameraId == null) {
                return null;
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size deviceSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            if (streamConfigurationMap == null) {
                return null;
            }
            int deviceWidth = ScreenUtils.getScreenWidth();
            int deviceHeight = ScreenUtils.getScreenHeight();
            if (byCamera && deviceSize != null) {
                deviceWidth = Math.min(deviceSize.getWidth(), deviceSize.getHeight());
                deviceHeight = Math.max(deviceSize.getWidth(), deviceSize.getHeight());
            }
            if (onLogListener != null) {
                onLogListener.writeLog("currentSize = " + deviceWidth + ", " + deviceHeight);
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
            if (onLogListener != null) {
                onLogListener.writeLog("resultSize = " + (resultSize == null ? null : resultSize.getWidth() + ", " + resultSize.getHeight()));
            }
            return resultSize;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
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
        setupImageReader();
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        Size matchingSize = getMatchingSize(false);
        if (matchingSize == null) {
            return;
        }
        int width = matchingSize.getWidth();
        int height = matchingSize.getHeight();
        texture.setDefaultBufferSize(width, height);
        ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
        layoutParams.width = ScreenUtils.getScreenWidth();
        layoutParams.height = (int) (1f * Math.max(width, height) / Math.min(width, height) * ScreenUtils.getScreenWidth());
        mPreviewSurface = new Surface(texture);
        try {
            //CameraRequest表示一次捕获请求，用来对z照片的各种参数设置，比如对焦模式、曝光模式等。CameraRequest.Builder用来生成CameraRequest对象
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(mPreviewSurface);
        camera.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), mSessionStateCallback, mHandler);
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
        Size matchingSize = getMatchingSize(false);
        surfaceTexture.setDefaultBufferSize(matchingSize.getWidth(), matchingSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        Surface recorderSurface = mMediaRecorder.getSurface();//从获取录制视频需要的Surface
        try {
            mPreviewBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            int rotation = ((Activity) mTextureView.getContext()).getWindowManager().getDefaultDisplay().getRotation();
            int rotationDu = getRotationDu(rotation);
            mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, isFont ? 270 : 90);
            mPreviewBuilder.addTarget(previewSurface);
            mPreviewBuilder.addTarget(recorderSurface);
            //请注意这里设置了Arrays.asList(previewSurface,recorderSurface) 2个Surface，很好理解录制视频也需要有画面预览，第一个是预览的Surface，第二个是录制视频使用的Surface
            mCamera.createCaptureSession(Arrays.asList(previewSurface, recorderSurface), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();
    }

    private int getRotationDu(int rotation) {
        int rotationDu = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDu = 0;
                break;
            case Surface.ROTATION_90:
                rotationDu = 90;
                break;
            case Surface.ROTATION_180:
                rotationDu = 180;
                break;
            case Surface.ROTATION_270:
                rotationDu = 270;
                break;
        }
        return rotationDu;
    }

    public void pauseRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder.pause();
        }
    }

    public void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        ToastUtils.showShort("录制成功，去相册查看");
    }

    public void restartRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder.resume();
        }
    }

    public void takePicture() {
        try {
            //首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mPreviewSurface);
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            //设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, isFont ? 270 : 90);
            //停止预览
            mSession.stopRepeating();
            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                    try {
                        mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };

            mSession.capture(mCaptureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader() {
        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据
        Size matchingSize = getMatchingSize(true);
        mImageReader = ImageReader.newInstance(matchingSize == null ? ScreenUtils.getScreenWidth() : matchingSize.getWidth()
                , matchingSize == null ? ScreenUtils.getScreenHeight() : matchingSize.getHeight(), ImageFormat.JPEG, 1);
        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                // 开启线程异步保存图片
                if (image == null) {
                    Log.i("zune: ", "image = " + null);
                    return;
                }
                saveImage(image);
            }
        }, null);
    }

    private void saveImage(Image image) {
        Observable.just(image).map(new Function<Image, Integer>() {
            @Override
            public Integer apply(Image image) throws Exception {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                File imageFile = new File(Environment.getExternalStorageDirectory() + String.format("/DCIM/Camera/%s.jpg", getNameByTime()));
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imageFile);
                    fos.write(data, 0, data.length);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    imageFile = null;
                    if (fos != null) {
                        try {
                            fos.close();
                            fos = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ToastUtils.showShort("拍照成功，已保存至相册");
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    private String getNameByTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(System.currentTimeMillis());
    }

    public void release() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                mPreviewBuilder = null;
                if (mSession != null) {
                    try {
                        mSession.stopRepeating();
                        mSession.abortCaptures();
                        mSession.close();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    mSession = null;
                }
                if (mCamera != null) {
                    mCamera.close();
                }
                if (mMediaRecorder != null) {
                    Surface surface = mMediaRecorder.getSurface();
                    if (surface != null) {
                        surface.release();
                        surface = null;
                    }
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                }
                if (mTextureView != null) {
                    SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                    if (surfaceTexture != null) {
                        surfaceTexture.release();
                    }
                    mTextureView.setSurfaceTextureListener(null);
                    mTextureView = null;
                }
            }
        }.start();
    }

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i("zune: ", "onSurfaceTextureAvailable = " + surfaceTexture);
            if (onLogListener != null) {
                onLogListener.writeLog("onSurfaceTextureAvailable = " + surfaceTexture);
            }
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i("zune: ", "改变大小 width = " + width + ", height = " + height);
            if (onLogListener != null) {
                onLogListener.writeLog("改变大小 width = " + width + ", height = " + height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.i("zune: ", "onSurfaceTextureDestroyed");
            if (onLogListener != null) {
                onLogListener.writeLog("onSurfaceTextureDestroyed");
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            Log.i("zune: ", "onSurfaceTextureUpdated");
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
            Log.i("zune: ", "相机创建成功");
            if (onLogListener != null) {
                onLogListener.writeLog("相机创建成功");
            }
            try {
                mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);//返回结果
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.i("zune: ", "CameraAccessException = " + GsonGetter.getInstance().getGson().toJson(e));
                if (onLogListener != null) {
                    onLogListener.writeLog("CameraAccessException = " + GsonGetter.getInstance().getGson().toJson(e));
                }
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.i("zune: ", "onConfigureFailed = " + GsonGetter.getInstance().getGson().toJson(session));
        }
    };

    //CameraCaptureSession.CaptureCallback监听拍照过程
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            Log.i("zune: ", "onCaptureCompleted, CaptureRequest = " + GsonGetter.getInstance().getGson().toJson(request));
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            Log.i("zune: ", "onCaptureProgressed, CaptureRequest = " + GsonGetter.getInstance().getGson().toJson(request));
        }
    };

    public interface OnLogListener {
        void writeLog(String log);
    }

    private OnLogListener onLogListener;

    public void setOnLogListener(OnLogListener onLogListener) {
        this.onLogListener = onLogListener;
    }
}
