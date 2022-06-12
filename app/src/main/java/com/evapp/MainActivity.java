package com.evapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.techtown.carchap_v11.API_corona.CoronaApi;
import org.techtown.carchap_v11.API_corona.corona_item;
import org.techtown.carchap_v11.API_hydrogenChargingStation.hydrogen_station_item;
import org.techtown.carchap_v11.Dialog.ConnectDialog;
import org.techtown.carchap_v11.databinding.ActivityMapCoronaBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnMarkerClickListener {

    private int apiRequestCount;
    public static GoogleMap mMap;
    public static boolean startFlagForCoronaApi;
    private ArrayList<Marker> markerList = new ArrayList();
    public static ArrayList<ev_item> ev_list = new ArrayList();

    private ActivityMapCoronaBinding binding;
    private BottomSheetBehavior mBottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityMapCoronaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_corona);
        mapFragment.getMapAsync(this);

        binding.rootBottomSheet.setVisibility(View.INVISIBLE);
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

        binding.mapZoomPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        binding.mapZoomMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //리스너 달아주기
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);


        //zoom level 별 지도 크기 예시
        //  1   : 세계
        //  5   : 대륙
        //  10  : 시
        //  15  : 거리
        //  20  : 건물
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.510759, 126.977943), 15));


    }

    @Override
    public void onCameraIdle() {
        removeMarkerAll();

        String lat = String.valueOf(mMap.getCameraPosition().target.latitude);
        String lon = String.valueOf(mMap.getCameraPosition().target.longitude);
        startFlagForCoronaApi = true;
        evApi coronaApi = new evApi();
        coronaApi.execute(lat,lon,"");

        apiRequestCount = 0;
        final Handler temp_handler = new Handler();
        temp_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (apiRequestCount < 100) {
                    if (startFlagForCoronaApi) {
                        apiRequestCount++;
                        temp_handler.postDelayed(this, 100);
                    } else {
                        //api 호출이 완료 되었을 떄
                        drawMarker();
                    }
                } else {
                    //api 호출이 10초 이상 경괴했을 때
                    Toast.makeText(getApplicationContext(), "호출에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                }

            }


        }, 100);
    }

    private void removeMarkerAll() {
        for (Marker marker : markerList) {
            marker.remove();
        }

    }

    private void drawMarker() {
        for (int i =0 ; i< corona_list.size(); i++){
            corona_item item = corona_list.get(i);
            String remain_stat =item.getRemain_stat();
            switch (remain_stat) {
                case "plenty" : {
                    remain_stat = "100개이상";
                    break;
                }
                case "some" : {
                    remain_stat = "30개 이상 100개 미만";
                    break;
                }
                case "few" : {
                    remain_stat = "2개 이상 30개 미만";
                    break;
                }
                case "empty" : {
                    remain_stat = "1개 이하";
                    break;
                }
            }
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLng()) ))
                    .title(item.getName())
                    .snippet(item.getAddr()+"@"+item.getCreated_at()+"@"+item.getRemain_stat()+"@"+item.getStock_at()+"@"+item.getType())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_xingxing)));
            markerList.add(marker);

        }
        return;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("onMarkerClick", "click");

        binding.rootBottomSheet.setVisibility(View.GONE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        Animation animation_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enter_from_bottom);
        binding.rootBottomSheet.setAnimation(animation_up);
        binding.rootBottomSheet.setVisibility(View.VISIBLE);

        String addr= marker.getSnippet().split("@")[0];
        String created_at= marker.getSnippet().split("@")[1];
        String remain_stat= marker.getSnippet().split("@")[2];
        String stock_at= marker.getSnippet().split("@")[3];
        String type= marker.getSnippet().split("@")[4];

        switch (type) {
            case "01" :{
                type = "약국";
                break;
            }
            case "02" :{
                type = "우체국";
                break;
            }
            case "03" :{
                type = "농협";
                break;
            }
        }

        switch (remain_stat) {
            case "plenty" : {
                remain_stat = "100개이상";
                break;
            }
            case "some" : {
                remain_stat = "30개 이상 100개 미만";
                break;
            }
            case "few" : {
                remain_stat = "2개 이상 30개 미만";
                break;
            }
            case "empty" : {
                remain_stat = "1개 이하";
                break;
            }
        }
        binding.bottomInfoAddress.setText(addr);
        binding.bottomInfoPlaceName.setText(marker.getTitle());
        binding.bottomInfoType.setText(type);
        binding.bottomInfoRemain.setText(remain_stat);
        binding.bottomInfoUpdate.setText(stock_at);


        return true;
    }

    @Override
    public void onCameraMoveStarted(int i) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}