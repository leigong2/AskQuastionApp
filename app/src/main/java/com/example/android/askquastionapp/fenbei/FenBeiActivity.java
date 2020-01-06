package com.example.android.askquastionapp.fenbei;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.askquastionapp.R;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class FenBeiActivity extends AppCompatActivity {

    private MicroPhoneThread microPhone = new MicroPhoneThread();  //线程用于实时录制周围声音
    public boolean istrue = true;

    private MediaRecorder mARecorder;    //麦克风控制
    private File mAudiofile, mSampleDir;  //录音文件保存
    private MHandler mHandler = new MHandler();
    private TextView desc;

    class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    fenBeiCount.setText(msg.obj.toString() + "dB");
            }
        }
    }

    private TextView fenBeiCount;

    public static void start(Context context) {
        Intent intent = new Intent(context, FenBeiActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fenbei);
        fenBeiCount = findViewById(R.id.fenbei_count);
        desc = findViewById(R.id.desc);
        desc.setText("10dB 呼吸声\n"
                + "20dB 树上叶子碰撞声\n"
                + "30dB 亲密的窃窃私语\n"
                + "40dB 安静的图书馆低声细语\n"
                + "50dB 安静的办公室空调外机工作\n"
                + "60dB 两个人正常谈话\n"
                + "70dB 略微嘈杂\n"
                + "80dB 烦闷\n"
                + "90dB 吵架\n"
                + "100dB 火车启动声\n"
                + "120dB 飞机发动\n"      );
    }


    @Override
    protected void onStart() {
        super.onStart();
        //录音获取麦克风声音
        mARecorder = new MediaRecorder();                                //声音录制
        mARecorder.setAudioSource(MediaRecorder.AudioSource.MIC);       //录制的音源为麦克风
        mARecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR); //设置音频文件的编码
        mARecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //设置audio格式

        mSampleDir = getCacheDir();
        //用IM+系统当前时间为文件名建立.amr的文件，文件路径为mSampleDir
        mAudiofile = new File(mSampleDir.getPath() + "/IM" + System.currentTimeMillis() + "aar");
        if (!mAudiofile.exists()) {
            try {
                mAudiofile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mARecorder.setOutputFile(mAudiofile.getAbsolutePath()); //设置路径
        try {
            mARecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mARecorder.start();
        microPhone.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mARecorder.stop();
        mAudiofile.delete();
        mHandler.removeCallbacksAndMessages(null);
        mARecorder = null;
        mAudiofile = null;
    }

    class MicroPhoneThread extends Thread {       //测试当前分贝值通知UI修改
        final float minAngle = (float) Math.PI * 4 / 11;
        float angle;

        @Override
        public void run() {
            while (istrue) {
                angle = 100 * minAngle * mARecorder.getMaxAmplitude() / 32768;
                if (angle > 100) {
                    angle = 100;
                }
                //构造方法的字符格式这里如果小数不足2位，会已0补足
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String p = decimalFormat.format(angle);

                mHandler.sendMessage(mHandler.obtainMessage(1, p));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
