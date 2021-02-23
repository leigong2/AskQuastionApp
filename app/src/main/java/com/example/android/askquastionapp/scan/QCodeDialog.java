package com.example.android.askquastionapp.scan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QCodeDialog extends DialogFragment {

    private Bitmap qCodeBitmap;

    public static QCodeDialog showDialog(FragmentActivity activity) {
        QCodeDialog dialog = new QCodeDialog();
        dialog.show(activity.getSupportFragmentManager(), QCodeDialog.class.getSimpleName());
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_q_code, container, false);
        ImageView imageView = rootView.findViewById(R.id.image_view);
        if (getActivity() == null) {
            dismissAllowingStateLoss();
            return rootView;
        }
        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = cm.getPrimaryClip();
        if (primaryClip != null && primaryClip.getItemAt(0) != null) {
            if (primaryClip.getItemAt(0).getText() != null) {
                String s = primaryClip.getItemAt(0).getText().toString();
                qCodeBitmap = CapturePictureUtil.getQCodeBitmap(s);
                imageView.setImageBitmap(qCodeBitmap);
            }
        }
        rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qCodeBitmap == null) {
                    return;
                }
                CapturePictureUtil.saveImageToGallery(getActivity(), qCodeBitmap, "Pictures");
                ToastUtils.showShort("已将剪切板生成二维码并保存至本地");
                dismissAllowingStateLoss();
            }
        });
        rootView.setOnClickListener(view -> dismissAllowingStateLoss());
        return rootView;
    }
}
