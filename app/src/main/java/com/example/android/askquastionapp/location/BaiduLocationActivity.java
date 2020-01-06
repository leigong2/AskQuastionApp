package com.example.android.askquastionapp.location;

import android.support.v7.app.AppCompatActivity;

/**
 * @author wangzhilong
 */
public class BaiduLocationActivity extends AppCompatActivity {
/*
    private MapView mMapView;
    private BaiduMapUtil mBaiduMapUtil;
    private TextView tvContent;

    public static void start(Context context) {
        Intent intent = new Intent(context, BaiduLocationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_location);
        tvContent = findViewById(R.id.content);
        mMapView = (MapView) findViewById(R.id.bmapView);
        tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = Math.random() * 180 - 90;
                double lng = Math.random() * 360 - 180;
                mBaiduMapUtil.moveToCamera(lat, lng);
                mBaiduMapUtil.getGeocoder(lat, lng, new BaiduMapUtil.OnResultListener() {
                    @Override
                    public void onResult(StringBuilder sb) {
                        tvContent.setText(sb.toString());
                    }
                });
            }
        });
        mBaiduMapUtil = new BaiduMapUtil();
        mBaiduMapUtil.attachView(mMapView);
        mBaiduMapUtil.startLocation(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                double lat = bdLocation.getLatitude();
                double lng = bdLocation.getLongitude();
                mBaiduMapUtil.moveToCamera(lat, lng);
                mBaiduMapUtil.addMark(lat, lng);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBaiduMapUtil.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBaiduMapUtil.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMapUtil.onDestroy();
    }*/
}
