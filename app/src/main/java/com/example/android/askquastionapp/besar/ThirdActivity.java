package com.example.android.askquastionapp.besar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.databinding.ActivityThirdBinding;

public class ThirdActivity extends AppCompatActivity {

    private ActivityThirdBinding binding;
    public static void launch(Context context){
        context.startActivity(new Intent(context, ThirdActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil. setContentView(this, R.layout.activity_third);
        binding.setPresenter(this);

        binding.groupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.radio_mode_first){
                    binding.besselView.setMode(0);
                }else{
                    binding.besselView.setMode(1);
                }
            }
        });

    }

}
