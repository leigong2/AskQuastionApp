package com.example.android.askquastionapp.media;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.picture.PhotoImageView;
import com.example.android.askquastionapp.scan.CapturePictureUtil;
import com.example.android.askquastionapp.utils.BrowserUtils;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private int mClickPosition;
    private int mediaType;  //0-all  1-video  2-image

    public static PhotoSheetDialog show(FragmentManager fragmentManager) {
        PhotoSheetDialog dialog = new PhotoSheetDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("mediaType", 1);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, PhotoSheetDialog.class.getSimpleName());
        return dialog;
    }

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
        mBitImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view instanceof PhotoImageView) {
                    ToastUtils.showShort("已捕获，正在扫描");
                    Bitmap bitmap = ((PhotoImageView) view).getImageBitmap();
                    CapturePictureUtil.parseQCodeInBitmap(bitmap, new CapturePictureUtil.OnResultListener() {
                        @Override
                        public void onResult(String s) {
                            if (s == null) {
                                ToastUtils.showShort("扫描失败");
                                return;
                            } else {
                                ToastUtils.showShort("扫描成功，扫描结果已返回剪切板");
                            }
                            ClipboardManager clipboardManager = (ClipboardManager) BaseApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, s));
                            if (clipboardManager.getPrimaryClip() != null && clipboardManager.hasPrimaryClip()) {
                                clipboardManager.getPrimaryClip().getItemAt(0).getText();
                            }
                            if (s.startsWith("http")) {
                                BrowserUtils.goToBrowser(getContext(), s);
                            }
                        }
                    });
                }
                return false;
            }
        });
        mBitImageView.setOnDismissCallBack(new PhotoImageView.OnLimitCallBack() {
            long time = System.currentTimeMillis();

            @Override
            public void onDismiss() {
                close.setVisibility(View.GONE);
                mBitImageView.setVisibility(View.GONE);
            }

            @Override
            public void onLeftLimit() {
                if (System.currentTimeMillis() - time < 500) {
                    return;
                }
                time = System.currentTimeMillis();
                if (mClickPosition - 1 >= 0) {
                    mClickPosition--;
                    if (mDataList.get(mClickPosition).path != null) {
                        itemClick(mClickPosition);
                    } else if (mClickPosition - 1 >= 0) {
                        mClickPosition--;
                        if (mDataList.get(mClickPosition).path != null) {
                            itemClick(mClickPosition);
                        }
                    }
                }
            }

            @Override
            public void onRightLimit() {
                if (System.currentTimeMillis() - time < 500) {
                    return;
                }
                time = System.currentTimeMillis();
                if (mClickPosition + 1 < mDataList.size()) {
                    mClickPosition++;
                    if (mDataList.get(mClickPosition).path != null) {
                        itemClick(mClickPosition);
                    } else if (mClickPosition + 1 < mDataList.size()) {
                        mClickPosition++;
                        if (mDataList.get(mClickPosition).path != null) {
                            itemClick(mClickPosition);
                        }
                    }
                }
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
        if (getArguments() != null) {
            mediaType = getArguments().getInt("mediaType", 0);
        }
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
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false)) {
                };
                ImageView imageView = viewHolder.itemView.findViewById(R.id.image_view);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickPosition = (int) view.getTag();
                        itemClick(mClickPosition);
                    }
                });
                return viewHolder;
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
                ImageView playIcon = holder.itemView.findViewById(R.id.play_icon);
                if (mediaType == 1) {
                    playIcon.setVisibility(View.VISIBLE);
                }
                PictureCheckManager.MediaData mediaData = mDataList.get(position);
                Glide.with(imageView.getContext())
                        .load(mediaData.path)
                        .apply(new RequestOptions().override(200, 200).fitCenter().placeholder(R.mipmap.place_loading))
                        .into(imageView);
                imageView.setTag(position);
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        });
        Observable.just(1).map(new Function<Integer, Map<String, List<PictureCheckManager.MediaData>>>() {
            @Override
            public Map<String, List<PictureCheckManager.MediaData>> apply(Integer integer) throws Exception {
                if (mediaType == 0) {
                    return PictureCheckManager.getInstance().getNormalPictures();
                } else if (mediaType == 1) {
                    return PictureCheckManager.getInstance().getNormalVideos();
                }
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

    private void itemClick(int position) {
        PictureCheckManager.MediaData mediaData = mDataList.get(position);
        if (mediaData.mediaType == 0) {
            mBitImageView.dismiss();
            mBitImageView.setVisibility(View.VISIBLE);
            close.setVisibility(View.VISIBLE);
            File file = new File(mediaData.path);
            mBitImageView.setFile(file);
        } else {
            List<WatchVideoActivity.MediaData> datas = new ArrayList<>();
            int index = 0;
            for (PictureCheckManager.MediaData data : mDataList) {
                if (data.path == null || data.folder == null || !mediaData.folder.equalsIgnoreCase(data.folder)) {
                    continue;
                }
                if (data.path.equalsIgnoreCase(mediaData.path)) {
                    index = datas.size();
                }
                WatchVideoActivity.MediaData temp = new WatchVideoActivity.MediaData(data.path, data.path, String.valueOf(mediaType), null);
                datas.add(temp);
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(datas, index);
            }
        }
    }

    public void setData(Map<String, List<PictureCheckManager.MediaData>> datas) {
        if (datas != null) {
            for (String s : datas.keySet()) {
                List<PictureCheckManager.MediaData> collection = datas.get(s);
                if (collection == null) {
                    continue;
                }
                Collections.sort(collection, new Comparator<PictureCheckManager.MediaData>() {
                    @Override
                    public int compare(PictureCheckManager.MediaData o1, PictureCheckManager.MediaData o2) {
                        return (int) (new File(o2.path).length() - new File(o1.path).length());
                    }
                });
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

    public interface OnItemClickListener {
        void onItemClick(List<WatchVideoActivity.MediaData> datas, int index);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
