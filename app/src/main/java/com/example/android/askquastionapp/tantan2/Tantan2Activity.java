package com.example.android.askquastionapp.tantan2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.MainActivity;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.tantan.CardLayoutManager;
import com.example.android.askquastionapp.utils.DefaultItemAnimator;
import com.example.android.askquastionapp.utils.GlideUtils;
import com.example.android.askquastionapp.video.DownloadObjManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.android.askquastionapp.tantan.CardConfig.DEFAULT_ROTATE_DEGREE;

public class Tantan2Activity extends AppCompatActivity {
    private String[] srr = new String[]{"https://mp4.facecast.xyz/storage1/M00/11/79/aPODCl1LlKeAJi7EAAFNOex_5qc189.jpg"
            , "https://mp4.facecast.xyz/storage1/M06/21/06/aPODC15WdL6AemiIAAOBxXUiSj8075.jpg"
            , "https://mp4.facecast.xyz/storage1/M08/0B/87/aPODClyIfe6AT9AVAADJTpPoZnU598.png"
            , "http://zaimeyffile.gchao.cn/storage1/M00/00/07/KjNKEWDAbc6AeJHPAAQFTF3v4cA238.jpg"
            , "http://mp4.facecast.xyz/storage1/M07/01/A1/aPODCluLl-GAdqPjAAB9sKedIE8853.jpg"
            , "https://mp5.facecast.xyz/storage1/M09/18/3F/aPODC13a0cqAZadGAAOA5kW4-LQ366.jpg",
            "https://mp4.facecast.xyz/storage1/M05/2B/B9/aPODCl6iF7KAEKwgAAheBSRPgA0775.jpg",
            "https://mp4.facecast.xyz/storage1/M05/19/37/aPODC13r_02AdG5rAAL5pKajzhI520.png",
            "https://mp5.facecast.xyz/storage1/M02/00/7F/aPODC1s13P6AAGyjAAD_l6lXpWY135.jpg",
            "https://mp4.facecast.xyz/storage1/M01/46/68/aPODCl76yQmAcqBhAANDIjmEdo0739.jpg",
            "https://mp4.facecast.xyz/storage1/M03/1C/6D/aPODC14k4_GAB_KkAARQJZp45q0232.jpg",
            "https://mp4.facecast.xyz/storage1/M05/04/4F/aPODClwdss6ALUlWAABg-RApSJM305.jpg",
            "https://mp4.facecast.xyz/storage1/M02/0D/8C/aPODC1y420yAW_XKAAC1Wicft5E964.png"};

    public static void start(Context context) {
        Intent intent = new Intent(context, Tantan2Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tantan);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            protected void dispatchRemoveAnim(RecyclerView.ViewHolder holder, ViewPropertyAnimator animation) {
                super.dispatchRemoveAnim(holder, animation);
                holder.itemView.setOnTouchListener(null);
                float translationX = holder.itemView.getTranslationX();
                float translationY = holder.itemView.getTranslationY();
                animation.translationX(translationX * (ScreenUtils.getScreenWidth() / Math.abs(translationX)));
                animation.translationY(translationY * (ScreenUtils.getScreenWidth() / Math.abs(translationX)));
            }
        });
        RecyclerView.Adapter<MyViewHolder> adapter = new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tantan, parent, false);
                MyViewHolder viewHolder = new MyViewHolder(view);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                if (holder.withAnim) {
                    holder.itemView.setTranslationX(-ScreenUtils.getScreenWidth());
                    holder.itemView.setTranslationY(ScreenUtils.getScreenHeight() / 2f);
                    holder.itemView.setRotation(-DEFAULT_ROTATE_DEGREE * 5);
                    ObjectAnimator oaX = ObjectAnimator.ofFloat(holder.itemView, "translationX", -ScreenUtils.getScreenWidth(), 0);
                    ObjectAnimator oaY = ObjectAnimator.ofFloat(holder.itemView, "translationY", ScreenUtils.getScreenHeight() / 2f, 0);
                    ObjectAnimator rotation = ObjectAnimator.ofFloat(holder.itemView, "rotation", -DEFAULT_ROTATE_DEGREE * 5, 0);
                    AnimatorSet set = new AnimatorSet();
                    set.setDuration(300);
                    set.playTogether(oaX, oaY, rotation);
                    set.start();
                }
                ((TextView) holder.itemView.findViewById(R.id.text_view_convert)).setText(String.valueOf(position));
                ((TextView) holder.itemView.findViewById(R.id.text_view_post)).setText(String.valueOf(holder.hashCode()));
                holder.setData(list.get(position));
                holder.withAnim = false;
                holder.itemView.setTranslationX(0);
                holder.itemView.setTranslationY(0);
                holder.itemView.setTag(list.get(position));
                ImageView avatarImageView = holder.itemView.findViewById(R.id.iv_avatar);
//                avatarImageView.setImageResource(list.get(position));
                GlideUtils.getInstance().loadUrl(list.get(position), avatarImageView, false, false);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        };
        recyclerView.setAdapter(adapter);
        CardLayout2Manager cardLayoutManager = new CardLayout2Manager(recyclerView);
        cardLayoutManager.setOnRemoveItemListener(new CardLayout2Manager.OnRemoveItemListener() {
            @Override
            public void onRemove(RecyclerView.ViewHolder childViewHolder) {
                list.remove(0);
                adapter.notifyItemRemoved(0);
            }
        });
        recyclerView.setLayoutManager(cardLayoutManager);
        initData();
        adapter.notifyDataSetChanged();
    }

    private List<String> mRemovedList = new ArrayList<>();
    private List<String> list = new ArrayList<>();

    private void initData() {
        for (int i = 0; i < 5; i++) {
            list.addAll(Arrays.asList(srr));
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public boolean withAnim;
        public String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
