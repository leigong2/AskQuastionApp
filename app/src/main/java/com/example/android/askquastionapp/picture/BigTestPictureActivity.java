package com.example.android.askquastionapp.picture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class BigTestPictureActivity extends AppCompatActivity {
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                float progress = (float) msg.obj;
                DecimalFormat df = new DecimalFormat("######0.00");
                String s = df.format(progress * 100);
                textView.setText(String.format("已加载%s", s));
                textView.append("%");
                seekBar.setProgress((int) (progress * 100));
                if (progress == 1) {
                    BigTestPictureActivity.this.progress.setVisibility(View.GONE);
                }
            }
        }
    };
    private TextView textView;
    private SeekBar seekBar;
    private View progress;

    public static void start(Context context) {
        Intent intent = new Intent(context, BigTestPictureActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_test_picture);
        PhotoImageView bigImageView = findViewById(R.id.big_image_view);
        progress = findViewById(R.id.progress);
        textView = findViewById(R.id.text_view);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        bigImageView.setOnProgressCallBack(new PhotoImageView.OnProgressCallBack() {
            @Override
            public void onPosition(float progress) {
                Message msg = new Message();
                msg.what = 1001;
                msg.obj = progress;
                mHandler.sendMessage(msg);
            }
        });
        try {
            InputStream inputStream = getResources().getAssets().open("big_world.jpg");
            bigImageView.setImageResource(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
