package com.example.android.askquastionapp.tantan2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.example.android.askquastionapp.tantan.CardConfig;
import com.example.android.askquastionapp.utils.LogUtils;

/**
 * @author yuqirong
 * @link https://github.com/yuqirong/CardSwipeLayout
 */

public class CardLayout2Manager extends RecyclerView.LayoutManager {

    private RecyclerView mRecyclerView;

    public CardLayout2Manager(@NonNull RecyclerView recyclerView) {
        this.mRecyclerView = checkIsNull(recyclerView);
    }

    private <T> T checkIsNull(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int itemCount = getItemCount();
        // 当数据源个数大于最大显示数时
        if (itemCount > CardConfig.DEFAULT_SHOW_ITEM) {
            for (int position = CardConfig.DEFAULT_SHOW_ITEM; position >= 0; position--) {
                final View view = recycler.getViewForPosition(position);
                view.setOnTouchListener(mOnTouchListener);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
                int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
                // recyclerview 布局
                layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                        widthSpace / 2 + getDecoratedMeasuredWidth(view),
                        heightSpace / 2 + getDecoratedMeasuredHeight(view));

                if (position == CardConfig.DEFAULT_SHOW_ITEM) {
                    view.setScaleX(1 - (position - 1) * CardConfig.DEFAULT_SCALE);
                    view.setScaleY(1 - (position - 1) * CardConfig.DEFAULT_SCALE);
                    view.setTranslationY((position - 1) * view.getMeasuredHeight() / CardConfig.DEFAULT_TRANSLATE_Y);
                } else if (position > 0) {
                    view.setScaleX(1 - position * CardConfig.DEFAULT_SCALE);
                    view.setScaleY(1 - position * CardConfig.DEFAULT_SCALE);
                    view.setTranslationY(position * view.getMeasuredHeight() / CardConfig.DEFAULT_TRANSLATE_Y);
                }
            }
        } else {
            // 当数据源个数小于或等于最大显示数时
            for (int position = itemCount - 1; position >= 0; position--) {
                final View view = recycler.getViewForPosition(position);
                view.setOnTouchListener(mOnTouchListener);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
                int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
                // recyclerview 布局
                layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                        widthSpace / 2 + getDecoratedMeasuredWidth(view),
                        heightSpace / 2 + getDecoratedMeasuredHeight(view));

                if (position > 0) {
                    view.setScaleX(1 - position * CardConfig.DEFAULT_SCALE);
                    view.setScaleY(1 - position * CardConfig.DEFAULT_SCALE);
                    view.setTranslationY(position * view.getMeasuredHeight() / CardConfig.DEFAULT_TRANSLATE_Y);
                }
            }
        }
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            RecyclerView.ViewHolder childViewHolder = mRecyclerView.getChildViewHolder(v);
            dispatchTouchEvent(childViewHolder, event);
            return true;
        }
    };

    private float mX;
    private float mY;

    private void dispatchTouchEvent(RecyclerView.ViewHolder childViewHolder, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX = event.getRawX();
                mY = event.getRawY();
                LogUtils.i("zune: ", "down x = " + event.getRawX() + ", down y = " + event.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX();
                float moveY = event.getRawY();
                float dx = moveX - mX;
                float dy = moveY - mY;
                childViewHolder.itemView.setTranslationX(dx);
                childViewHolder.itemView.setTranslationY(dy);
                childViewHolder.itemView.setRotation(getRotation(childViewHolder.itemView, mY));
                LogUtils.i("zune: ", "move x = " + event.getRawX() + ", move y = " + event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mX = 0;
                mY = 0;
                LogUtils.i("zune: ", "up x = " + event.getRawX() + ", up y = " + event.getRawY());
                if (isLimitRemoved(childViewHolder.itemView)) {
                    return;
                }
                if (onRemoveItem != null) {
                    onRemoveItem.onRemove(childViewHolder);
                }
                break;
        }
    }

    private boolean isLimitRemoved(View itemView) {
        float translationX = itemView.getTranslationX();
        float translationY = itemView.getTranslationY();
        if (Math.abs(translationX) > ScreenUtils.getScreenHeight() * 0.03f) {
            return false;
        }
        if (Math.abs(translationY) > ScreenUtils.getScreenHeight() * 0.6f) {
            return false;
        }
        /*zune：插值器，仿探探做个回弹效果**/
        itemView.setRotation(0f);
        ObjectAnimator translateX = ObjectAnimator.ofFloat(itemView, "translationX", -0.3f * translationX, 0.2f * translationX, 0);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(itemView, "translationY", -0.3f * translationY, 0.2f * translationY, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                float factor = 1.5f;
                return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
            }
        });
        animatorSet.playTogether(translateX, translateY);
        animatorSet.start();
        return true;
    }

    private float getRotation(View itemView, float touchY) {
        float translationX = itemView.getTranslationX();
        float curRotation = (ScreenUtils.getScreenHeight() / 2f - touchY) / (ScreenUtils.getScreenHeight() / 2f) * CardConfig.DEFAULT_ROTATE_DEGREE * 2;
        return translationX / ScreenUtils.getScreenWidth() * curRotation;
    }

    private OnRemoveItemListener onRemoveItem;

    public void setOnRemoveItemListener(OnRemoveItemListener onRemoveItem) {
        this.onRemoveItem = onRemoveItem;
    }

    public interface OnRemoveItemListener {
        void onRemove(RecyclerView.ViewHolder childViewHolder);
    }

}
