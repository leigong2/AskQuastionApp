package com.example.android.askquastionapp.media;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.picture.PhotoImageView;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PhotoSheetDialog extends BottomSheetDialogFragment {

    private RecyclerView mRecyclerView;
    private BottomSheetBehavior<View> mBehavior;
    private PhotoImageView mBitImageView;
    private View close;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), getLayoutId(), null);
        dialog.setContentView(view);
        mRecyclerView = view.findViewById(R.id.photo_data);
        mBitImageView = view.findViewById(R.id.big_image_view);
        close = view.findViewById(R.id.close);
        close.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            mBitImageView.setVisibility(View.GONE);
        });
        mBitImageView.setOnProgressCallBack(new PhotoImageView.OnProgressCallBack() {
            @Override
            public void onDismiss() {
                close.setVisibility(View.GONE);
                mBitImageView.setVisibility(View.GONE);
            }
        });
        mRecyclerView.setMinimumHeight((int) (ScreenUtils.getScreenHeight() * 0.618f));
        initView();
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight((int) (ScreenUtils.getScreenHeight() * 0.618f));
        return dialog;
    }

    public int getLayoutId() {
        return R.layout.dialog_photo_sheet;
    }

    private List<PictureCheckManager.MediaData> mDataList = new ArrayList<>();

    private void initView() {
        GridLayoutManager manager = new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mDataList.get(position).path == null) {
                    return 4;
                } else {
                    return 1;
                }
            }
        });
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new CurrentItemDecoration());
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 1001) {
                    return getHeaderHolder(parent);
                }
                return getNormalViewHolder(parent);
            }

            @Override
            public int getItemViewType(int position) {
                if (mDataList.get(position).path == null) {
                    return 1001;
                }
                return super.getItemViewType(position);
            }

            @NotNull
            private RecyclerView.ViewHolder getNormalViewHolder(@NonNull ViewGroup parent) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false)) {
                };
            }

            private RecyclerView.ViewHolder getHeaderHolder(ViewGroup parent) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getHeaderLayoutId(), parent, false)) {
                };
            }

            public int getLayoutId() {
                return R.layout.item_photo_sheet;
            }

            private int getHeaderLayoutId() {
                return R.layout.header_photo_sheet;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                String tag = mDataList.get(position).folder.replaceAll(Environment.getExternalStorageDirectory().getPath(), "");
                holder.itemView.setTag(tag);
                if (holder.getItemViewType() == 1001) {
                    TextView textView = holder.itemView.findViewById(R.id.text_view);
                    textView.setText(tag);
                    return;
                }
                ImageView imageView = holder.itemView.findViewById(R.id.image_view);
                PictureCheckManager.MediaData mediaData = mDataList.get(position);
                Glide.with(imageView.getContext())
                        .load(mediaData.path)
                        .apply(new RequestOptions().override(200, 200).fitCenter().placeholder(R.mipmap.place_loading))
                        .into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBitImageView.setVisibility(View.VISIBLE);
                        close.setVisibility(View.VISIBLE);
                        File file = new File(mediaData.path);
                        mBitImageView.setFile(file);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        });
        Observable.just(1).map(new Function<Integer, Map<String, List<PictureCheckManager.MediaData>>>() {
            @Override
            public Map<String, List<PictureCheckManager.MediaData>> apply(Integer integer) throws Exception {
                return PictureCheckManager.getInstance().getNormalPictures();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Map<String, List<PictureCheckManager.MediaData>>, Integer>(1, false) {
                    @Override
                    public void onNext(Map<String, List<PictureCheckManager.MediaData>> stringListMap, Integer integer) {
                        setData(stringListMap);
                    }
                });
    }

    public void setData(Map<String, List<PictureCheckManager.MediaData>> datas) {
        if (datas != null) {
            for (String s : datas.keySet()) {
                List<PictureCheckManager.MediaData> collection = datas.get(s);
                if (collection == null) {
                    continue;
                }
                PictureCheckManager.MediaData mediaData = new PictureCheckManager.MediaData();
                mediaData.folder = s;
                mDataList.add(mediaData);
                for (PictureCheckManager.MediaData data : collection) {
                    data.folder = s;
                }
                mDataList.addAll(collection);
            }
        }
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public static class CurrentItemDecoration extends RecyclerView.ItemDecoration {

        private final Paint paint;
        private final TextPaint textPaint;

        public CurrentItemDecoration() {
            paint = new Paint();
            paint.setColor(Color.parseColor("#999999"));

            textPaint = new TextPaint();
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(DensityUtil.dp2px(16));
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.LEFT);
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
            if (position <= -1 || position >= parent.getAdapter().getItemCount() - 1) {
                // 越界检查
                return;
            }
            RecyclerView.ViewHolder viewHolder = parent.findViewHolderForAdapterPosition(position);
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            int top = parent.getPaddingTop();
            int bottom = top + DensityUtil.dp2px(50);
            c.drawRect(left, top, right, bottom, paint);
            //计算baseline
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
            RectF rectF = new RectF(left, top, right, bottom);
            float baseline = rectF.centerY() + distance;
            c.drawText((String) viewHolder.itemView.getTag(), DensityUtil.dp2px(5), baseline, textPaint);
        }
    }
}
