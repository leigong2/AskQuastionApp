package com.example.android.askquastionapp.expand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.views.ExpandableImageTextView;

public class ExpandActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, ExpandActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand);
        ExpandableImageTextView tvExpandable = findViewById(R.id.tv_expandable);
        tvExpandable.setMeasureText("asdfa沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了asdfa沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了asdfa沙拉快点放假啊阿斯沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了asdfa沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了asdfa沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了asdfa沙拉快点放假啊阿斯兰的饭卡里说的咖啡机拉萨扩大解放了");
        ExpandableImageTextView tvExpandable2 = findViewById(R.id.tv_expandable2);
        tvExpandable2.setMeasureText("what is your name ? my name is LiLei HanMeiMei Hello How are you fine thank you and you i'm fine too thank How do you do Nice to meet you. My name is Jim·Green . Can you spell it? yes sureCanwhat is your name ? my name is LiLei HanMeiMei Hello How are you fine thank you and you i'm fine too thank How do you do Nice to meet you. My name is Jim·Green . Can you spell it? yes sureCan");
    }
}
