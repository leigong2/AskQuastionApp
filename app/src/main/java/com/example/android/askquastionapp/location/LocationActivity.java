package com.example.android.askquastionapp.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.MapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LocationActivity extends AppCompatActivity {
    String googleKey = "AIzaSyA_E7eaNFIUGnh0eEgZsWLp41omPbEkv0g";
    private MapUtils mMapUtils;
    private TextView content;
    private MapUtils.OnResultListener listener;

    public static void start(Context context) {
        Intent intent = new Intent(context, LocationActivity.class);
        context.startActivity(intent);
    }
    double lat = Math.random() * 180 - 90;
    double lng = Math.random() * 360 - 180;
    private int loop = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        content = findViewById(R.id.content);
        content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMapUtils.deleteData();
                return false;
            }
        });
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener = new MapUtils.OnResultListener() {
                    @Override
                    public void onResult(StringBuilder geocoder, StringBuilder title) {
                        if (++loop >= 10) {
                            content.setText(geocoder.toString());
                            mMapUtils.moveTo(lat, lng);
                            mMapUtils.addMark(lat, lng, title.toString());
                            mMapUtils.appendLatLng(lat, lng, title.toString());
                            mMapUtils.save(mMapUtils.getCurData());
                            return;
                        }
                        content.setText(geocoder.toString());
                        mMapUtils.moveTo(lat, lng);
                        mMapUtils.addMark(lat, lng, title.toString());
                        mMapUtils.appendLatLng(lat, lng, title.toString());
                        lat = Math.random() * 180 - 90;
                        lng = Math.random() * 360 - 180;
                        mMapUtils.getGeocoder(lat, lng, listener);
                    }
                };
                lat = Math.random() * 180 - 90;
                lng = Math.random() * 360 - 180;
                mMapUtils.getGeocoder(lat, lng, listener);
            }
        });
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) fragment;
        mMapUtils = new MapUtils(this);
        mMapUtils.setCustomSetting(false);
        mMapUtils.setOnLocationListener(new MapUtils.OnLocationListener() {
            @Override
            public void onLocation(LatLng latLng) {
                Log.i("zune", "定位成功" + latLng.latitude + "........" + latLng.longitude);
                mMapUtils.moveTo(latLng.latitude, latLng.longitude);
            }

            @Override
            public void onFailed() {
                Log.i("zune", "定位失败");
            }

            @Override
            public void onLocationChange(Location location) {
                Log.i("zune", "位置改变" + location.getLatitude() + "........" + location.getLongitude());
            }
        });
        /**zune: 准备地图**/
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.i("zune", "onMapReady");
                mMapUtils.onCreate(googleMap);
                Location location = mMapUtils.getGpsInfo(LocationActivity.this);
                if (location == null) {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
                    Task<Location> lastLocation = fusedLocationClient.getLastLocation();
                    if (lastLocation != null) {
                        lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                Log.i("zune", "getLastLocation  lat = " + location.getLatitude() + "， lng = " + location.getLongitude());
                                mMapUtils.moveTo(location.getLatitude(), location.getLongitude());
                                mMapUtils.getGeocoder(location.getLatitude(), location.getLongitude(), new MapUtils.OnResultListener() {
                                    @Override
                                    public void onResult(StringBuilder sb, StringBuilder title) {
                                        content.setText(sb.toString());
                                    }
                                });
                            }
                        });
                    }
                    return;
                }
                mMapUtils.moveTo(location.getLatitude(), location.getLongitude());
                mMapUtils.getGeocoder(location.getLatitude(), location.getLongitude(), new MapUtils.OnResultListener() {
                    @Override
                    public void onResult(StringBuilder sb, StringBuilder title) {
                        content.setText(sb.toString());
                    }
                });
            }
        });
        Observable.just(1).map(new Function<Integer, List<LatLngBean>>() {
            @Override
            public List<LatLngBean> apply(Integer integer) throws Exception {
                return mMapUtils.getLatLng();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<LatLngBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(List<LatLngBean> o) {
                        mMapUtils.getCurData().addAll(o);
                        mMapUtils.addMarks(o);
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapUtils.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapUtils.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapUtils.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapUtils.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mMapUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
