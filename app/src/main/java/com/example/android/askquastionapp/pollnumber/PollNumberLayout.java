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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PollNumberLayout {
    private final View mView;
    private List<String> mData = new ArrayList<>();
    private DisTouchRecyclerView firstItem;
    private DisTouchRecyclerView secondItem;
    private DisTouchRecyclerView thirdItem;
    private MediaPlayer mPlayer;
    private static final int MAX_VALUE = 100;

    public PollNumberLayout(View view) {
        mView = view;
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
        firstItem.scrollToPosition(10);
        secondItem.scrollToPosition(10);
        thirdItem.scrollToPosition(10);
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
            return MAX_VALUE;
        }
    }

    public void setNumber(@IntRange(from = 0, to = 999) int number, boolean smooth) {
        if (!smooth) {
            mView.setAlpha(0);
            BaseApplication.getInstance().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mView.setAlpha(1);
                }
            }, 300);
        }
        number = Math.min(999, Math.max(0, number));
        firstItem.scrollToPosition(10);
        secondItem.scrollToPosition(10);
        thirdItem.scrollToPosition(10);
        mView.post(new CustomRunnable(number, smooth) {
            @Override
            public void run(int number, boolean smooth) {
                if (firstItem == null || secondItem == null || thirdItem == null) {
                    return;
                }
                for (int i = 3; i > 0; i--) {
                    int offsetScroll = mData.size() * 3;
                    switch (i) {
                        case 3:
                            String selectString;
                            int position;
                            if (number < 100) {
                                selectString = "0";
                                position = 10 + offsetScroll;
                            } else {
                                selectString = String.valueOf(number / 100);
                                position = 10 + offsetScroll + mData.indexOf(selectString);
                            }
                            if (smooth) {
                                firstItem.smoothScrollToPosition(position);
                            } else {
                                firstItem.smoothScrollToPosition(position - offsetScroll);
                            }
                            break;
                        case 2:
                            int position2;
                            String selectString2;
                            if (number < 10) {
                                selectString2 = "0";
                                position2 = 10 + offsetScroll;
                            } else {
                                selectString2 = String.valueOf(number / 10 % 10);
                                position2 = 10 + offsetScroll + mData.indexOf(selectString2);
                            }
                            if (smooth) {
                                secondItem.smoothScrollToPosition(position2);
                            } else {
                                secondItem.smoothScrollToPosition(position2 - offsetScroll);
                            }
                            break;
                        case 1:
                            int position3;
                            String selectString3 = "0";
                            if (number == 0) {
                                selectString3 = "0";
                                position3 = 10 + offsetScroll;
                            } else {
                                selectString3 = String.valueOf(number % 10);
                                position3 = 10 + offsetScroll + mData.indexOf(selectString3);
                            }
                            if (smooth) {
                                thirdItem.smoothScrollToPosition(position3);
                            } else {
                                thirdItem.smoothScrollToPosition(position3 - offsetScroll);
                            }
                            break;
                    }
                }
            }
        });
    }
    public static abstract class CustomRunnable implements Runnable {

        private int number;
        private boolean smooth;

        public CustomRunnable(int number, boolean smooth) {
            this.number = number;
            this.smooth = smooth;
        }

        public abstract void run(int number, boolean smooth);

        @Override
        public void run() {
            run(number, smooth);
        }
    }

}
