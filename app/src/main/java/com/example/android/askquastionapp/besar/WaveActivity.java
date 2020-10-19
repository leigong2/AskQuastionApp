package com.example.android.askquastionapp.besar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

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
