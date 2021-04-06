package com.example.android.askquastionapp.qqdrag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.askquastionapp.utils.ToastUtils;
import com.example.android.askquastionapp.R;

public class QQDragActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, QQDragActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qq_drag);
        DragIndicatorView dragView = findViewById(R.id.drag_view);
        dragView.setText("99+");
        dragView.setOnDismissAction(new DragIndicatorView.OnIndicatorDismiss() {
            @Override
            public void OnDismiss(DragIndicatorView view) {
                ToastUtils.showShort("我消失了");
                view.setVisibility(View.GONE);
            }
        });
    }
}
