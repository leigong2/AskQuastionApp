package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.video.WatchVideoActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, MediaActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        findViewById(R.id.record_music).setOnClickListener(this::recordMusic);
        findViewById(R.id.play_music).setOnClickListener(v -> playMusic());
        findViewById(R.id.pre_video).setOnClickListener(v -> preVideo());
        findViewById(R.id.record_video).setOnClickListener(v -> recordVideo());
        findViewById(R.id.play_video).setOnClickListener(v -> playVideo());
    }

    private void recordMusic(View v) {
        v.setTag(v.getTag() == null || (int) v.getTag() == 0 ? 1 : 0);
        if ((int) v.getTag() == 1) {
            AudioRecordManager.getInstance().startRecord();
            ((TextView) v).setText("录制中...");
        } else {
            AudioRecordManager.getInstance().stopRecord();
            ((TextView) v).setText("音频录制");
        }
    }

    private void playMusic() {

    }

    private void preVideo() {
        VideoRecordActivity.start(this, false);
    }

    private void recordVideo() {
        VideoRecordActivity.start(this, true);
    }

    private void playVideo() {
        List<WatchVideoActivity.MediaData> mediaDatas = new ArrayList<>();
        File dir = new File(Environment.getExternalStorageDirectory(), "Telegram/Telegram Video");
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (!file.getName().endsWith(".mp4") && !file.getName().endsWith(".MOV") && !file.getName().endsWith(".MP4")) {
                continue;
            }
            WatchVideoActivity.MediaData mediaData = new WatchVideoActivity.MediaData(file.getName(), file.getPath(), ""
                    , new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(file.lastModified())));
            mediaDatas.add(mediaData);
        }
        VideoPlayerActivity.start(this, mediaDatas, 0);
    }

    @Override
    public void finish() {
        super.finish();
        AudioRecordManager.getInstance().release();
    }
}
