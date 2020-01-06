package com.example.android.askquastionapp.read;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.SaveUtils;

import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author wangzhilong
 */
public class ReadTxtActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private ReadFragmentUtil fragmentUtil;
    private SaveUtils.SaveBean saveBean;
    private int index;
    private int position;
    private String path;
    private FileReader fileInputStream;
    private boolean showSeekbar;
    private TextView progressText;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, ReadTxtActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    public static void start(Context context, SaveUtils.SaveBean saveBean, int index) {
        Intent intent = new Intent(context, ReadTxtActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("saveBean", saveBean);
        bundle.putInt("index", index);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_txt);
        seekBar = findViewById(R.id.seek_bar);
        progressText = findViewById(R.id.progress_text);
        fragmentUtil = new ReadFragmentUtil();
        initClick();
        path = getIntent().getStringExtra("path");
        seekBar.setEnabled(false);
        if (path == null) {
            saveBean = (SaveUtils.SaveBean) getIntent().getSerializableExtra("saveBean");
            index = getIntent().getIntExtra("index", 0);
            if (saveBean == null || saveBean.saves == null || saveBean.saves.isEmpty()) {
                finish();
            } else {
                onNext();
                int total = saveBean.saves.get(index).total;
                seekBar.setMax(total);
                seekBar.setProgress(saveBean.saves.get(index).position);
                seekBar.setEnabled(true);
            }
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            finish();
            return;
        }
        try {
            fileInputStream = new FileReader(new File(path));
        } catch (Exception e) {
        }
        readInputStream();
    }

    private void initClick() {
        findViewById(R.id.progress_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveBean == null) {
                    return;
                }
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
                saveBean.saves.get(index).position = this.progress + 1;
                onPre();
            }
        });
        fragmentUtil.setOnLoadingListener(new ReadFragmentUtil.OnLoadingListener() {
            @Override
            public void onLoading(float percent) {
                if (path == null) {
                    return;
                }
                runnable.setPercent(percent);
                handler.post(runnable);
            }
        });
    }

    private void onPre() {
        if (saveBean != null && saveBean.saves.get(index).position >= 1) {
            saveBean.saves.get(index).position -= 2;
            ReadFragment fragment = ReadFragment.getInstance(saveBean, index);
            fragment.setOnReadListener(onReadListener);
            fragmentUtil.onPre(R.id.fragment_container, getSupportFragmentManager(), fragment);
            fragmentUtil.save(saveBean.saves.get(index));
            setProgress();
        } else if (position > 0){
            position -= 1;
            ReadFragment fragment = ReadFragment.getInstance(fragmentUtil.mFilesString.get(position));
            fragment.setOnReadListener(onReadListener);
            fragmentUtil.onPre(R.id.fragment_container, getSupportFragmentManager(), fragment);
        }
    }

    private void onNext() {
        if (saveBean != null) {
            if (saveBean.saves.get(index).position >= saveBean.saves.get(index).total) {
                return;
            }
            ReadFragment fragment = ReadFragment.getInstance(saveBean, index);
            fragment.setOnReadListener(onReadListener);
            fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
            fragmentUtil.save(saveBean.saves.get(index));
            setProgress();
        } else {
            position += 1;
            ReadFragment fragment = ReadFragment.getInstance(fragmentUtil.mFilesString.get(position));
            fragment.setOnReadListener(onReadListener);
            fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
        }
    }

    private void onMiddle() {
        if (showSeekbar) {
            ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.seek_bar_lay),"translationY",0, SizeUtils.dp2px(40));
            oa.setDuration(300);
            oa.start();
        } else {
            ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.seek_bar_lay),"translationY",SizeUtils.dp2px(40), 0);
            oa.setDuration(300);
            oa.start();
        }
        showSeekbar = !showSeekbar;
    }

    private Handler handler = new Handler();
    private MyRunnable runnable = new MyRunnable();
    private class MyRunnable implements Runnable {
        private float percent;
        public void setPercent(float percent) {
            this.percent = percent;
        }
        @Override
        public void run() {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String string = decimalFormat.format(percent < 0 ? 0 : percent);//返回字符串
            String[] split = path.split("/");
            progressText.setText(String.format("%s (正在下载%s)", split[split.length - 1], string + "%"));
            seekBar.setMax(100);
            seekBar.setProgress((int) (percent));
        }
    }

    private void showProgressDialog() {
        ProgressEditDialog editDialog = new ProgressEditDialog(this);
        editDialog.show(this);
        editDialog.setOnResultListener(new ProgressEditDialog.OnResultListener() {
            @Override
            public void onResult(double result) {
                saveBean.saves.get(index).position = (int) (result * saveBean.saves.get(index).total / 100);
                onNext();
            }
        });
    }

    private void setProgress() {
        if (saveBean == null) {
            return;
        }
        float progress = saveBean.saves.get(index).position / (float) saveBean.saves.get(index).total * 100;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String string = decimalFormat.format(progress < 0 ? 0 : progress);//返回字符串
        String[] split = saveBean.saves.get(index).path.split("/");
        progressText.setText(String.format("%s (%s)", split[split.length - 1], string + "%"));
        seekBar.setProgress(saveBean.saves.get(index).position);
    }

    boolean isInit;
    private void readInputStream() {
        ReadFragment fragment = ReadFragment.getInstance(saveBean, index);
        fragmentUtil.onNext(R.id.fragment_container, getSupportFragmentManager(), fragment);
        fragment.setOnLayoutListener(new ReadFragment.OnLayoutListener() {
            @Override
            public void onLayout(EditText editText) {
                try {
                    StringBuilder currentContent = new StringBuilder();
                    int length;
                    char[] a = new char[16];
                    while ((length = fileInputStream.read(a)) != -1) {
                        currentContent.append(new String(a, 0, length));
                        if (fragmentUtil.checkNext(editText, currentContent)) {
                            break;
                        }
                    }
                    editText.setText(currentContent.toString());
                } catch (Exception ignore) { }
                if (!isInit) {
                    isInit = true;
                    init(editText);
                }
            }
        });
        fragment.setOnReadListener(onReadListener);
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

    public void init(EditText editText) {
        Observable.just(1).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return fragmentUtil.init(editText, path);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer integer) {
                        SaveUtils.SaveBean saveBean = SaveUtils.get();
                        if (saveBean == null) {
                            saveBean = new SaveUtils.SaveBean();
                        }
                        if (saveBean.saves == null) {
                            saveBean.saves = new ArrayList<>();
                        }
                        saveBean.inited = true;
                        boolean contained = false;
                        for (SaveUtils.SaveBean.Save temp : saveBean.saves) {
                            if (ReadTxtActivity.this.path.equals(temp.path)) {
                                temp.total = integer;
                                temp.position = position;
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            SaveUtils.SaveBean.Save save = new SaveUtils.SaveBean.Save();
                            save.path = path;
                            save.position = position;
                            save.total = integer;
                            saveBean.saves.add(save);
                        }
                        SaveUtils.save(saveBean);
                        int total = saveBean.saves.get(index).total;
                        seekBar.setMax(total);
                        seekBar.setProgress(saveBean.saves.get(index).position);
                        seekBar.setEnabled(true);
                        Log.i("zune", "保存完成");
                        ReadTxtActivity.this.saveBean = saveBean;
                        ReadTxtActivity.this.index = saveBean.saves.size() - 1;
                        ReadTxtActivity.this.position = saveBean.saves.get(index).position;
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
