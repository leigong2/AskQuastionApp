package com.example.android.askquastionapp.pollnumber;

import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PollNumberLayout {
    private List<String> mData = new ArrayList<>();
    private DisTouchRecyclerView firstItem;
    private DisTouchRecyclerView secondItem;
    private DisTouchRecyclerView thirdItem;
    private int mCurrentFirstPosition;
    private int mCurrentSecondPosition;
    private int mCurrentThirdPosition;
    private MediaPlayer mPlayer;

    public PollNumberLayout(View view) {
        initView(view);
    }

    public int getLayoutId() {
        return R.layout.layout_poll_number;
    }

    private void initView(View view) {
        firstItem = view.findViewById(R.id.first_item_view);
        secondItem = view.findViewById(R.id.second_item_view);
        thirdItem = view.findViewById(R.id.third_item_view);
        for (int i = 0; i < 10; i++) {
            mData.add(String.valueOf(i));
        }
        initRecyclerView(firstItem);
        initRecyclerView(secondItem);
        initRecyclerView(thirdItem);
        mCurrentFirstPosition = mCurrentSecondPosition = mCurrentThirdPosition = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % 10;
        firstItem.scrollToPosition(mCurrentFirstPosition);
        secondItem.scrollToPosition(mCurrentFirstPosition);
        thirdItem.scrollToPosition(mCurrentFirstPosition);
        if (firstItem.getAdapter() != null) {
            firstItem.getAdapter().notifyDataSetChanged();
        }
        if (secondItem.getAdapter() != null) {
            secondItem.getAdapter().notifyDataSetChanged();
        }
        if (thirdItem.getAdapter() != null) {
            thirdItem.getAdapter().notifyDataSetChanged();
        }

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioAttributes(attributes);
        Uri uri = Uri.parse("android.resource://" + view.getContext().getPackageName() + "/" + R.raw.keypress_spacebar);
        try {
            mPlayer.setDataSource(view.getContext(), uri);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new ScrollSpeedLinearLayoutManger(recyclerView.getContext()));
        recyclerView.setAdapter(new MyAdapter());
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poll_number, parent, false)) {
            };
            TextView itemView = (TextView) viewHolder.itemView;
            setFont(itemView, itemView.getTypeface().getStyle());
            return viewHolder;
        }

        public void setFont(TextView textView, int style) {
            Typeface typeface = Typeface.createFromAsset(BaseApplication.getInstance().getAssets(), "font/DIN_Alternate_Bold.ttf");
            if (typeface != null) {//防止系统抽风，没拿到字体
                textView.setTypeface(typeface, style);
            }
            textView.setIncludeFontPadding(true);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            mPlayer.start();
            String currentContent = mData.get(position % mData.size());
            if (currentContent == null) {
                currentContent = "0";
            }
            ((TextView) holder.itemView).setText(currentContent);
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }
    }

    public void setText(String number, boolean smooth) {
        if (number == null) {
            number = "";
        }
        for (int i = 3; i > 0; i--) {
            int offsetScroll = mData.size() * 3;
            switch (i) {
                case 3:
                    int position;
                    if (number.length() < 3) {
                        position = mCurrentFirstPosition + offsetScroll;
                    } else {
                        position = mCurrentFirstPosition + offsetScroll + mData.indexOf(String.valueOf(number.charAt(0)));
                    }
                    mCurrentFirstPosition += offsetScroll;
                    if (smooth) {
                        firstItem.smoothScrollToPosition(position);
                    } else {
                        firstItem.smoothScrollToPosition(position - offsetScroll);
                    }
                    break;
                case 2:
                    int position2;
                    if (number.length() < 2) {
                        position2 = mCurrentSecondPosition + offsetScroll;
                    } else {
                        position2 = mCurrentSecondPosition + offsetScroll + mData.indexOf(String.valueOf(number.charAt(1)));
                    }
                    mCurrentSecondPosition += offsetScroll;
                    if (smooth) {
                        secondItem.smoothScrollToPosition(position2);
                    } else {
                        secondItem.smoothScrollToPosition(position2 - offsetScroll);
                    }
                    break;
                case 1:
                    int position3;
                    if (number.length() < 1) {
                        position3 = mCurrentThirdPosition + offsetScroll;
                    } else {
                        position3 = mCurrentThirdPosition + offsetScroll + mData.indexOf(String.valueOf(number.charAt(2)));
                    }
                    mCurrentThirdPosition += offsetScroll;
                    if (smooth) {
                        thirdItem.smoothScrollToPosition(position3);
                    } else {
                        thirdItem.smoothScrollToPosition(position3 - offsetScroll);
                    }
                    break;
            }
        }
    }
}
