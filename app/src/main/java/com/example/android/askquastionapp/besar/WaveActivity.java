package com.example.android.askquastionapp.besar;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.databinding.ActivityWaveBinding;

public class WaveActivity extends AppCompatActivity {

    private ActivityWaveBinding binding;
    public static void launch(Context context){
        context.startActivity(new Intent(context,WaveActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil. setContentView(this, R.layout.activity_wave);
        binding.setPresenter(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.waveView.onStop();
    }
}
