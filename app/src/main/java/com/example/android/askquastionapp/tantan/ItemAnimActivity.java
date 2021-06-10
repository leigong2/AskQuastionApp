package com.example.android.askquastionapp.tantan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.DefaultItemAnimator;

import java.util.ArrayList;
import java.util.List;

public class ItemAnimActivity extends AppCompatActivity {
    private final List<Integer> mDatas = new ArrayList<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, ItemAnimActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_anim);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator() {

        });
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false)) {
                };
                viewHolder.itemView.setOnClickListener(v -> {
                    Integer tag = (Integer) viewHolder.itemView.getTag();
                    mDatas.remove(tag);
                    recyclerView.getAdapter().notifyItemRemoved(tag);
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = holder.itemView.findViewById(R.id.text_view);
                holder.itemView.setTag(mDatas.get(position));
                textView.setText("我是 一条鱼" + position);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        for (int i = 0; i < 20; i++) {
            mDatas.add(i);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
