package com.example.android.askquastionapp.media;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecordManager {
    private static AudioRecordManager sAudioRecordManager;
    private MediaRecorder mRecorder;

    private AudioRecordManager() {
    }

    public static AudioRecordManager getInstance() {
        if (sAudioRecordManager == null) {
            sAudioRecordManager = new AudioRecordManager();
        }
        return sAudioRecordManager;
    }

    public void startRecord() {
        if (mRecorder == null) {
            //录音获取麦克风声音
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);       //录制的音源为麦克风
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB); //设置音频文件的编码
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //设置audio格式
        }
        File audioFile = new File(Environment.getExternalStorageDirectory() + String.format("/Music/%s.amr", getNameByTime()));
        if (!audioFile.exists()) {
            try {
                audioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mRecorder.setOutputFile(audioFile.getAbsolutePath()); //设置路径
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    public void pauseRecord() {
        if (mRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mRecorder.pause();
            }
        }
    }

    public void resumeRecord() {
        if (mRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mRecorder.resume();
            }
        }
    }

    public void stopRecord() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
        }
    }

    public void release() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mRecorder != null) {
                    mRecorder.release();
                    mRecorder = null;
                }
            }
        }.start();
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
}
