//package com.example.android.askquastionapp.utils;
//
//import android.app.Activity;
//import android.content.Intent;
//
//import androidx.annotation.Nullable;
//
//import com.guochao.faceshow.aaspring.base.activity.BaseActivity;
//import com.guochao.faceshow.aaspring.modulars.login.utils.ThirdPartyLoginManager;
//import com.guochao.faceshow.aaspring.utils.LogUtils;
//import com.guochao.faceshow.utils.HandlerGetter;
//
//import org.jetbrains.annotations.NotNull;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.FormBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//import static com.guochao.faceshow.aaspring.modulars.login.utils.ins.InstagramLoginActivity.INSTAGRAM_REQUEST_CODE;
//
//public class InstagramLoginManager extends ThirdPartyLoginManager {
//    public static final String appId = "896255734201697";
//    public static final String appSecret = "7716268f129ecb1e6858358462cc09d7";
//    public static final String redirect_uri = "https://socialsizzle.herokuapp.com/auth/";
//    private String client_id = appId;
//    private Runnable mTimeoutRunnable;
//
//    public InstagramLoginManager(Activity context) {
//        super(context);
//        mTimeoutRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mContext instanceof BaseActivity) {
//                    ((BaseActivity) mContext).dismissProgressDialog();
//                }
//            }
//        };
//    }
//
//    @Override
//    public void startLogin() {
//        String preUrl = String.format("https://api.instagram.com/oauth/authorize/?force_authentication=1" +
//                "&client_id=%s&redirect_uri=%s&scope=%s&response_type=code", client_id, redirect_uri, "user_profile,user_media");
//        InstagramLoginActivity.startWeb((Activity) mContext
//                , preUrl
//                , new OnValueCallBack() {
//                    @Override
//                    public void onResult(String code) {
//                        getShortTokenWithCode(code);
//                    }
//
//                    @Override
//                    public void onError() {
//                        callFail(-1, "");
//                    }
//                });
//    }
//
//    private void getShortTokenWithCode(String code) {
//        HandlerGetter.getMainHandler().postDelayed(mTimeoutRunnable, 30000);
//        String url = "https://api.instagram.com/oauth/access_token";
//        OkHttpClient mOkHttpClient = new OkHttpClient();
//        Map<String, String> map = new HashMap<>();
//        map.put("client_id", client_id);
//        map.put("client_secret", appSecret);
//        LogUtils.i("zune：", "code = " + code);
//        map.put("code", code);
//        map.put("grant_type", "authorization_code");
//        map.put("redirect_uri", redirect_uri);
//        FormBody.Builder builder = new FormBody.Builder();
//        for (String s : map.keySet()) {
//            builder.add(s, Objects.requireNonNull(map.get(s)));
//        }
//        FormBody body = builder.build();
//        final Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//        mOkHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                callFail(-1, e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                String body = response.body().string();
//                LogUtils.i("zune：", "getCode = " + body);
//                try {
//                    JSONObject jsonObject = new JSONObject(body);
//                    String access_token = (String) jsonObject.get("access_token");
//                    Long user_id = (Long) jsonObject.get("user_id");
//                    getData(String.valueOf(user_id), access_token);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    public void getData(String userId, String token) {
//        OkHttpClient mOkHttpClient = new OkHttpClient();
//        final Request request = new Request.Builder()
//                .url("https://graph.instagram.com/" + userId + "/?fields=id,username,ig_id&access_token=" + token)
//                .get()
//                .build();
//        mOkHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                callFail(-1, e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String body = response.body().string();
//                LogUtils.i("zune：", "getData body = " + body);
//                try {
//                    JSONObject jsonObject = new JSONObject(body);
//                    String username = (String) jsonObject.get("username");
//                    HandlerGetter.getMainHandler().post(new Runnable() {
//                        @Override
//                        public void run() {
//                            ThirdPartyUserInfo userInfo = new ThirdPartyUserInfo();
//                            userInfo.setUserId(userId);
//                            userInfo.setNickName(username);
//                            callSuccess(userInfo);
//                        }
//                    });
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == INSTAGRAM_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
//            String token = data.getStringExtra("result");
////            getData(token, token);
//        } else if (requestCode == INSTAGRAM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            callFail(-1, "cancel");
//        }
//    }
//
//    @Override
//    public void logout() {
//    }
//
//    public interface OnValueCallBack {
//        void onResult(String token);
//
//        void onError();
//    }
//}
