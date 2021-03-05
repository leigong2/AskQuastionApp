package com.example.android.askquastionapp.views;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.aes.AES;
import com.example.android.askquastionapp.bean.Company;
import com.example.android.askquastionapp.utils.MemoryCache;

import java.util.ArrayList;
import java.util.List;

public class ListDialog<T extends ListDialog.BaseData> extends DialogFragment {

    private WaveView progress;
    private View bgResult;
    private RecyclerView results;
    private final List<T> mDatas = new ArrayList<>();
    private boolean withAes;
    private List<T> ts;

    public static <T extends ListDialog.BaseData> ListDialog<T> showDialog(FragmentActivity activity, List<T> ts, boolean withAes) {
        ListDialog<T> dialog = new ListDialog<>();
        MemoryCache.getInstance().put("ts", ts);
        MemoryCache.getInstance().put("withAes", withAes);
        dialog.show(activity.getSupportFragmentManager(), ListDialog.class.getSimpleName());
        return dialog;
    }

    public static <T extends ListDialog.BaseData> ListDialog<T> showDialog(FragmentActivity activity, boolean progress) {
        ListDialog<T> dialog = new ListDialog<>();
        MemoryCache.getInstance().put("progress", progress);
        dialog.show(activity.getSupportFragmentManager(), ListDialog.class.getSimpleName());
        return dialog;
    }

    public void showWithData(List<T> ts, boolean withAes) {
        this.ts = ts;
        this.withAes = withAes;
        loadData();
    }

    private void loadData() {
        if (ts != null) {
            mDatas.clear();
            mDatas.addAll(ts);
            if (results.getAdapter() != null) {
                results.getAdapter().notifyDataSetChanged();
            }
            progress.setVisibility(View.GONE);
            results.setVisibility(View.VISIBLE);
            bgResult.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        progress = rootView.findViewById(R.id.progress);
        bgResult = rootView.findViewById(R.id.bg_result);
        results = rootView.findViewById(R.id.results);
        initView();
        loadData();
        return rootView;
    }

    private void initView() {
        ts = MemoryCache.getInstance().remove("ts");
        Boolean withAes = MemoryCache.getInstance().remove("withAes");
        if (withAes != null) {
            this.withAes = withAes;
        }
        Boolean progress = MemoryCache.getInstance().remove("progress");
        if (progress != null && progress) {
            this.progress.setVisibility(View.VISIBLE);
        }
        results.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        if (!ListDialog.this.withAes || view.isSelected()) {
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
                pathText.setText(ListDialog.this.withAes ? AES.encryptToBase64(mDatas.get(position).text, s) : mDatas.get(position).text);
                if (mDatas.get(position) instanceof Company) {
                    holder.itemView.setBackgroundColor(Color.parseColor(((Company) mDatas.get(position)).timeLimit ? "#33003333" : "#00000000"));
                    pathText.append(((Company) mDatas.get(position)).timeLimit ? "[过期]" : "");
                }
                pathText.setSelected(false);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        bgResult.setOnClickListener(v -> dismiss());
    }

    private int getLayoutId() {
        return R.layout.layout_clear;
    }

    private String s = "$%^ETDFc)(KL:JD)";//key16位，可自行修改

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        <T extends BaseData> void onItemClick(T data, int position);
    }

    public static class BaseData {
        public String text;

        public BaseData(String text) {
            this.text = text;
        }

        public BaseData() {
        }
    }
}
