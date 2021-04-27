package com.example.android.askquastionapp.read;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.example.android.askquastionapp.BasePopup;
import com.example.android.askquastionapp.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class ProgressEditDialog extends BasePopup {

    private EditText editText;

    public ProgressEditDialog(Context context) {
        super(context, R.layout.dialog_progress_edit);
        editText = rootView.findViewById(R.id.edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_DONE) {
                    KeyboardUtils.hideSoftInput(editText);
                    dismiss();
                    if (onResultListener != null) {
                        String result = editText.getText().toString().trim();
                        onResultListener.onResult(result);
                    }
                }
                return true;
            }
        });
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideSoftInput(editText);
                dismiss();
            }
        });
    }

    @Override
    public void show(Activity context) {
        super.show(context);
        Observable.just(1).delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(Integer integer) {
                if (isShowing()) {
                    KeyboardUtils.showSoftInput(editText);
                }
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }

    public void setHint(String hint) {
        editText.setText(hint);
        editText.setSelection(editText.getText().length());
    }

    public interface OnResultListener {
        void onResult(String result);
    }

    private OnResultListener onResultListener;

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }
}
