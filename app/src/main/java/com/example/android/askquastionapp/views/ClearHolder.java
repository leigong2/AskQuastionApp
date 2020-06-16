package com.example.android.askquastionapp.views;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jsoup.GsonGetter;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.ContactsUtils;
import com.example.android.askquastionapp.utils.HttpUtils;
import com.example.android.askquastionapp.contacts.PhoneBean;
import com.example.android.askquastionapp.utils.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ClearHolder {
    public View view;
    private BaseAdapter mAdapter;
    private ListView results;
    private View bgResult;
    private WaveView progress;
    private List<String> mDatas = new ArrayList<>();

    public ClearHolder(View view) {
        this.view = view;
        progress = view.findViewById(R.id.progress);
        bgResult = view.findViewById(R.id.bg_result);
        results = view.findViewById(R.id.results);
        mAdapter = getAdapter();
        results.setAdapter(mAdapter);
        bgResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dismiss();
    }

    public int getLayoutId() {
        return R.layout.layout_clear;
    }

    public void startLoad() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    public void stopLoad() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    public ListView getResults() {
        return results;
    }

    public void stopLoad(List<String> datas, boolean withHeader) {
        if (datas == null || datas.isEmpty()) {
            datas = new ArrayList<>();
        }
        results.setVisibility(View.VISIBLE);
        bgResult.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        mDatas.clear();
        if (withHeader) {
            File test1 = FileUtil.assetsToFile(view.getContext(), "1_2019年 47 人旅游人员电话.xls");
            if (test1 != null) {
                mDatas.add(test1.getAbsolutePath());
            }
            File test2 = FileUtil.assetsToFile(view.getContext(), "白马啸西风.txt");
            if (test2 != null) {
                mDatas.add(test2.getAbsolutePath());
            }
        }
        mDatas.addAll(datas);
        mDatas.add("更多");
        mAdapter.notifyDataSetChanged();
    }

    public void stopLoadContact(List<String> datas) {
        Observable.just(datas).map(new Function<List<String>, List<String>>() {
            @Override
            public List<String> apply(List<String> data) throws Exception {
                return ContactsUtils.getInstance().getGeo(view.getContext(), "86", data);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(List<String> integer) {
                        stopLoad(integer, false);
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void dispatchContacts() {
        for (int i = 0; i < mDatas.size(); i++) {
            Observable.just(i).map(new Function<Integer, Integer>() {
                @Override
                public Integer apply(Integer integer) throws Exception {
                    String phoneNum = mDatas.get(integer).split(" : ")[1];
                    if (TextUtils.isEmpty(phoneNum)) {
                        return 0;
                    } else {
                        phoneNum = replace(phoneNum);
                    }
                    String address = GsonGetter.getInstance().getGson().fromJson(HttpUtils.getDatas("http://tcc.taobao.com/cc/json/mobile_tel_segment.htm", phoneNum), PhoneBean.class).getCarrier();
                    if (TextUtils.isEmpty(address)) {
                        mDatas.set(integer, mDatas.get(integer) + " : " + "未知");
                    } else {
                        mDatas.set(integer, mDatas.get(integer) + " : " + address);
                    }
                    return 1;
                }
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }
                        @Override
                        public void onNext(Integer integer) {
                            mAdapter.notifyDataSetChanged();
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

    @NotNull
    private String replace(String phoneNum) {
        return phoneNum.replace("+86", "").replaceAll(" ", "");
    }

    public void dismiss() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    private BaseAdapter getAdapter() {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return mDatas.size();
            }

            @Override
            public Object getItem(int position) {
                return mDatas.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                ViewHolder viewHolder;
                if (view == null) {
                    viewHolder = new ViewHolder();
                    view = LayoutInflater.from(ClearHolder.this.view.getContext()).inflate(R.layout.layout_item_path, parent, false);
                    viewHolder.pathText = view.findViewById(R.id.path_text);
                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }
                viewHolder.pathText.setText(mDatas.get(position));
                return view;
            }

            class ViewHolder {
                TextView pathText;
            }
        };
    }

}
