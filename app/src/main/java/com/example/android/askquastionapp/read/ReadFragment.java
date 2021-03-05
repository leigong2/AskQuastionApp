package com.example.android.askquastionapp.read;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.SaveUtils;

public class ReadFragment extends Fragment {
    private EditText mReadPager;
    private SaveUtils.SaveBean.Save mCurData;

    public static ReadFragment getInstance(SaveUtils.SaveBean saveBean, int index) {
        ReadFragment readFragment = new ReadFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("saveBean", saveBean);
        bundle.putInt("index", index);
        readFragment.setArguments(bundle);
        return readFragment;
    }

    public static ReadFragment getInstance(String text) {
        ReadFragment readFragment = new ReadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        readFragment.setArguments(bundle);
        return readFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        initView(view);
        if (onLayoutListener != null) {
            onLayoutListener.onLayout(mReadPager);
        }
        Bundle arguments = getArguments();
        SaveUtils.SaveBean saveBean = (SaveUtils.SaveBean) arguments.getSerializable("saveBean");
        int index = arguments.getInt("index");
        String text = arguments.getString("text");
        if (saveBean != null) {
            setSaveBean(saveBean, index);
        } else if (!TextUtils.isEmpty(text)) {
            mReadPager.setText(text);
        }
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View view) {
        mReadPager = (EditText) view.findViewById(R.id.read_pager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mReadPager.setBackground(null);
        }
        mReadPager.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mReadPager.setLineSpacing(0, 1);
        mReadPager.setCursorVisible(false);
        mReadPager.setFocusable(false);
        mReadPager.setFocusableInTouchMode(false);
        mReadPager.setClickable(true);
        mReadPager.setTextIsSelectable(true);
        mReadPager.setIncludeFontPadding(false);
        mReadPager.setPadding(0, -SizeUtils.dp2px(3), 0, -SizeUtils.dp2px(3));
        mReadPager.setTextColor(Color.BLACK);
        mReadPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isClick = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isClick && onReadListener != null) {
                            float curX = event.getX();
                            float curY = event.getY();
                            if (curY > maxY / 2 + 100) {
                                onReadListener.onNext();
                            } else if (curY < maxY / 2 - 100) {
                                onReadListener.onPre();
                            } else if (curY >= maxY / 2 - 100 && curY <= maxY / 2 + 100) {
                                onReadListener.onMiddle();
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        maxX = ScreenUtils.getScreenWidth();
        maxY = ScreenUtils.getScreenHeight();
    }

    boolean isClick = false;
    float maxX;
    float maxY;

    public void setSaveBean(SaveUtils.SaveBean saveBean, int index) {
        SaveUtils.SaveBean.Save save = saveBean.saves.get(index);
        this.mCurData = save;
        setText();
    }

    private void setText() {
        if (mCurData.position >= 0) {
            this.mCurData.position++;
            mReadPager.setText(SaveUtils.getCache(mCurData.path, mCurData.position));
        } else {
            mReadPager.setText(SaveUtils.getCache(mCurData.path, 0));
            this.mCurData.position = 0;
        }
    }

    public interface OnLayoutListener {
        void onLayout(EditText editText);
    }

    private OnLayoutListener onLayoutListener;

    public void setOnLayoutListener(OnLayoutListener onLayoutListener) {
        this.onLayoutListener = onLayoutListener;
    }

    public interface OnReadListener {
        void onNext();

        void onPre();

        void onMiddle();
    }
    private OnReadListener onReadListener;

    public void setOnReadListener(OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
    }
}
