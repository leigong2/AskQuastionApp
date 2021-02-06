package com.example.android.askquastionapp.pollnumber;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;

import java.util.Random;

public class PollNumberActivity extends AppCompatActivity {
    private int mCount = 0;
    public static void start(Context context) {
        Intent intent = new Intent(context, PollNumberActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_number);
        setTitle("数字滚轮测试");
        ViewGroup pollNumberView = findViewById(R.id.poll_number_view);
        PollNumberLayout pollNumber = new PollNumberLayout(pollNumberView);
        pollNumber.setNumber(mCount, false);
        findViewById(R.id.button).setOnClickListener(view -> {
            if (mCount == 0) {
                mCount = 999;
                pollNumber.setNumber(999, true);
                return;
            }
            int i = new Random().nextInt(mCount);
            pollNumber.setNumber(i, true);
            ToastUtils.showShort("目标数字是：" + i);
        });
    }
}
