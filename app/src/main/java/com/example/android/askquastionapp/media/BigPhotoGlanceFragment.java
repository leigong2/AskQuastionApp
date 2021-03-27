package com.example.android.askquastionapp.media;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.picture.PhotoImageView;
import com.example.android.askquastionapp.scan.CapturePictureUtil;
import com.example.android.askquastionapp.utils.BrowserUtils;
import com.example.android.askquastionapp.views.BottomPop;

import java.io.File;

public class BigPhotoGlanceFragment extends Fragment {

    private PhotoImageView bigImageView;
    private Button rotationView;

    public static BigPhotoGlanceFragment getInstance() {
        return new BigPhotoGlanceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        initView(view);
        view.setOnLongClickListener(v -> onLongClick());
        return view;
    }

    private void initView(View view) {
        bigImageView = view.findViewById(R.id.big_image_view);
        bigImageView.setOnLongClickListener(v -> onLongClick());
        bigImageView.setSimple(false);
        rotationView = view.findViewById(R.id.remote_image);
        rotationView.setOnClickListener(this::startRemote);
    }

    private int mCurrentRotation;

    private void startRemote(View v) {
        new AlertDialog.Builder(getContext())
                .setTitle("顺时针旋转")
                .setNegativeButton("更改原图", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mCurrentRotation = mCurrentRotation % 360 + 90;
                        bigImageView.setCurRotation(mCurrentRotation, true);
                        ((Button) v).setText(String.valueOf(mCurrentRotation));
                    }
                })
                .setPositiveButton("不更改原图", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mCurrentRotation = mCurrentRotation % 360 + 90;
                        bigImageView.setCurRotation(mCurrentRotation, false);
                        ((Button) v).setText(String.valueOf(mCurrentRotation));
                    }
                }).create().show();
    }

    @Override
    public void onStop() {
        super.onStop();
        bigImageView.dismiss();
    }

    private int getLayoutId() {
        return R.layout.fragment_big_photo_glance;
    }

    private boolean onLongClick() {
        BottomPop current = BottomPop.getCurrent(getActivity());
        current.addItemText("扫描");
        current.addItemText("删除");
        current.show(bigImageView);
        current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
            @Override
            public void onItemClick(BottomPop bottomPop, int position) {
                String tag = bottomPop.getPosition(position);
                switch (tag) {
                    case "扫描":
                        bottomPop.dismiss();
                        if (bigImageView != null) {
                            Bitmap bitmap = bigImageView.getImageBitmap();
                            ToastUtils.showShort("已捕获，正在扫描");
                            scanBitmap(bitmap);
                        }
                        break;
                    case "删除":
                        String filePath = mediaData.path;
                        File file = new File(filePath);
                        bottomPop.dismiss();
                        boolean delete = file.delete();
                        ToastUtils.showShort(delete ? "删除成功" : "删除失败");
                        if (getActivity() instanceof BigPhotoGlanceActivity) {
                            ((BigPhotoGlanceActivity) getActivity()).removeItem(mediaData);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        return false;
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

    private PictureCheckManager.MediaData mediaData;

    public void loadPicture(PictureCheckManager.MediaData mediaData) {
        if (mediaData == null || TextUtils.isEmpty(mediaData.path)) {
            return;
        }
        rotationView.setText("旋转");
        this.mediaData = mediaData;
        File file = new File(mediaData.path);
        bigImageView.setVisibility(View.VISIBLE);
        bigImageView.setFile(file, true);
    }
}
