package com.example.android.askquastionapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.askquastionapp.location.LatLngBean;
import com.example.jsoup.GsonGetter;
import com.example.android.askquastionapp.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapUtils {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private FragmentActivity mContext;
    private LocationRequest mLocationRequest;
    private int sCount = 0;
    private float sSurvideRate = 0;
    // 位置监听
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 精确度越小越准，单位：米
            if (location.getAccuracy() > 100) {
                return;
            }
            if (onLocationListener != null) {
                onLocationListener.onLocationChange(location);
            }
        }
    };
    private boolean customSetting;

    public MapUtils(FragmentActivity activity) {
        this.mContext = activity;
    }

    /**
     * zune: 是否要自定义设置的属性
     **/
    public void setCustomSetting(boolean customSetting) {
        this.customSetting = customSetting;
    }

    public void onCreate(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });
        if (!customSetting) {
            mapSetting();
        }
        startLocationWithPermission();
    }

    public void startLocationWithPermission() {
        /**zune: 检查权限**/
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            } else {
                startLocation();
            }
        }
    }

    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();// 连接Google Play服务
            Log.i("zune", "onStart");
        }
    }

    public void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();// 停止位置更新
            Log.i("zune", "onPause");
        }
    }

    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();// 断开连接
            Log.i("zune", "onStop");
        }
    }

    public void onDestroy() {
        Log.i("zune", "onDestroy");
        if (mContext != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(mContext);
        }
        if (mGoogleMap != null) {
            mGoogleMap.stopAnimation();
        }
        mLocationRequest = null;
        mGoogleMap = null;
        mGoogleApiClient = null;
        mContext = null;
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        if (mGoogleApiClient == null || mLocationRequest == null) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient == null) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
    }


    @SuppressLint("MissingPermission")
    private void mapSetting() {
        /**zune: 地图类型**/
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        /**zune: 定位精度**/
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocation() {
        /**zune: 开启定位服务**/
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i("zune", "定位连接成功");
                        @SuppressLint("MissingPermission")
                        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        if (onLocationListener != null) {
                            onLocationListener.onLocation(latLng);
                        }
                        // 启动位置更新
                        startLocationUpdates();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i("zune", "onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> {
                    if (onLocationListener != null) {
                        onLocationListener.onFailed();
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    public Location getGpsInfo(Context context) {
        Location location = null;
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
            criteria.setAltitudeRequired(false);//不要求海拔
            criteria.setBearingRequired(false);//不要求方位
            criteria.setCostAllowed(true);//允许有花费
            criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //从可用的位置提供器中，匹配以上标准的最佳提供器
            if (locationManager != null) {
                String locationProvider = locationManager.getBestProvider(criteria, true);
                location = locationManager.getLastKnownLocation(locationProvider);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return location;
    }

    public void getGeocoder(double lat, double lng, OnResultListener listener) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("经纬度是lat = %s, lng = %s\n", dispatchDouble(lat, 3), dispatchDouble(lng, 3)));
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Observable.just(geocoder).map(new Function<Geocoder, List<Address>>() {
            @Override
            public List<Address> apply(Geocoder geocoder) throws Exception {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                return addresses;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Address>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<Address> addresses) {
                        StringBuilder title = new StringBuilder();
                        if (addresses != null && !addresses.isEmpty()) {
                            for (Address address : addresses) {
                                sb.append(address.getCountryName()).append("\n");
                                try {
                                    sb.append(address.getAddressLine(0));
                                } catch (Exception ignore) {
                                }
                                if (!TextUtils.isEmpty(address.getFeatureName()) && address.getFeatureName().equals(address.getCountryName())) {
                                    sb.append(address.getFeatureName()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getAdminArea())) {
                                    sb.append(address.getAdminArea()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getSubAdminArea()) && address.getSubAdminArea().equals(address.getAdminArea())) {
                                    sb.append(address.getSubAdminArea()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getLocality())) {
                                    sb.append(address.getLocality()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getSubLocality()) && address.getSubLocality().equals(address.getLocality())) {
                                    sb.append(address.getSubLocality()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getSubThoroughfare())) {
                                    sb.append(address.getSubThoroughfare()).append("\n");
                                }
                                if (!TextUtils.isEmpty(address.getThoroughfare()) && address.getThoroughfare().equals(address.getSubThoroughfare())) {
                                    sb.append(address.getThoroughfare()).append("\n");
                                }
                                if (TextUtils.isEmpty(address.getCountryName())
                                        || "Ocean".equals(address.getCountryName())
                                        || "南极洲".equals(address.getCountryName())) {
                                    sb.append("你嗝屁了，再来一次吧！\n ");
                                    title.append(address.getCountryName());
                                    title.append(String.format("...存活几率%s ", 0)).append("%");
                                    sSurvideRate += 0;
                                } else if (TextUtils.isEmpty(address.getAdminArea())) {
                                    sb.append("没有省份，存活几率10% \n");
                                    sSurvideRate += 0.1f;
                                    title.delete(0, title.length());
                                    title.append(address.getCountryName());
                                    title.append(String.format("...存活几率%s ", 10)).append("%");
                                } else if (TextUtils.isEmpty(address.getSubAdminArea()) && TextUtils.isEmpty(address.getLocality())) {
                                    title.delete(0, title.length());
                                    title.append(address.getAdminArea());
                                    sb.append("没有市，存活几率50% \n");
                                    title.append(String.format("...存活几率%s ", 50)).append("%");
                                    sSurvideRate += 0.5f;
                                } else if (TextUtils.isEmpty(address.getLocality())) {
                                    title.delete(0, title.length());
                                    if (TextUtils.isEmpty(address.getSubAdminArea())) {
                                        title.append(address.getLocality());
                                    } else {
                                        title.append(address.getSubAdminArea());
                                    }
                                    sb.append("没有区，存活几率90% \n");
                                    title.append(String.format("...存活几率%s ", 90)).append("%");
                                    sSurvideRate += 0.9f;
                                } else if (TextUtils.isEmpty(address.getThoroughfare()) && TextUtils.isEmpty(address.getSubThoroughfare())) {
                                    sb.append("没有大街道，存活几率100% \n");
                                    sSurvideRate += 1f;
                                    title.delete(0, title.length());
                                    title.append(address.getLocality());
                                    title.append(String.format("...存活几率%s ", 100)).append("%");
                                } else if (TextUtils.isEmpty(address.getThoroughfare())) {
                                    sb.append("没有小街道，存活几率100% \n");
                                    sSurvideRate += 1f;
                                    title.delete(0, title.length());
                                    title.append(address.getSubThoroughfare());
                                    title.append(String.format("...存活几率%s ", 100)).append("%");
                                } else {
                                    title.delete(0, title.length());
                                    title.append(address.getThoroughfare());
                                    title.append(String.format("...存活几率%s ", 100)).append("%");
                                    sb.append("存活几率100% \n");
                                    sSurvideRate += 1f;
                                }
                                sCount++;
                                sb.append("总的存活几率是").append(dispatchDouble(sSurvideRate / sCount, 4));
                            }
                        } else {
                            sb.append("Ocean \n");
                            title.append("Ocean");
                            title.append(String.format("...存活几率%s ", 0)).append("%");
                            sb.append("你嗝屁了，再来一次吧！\n ");
                            sSurvideRate += 0;
                            sCount++;
                            sb.append("总的存活几率是").append(dispatchDouble(sSurvideRate / sCount, 4));
                        }
                        Log.e("zune", title.toString());
                        if (listener != null) {
                            listener.onResult(sb, title);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void whiteCount(float survideRate, int count) {
        BufferedWriter bw = null;
        try {
            File fileDir = mContext.getExternalCacheDir();
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir, "log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(file, false));
            bw.newLine();
            bw.write(survideRate + "," + count);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save(List<LatLngBean> datas) {
        BufferedWriter bw = null;
        try {
            File fileDir = mContext.getExternalCacheDir();
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir, "location.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(file, true));
            for (LatLngBean data : datas) {
                bw.write(GsonGetter.getInstance().getGson().toJson(data));
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void appendLatLng(double lat, double lng, String title) {
        LatLngBean bean = new LatLngBean();
        bean.lat = lat;
        bean.lng = lng;
        bean.title = title;
        mCurData.add(bean);
    }

    private List<LatLngBean> mCurData = new ArrayList<>();

    public List<LatLngBean> getCurData() {
        return mCurData;
    }

    public List<LatLngBean> getLatLng() {
        BufferedReader br = null;
        List<LatLngBean> temp = new ArrayList<>();
        try {
            File fileDir = mContext.getExternalCacheDir();
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir, "location.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                LatLngBean bean = null;
                try {
                    bean = GsonGetter.getInstance().getGson().fromJson(line, LatLngBean.class);
                } catch (Exception ignore) {
                }
                if (bean != null) {
                    temp.add(bean);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return temp;
    }

    public void deleteData() {
        BufferedWriter bw = null;
        try {
            File fileDir = mContext.getExternalCacheDir();
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir, "location.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(file, false));
            bw.write("");
            bw.close();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String dispatchDouble(double d, int length) {
        StringBuilder sb = new StringBuilder();
        String s = String.valueOf(d);
        if (s.contains(".")) {
            String[] split = s.split("\\.");
            for (int i = 0; i < split.length; i++) {
                if (i == split.length - 1) {
                    if (split[i].length() < length) {
                        sb.append(split[i]);
                    } else {
                        sb.append(split[i].substring(0, length));
                    }
                } else {
                    sb.append(split[i]).append(".");
                }
            }
        } else {
            sb.append(s);
        }
        return sb.toString();
    }

    public void moveTo(double lat, double lng) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10.0f));
    }

    /**
     * zune: 添加标记
     **/
    public void addMark(double lng, double lat, String title) {
        LatLng googleLatLng = new LatLng(lng, lat);
        MarkerOptions marker = new MarkerOptions().position(googleLatLng)
                .title(title).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_custom));
        mGoogleMap.addMarker(marker);
    }

    /**
     * zune: 权限回调
     **/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            startLocation();
        }
    }

    public void addMarks(List<LatLngBean> datas) {
        for (LatLngBean data : datas) {
            addMark(data.lng, data.lat, data.title);
        }
    }

    public interface OnLocationListener {
        void onLocation(LatLng latLng);

        void onFailed();

        void onLocationChange(Location latLng);
    }

    private OnLocationListener onLocationListener;

    public void setOnLocationListener(OnLocationListener onLocationListener) {
        this.onLocationListener = onLocationListener;
    }

    public interface OnResultListener {
        void onResult(StringBuilder sb, StringBuilder title);
    }
}
