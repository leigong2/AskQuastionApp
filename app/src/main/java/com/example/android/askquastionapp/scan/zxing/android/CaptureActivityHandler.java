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

package com.example.android.askquastionapp.scan.zxing.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.android.askquastionapp.scan.zxing.bean.ZxingConfig;
import com.example.android.askquastionapp.scan.zxing.camear.CameraManager;
import com.example.android.askquastionapp.scan.zxing.common.Constant;
import com.example.android.askquastionapp.scan.zxing.decode.DecodeThread;
import com.example.android.askquastionapp.scan.zxing.view.ViewfinderResultPointCallback;
import com.example.android.askquastionapp.scan.zxing.view.ViewfinderView;
import com.google.zxing.Result;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture. 该类用于处理有关拍摄状态的所有信息
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class
            .getSimpleName();

    private final Activity activity;
    private final DecodeThread decodeThread;
    private final ViewfinderView viewfinderView;
    private State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(Activity activity, ZxingConfig config, ViewfinderView viewfinderView, CameraManager cameraManager) {
        this.activity = activity;
        this.viewfinderView = viewfinderView;
        decodeThread = new DecodeThread(activity, config, viewfinderView, new ViewfinderResultPointCallback(viewfinderView), this);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        // 开始拍摄预览和解码
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Constant.RESTART_PREVIEW:
                // 重新预览
                restartPreviewAndDecode();
                break;
            case Constant.DECODE_SUCCEEDED:
                // 解码成功
                state = State.SUCCESS;
                String text = ((Result) message.obj).getText();
                Log.e("zune", "解码成功" + "text = " + text);
                if (callBack != null) {
                    callBack.onScanResult(text);
                }
                break;
            case Constant.DECODE_FAILED:
                // 尽可能快的解码，以便可以在解码失败时，开始另一次解码
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(),
                        Constant.DECODE);
                break;
            case Constant.RETURN_SCAN_RESULT:
                activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                activity.finish();
                break;
            case Constant.FLASH_OPEN:
//                activity.switchFlashImg(Constant.FLASH_OPEN);
                break;
            case Constant.FLASH_CLOSE:
//                activity.switchFlashImg(Constant.FLASH_CLOSE);
                break;
            default:
                break;
        }
    }

    /**
     * 完全退出
     */
    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Constant.QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        //确保不会发送任何队列消息
        removeMessages(Constant.DECODE_SUCCEEDED);
        removeMessages(Constant.DECODE_FAILED);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(),
                    Constant.DECODE);
            if (viewfinderView != null) {
                viewfinderView.drawViewfinder();
            }
        }
    }

    public interface CallBack {
        void onScanResult(String data);
    }

    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
