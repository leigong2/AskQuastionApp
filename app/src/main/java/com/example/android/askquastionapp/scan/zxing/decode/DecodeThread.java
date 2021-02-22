/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.askquastionapp.scan.zxing.decode;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.example.android.askquastionapp.scan.zxing.android.CaptureActivityHandler;
import com.example.android.askquastionapp.scan.zxing.bean.ZxingConfig;
import com.example.android.askquastionapp.scan.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class DecodeThread extends Thread {

    private final Activity activity;
    private final Hashtable<DecodeHintType, Object> hints;
    private final ViewfinderView viewfinderView;
    private final CaptureActivityHandler captureActivityHandler;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public DecodeThread(Activity activity, ZxingConfig config, ViewfinderView viewfinderView, ResultPointCallback resultPointCallback, CaptureActivityHandler captureActivityHandler) {

        this.activity = activity;
        this.viewfinderView = viewfinderView;
        this.captureActivityHandler = captureActivityHandler;
        handlerInitLatch = new CountDownLatch(1);

        hints = new Hashtable<>();

        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();

        /*是否解析有条形码（一维码）*/
        if (config.isDecodeBarCode()) {
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        }
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);

    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity, hints, viewfinderView, captureActivityHandler);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
