package com.example.android.askquastionapp.besar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.askquastionapp.R;

public class BesarActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, BesarActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_besar);
        findViewById(R.id.second).setOnClickListener(v -> SecondActivity.launch(this));
        findViewById(R.id.third).setOnClickListener(v -> ThirdActivity.launch(this));
        findViewById(R.id.water).setOnClickListener(v -> WaveActivity.launch(this));
    }
}
