package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.VideoPlayerActivity;

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
        PhotoSheetDialog.show(getSupportFragmentManager());
    }

    @Override
    public void finish() {
        super.finish();
        AudioRecordManager.getInstance().release();
    }
}
