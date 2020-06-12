package com.example.android.askquastionapp.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.wxapi.ShareDialog;

public class SetIpDialog extends DialogFragment {

    private EditText editIp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogFragment);
    }

    public void showSoftInputFromWindow(EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editIp.postDelayed(new Runnable() {
            @Override
            public void run() {
                showSoftInputFromWindow(editIp);
            }
        }, 300);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_set_ip, container, false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        editIp = rootView.findViewById(R.id.edit_ip);
        editIp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //回车键
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if (onResultListener != null) {
                        onResultListener.onResult(editIp.getText().toString());
                    }
                    dismiss();
                }
                return true;
            }
        });
        return rootView;
    }

    public static SetIpDialog showDialog(FragmentActivity activity) {
        SetIpDialog dialog = new SetIpDialog();
        dialog.show(activity.getSupportFragmentManager(), SetIpDialog.class.getSimpleName());
        return dialog;
    }

    public interface OnResultListener {
        void onResult(String ip);
    }

    private OnResultListener onResultListener;

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }
}
