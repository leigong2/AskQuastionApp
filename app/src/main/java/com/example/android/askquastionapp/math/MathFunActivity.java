package com.example.android.askquastionapp.math;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.R;

public class MathFunActivity extends AppCompatActivity {

    private MathFunView funView;
    private LinearLayout layout;

    public static void start(Context context) {
        Intent intent = new Intent(context, MathFunActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_fun);
        layout = findViewById(R.id.layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View childAt = layout.getChildAt(i);
            childAt.setTag(i);
            childAt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    funView.setTest((int)v.getTag());
                    funView.drawFun();
                }
            });
        }
        funView = findViewById(R.id.math_fun);
        float screenWidth = ScreenUtils.getScreenWidth() - funView.getPaddingLeft() - funView.getPaddingRight();
        funView.getLayoutParams().height = (int) screenWidth;
        funView.setO(screenWidth / 2, screenWidth / 2);
        funView.setMaxX(screenWidth / 2 - 50f);
        funView.drawFun();
    }
}
