package com.example.android.askquastionapp.video.camera2;

import android.net.Uri;

public interface Camera2Delegate {

    void onRecordStarted(boolean started);

    void onRecordStoped(Uri uri);
}
