package com.example.android.askquastionapp.media;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
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
import com.example.android.askquastionapp.VideoPlayerActivity;
import com.example.android.askquastionapp.picture.PhotoImageView;
import com.example.android.askquastionapp.scan.CapturePictureUtil;
import com.example.android.askquastionapp.utils.BrowserUtils;
import com.example.android.askquastionapp.utils.SimpleObserver;
import com.example.android.askquastionapp.video.WatchVideoActivity;
import com.example.android.askquastionapp.views.BottomPop;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.os.Build.VERSION_CODES.N;

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

    private View mView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), getLayoutId(), null);
        mView = view;
        dialog.setContentView(view);
        mRecyclerView = view.findViewById(R.id.photo_data);
        mBitImageView = view.findViewById(R.id.big_image_view);
        close = view.findViewById(R.id.close);
        close.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            mBitImageView.setVisibility(View.GONE);
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
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mBitImageView.getVisibility() == View.VISIBLE) {
                    close.callOnClick();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    private void scanBitmap(Bitmap bitmap) {
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
                if (mDataList.size() > position && mDataList.get(position).path == null) {
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
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mView == null) {
                            return false;
                        }
                        int position = (int) view.getTag();
                        PictureCheckManager.MediaData mediaData = mDataList.get(position);
                        BottomPop current = BottomPop.getCurrent(getActivity());
                        current.addItemText("排序方式");
                        current.addItemText("复制");
                        current.addItemText("删除");
                        current.addItemText("分享");
                        if (mediaData.mediaType == 0) {
                            current.addItemText("扫描");
                        }
                        current.show(mView);
                        current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
                            @Override
                            public void onItemClick(BottomPop bottomPop, int position) {
                                String tag = bottomPop.getPosition(position);
                                bottomPop.dismiss();
                                String filePath = mediaData.path;
                                File file = new File(filePath);
                                switch (tag) {
                                    case "复制":
                                        if (getActivity() == null) {
                                            return;
                                        }
                                        ClipboardManager clipboardmanager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        if (clipboardmanager == null) {
                                            return;
                                        }
                                        Uri copyUri = Uri.parse(filePath);
                                        ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "URI", copyUri);
                                        clipboardmanager.setPrimaryClip(clip);
                                        ToastUtils.showShort(filePath + "\n已复制到剪贴板");
                                        break;
                                    case "删除":
                                        boolean delete = file.delete();
                                        mDataList.remove(mediaData);
                                        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
                                            mRecyclerView.getAdapter().notifyDataSetChanged();
                                        }
                                        ToastUtils.showShort(delete ? "删除成功" : "删除失败");
                                        break;
                                    case "分享":
                                        Intent shareIntent = new Intent();
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        shareIntent.setType("video/*");
                                        Uri currentUri = getCurrentUri(filePath);
                                        if (currentUri == null) {
                                            return;
                                        }
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, currentUri);
                                        startActivity(Intent.createChooser(shareIntent, "Here is the title of video"));
                                        break;
                                    case "扫描":
                                        ToastUtils.showShort("正在捕获图片");
                                        Observable.just(filePath).map(new Function<String, Bitmap>() {
                                            @Override
                                            public Bitmap apply(String s) throws Exception {
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inJustDecodeBounds = true;
                                                BitmapFactory.decodeFile(s, options);
                                                int bitmapWidth = options.outWidth;
                                                int bitmapHeight = options.outHeight;
                                                int screenWidth = ScreenUtils.getScreenWidth();
                                                int screenHeight = ScreenUtils.getScreenHeight();
                                                float inSampleSize = Math.max(1f * bitmapWidth / screenWidth, 1f * bitmapHeight / screenHeight);
                                                options.inSampleSize = (int) Math.ceil(inSampleSize);
                                                options.inJustDecodeBounds = false;
                                                return BitmapFactory.decodeFile(s, options);
                                            }
                                        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new SimpleObserver<Bitmap, Integer>(1, false) {
                                                    @Override
                                                    public void onNext(Bitmap bitmap, Integer integer) {
                                                        ToastUtils.showShort("已捕获，正在扫描");
                                                        scanBitmap(bitmap);
                                                    }
                                                });
                                        break;
                                    case "排序方式":
                                        PictureCheckManager.getInstance().setSortType((PictureCheckManager.getInstance().getSortType() + 1) % 3);
                                        dismissAllowingStateLoss();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        return false;
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
        loadData();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                case 1:
                    Object obj = msg.obj;
                    int position = 0;
                    if (obj instanceof PictureCheckManager.MediaData) {
                        if (((PictureCheckManager.MediaData) obj).path != null) {
                            for (int i = 0; i < mDataList.size(); i++) {
                                if (mDataList.get(i).path == null && ((PictureCheckManager.MediaData) obj).folder.equalsIgnoreCase(mDataList.get(i).folder)) {
                                    mDataList.add(i + 1, (PictureCheckManager.MediaData) obj);
                                    position = i + 1;
                                    break;
                                }
                            }
                        } else {
                            mDataList.add((PictureCheckManager.MediaData) obj);
                            position = mDataList.size();
                        }
                        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
                            mRecyclerView.getAdapter().notifyItemChanged(position);
                        }
                    }
                    break;
            }
        }
    };

    public void loadData() {
        mDataList.clear();
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mediaType == 0) {
                    PictureCheckManager.getInstance().getNormalPictures(mHandler, mediaType);
                } else if (mediaType == 1) {
                    PictureCheckManager.getInstance().getNormalVideos(mHandler, mediaType);
                } else {
                    PictureCheckManager.getInstance().getNormalPictures(mHandler, mediaType);
                }
            }
        }.start();
    }

    private void itemClick(int position) {
        PictureCheckManager.MediaData mediaData = mDataList.get(position);
        if (mediaData.mediaType == 0) {
//            mBitImageView.dismiss();
//            mBitImageView.setVisibility(View.VISIBLE);
//            close.setVisibility(View.VISIBLE);
//            File file = new File(mediaData.path);
//            mBitImageView.setFile(file);
            List<PictureCheckManager.MediaData> datas = new ArrayList<>();
            int index = 0;
            for (PictureCheckManager.MediaData data : mDataList) {
                if (data.path == null || data.folder == null || !mediaData.folder.equalsIgnoreCase(data.folder)) {
                    continue;
                }
                if (data.path.equalsIgnoreCase(mediaData.path)) {
                    index = datas.size();
                }
                datas.add(data);
            }
            BigPhotoGlanceActivity.start(getContext(), datas, index);
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
            VideoPlayerActivity.start(this, datas, index);
        }
    }

    public void remove(WatchVideoActivity.MediaData mediaData) {
        for (PictureCheckManager.MediaData data : mDataList) {
            if (data.path != null && data.path.equalsIgnoreCase(mediaData.url)) {
                mDataList.remove(data);
                break;
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

    private Uri getCurrentUri(String filePath) {
        if (getActivity() == null) {
            return null;
        }
        File file = new File(filePath);
        if (Build.VERSION.SDK_INT < N) {
            return Uri.fromFile(file);
        } else if (Build.VERSION.SDK_INT < 29) {
            return FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".FileProvider", file);
        } else {
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (file.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
