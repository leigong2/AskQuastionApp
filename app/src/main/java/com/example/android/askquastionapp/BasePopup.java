package com.example.android.askquastionapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by yibin on 2016-04-25.
 */
public abstract class BasePopup extends PopupWindow {

    protected int height;
    protected View rootView;
    protected Context mContext;
    protected ViewGroup contentView;
    protected View bgView;

    public BasePopup(Context context, int layoutResId) {
        super(context);
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(layoutResId, null);
        this.setContentView(rootView);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
//        this.setAnimationStyle(R.style.popupBottomAnim);
        ColorDrawable dw = new ColorDrawable(0x34000000);
        this.setBackgroundDrawable(dw);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public BasePopup(Context context, int layoutResId, int width, int height) {
        super(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(layoutResId, null);
        this.setContentView(rootView);
        this.setWidth(width);
        this.setHeight(height);
        this.height = height;
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        contentView = (ViewGroup) ((Activity) context).findViewById(android.R.id.content);
        bgView = new View(context);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT);
        bgView.setLayoutParams(lp);
        bgView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTranslate1));
        contentView.addView(bgView);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                contentView.removeView(bgView);
            }
        });
    }

    public void show(Activity context) {
        try {
            showAtLocation(context.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(View view) {
        showAsDropDown(view);
    }

    public void show(View view, int gravity, int xoff, int yoff) {
        boolean hasChild = false;
        if (contentView != null) {
            for (int i = 0; i < contentView.getChildCount(); i++) {
                if (bgView == contentView.getChildAt(i)) {
                    hasChild = true;
                }
            }
            if (!hasChild) {
                contentView.addView(bgView);
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(Build.VERSION.SDK_INT == 24) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int h = view.getResources().getDisplayMetrics().heightPixels - rect.bottom;
                    setHeight(h);
                }
                showAsDropDown(view, xoff, yoff, gravity);
            } else {
                showAtLocation(view, Gravity.BOTTOM, 0, 50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
