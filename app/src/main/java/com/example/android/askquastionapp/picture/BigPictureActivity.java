package com.example.android.askquastionapp.picture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.R;
import com.phoenix.xphotoview.XPhotoView;

import java.io.IOException;
import java.io.InputStream;

public class BigPictureActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, BigPictureActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_picture);
        XPhotoView bigImageView = findViewById(R.id.big_image_view);
        try {
            InputStream inputStream = getResources().getAssets().open("long_pic.jpg");
            bigImageView.setImage(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
