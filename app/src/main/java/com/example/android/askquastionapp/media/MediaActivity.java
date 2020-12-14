package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;

public class MediaActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, MediaActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        findViewById(R.id.record_music).setOnClickListener(v -> recordMusic());
        findViewById(R.id.play_music).setOnClickListener(v -> playMusic());
        findViewById(R.id.pre_video).setOnClickListener(v -> preVideo());
        findViewById(R.id.record_video).setOnClickListener(v -> recordVideo());
        findViewById(R.id.play_video).setOnClickListener(v -> playVideo());
    }

    private void recordMusic() {

    }

    private void playMusic() {

    }

    private void preVideo() {
        VideoRecordActivity.start(this, false);
    }

    private void recordVideo() {
        VideoRecordActivity.start(this, true);
//        startActivity(new Intent(this, MedioRecorderCamera2Activity.class));
    }

    private void playVideo() {

    }

}
