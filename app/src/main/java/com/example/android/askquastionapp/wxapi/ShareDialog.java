package com.example.android.askquastionapp.wxapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.FileUtil;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.example.android.askquastionapp.utils.FileUtil.getDataColumn;
import static com.example.android.askquastionapp.utils.FileUtil.isDownloadsDocument;
import static com.example.android.askquastionapp.utils.FileUtil.isExternalStorageDocument;
import static com.example.android.askquastionapp.utils.FileUtil.isGooglePhotosUri;
import static com.example.android.askquastionapp.utils.FileUtil.isMediaDocument;

/**
 * @author wangzhilong
 */
@SuppressLint("ValidFragment")
public class ShareDialog extends DialogFragment {

    private EditText shareUrl;
    private EditText imageUrl;
    private EditText shareTitle;
    private EditText shareText;
    private ImageView preView;
    private String selectPath;
    public static final int REQ_CAMERA_CODE = 100;
    private final static int SELECT_PHOTO_CODE = 101;
    private final static int CROP_PHOTO_CODE = 102;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_share_dialog, container, false);
        shareUrl = rootView.findViewById(R.id.share_url);
        imageUrl = rootView.findViewById(R.id.image_url);
        shareTitle = rootView.findViewById(R.id.share_title);
        shareText = rootView.findViewById(R.id.share_text);
        preView = rootView.findViewById(R.id.preview_image);
        rootView.setOnClickListener(v -> dismiss());
        rootView.findViewById(R.id.select_image).setOnClickListener(v -> selectImage());
        rootView.findViewById(R.id.share_to_wechat).setOnClickListener(v -> checkShare(1));
        rootView.findViewById(R.id.share_to_friend).setOnClickListener(v -> checkShare(2));
        rootView.findViewById(R.id.center_lay).setOnClickListener(v -> {
        });
        mContext = getContext();
        return rootView;
    }

    public static ShareDialog showDialog(FragmentActivity activity) {
        ShareDialog dialog = new ShareDialog();
        dialog.show(activity.getSupportFragmentManager(), ShareDialog.class.getSimpleName());
        return dialog;
    }

    private void checkShare(int weixin) {
        if (TextUtils.isEmpty(imageUrl.getText().toString()) && TextUtils.isEmpty(selectPath)) {
            ToastUtils.showShort("请选好图片");
            return;
        }
        if (TextUtils.isEmpty(shareTitle.getText().toString())) {
            ToastUtils.showShort("请输入分享标题");
            return;
        }
        if (TextUtils.isEmpty(shareText.getText().toString())) {
            ToastUtils.showShort("请输入分享内容");
            return;
        }
        if (TextUtils.isEmpty(shareUrl.getText().toString())) {
            ToastUtils.showShort("请输入分享链接");
            return;
        }
        if (TextUtils.isEmpty(selectPath)) {
            share(shareUrl.getText().toString(), imageUrl.getText().toString(), shareTitle.getText().toString(), shareText.getText().toString(), weixin);
        } else {
            share(shareUrl.getText().toString(), selectPath, shareTitle.getText().toString(), shareText.getText().toString(), weixin);
        }
    }

    public static final String APP_ID = "wxc7d0db8854fadb5b";
    public static final String APP_KEY = "63c8562f0a36e601bae0dcd0ff029624";
    private IWXAPI api;

    private void share(String shareUrl, String image, String title, String text, int weixin) {
        if (getActivity() == null) {
            return;
        }
        if (api == null) {
            api = WXAPIFactory.createWXAPI(getActivity(), APP_ID, true);//创建一个实例
            api.registerApp(APP_ID);//注册实例
        }
        try {
            if (weixin == 1) {
                SendMessageToWX.Req req = getReq(shareUrl, image, title, text, SendMessageToWX.Req.WXSceneSession, image);
                api.sendReq(req);
            } else {
                SendMessageToWX.Req req = getReq(shareUrl, image, title, text, SendMessageToWX.Req.WXSceneTimeline, image);
                api.sendReq(req);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private SendMessageToWX.Req getReq(String shareUrl, String imageUrl, String title, String content, int wechatType, @Nullable String mediaPath) throws IOException {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareUrl;//分享出去的网页地址
        WXMediaMessage msg = new WXMediaMessage(webpage);
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            msg.title = "微信";//分享的标题
        } else if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
            msg.title = content;//分享的标题
        } else {
            msg.title = title;//分享的标题
            msg.description = content;//分享的描述信息
        }
        //获取网络图片资源
        Bitmap bmp = null;
        if (TextUtils.isEmpty(mediaPath)) {
            bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
        } else {
            bmp = BitmapFactory.decodeFile(mediaPath, new BitmapFactory.Options());
        }
        //创建缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = wechatType;
        return req;
    }

    public byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void selectImage() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请相机权限
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_CAMERA_CODE);
            return;
        }
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                    .Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.setType("image/*");
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            ((Activity) mContext).startActivityForResult(intent, SELECT_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SELECT_PHOTO_CODE:
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String cameraPath = path + File.separator + "Camera" + File.separator;
                selectPath = cameraPath + "coverImg" + System.currentTimeMillis() / 1000 + ".jpg";
                String sourcePath = getImageAbsolutePath(mContext, data.getData());
                try {
                    //剪切原图片路径和输出路径不能相同否则图片大小信息不能在MIUI8上更新，图片应该在剪切成功后再保存到要上传的路径
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    if (!TextUtils.isEmpty(sourcePath)) {
                        intent.setDataAndType(FileUtil.getUriFromFile(mContext, new File(sourcePath)), "image/*");
                    }
                    // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
                    intent.putExtra("crop", "true");
                    // aspectX aspectY 是宽高的比例
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    // outputX outputY 是裁剪图片宽高
                    intent.putExtra("outputX", 420);
                    intent.putExtra("outputY", 420);
                    intent.putExtra("scale", true);
                    intent.putExtra("scaleUpIfNeeded", true);
                    intent.putExtra("return-data", false);
                    //剪切后的图片直接保存到要上传的路径
                    if (!TextUtils.isEmpty(selectPath)) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(selectPath)));
                    }
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).startActivityForResult(intent, CROP_PHOTO_CODE);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case CROP_PHOTO_CODE:
                if (!TextUtils.isEmpty(selectPath)) {
                    Bitmap bm = BitmapFactory.decodeFile(selectPath);
                    preView.setImageBitmap(bm);
                    preView.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @date 2014-10-12
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }
}
