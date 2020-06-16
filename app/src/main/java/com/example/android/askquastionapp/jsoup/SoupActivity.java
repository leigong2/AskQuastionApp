package com.example.android.askquastionapp.jsoup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.EditText;

import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.utils.MemoryCache;
import com.example.android.askquastionapp.R;

public class SoupActivity extends AppCompatActivity {
    public static void start(Context context, StringBuilder sb) {
        Intent intent = new Intent(context, SoupActivity.class);
        MemoryCache.getInstance().put(sb);
        context.startActivity(intent);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soup);
        EditText textView = findViewById(R.id.text_view);
        StringBuilder clear = MemoryCache.getInstance().clear(StringBuilder.class);
        textView.setText(clear);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(null);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setLineSpacing(0, 1);
        textView.setCursorVisible(false);
        textView.setFocusable(false);
        textView.setFocusableInTouchMode(false);
        textView.setClickable(true);
        textView.setTextIsSelectable(true);
        textView.setIncludeFontPadding(false);
        textView.setPadding(0, -SizeUtils.dp2px(3), 0, -SizeUtils.dp2px(3));
        textView.setTextColor(Color.BLACK);
    }
}
