package com.example.android.askquastionapp.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.expand.PushActivity;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.utils.SpanColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.widget.TextView.BufferType.NORMAL;
import static android.widget.TextView.BufferType.SPANNABLE;
import static com.example.android.askquastionapp.expand.PushActivity.INVISIBLE_CHAR;

@SuppressLint("AppCompatCustomView")
public class ExpandableImageTextView extends TextView {

    private int mExpandMaxLine; //最大折叠行数
    private boolean mMeasured; //初始化的时候无法获取正确的宽度
    private float mExpandSpace; //折叠icon与...之间的距离
    private Drawable mExpandDrawable; //展开drawable
    private Drawable mEnfoldDrawable; //折叠drawable
    private boolean showEnfold; //展开后，是否还需要折叠
    private boolean alertLayout; //折叠按钮是否靠着边, 贴边的话，需要在外层包一个icon，自定义布局

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
        mEnfoldDrawable = typedArray.getDrawable(R.styleable.ExpandableImageTextView_enfold_drawable);
        showEnfold = typedArray.getBoolean(R.styleable.ExpandableImageTextView_show_enfold_icon, false);
        alertLayout = typedArray.getBoolean(R.styleable.ExpandableImageTextView_alertLayout, false);
        mExpandMaxLine = typedArray.getInt(R.styleable.ExpandableImageTextView_expand_max_line, 3);
        mExpandSpace = typedArray.getDimension(R.styleable.ExpandableImageTextView_expand_space, 10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        CharSequence text = getText();
        if (!mMeasured && !TextUtils.isEmpty(getText())) {
            mMeasured = true;
            int measuredWidth = getMeasuredWidth();
            StaticLayout layout = new StaticLayout(text, getPaint(), measuredWidth - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, true);
            int lineCount = layout.getLineCount();
            SpannableStringBuilder spannableStringBuilder = measureSpec(layout, lineCount, getText());
            /*zune: 延迟10ms，等绘制完了再继续绘制，否则会发生绘制的紊乱**/
            Observable.just(spannableStringBuilder).delay(10, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<SpannableStringBuilder, Integer>(null, false) {
                        @Override
                        public void onNext(SpannableStringBuilder spannableStringBuilder, Integer integer2) {
                            resetClickableList(spannableStringBuilder);
                            setText(spannableStringBuilder, SPANNABLE);
                        }
                    });
        }
    }

    private String totalEndLine;

    /**
     * Measure spec spannable string builder.
     * 测量宽度，返回的时候截取后的文本内容
     *
     * @param text the text
     * @return the spannable string builder
     */
    private SpannableStringBuilder measureSpec(StaticLayout layout, int lineCount, CharSequence text) {
        SpannableStringBuilder temp = new SpannableStringBuilder();
        int min = mExpandMaxLine < 1 ? 1 : Math.min(mExpandMaxLine, lineCount);
        for (int index = 0; index < min; index++) {
            int start = layout.getLineStart(index);
            int end = layout.getLineEnd(index);
            String substring = text.toString().substring(start, end);
            if (index == mExpandMaxLine - 1 && mExpandMaxLine < lineCount) {
                totalEndLine = text.toString().substring(layout.getLineStart(lineCount - 1), layout.getLineEnd(lineCount - 1));
                if (alertLayout) {
                    temp.append(appendEnd(substring));
                } else {
                    temp.append(appendIcon(substring));
                }
            } else {
                temp.append(substring);
            }
        }
        return temp;
    }

    /**
     * zune: 拼接展开icon
     * ... 20dp   space 20dp icon 13.5dp
     * 从后往前截取
     * @param endLineText 最后一行的文本
     **/
    private SpannableStringBuilder appendIcon(String endLineText) {
        SpannableStringBuilder span = new SpannableStringBuilder();
        StringBuilder temp = new StringBuilder();  //被截取文本的长度
        int minimumWidth = mExpandDrawable.getMinimumWidth();
        int minimumHeight = mExpandDrawable.getMinimumHeight();
        for (int i = endLineText.length() - 1; i >= 0; i--) {
            temp.append(endLineText.charAt(i));
            float measureTextWidth = getPaint().measureText(temp.toString());
            /*zune: 当被截取文本的产度，大于阈值时，不再截取，转而拼接需要的内容**/
            if (measureTextWidth >= (getPaint().measureText("...") + minimumWidth + mExpandSpace)) {
                span.append(endLineText.substring(0, i));
                span.append("...");
                int c = (int) (mExpandSpace / getPaint().measureText(" "));
                for (int j = 0; j < c; j++) {
                    span.append(" ");
                }
                appendExpandIcon(span, minimumWidth, minimumHeight, c);
                return span;
            }
        }
        return new SpannableStringBuilder(endLineText);
    }

