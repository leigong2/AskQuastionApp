package com.example.android.askquastionapp.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.android.askquastionapp.R;

/**
 * Created by mayn on 2018/2/6.
 */

public class CommonDialog extends Dialog implements View.OnClickListener {

    private ViewGroup rootView;
    private TextView titleText;
    private TextView contentTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private View verticalLine;

    private Context mContext;
    private CharSequence title;
    private CharSequence content;
    private OnCloseListener listener;
    private SpannableStringBuilder positiveName;
    private CharSequence negativeName;
    private int cancelTextColor = 0;
    private int submitTextColor = 0;

    private WindowManager.LayoutParams windowParams;
    private Window window;
    private boolean canceledOnTouchOutside;

    private boolean mIsShowOneKey;

    public CommonDialog(Context context) {
        this(context, null);
    }

    public CommonDialog(Context context, OnCloseListener listener) {
        this(context, R.style.commonDialog, listener);
    }

    public CommonDialog(Context context, int themeResId) {
        this(context, themeResId, null);
    }

    public CommonDialog(Context context, int themeResId, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.listener = listener;
        window = getWindow();
    }

    public CommonDialog(Context context, boolean canceledOnTouchOutside, OnCloseListener listener) {
        super(context, R.style.commonDialog);
        this.mContext = context;
        this.listener = listener;
        this.canceledOnTouchOutside = canceledOnTouchOutside;
        window = getWindow();
    }

    @Deprecated
    public CommonDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
        window = getWindow();
    }


    public CommonDialog setAlertTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public CommonDialog setContent(CharSequence content) {
        this.content = content;
        return this;
    }

    public CommonDialog setPositiveButton(CharSequence name) {
        positiveName = new SpannableStringBuilder();
        positiveName.append(name);
        if (submitTxt != null) {
            submitTxt.setText(name);
        }
        return this;
    }

    public CommonDialog setNegativeButton(CharSequence name) {
        this.negativeName = name;
        if (cancelTxt != null) {
            cancelTxt.setText(name);
        }
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_commom);
        if (window != null && window.getAttributes() != null) {
            windowParams = window.getAttributes();
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(windowParams);
        }
        setCanceledOnTouchOutside(canceledOnTouchOutside);
        initView();
        if (listener != null) {
            listener.onCreate(this);
        }
    }

    public void setGravity(int gravity) {
        if (window != null) {
            window.setGravity(gravity);
        }
    }

    public void setFullScreen() {
    }

    public void setCancelTextColor(int Color) {
        cancelTextColor = Color;
        if (cancelTxt != null) {
            cancelTxt.setTextColor(Color);
        }
    }

    public void setSubmitTextColor(int Color) {
        submitTextColor = Color;
        if (submitTxt != null) {
            submitTxt.setTextColor(Color);
        }
    }

    public void setOneKeyMode() {
        mIsShowOneKey = true;
    }

    private void initView() {
        rootView = findViewById(R.id.common_dialog_root);
        titleText = findViewById(R.id.common_dialog_title);
        contentTxt = findViewById(R.id.common_dialog_content);
        submitTxt = findViewById(R.id.common_dialog_submit);
        cancelTxt = findViewById(R.id.common_dialog_cancel);
        verticalLine = findViewById(R.id.common_dialog_vertical_line);

        submitTxt.setOnClickListener(this);
        cancelTxt.setOnClickListener(this);
        if (cancelTextColor != 0) {
            cancelTxt.setTextColor(cancelTextColor);
        }
        if (submitTextColor != 0) {
            submitTxt.setTextColor(submitTextColor);
        }
        if (!TextUtils.isEmpty(title)) {
            titleText.setVisibility(View.VISIBLE);
            titleText.setText(title);
        } else {
            titleText.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(content)) {
            contentTxt.setVisibility(View.VISIBLE);
            contentTxt.setText(content);
        } else {
            contentTxt.setVisibility(View.GONE);
        }

        //保证有一个占位,不能都是gone
        if (titleText.getVisibility() == View.GONE
                && contentTxt.getVisibility() == View.GONE) {
            contentTxt.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(positiveName)) {
            submitTxt.setText(positiveName);
        }

        if (!TextUtils.isEmpty(negativeName)) {
            cancelTxt.setText(negativeName);
        }

        if (mIsShowOneKey) {
            cancelTxt.setVisibility(View.GONE);
            verticalLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_dialog_cancel:
                if (listener != null) {
                    listener.onClick(this, false);
                }
                this.dismiss();
                break;
            case R.id.common_dialog_submit:
                if (listener != null) {
                    listener.onClick(this, true);
                } else {
                    this.dismiss();
                }
                break;
        }
    }

    @Override
    public void show() {
        if (mContext == null) {
            return;
        }
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        super.show();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void setCancelText(String string) {
        if (cancelTxt != null) {
            cancelTxt.setText(string);
        }
    }

    public void setConfirmText(String string) {
        if (submitTxt != null) {
            submitTxt.setText(string);
        }
    }

    public void setTitleNoLimit(boolean b) {
        if (titleText != null) {
            if (b) {
                titleText.setMaxLines(Integer.MAX_VALUE);
            } else {
                titleText.setMaxLines(2);
            }
        }
    }

    public TextView getContentTextView() {
        return contentTxt;
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm);

        default void onCreate(CommonDialog commonDialogByTwoKey) {
        }
    }
}
