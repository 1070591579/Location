package com.example.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public LocationClient mLocationClient;
    private MapView mapView;

    /** 定位相关 经纬度信息 */
    private double mLatitude;
    private double mLongtitude;
    private boolean isFirstLocate = true;
    private TextView positionText  = null;
    /**日志的TAG */
    private static final String TAG;
    static {
        TAG = "MainActivity";
    }
    private BaiduMap baiduMap;
    private LatLng mLastLocationData;
    private NetWorkStateReceiver netWorkStateReceiver;

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            mLatitude = bdLocation.getLatitude();
            mLongtitude = bdLocation.getLongitude();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
                    currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
                    //国家，省，市，区，街道
                    currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
                    currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
                    currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
                    currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
                    currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");
                    }
                    positionText.setText(currentPosition);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 展示GPS定位信息
        mLocationClient = new LocationClient(this.getApplicationContext());
        //获取到位置信息时会回调该定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);
        positionText = findViewById(R.id.position_text_view);
        mapView = findViewById(R.id.b_map_View);
        //获取BaiduMap实例
        baiduMap = mapView.getMap();

        //动态申请权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions =permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        else{
            requestLocation();
        }
    }

    @Override
    protected void onResume() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
        System.out.println("注册");
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        unregisterReceiver(netWorkStateReceiver);
        System.out.println("注销");
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();
        //停止方向传感器
        //mMyOrientationListener.stop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //活动销毁时停止定位
        mLocationClient.stop();
        // 活动销毁时地图销毁
        mapView.onDestroy();
        //关闭显示当前设备位置功能
        baiduMap.setMyLocationEnabled(false);
    }
    private void requestLocation() {
        initLocation();
        button();
        //开始定位，定位结果会回调到前面注册的监听器中
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        /*
        可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        LocationMode.Hight_Accuracy：高精度；
        LocationMode. Battery_Saving：低功耗；
        LocationMode. Device_Sensors：仅使用设备；
        */

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        int span = 6000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }
    /** 按钮响应  获取GPS定位信息 */
    private void button() {
        //按钮
        Button mbut_Loc = findViewById(R.id.but_Loc);
        Button mbut_RoutrPlan = findViewById(R.id.but_RoutrPlan);
        Button mbut_Attribute = findViewById(R.id.but_Attribute);
        Button mbut_Command = findViewById(R.id.but_Command);
        //按钮处理
        mbut_Loc.setOnClickListener(this);
        mbut_RoutrPlan.setOnClickListener(this);
        mbut_Attribute.setOnClickListener(this);
        mbut_Command.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        SDKInitializer.initialize(this.getApplicationContext());
        switch (v.getId()) {
            case R.id.but_Loc: {
                navigateTo(mLatitude, mLongtitude);
                break;
            }
            default:
        }
    }

    private void navigateTo(double mLatitude, double mLongtitude) {
        baiduMap.clear();
        mLastLocationData = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLastLocationData);
        baiduMap.animateMapStatus(msu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "You must allow all the permissions", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    private void navigateTo(BDLocation bdLocation) {
        // isFirstLocate变量为了防止多次调用animateMapStatus()方法，因为将地图移动到当前位置只需要在程序第一次定位时调用即可
        if(isFirstLocate){
            //设置地图缩放级别和将地图移动到当前经纬度
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        //设备在地图上显示的位置应随着设备的移动而实时改变，执行多次
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData = locationBuilder.build();
        baiduMap.setMyLocationData(myLocationData);
    }

}
