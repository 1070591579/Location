package com.example.location;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Main activity.
 *
 * @param <mInfoWindow> the type parameter
 * @author Administrator
 */
public class MainActivity<mInfoWindow> extends AppCompatActivity implements View.OnClickListener{

    private MapView mapView = null;
    private BaiduMap baiduMap = null;
    private Context context;
    /** 定位相关 经纬度信息 */
    private double mLatitude ;
    private double mLongtitude;
    private boolean isFirstLocate = true;
    private TextView positionText  = null;
    /**日志的TAG */
    private static final String TAG;
    static {
        TAG = "MainActivity";
    }
    /** 覆盖物 */
    private Marker marker = null;
    private LocationClient mLocationClient;
    /** 方向传感器 */
    private MyOrientationListener mMyOrientationListener;
    private NetWorkStateReceiver netWorkStateReceiver;
    private LatLng mLastLocationData;
    private BitmapDescriptor mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.arrow);
    private float mcurrentx ;
    private View myview ;
    private RoutePlanSearch mSearch;

    /**
     * Instantiates a new Main activity.
     */
    public MainActivity() {
    }

    /**
     * The type My location listener.
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            // Log.d(TAG, "BDLocationListener -> onReceiveLocation");
            /** 定位结果 */
            String addr;
            /** mapView 销毁后不在处理新接收的位置 */
            if (bdLocation == null || mapView == null) {
                Log.d(TAG, "BDLocation or mapView is null");
                positionText.setText("定位失败...");
                return;
            }
            if(!bdLocation.getLocationDescribe().isEmpty()) {
                addr = bdLocation.getLocationDescribe();
            }else if (bdLocation.hasAddr()) {
                addr = bdLocation.getAddrStr();
            }else {
                Log.d(TAG, "BDLocation has no addr info");
                addr = "定位失败...";
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mcurrentx).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            // 设置自定义图标
            MyLocationConfiguration config = new
                    MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
            baiduMap.setMyLocationConfiguration(config);
            // 更新经纬度
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
                    }else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {
                        // 离线定位结果
                        currentPosition.append("离线定位");
                    }
                    positionText.setText(currentPosition);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        SDKInitializer.setCoordType(CoordType.BD09LL);
        /* 展示GPS定位信息 */
        mLocationClient = new LocationClient(this.getApplicationContext());
        /* 初始化图标 */
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps);
        /* 获取到位置信息时会回调该定位监听器 */
        mLocationClient.registerLocationListener(new MyLocationListener());
        this.context = this;
        positionText = findViewById(R.id.position_text_view);
        mapView = findViewById(R.id.b_map_View);
        myview = findViewById(R.id.view_attribute);
        /* 获取BaiduMap实例 */
        baiduMap = mapView.getMap();

        /* 动态申请权限 */
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

    private void requestLocation() {
        initLocation();
        initRoutePlan();
        button();
        /** 开始定位，定位结果会回调到前面注册的监听器中 */
        mLocationClient.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /** 开启定位 */
        baiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        /** 开启方向传感器 */
        mMyOrientationListener.start();
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
        /** 停止定位 */
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        /** 停止方向传感器 */
        mMyOrientationListener.stop();
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

    /** 按钮响应事件：
     * 获取GPS定位信息
     * 路线规划
     * 视图隐藏与展示
     * 命令下发
    */
    private void button() {
        /* 按钮 */
        Button mbut_Loc = findViewById(R.id.but_Loc);
//        Button mbut_RoutrPlan = findViewById(R.id.but_RoutrPlan);
        Button mbut_Attribute = findViewById(R.id.but_Attribute);
        Button mbut_Command = findViewById(R.id.but_Command);
        /** 按钮处理 */
        mbut_Loc.setOnClickListener(this);
//        mbut_RoutrPlan.setOnClickListener(this);
        mbut_Attribute.setOnClickListener(this);
        mbut_Command.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        SDKInitializer.initialize(this.getApplicationContext());
        switch (v.getId()) {
            case R.id.but_Loc: {
                centerToMyLocation(mLatitude, mLongtitude);
                break;
            }
//            case R.id.but_RoutrPlan: {
//                StarRoute();
//                break;
//            }
            case R.id.but_Attribute: {
                ShowViewAttribute();
                break;
            }
            case R.id.but_Command: {
                ShowViewCommand();
                break;
            }
            default:
        }
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        /**
        * 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        * LocationMode.Hight_Accuracy：高精度
        * LocationMode. Battery_Saving：低功耗
        * LocationMode. Device_Sensors：仅使用设备
        */

        option.setCoorType("bd09ll");
        /**
        * 可选，设置返回经纬度坐标类型，默认GCJ02
        * GCJ02：国测局坐标；
        * BD09ll：百度经纬度坐标；
        * BD09：百度墨卡托坐标；
        * 海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        */

        int span = 6000;
        option.setScanSpan(span);
        /** 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的 */

        option.setIsNeedAddress(true);
        /** 可选，设置是否需要地址信息，默认不需要 */

        option.setOpenGps(true);
        /** 可选，默认false,设置是否使用gps */

        option.setLocationNotify(true);
        /** 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果 */

        option.setIsNeedLocationDescribe(true);
        /** 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近” */

        option.setIsNeedLocationPoiList(true);
        /** 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到 */

        option.setIgnoreKillProcess(false);
        /** 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死 */

        option.SetIgnoreCacheException(false);
        /** 可选，默认false，设置是否收集CRASH信息，默认收集 */

        option.setEnableSimulateGps(false);
        /**
        * 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        * 初始化传感器
        */
        initOrientation();
        mLocationClient.setLocOption(option);
    }

    /**
     * 传感器
     */
    private void initOrientation() {
        mMyOrientationListener = new MyOrientationListener(this.getApplicationContext());
        mMyOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mcurrentx = x;
            }
        });
    }
    /** 路线规划初始化 */
    private void initRoutePlan() {
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(listener);
    }

    /**
     * The Listener.路线规划
     */
    public OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "路线规划:未找到结果,检查输入", Toast.LENGTH_SHORT).show();
                /* 禁止定位 */
                isFirstLocate  = false;
            }
            assert result != null;
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                /* 起终点或途经点地址有岐义，通过以下接口获取建议查询信息 */
                result.getSuggestAddrInfo();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                baiduMap.clear();
                Toast.makeText(MainActivity.this, "路线规划:搜索完成", Toast.LENGTH_SHORT).show();
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(baiduMap);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
            /* 禁止定位 */
            isFirstLocate  = false;
        }
        @Override
        public void onGetTransitRouteResult(TransitRouteResult var1) {
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult var1) {
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult result) {
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult var1) {
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult var1) {
        }
    };
    /** 重新获取并显示定位 */
    private void centerToMyLocation(double mLatitude, double mLongtitude) {
        baiduMap.clear();
        mLastLocationData = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLastLocationData);
        baiduMap.animateMapStatus(msu);
    }
    /** 开始规划 */
    private void StarRoute() {
        Log.d(TAG, "StarRoute+进入函数");
        SDKInitializer.initialize(this.getApplicationContext());
        // 设置起、终点信息
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", "西二旗地铁站");
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", "百度科技园");
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(enNode));
    }
    /** 属性View显示 */
    private void ShowViewAttribute() {
        if(myview.getVisibility() == View.VISIBLE){
            myview.setVisibility(View.INVISIBLE);
        }else {
            myview.setVisibility(View.VISIBLE);
        }
    }
    /** 命令View显示 */
    private void ShowViewCommand() {
        Toast.makeText(getApplicationContext(), "按钮被点击了", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "ShowViewCommand+进入函数");
    }

    /** 首次进入权限验证 */
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
