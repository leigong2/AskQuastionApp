package com.example.android.askquastionapp.read;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.utils.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author wangzhilong
 */
public class ReadTxtActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private ReadFragmentUtil fragmentUtil;
    private @NonNull
    String path = "";
    private InputStreamReader fileInputStream;
    private boolean showSeekbar;
    private TextView progressTextView;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, ReadTxtActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_txt);
        seekBar = findViewById(R.id.seek_bar);
        progressTextView = findViewById(R.id.progress_text);
        findViewById(R.id.search_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressEditDialog dialog = new ProgressEditDialog(ReadTxtActivity.this);
                dialog.show(ReadTxtActivity.this);
                dialog.setOnResultListener(result -> startSearchText(result));
            }
        });
        fragmentUtil = new ReadFragmentUtil();
        initClick();
        String temp = getIntent().getStringExtra("path");
        this.path = temp == null ? "" : temp;
        seekBar.setEnabled(false);
        File file = new File(this.path);
        if (!file.exists()) {
            finish();
            return;
        }
        try {
            fileInputStream = new InputStreamReader(new FileInputStream(new File(this.path)), FileUtil.getFileEncode(this.path));
        } catch (Exception ignore) {
        }
        readInputStream();
    }

    private void startSearchText(String result) {
        if (mCurrentEdit == null) {
            return;
        }
        String searchString = fragmentUtil.startSearchText(result);
        if (!TextUtils.isEmpty(searchString)) {
            String progress = getCurrentProgress();
            progressTextView.setText(progress);
            ReadFragment fragment = ReadFragment.getInstance(searchString, result);
            fragment.setOnReadListener(onReadListener);
            fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
            return;
        }
        StringBuilder currentContent = new StringBuilder();
        Observable.just(currentContent).map(new Function<StringBuilder, String>() {
            @Override
            public String apply(StringBuilder currentContent) throws Exception {
                int length;
                char[] a = new char[16];
                boolean ready = false;
                String progressText = "";
                while ((length = fileInputStream.read(a)) != -1) {
                    currentContent.append(new String(a, 0, length));
                    if (fragmentUtil.checkNext(mCurrentEdit, currentContent)) {
                        progressText = onNewText(currentContent);
                        if (!ready) {
                            currentContent.setLength(0);
                        } else {
                            break;
                        }
                    } else {
                        if (currentContent.toString().contains(result)) {
                            ready = true;
                        }
                    }
                }
                return progressText;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String, StringBuilder>(currentContent, false) {
                    @Override
                    public void onNext(String progressText, StringBuilder currentContent) {
                        mCurrentEdit.setText(currentContent.toString());
                        progressTextView.setText(progressText);
                        int start = mCurrentEdit.getText().toString().indexOf(result);
                        if (start < 0) {
                            return;
                        }
                        mCurrentEdit.setSelection(start, start + result.length());
                    }
                });
    }

    private String onNewText(StringBuilder currentContent) {
        fragmentUtil.raise(currentContent.toString());
        return getCurrentProgress();
    }

    private void initClick() {
        findViewById(R.id.progress_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onPre();
            }
        });
    }

    private void onPre() {
        if (fragmentUtil.isFirst()) {
            ToastUtils.showToast(this, "已到第一页");
            return;
        }
        String s = getCurrentProgress();
        progressTextView.setText(s);
        String preString = fragmentUtil.getPreString();
        ReadFragment fragment = ReadFragment.getInstance(preString,"");
        fragment.setOnReadListener(onReadListener);
        fragmentUtil.onPre(R.id.fragment_container, getSupportFragmentManager(), fragment);
    }

    @NotNull
    private String getCurrentProgress() {
        double d = 1f * fragmentUtil.getCurrentLength() / new File(path).length();
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        return decimalFormat.format(d) + "‰";
    }

    private void onNext() {
        String nextString = fragmentUtil.getNextString();
        if (TextUtils.isEmpty(nextString)) {
            readInputStream();
            return;
        }
        String s = getCurrentProgress();
        progressTextView.setText(s);
        ReadFragment fragment = ReadFragment.getInstance(nextString, "");
        fragment.setOnReadListener(onReadListener);
        fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
    }

    private void onMiddle() {
        if (showSeekbar) {
            ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.seek_bar_lay), "translationY", 0, SizeUtils.dp2px(40));
            oa.setDuration(300);
            oa.start();
            ObjectAnimator oa2 = ObjectAnimator.ofFloat(findViewById(R.id.search_icon), "translationY", 0, SizeUtils.dp2px(-40));
            oa2.setDuration(300);
            oa2.start();
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.seek_bar_lay), "translationY", SizeUtils.dp2px(40), 0);
            oa.setDuration(300);
            oa.start();
            ObjectAnimator oa2 = ObjectAnimator.ofFloat(findViewById(R.id.search_icon), "translationY", SizeUtils.dp2px(-40), 0);
            oa2.setDuration(300);
            oa2.start();
        }
        showSeekbar = !showSeekbar;
    }

    private void showProgressDialog() {
        ProgressEditDialog editDialog = new ProgressEditDialog(this);
        editDialog.show(this);
        editDialog.setOnResultListener(result -> {
            try {
                double d = Double.parseDouble(result);
                //Todo d 百分比
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private EditText mCurrentEdit;

    private void readInputStream() {
        ReadFragment fragment = ReadFragment.getInstance("", "");
        fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
        fragment.setOnLayoutListener(new ReadFragment.OnLayoutListener() {
            @Override
            public void onLayout(EditText editText) {
                mCurrentEdit = editText;
                readText();
            }
        });
        fragment.setOnReadListener(onReadListener);
    }

    private void readText() {
        StringBuilder currentContent = new StringBuilder();
        Observable.just(currentContent).map(new Function<StringBuilder, String>() {
            @Override
            public String apply(StringBuilder currentContent) throws Exception {
                int length;
                char[] a = new char[16];
                String progressText = "";
                while ((length = fileInputStream.read(a)) != -1) {
                    currentContent.append(new String(a, 0, length));
                    if (fragmentUtil.checkNext(mCurrentEdit, currentContent)) {
                        progressText = onNewText(currentContent);
                        break;
                    }
                }
                return progressText;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String, StringBuilder>(currentContent, false) {
            @Override
            public void onNext(String progressText, StringBuilder currentContent) {
                mCurrentEdit.setText(currentContent);
                progressTextView.setText(progressText);
            }
        });
    }

    ReadFragment.OnReadListener onReadListener = new ReadFragment.OnReadListener() {
        @Override
        public void onNext() {
            ReadTxtActivity.this.onNext();
        }

        @Override
        public void onPre() {
            ReadTxtActivity.this.onPre();
        }

        @Override
        public void onMiddle() {
            ReadTxtActivity.this.onMiddle();
        }
    };
}
