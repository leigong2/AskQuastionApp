package com.example.android.askquastionapp.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.android.askquastionapp.R;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import static android.widget.TextView.BufferType.NORMAL;

@SuppressLint("AppCompatCustomView")
public class ExpandableImageTextView extends TextView {

    private int mExpandMaxLine; //超过这个行数之后就折叠
    private boolean mMeasured; //初始化的时候无法获取正确的宽度
    private float mExpandSpace;
    private Drawable mExpandDrawable;

    public ExpandableImageTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ExpandableImageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExpandableImageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableImageTextView);
        mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableImageTextView_expand_drawable);
        mExpandMaxLine = typedArray.getInt(R.styleable.ExpandableImageTextView_expand_max_line, 3);
        mExpandSpace = typedArray.getDimension(R.styleable.ExpandableImageTextView_expand_space, 10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        if (!mMeasured && !TextUtils.isEmpty(getText())) {
            SpannableStringBuilder spannableStringBuilder = measureSpec(measuredWidth, getText());
            setText(spannableStringBuilder, NORMAL);
        }
    }

    /**
     * Measure spec spannable string builder.
     * 测量宽度，返回的时候截取后的文本内容
     *
     * @param width the width
     * @param text  the text
     * @return the spannable string builder
     */
    private SpannableStringBuilder measureSpec(int width, CharSequence text) {
        StaticLayout layout = new StaticLayout(text, getPaint(), width - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, true);
        int lineCount = layout.getLineCount();
        SpannableStringBuilder temp = new SpannableStringBuilder();
        int min = mExpandMaxLine < 1 ? 1 : Math.min(mExpandMaxLine, lineCount);
        for (int index = 0; index < min; index++) {
            int start = layout.getLineStart(index);
            int end = layout.getLineEnd(index);
            String substring = text.toString().substring(start, end);
            if (index == mExpandMaxLine - 1 && mExpandMaxLine < lineCount) {
                temp.append(appendIcon(substring));
            } else {
                temp.append(substring);
            }
        }
        return temp;
    }

    /**
     * zune: 拼接icon
     * ... 20dp   space 20dp icon 13.5dp
     * 从后往前截取
     **/
    private SpannableStringBuilder appendIcon(String endLineText) {
        SpannableStringBuilder span = new SpannableStringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = endLineText.length() - 1; i >= 0; i--) {
            temp.append(endLineText.charAt(i));
            float measureTextWidth = getPaint().measureText(temp.toString());
            if (measureTextWidth >= (getPaint().measureText("...   ") + DensityUtil.dp2px(13.5f))) {
                span.append(endLineText.substring(0, i));
                span.append("...").append("    ");
                int minimumWidth = mExpandDrawable.getMinimumWidth();
                int minimumHeight = mExpandDrawable.getMinimumHeight();
                mExpandDrawable.setBounds(0, 0, minimumWidth, minimumHeight);
                span.setSpan(new VerticalCenterSpan(mExpandDrawable), span.length() - 1, span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                span.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (!TextUtils.isEmpty(mFullContent)) {
                            setRealText(mFullContent);
                        }
                    }
                }, span.length() - 1, span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                return span;
            }
        }
        return new SpannableStringBuilder(endLineText);
    }

    /**
     * zune: 拼接icon
     * ... 20dp   space 20dp icon 13.5dp
     * 从后往前截取
     **/
    private SpannableStringBuilder appendEnd(String endLineText) {
        String expandString = "...   ";
        SpannableStringBuilder span = new SpannableStringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = endLineText.length() - 1; i >= 0; i--) {
            temp.append(endLineText.charAt(i));
            float measureTextWidth = getPaint().measureText(temp.toString());
            if (measureTextWidth >= (getPaint().measureText(expandString) + DensityUtil.dp2px(13.5f))) {
                span.append(endLineText.substring(0, i));
                span.append(expandString);
                if (onShowExpand != null) {
                    onShowExpand.showExpand();
                }
                return span;
            }
        }
        return new SpannableStringBuilder(endLineText);
    }

    private CharSequence mFullContent;

    /**
     * Sets measure text.
     *
     * @param measureText 给textView设置文本，做测量用，测量好了折叠文本
     */
    public void setMeasureText(CharSequence measureText) {
        mMeasured = false;
        mFullContent = measureText;
        setText(measureText, NORMAL);
    }

    /**
     * Sets real text.
     *
     * @param content the content 给textView设置文本，不做折叠处理
     */
    public void setRealText(CharSequence content) {
        mMeasured = true;
        setText(content, NORMAL);
    }

    public CharSequence getFullText() {
        return TextUtils.isEmpty(mFullContent) ? getText() : mFullContent;
    }

    public interface OnShowExpand {
        void showExpand();
    }

    private OnShowExpand onShowExpand;

    public void setOnShowExpand(OnShowExpand onShowExpand) {
        this.onShowExpand = onShowExpand;
    }
}
