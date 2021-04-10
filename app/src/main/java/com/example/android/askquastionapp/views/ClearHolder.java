package com.example.android.askquastionapp.views;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.aes.AES;
import com.example.android.askquastionapp.contacts.PhoneBean;
import com.example.android.askquastionapp.utils.ContactsUtils;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.android.askquastionapp.utils.HttpUtils;
import com.example.android.askquastionapp.utils.GsonGetter;

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
    private RecyclerView results;
    private View bgResult;
    private WaveView progress;
    private List<String> mDatas = new ArrayList<>();

    public ClearHolder(View view) {
        this.view = view;
        progress = view.findViewById(R.id.progress);
        bgResult = view.findViewById(R.id.bg_result);
        results = view.findViewById(R.id.results);
        results.setLayoutManager(new LinearLayoutManager(view.getContext()));
        results.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_path, parent, false)) {
                };
                viewHolder.itemView.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(mDatas.get((Integer) v.getTag()), (Integer) v.getTag());
                    }
                });
                viewHolder.itemView.findViewById(R.id.path_text).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!withRes) {
                            ((ViewGroup) view.getParent()).callOnClick();
                            return;
                        }
                        view.setSelected(!view.isSelected());
                        if (view.isSelected()) {
                            ((TextView) view).setText(AES.decryptFromBase64(((TextView) view).getText().toString(), s));
                        } else {
                            ((TextView) view).setText(AES.encryptToBase64(((TextView) view).getText().toString(), s));
                        }
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                holder.itemView.setTag(position);
                TextView pathText = holder.itemView.findViewById(R.id.path_text);
                pathText.setText(withRes ? AES.encryptToBase64(mDatas.get(position), s) : mDatas.get(position));
                pathText.setSelected(false);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
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

    public RecyclerView getResults() {
        return results;
    }

    private boolean withRes = true;

    public void stopLoad(List<String> datas, boolean withHeader, boolean withRes) {
        this.withRes = withRes;
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
        if (results.getAdapter() != null) {
            results.getAdapter().notifyDataSetChanged();
        }
    }

    public void stopLoad(List<String> datas, boolean withHeader) {
        stopLoad(datas, withHeader, true);
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
                            if (results.getAdapter() != null) {
                                results.getAdapter().notifyDataSetChanged();
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
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(ClearHolder.this.view.getContext()).inflate(R.layout.layout_item_path, parent, false);
                    viewHolder.pathText = convertView.findViewById(R.id.path_text);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                return view;
            }

            class ViewHolder {
                TextView pathText;
            }
        };
    }

    private String s = "$%^ETDFc)(KL:JD)";//key16位，可自行修改

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String data, int position);
    }
}
