package com.example.android.askquastionapp.views;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.BasePopup;
import com.example.android.askquastionapp.R;

import java.util.ArrayList;
import java.util.List;


public class BottomPop extends BasePopup {

    private final RecyclerView recyclerView;
    private final View animLay;
    private List<String> mDatas = new ArrayList<>();

    public static BottomPop getCurrent(Activity activity) {
        return new BottomPop(activity);
    }

    public BottomPop(Context context) {
        super(context, getLayoutId());
        this.setAnimationStyle(R.style.popupBottomAnim);
        setClippingEnabled(false);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        recyclerView = getContentView().findViewById(R.id.recycler_view);
        animLay = getContentView().findViewById(R.id.anim_lay);
        bgView = getContentView().findViewById(R.id.bg_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bottom_pop, viewGroup, false)) {
                };
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag();
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                textView.setText(mDatas.get(i));
                viewHolder.itemView.setTag(i);
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
    }

    @Override
    public void show(Activity context) {
        super.show(context);
        animLay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                animLay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = animLay.getHeight();
                ObjectAnimator oa = ObjectAnimator.ofFloat(animLay, "translationY", height, 0f);
                oa.setDuration(300);
                oa.start();
            }
        });
    }

    @Override
    public void dismiss() {
        bgView.setVisibility(View.GONE);
        bgView.setAlpha(0);
        animLay.post(new Runnable() {
            @Override
            public void run() {
                BottomPop.super.dismiss();
            }
        });
    }

    public void setItemText(String text) {
        mDatas.add(text);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private static int getLayoutId() {
        return R.layout.layout_pop_up;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
