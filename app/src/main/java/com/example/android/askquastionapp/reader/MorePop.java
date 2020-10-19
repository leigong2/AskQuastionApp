package com.example.android.askquastionapp.reader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.BasePopup;
import com.example.android.askquastionapp.R;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

public class MorePop extends BasePopup {

    private final RecyclerView recyclerView;

    public MorePop(Context context) {
        super(context, R.layout.layout_right_pop);
        this.setHeight(DensityUtil.dp2px(300));
        this.setWidth(DensityUtil.dp2px(80));
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_text, viewGroup, false)) {
                };
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClick != null) {
                            onItemClick.onItem(mDatas.get((Integer) v.getTag()));
                        }
                        dismiss();
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.setTag(i);
                TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                textView.setText(mDatas.get(i));
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
    }

    private List<String> mDatas = new ArrayList<>();

    public void setDatas(List<String> datas) {
        if (datas != null) {
            mDatas.addAll(datas);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public interface OnItemClick {
        void onItem(String index);
    }
    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }
}
