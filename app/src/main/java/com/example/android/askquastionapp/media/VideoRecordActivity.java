package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;

public class VideoRecordActivity extends AppCompatActivity {

    private boolean showButton;

    public static void start(Context context, boolean showButton) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        intent.putExtra("showButton", showButton);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        TextureView textureView = findViewById(R.id.texture_view);
        CameraV2Manager.getInstance().init(textureView);
        showButton = getIntent().getBooleanExtra("showButton", false);
        findViewById(R.id.start_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.pause_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.stop_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.restart_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.start_record).setOnClickListener(v -> startRecord());
        findViewById(R.id.pause_record).setOnClickListener(v -> pauseRecord());
        findViewById(R.id.stop_record).setOnClickListener(v -> stopRecord());
        findViewById(R.id.restart_record).setOnClickListener(v -> restartRecord());
    }

    private void startRecord() {
        CameraV2Manager.getInstance().startRecord();
    }

    private void pauseRecord() {
        CameraV2Manager.getInstance().pauseRecord();
    }

    private void stopRecord() {
        CameraV2Manager.getInstance().stopRecord();
    }

    private void restartRecord() {
        CameraV2Manager.getInstance().restartRecord();
    }

    @Override
    public void finish() {
        super.finish();
        CameraV2Manager.getInstance().release();
    }
}
