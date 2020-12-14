package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;

public class VideoRecordActivity extends AppCompatActivity {

    private boolean showButton;
    private TextView logText;

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
        showButton = getIntent().getBooleanExtra("showButton", false);
        CameraV2Manager.getInstance().init(textureView);
        findViewById(R.id.start_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.pause_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.stop_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.restart_record).setVisibility(showButton ? View.VISIBLE : View.GONE);
        findViewById(R.id.start_record).setOnClickListener(v -> CameraV2Manager.getInstance().startRecord());
        findViewById(R.id.pause_record).setOnClickListener(v -> CameraV2Manager.getInstance().pauseRecord());
        findViewById(R.id.stop_record).setOnClickListener(v -> CameraV2Manager.getInstance().stopRecord());
        findViewById(R.id.restart_record).setOnClickListener(v -> CameraV2Manager.getInstance().restartRecord());
        findViewById(R.id.switch_camera).setOnClickListener(view -> CameraV2Manager.getInstance().switchCamera());
        logText = findViewById(R.id.log_text);
        CameraV2Manager.getInstance().setOnLogListener(new CameraV2Manager.OnLogListener() {
            @Override
            public void writeLog(String log) {
                appendLog(log);
            }
        });
    }

    private void appendLog(String log) {
        logText.append(log);
        logText.append("\n");
    }

    @Override
    public void finish() {
        super.finish();
        CameraV2Manager.getInstance().release();
    }
}
