package com.example.android.askquastionapp.utils;

/**
 * @author wangzhilong
 */
public class BaiduMapUtil {
    /*public static final String baiduKey = "lfZm7KsjMpmee9EM2zSUABQkKuwDFqxD";
    private MapView mMapView;
    private BaiduMap mMap;
    private LocationClient mLocationClient;

    public void attachView(MapView mapView) {
        this.mMapView = mapView;
        mMap = mapView.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15));
    }

    public void onPause() {
        mMapView.onPause();
    }

    public void onResume() {
        mMapView.onResume();
    }

    public void onDestroy() {
        mMapView.onDestroy();
    }

    public void startLocation(BDAbstractLocationListener listener) {
        //定位初始化
        mLocationClient = new LocationClient(mMapView.getContext());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(listener);
        //开启地图定位图层
        mLocationClient.start();
    }

    public void moveToCamera(double lat, double lng) {
        mMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(lat, lng)));
    }

    public void addMark(double lat, double lng) {
        //定义Maker坐标点
        LatLng point = new LatLng(lat, lng);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.marker_custom);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point) //必传参数
                .icon(bitmap) //必传参数
                .draggable(true)
        //设置平贴地图，在地图中双指下拉查看效果
                .flat(true)
                .alpha(0.5f);
        //在地图上添加Marker，并显示
        mMap.addOverlay(option);
    }

    public void getGeocoder(double lat, double lng, OnResultListener listener) {
        GeoCoder coder = GeoCoder.newInstance();
        coder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(lat, lng)).radius(500));
        coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            }
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                    return;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(reverseGeoCodeResult.getAddress()).append("\n")
                            .append(reverseGeoCodeResult.getBusinessCircle()).append("\n")
                            .append(reverseGeoCodeResult.getSematicDescription()).append("\n")
                            .append(reverseGeoCodeResult.getAddressDetail()).append("\n");
                    if (listener != null) {
                        listener.onResult(sb);
                    }
                }
            }
        });
    }

    public interface OnResultListener {
        void onResult(StringBuilder sb);
    }*/
}
