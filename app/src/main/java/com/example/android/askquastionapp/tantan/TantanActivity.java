package com.example.android.askquastionapp.tantan;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.askquastionapp.tantan.CardConfig.DEFAULT_ROTATE_DEGREE;

public class TantanActivity extends AppCompatActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, TantanActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tantan);
        ViewPager2 viewPager2 = findViewById(R.id.recycler_view);
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.Adapter<MyViewHolder> adapter = new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tantan, parent, false);
                MyViewHolder viewHolder = new MyViewHolder(view);
                view.setOnClickListener(itemView -> {
                    viewHolder.withAnim = true;
                    int index = mCurrentList.indexOf((Integer) itemView.getTag());
                    if (index > 0) {
                        Integer obj = mCurrentList.get(index - 1);
                        list.add(0, obj);
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
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
                holder.withAnim = false;
                holder.itemView.setTag(list.get(position));
                ImageView avatarImageView = holder.itemView.findViewById(R.id.iv_avatar);
                avatarImageView.setImageResource(list.get(position));
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        };
        recyclerView.setAdapter(adapter);
        CardItemTouchHelperCallback cardCallback = new CardItemTouchHelperCallback(recyclerView.getAdapter(), list);
        cardCallback.setOnSwipedListener(new OnSwipeListener<Integer>() {

            private int direction;

            @Override
            public void onSwiping(RecyclerView.ViewHolder viewHolder, float ratio, int direction) {
                viewHolder.itemView.setAlpha(1 - Math.abs(ratio) * 0.2f);
                if (direction == CardConfig.SWIPING_LEFT) {
                    this.direction = direction;
                    viewHolder.itemView.findViewById(R.id.iv_dislike).setAlpha(Math.abs(ratio));
                } else if (direction == CardConfig.SWIPING_RIGHT) {
                    this.direction = direction;
                    viewHolder.itemView.findViewById(R.id.iv_like).setAlpha(Math.abs(ratio));
                } else {
                    viewHolder.itemView.findViewById(R.id.iv_dislike).setAlpha(0f);
                    viewHolder.itemView.findViewById(R.id.iv_like).setAlpha(0f);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, Integer o, int direction) {
                viewHolder.itemView.setAlpha(1f);
                viewHolder.itemView.findViewById(R.id.iv_dislike).setAlpha(0f);
                viewHolder.itemView.findViewById(R.id.iv_like).setAlpha(0f);
                switch (direction) {
                    case ItemTouchHelper.UP:
                        ToastUtils.showShort("swiped up" + (this.direction == CardConfig.SWIPING_LEFT ? " left" : " right"));
                        break;
                    case ItemTouchHelper.DOWN:
                        ToastUtils.showShort("swiped down" + (this.direction == CardConfig.SWIPING_LEFT ? " left" : " right"));
                        break;
                    case ItemTouchHelper.LEFT:
                        ToastUtils.showShort("swiped left");
                        break;
                    case ItemTouchHelper.RIGHT:
                        ToastUtils.showShort("swiped right");
                        break;
                }
            }

            @Override
            public void onSwipedClear() {
                ToastUtils.showShort("data clear");
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }, 3000L);
            }

        });
        final ItemTouchHelper touchHelper = new ItemTouchHelper(cardCallback);
        final CardLayoutManager cardLayoutManager = new CardLayoutManager(recyclerView, touchHelper);
        recyclerView.setLayoutManager(cardLayoutManager);
        touchHelper.attachToRecyclerView(recyclerView);
        initData();
    }

    private List<Integer> mCurrentList = new ArrayList<>();
    private List<Integer> list = new ArrayList<>();

    private void initData() {
        list.add(R.drawable.img_avatar_01);
        list.add(R.drawable.img_avatar_02);
        list.add(R.drawable.img_avatar_03);
        list.add(R.drawable.img_avatar_04);
        list.add(R.drawable.img_avatar_05);
        list.add(R.drawable.img_avatar_06);
        list.add(R.drawable.img_avatar_07);
        mCurrentList.clear();
        mCurrentList.addAll(list);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public boolean withAnim;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
