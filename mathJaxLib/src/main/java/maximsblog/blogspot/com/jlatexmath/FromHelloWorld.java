package maximsblog.blogspot.com.jlatexmath;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.telegram.mathjaxlib.R;

public class FromHelloWorld extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_hello);
        findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToMainModel();
            }
        });
    }

    private void jumpToMainModel() {
        Intent intent = new Intent("com.example.android.askquastionapp.ToHelloWorld");
        startActivity(intent);
    }
}