    private void appendExpandIcon(SpannableStringBuilder span, int minimumWidth, int minimumHeight, int c) {
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
        appendEnfoldIcon(c);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    /*zune: 拼接折叠icon**/
    private void appendEnfoldIcon(int c) {
        if (showEnfold) {
            int minimumWidth = mEnfoldDrawable.getMinimumWidth();
            int minimumHeight = mEnfoldDrawable.getMinimumHeight();
            for (int j = 0; j < c; j++) {
                mFullContent.append(" ");
            }
            mEnfoldDrawable.setBounds(0, 0, minimumWidth, minimumHeight);
            mFullContent.setSpan(new SpanColorUtils.VerticalCenterSpan(mEnfoldDrawable), mFullContent.length() - 1, mFullContent.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mFullContent.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (!TextUtils.isEmpty(mFullContent)) {
                        CharSequence text = mFullContent.subSequence(0, mFullContent.length() - c);
                        setMeasureText(new SpannableStringBuilder(text));
                    }
                }
            }, mFullContent.length() - 1, mFullContent.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * zune: 拼接icon
     * ... 20dp   space 20dp icon 13.5dp
     * 从后往前截取
     **/
    private SpannableStringBuilder appendEnd(String endLineText) {
        String expandString = "...";
        int minimumWidth = mExpandDrawable.getMinimumWidth();
        SpannableStringBuilder span = new SpannableStringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = endLineText.length() - 1; i >= 0; i--) {
            temp.append(endLineText.charAt(i));
            float measureTextWidth = getPaint().measureText(temp.toString());
            /*zune: 当被截取文本的产度，大于阈值时，不再截取，转而拼接需要的内容**/
            if (measureTextWidth >= (getPaint().measureText("...") + minimumWidth + mExpandSpace)) {
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

    private SpannableStringBuilder mFullContent = new SpannableStringBuilder();

    /**
     * Sets measure text.
     *
     * @param measureText 给textView设置文本，做测量用，测量好了折叠文本
     */
    public void setMeasureText(CharSequence measureText) {
        mMeasured = false;
        mFullContent.clear();
        SpannableStringBuilder span = new SpannableStringBuilder();
        span.append(measureText);
        resetClickableList(span);
        mFullContent.append(span);
        setText(measureText, NORMAL);
    }

    private void resetClickableList(SpannableStringBuilder content) {
        Log.i("zune ", "content = " + content);
        String[] split = content.toString().split("@|#| ");
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.endsWith(String.valueOf(INVISIBLE_CHAR))) {
                int index = getIndex(i, split) - 1;
                char afterChar = content.charAt(index > content.length() - 1 ? content.length() - 1 : index);
                temp.append(s).append(afterChar);
                if (i < split.length - 1) {
                    continue;
                } else {
                    s = temp.toString();
                    temp.setLength(0);
                }
            } else if (!TextUtils.isEmpty(temp)) {
                s = temp.append(s).toString();
                temp.setLength(0);
            }
            if (TextUtils.isEmpty(s.trim())) {
                continue;
            }
            int index = content.toString().indexOf(s);
            if (s.startsWith(INVISIBLE_CHAR + "#")) {
                String finalS = s;
                content.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (onShowExpand != null) {
                            onShowExpand.onClickTopic(finalS);
                        }
                    }
                }, index, index + s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else if (s.startsWith(INVISIBLE_CHAR + "@")) {
                String finalS = s;
                content.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (onShowExpand != null) {
                            onShowExpand.onClickUser(finalS);
                        }
                    }
                }, index, index + s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            while (index != -1) {
                index = content.toString().indexOf(s, index + 1);
            }
        }
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private int getIndex(int position, String[] split) {
        int index = 0;
        for (int i = 0; i < split.length; i++) {
            index += split[i].length();
            index++;
            if (position == i) {
                return index;
            }
        }
        return index;
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
        void onClickTopic(String topicName);
        void onClickUser(String userName);
    }

    private OnShowExpand onShowExpand;

    public void setOnShowExpand(OnShowExpand onShowExpand) {
        this.onShowExpand = onShowExpand;
    }
}
