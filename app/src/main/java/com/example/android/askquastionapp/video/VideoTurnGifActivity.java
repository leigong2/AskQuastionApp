package com.example.android.askquastionapp.video;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.video.camera2.Camera2DelegateImp;
import com.example.android.askquastionapp.video.exo.gifencoder.BitmapExtractor;
import com.example.android.askquastionapp.video.exo.gifencoder.GIFEncoder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.example.android.askquastionapp.MainActivity.EXTERNAL_FILE_CODE;

public class VideoTurnGifActivity extends AppCompatActivity {

    private Uri uri;
    private TextView recordVideo;
//    private Camera2DelegateImp mCamera2Delegate = new Camera2DelegateImp();
    private SurfaceView mSurfaceView;
    private Camera camera1;

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoTurnGifActivity.class);
        context.startActivity(intent);
    }

    private String filePath;

    private enum State {INIT, READY, BUILDING, COMPLETE}

    private State state = State.INIT;

    private TextView selectVideo;
    private TextView tip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_turn_gif);
        recordVideo = (TextView) findViewById(R.id.record_video);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_view);
        selectVideo = (TextView) findViewById(R.id.select_video);
        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordToUri();
            }
        });
        selectVideo.setOnClickListener(clickListener);
        tip = (TextView) findViewById(R.id.tip);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    camera1.setPreviewDisplay(surfaceHolder);
                    camera1.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera1.release();
            }
        });
//        mCamera2Delegate.setSurfaceView(mSurfaceView);
//        mCamera2Delegate.setCallBack(new Camera2DelegateImp.CallBack() {
//            @Override
//            public void onCallBack() {
//                BaseApplication.getInstance().getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCamera2Delegate.startRecord(VideoTurnGifActivity.this);
//                    }
//                });
//            }
//        });
        // 打开摄像头并将展示方向旋转90度
        camera1 = Camera.open();
        camera1.setDisplayOrientation(90);
    }

    private void startRecordToUri() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        mCamera2Delegate.startRecord(VideoTurnGifActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTERNAL_FILE_CODE) {
            if (resultCode == RESULT_OK) {
                uri = data.getData();
                if (uri == null) {
                    return;
                }
                filePath = FileUtil.getPath(this, uri);
                state = State.READY;
                selectVideo.setText(R.string.create_gif);
                tip.setText(R.string.building_init);
            }
        }
    }

    public String getRealFilePath(Uri uri) {
        String path = uri.getPath();
        String[] pathArray = path.split(":");
        String fileName = pathArray[pathArray.length - 1];
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (state == State.INIT || state == State.COMPLETE) {
                File file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (file == null) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //7.0以上跳转系统文件需用FileProvider，参考链接：https://blog.csdn.net/growing_tree/article/details/71190741
                Uri uri = FileUtil.getUriFromFile(VideoTurnGifActivity.this, file);
                intent.setData(uri);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, EXTERNAL_FILE_CODE);
            } else if (state == State.READY) {
                state = State.BUILDING;
                tip.setText(R.string.building_gif);
                BitmapExtractor extractor = new BitmapExtractor();
                extractor.setFPS(10);
                extractor.setScope(0, 5);
                extractor.setSize(540, 960);
                Observable.just(1).map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer list) throws Exception {
                        List<Bitmap> bitmaps = extractor.createBitmaps(uri, filePath);
                        String fileName = String.valueOf(System.currentTimeMillis()) + ".gif";
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;
                        GIFEncoder encoder = new GIFEncoder();
                        encoder.init(bitmaps.get(0));
                        encoder.setFrameRate(16);
                        encoder.start(filePath);
                        for (int i = 1; i < bitmaps.size(); i++) {
                            encoder.addFrame(bitmaps.get(i));
                        }
                        encoder.finish();
                        return filePath;
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String filePath) {
                                state = State.COMPLETE;
                                tip.setText(R.string.building_complete);
                                selectVideo.setText(R.string.select_video);
                                Toast.makeText(getApplicationContext(), "存储路径" + filePath, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        }
    };
}
