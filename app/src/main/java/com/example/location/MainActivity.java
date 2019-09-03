package com.example.location;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerTabStrip;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import wxz.us.ble.central.BLEDevice;
import wxz.us.ble.central.BLEManager;

/**
 * The type Main activity.
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener{

    private MapView mapView;
    private BaiduMap baiduMap;
    private Context context;
    private TextView mTvLog;
    private NavigationView navView;
    /** 定位相关 经纬度信息 */
    private double mLatitude ;
    private double mLongtitude;
    private boolean isFirstLocate = true;
    /**日志的TAG */
    private static final String TAG = "MainActivity";
    /** 覆盖物 */
    private Marker marker = null;
    private MyLocationListener myListener = new MyLocationListener();
    private LocationClient mLocationClient;
    /** 方向传感器 */
    private MyOrientationListener mMyOrientationListener;
    private NetWorkStateReceiver netWorkStateReceiver;
    private LatLng mLastLocationData;
    private BitmapDescriptor mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.arrow);
    private float mcurrentx ;
    private RoutePlanSearch mSearch;
    private DrawerLayout mDrawerLayout;
    private float startDegrees = 0;
    private float endDegrees = 180;

    private BluetoothManager myBluetoothManager;
    private BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1001;
    private PagerTabStrip pagerTabStrip;
    private CustomViewPager per_viewPager;
    private PerAdapter perAdater;
    private BLEManager mBleManager;
    private ListView lv;
    private TextView tv_hint;
    private ProgressBar pbar;
    private ShowBLEAdapter mViewAdapter;
    private MenuItem itemSacn;
    /* 是否正在扫描 */
    private boolean scaning = false;
    /* 每个设备Key */
    private List<String> addressView;
    /* 当前正在操作的设备 */
    private BLEDevice dBleDevice;
    /* 操作当前的设备 */
    private String dAddress;
    private ArrayList<BLEDevice> mBLEList;
    public boolean is_need_toWriteFile=false;
    /**
     * key is the MAC Address 多设备 每一个BLEDevice实例代表一个设备
     * 把所有的设备即BLEDevice实例放到一个集合里面，通过address 来获得对应的设备，做相应的操作
     */
    Map<String, BLEDevice> mBLEDevices = new LinkedHashMap<>();
    private List<DeviceFragment> mFragments = new ArrayList<>();
    Map<String, Handler> mHandlers = new LinkedHashMap<>();
    private Map<String, Integer> rssiMap = new LinkedHashMap<>();
    private Map<String, String> uuidMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar =  findViewById(R.id.tool_bar);
        /* 展示GPS定位信息 */
        mLocationClient = new LocationClient(this.getApplicationContext());
        /* 获取到位置信息时会回调该定位监听器 */
        mLocationClient.registerLocationListener(myListener);
        /* 初始化图标 */
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps);
        this.context = this;
        mapView = findViewById(R.id.b_map_View);
        mTvLog = findViewById(R.id.view_attribute);
        navView = findViewById(R.id.design_nav_view);
        //引入header和menu
        navView.inflateHeaderView(R.layout.nav_header);
        navView.inflateMenu(R.menu.menu);
        /* 获取BaiduMap实例 */
        baiduMap = mapView.getMap();
        //app logo
        myToolbar.setLogo(R.mipmap.ic_launcher_round);
        //title
        myToolbar.setTitle("  野外人员管理");
        //sub title
        myToolbar.setSubtitle("  阳光");
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.mipmap.ic_launcher);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, myToolbar, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        myBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = myBluetoothManager.getAdapter();
        getPermissionMethod();
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

    /** 权限请求 */
    private void getPermissionMethod() {
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
        headMenuItem();
        MenuItem();
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

    /**
     * The type My location listener.
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            // Log.d(TAG, "BDLocationListener -> onReceiveLocation");
            /** mapView 销毁后不在处理新接收的位置 */
            if (bdLocation == null || mapView == null) {
                Log.d(TAG, "BDLocation or mapView is null");
                mTvLog.setText("定位失败...");
                return;
            }
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
            /* 设备在地图上显示的位置应随着设备的移动而实时改变，执行多次 */
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    /* 此处设置开发者获取到的方向信息，顺时针0-360 */
                    .direction(mcurrentx).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            /* 设置自定义图标 */
            MyLocationConfiguration config = new
                    MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
            baiduMap.setMyLocationConfiguration(config);
            /* 更新经纬度 */
            mLatitude = bdLocation.getLatitude();
            mLongtitude = bdLocation.getLongitude();
            /* 获取定位精度，默认值为0.0f */
            float radius = bdLocation.getRadius();
            /* 获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明 */
            int errorCode = bdLocation.getLocType();

            mTvLog.setText("");
            //获取国家
            String country = bdLocation.getCountry();
            //获取省份
            String province = bdLocation.getProvince();
            //获取城市
            String city = bdLocation.getCity();
            //获取区县
            String district = bdLocation.getDistrict();
            //获取街道信息
            String street = bdLocation.getStreet();
            //获取位置描述信息
            String locationDescribe = bdLocation.getLocationDescribe();
            //获取详细地址信息
            String addr = bdLocation.getAddrStr();
            textViewAddText(mTvLog,"    纬度: "+ mLatitude +"\t\t经度: " + mLongtitude);
            textViewAddText(mTvLog,"    国家: "+ country   +"\t\t省份: " + province);
            textViewAddText(mTvLog,"    城市: "+ city + "\t\t区县: "+ district);
            textViewAddText(mTvLog,"    街道: " + street);
            textViewAddText(mTvLog,"    位置描述: "+addr);
            textViewAddText(mTvLog,"    详细地址: "+locationDescribe);
            List<Poi> poiList = bdLocation.getPoiList();
            //获取周边POI信息
            //POI信息包括POI ID、名称等，具体信息请参照类参考中POI类的相关说明
            if (poiList != null){
                int cnt = 0;
                for (Poi poi : poiList){
                    textViewAddText(mTvLog,"    Poi: "+poi.getName());
                    cnt++;
                    if (cnt >= 3) {
                        break;
                    }
                }
            }
        }
    }
    private void textViewAddText(TextView textView,String s){
        if (TextUtils.isEmpty(s)) {
            return;
        }
        textView.setText(textView.getText()+"\n"+s);
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
        /* Button mbut_RoutrPlan = findViewById(R.id.but_RoutrPlan);*/
        Button mbut_Attribute = findViewById(R.id.but_Attribute);
        Button mbut_Command = findViewById(R.id.but_Command);
        /** 按钮处理 */
        mbut_Loc.setOnClickListener(this);
        /* mbut_RoutrPlan.setOnClickListener(this); */
        mbut_Attribute.setOnClickListener(this);
        mbut_Command.setOnClickListener(this);
    }
    /** 侧滑抽屉页面头部菜单栏点击事件
     * 人物头像
     * 用户姓名
     * 用户邮箱
     * 三角标志
     * */
    private void headMenuItem() {
        //获取头部布局
        View navHeaderView = navView.getHeaderView(0);
        /* 人物头像 */
        CircleImageView cirIViewHead =  navHeaderView.findViewById(R.id.nav_cirI_head);
        /* 用户姓名 */
        TextView userName = navHeaderView.findViewById(R.id.nav_username);
        /* 用户邮箱 */
        TextView userEmail = navHeaderView.findViewById(R.id.nav_usermail);
        /* 三角标志 */
        ImageView iamgeThreeArrow = navHeaderView.findViewById(R.id.image_three_arrow);
        /*Click Event*/
        cirIViewHead.setOnClickListener(this);
        userName.setOnClickListener(this);
        userEmail.setOnClickListener(this);
        iamgeThreeArrow.setOnClickListener(this);
    }
    /** 侧滑抽屉页面菜单栏点击事件 */
    private void MenuItem() {
        Menu menu = navView.getMenu();
        for (int i=0;i<menu.size();i++)
        {
            MenuItem item = menu.getItem(i);
            /* 方法一 外部注册需要this实现 MenuItem.OnMenuItemClickListener */
            item.setOnMenuItemClickListener(this);
            /* 方法二 内部匿名函数 */
//          item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    return false;
//                }
//            });
        }
    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_scan: {
                showScanClick();
                break;
            }
            case R.id.action_setting: {
                showSettingClick();
                break;
            }
            case R.id.action_message: {
                showMessageClick();
                break;
            }
            case R.id.action_security: {
                showSecurityClick();
                break;
            }
            case R.id.action_sports: {

                showSportsClick();
                break;
            }
            case R.id.action_health: {

                showHealthClick();
                break;
            }
            case R.id.action_cheers: {
                showCheersClick();
                break;
            }
            case R.id.action_pushmessage: {
                showPushMeeageClick();
                break;
            }
            case R.id.action_rawdata: {
                showRawDataClick();
                break;
            }
            case R.id.action_clear: {
                showClearClick();
                break;
            }
            case R.id.action_disconn: {
                showDisconnectClick();
                break;
            }
            case R.id.action_connect: {
                showConnectClick();
                break;
            }
            case R.id.action_rssi: {
                showRssiClick();
                break;
            }
            default:
            //关闭滑动菜单
            mDrawerLayout.closeDrawers();
        }
        return false;
    }

    private void showRssiClick() {
        Toast.makeText(MainActivity.this, "action_rssi", Toast.LENGTH_SHORT).show();
    }

    private void showConnectClick() {
        Toast.makeText(MainActivity.this, "action_connect", Toast.LENGTH_SHORT).show();
    }

    private void showDisconnectClick() {
        Toast.makeText(MainActivity.this, "action_disconn", Toast.LENGTH_SHORT).show();
    }

    private void showClearClick() {
        Toast.makeText(MainActivity.this, "action_clear", Toast.LENGTH_SHORT).show();
    }

    private void showRawDataClick() {
        Toast.makeText(MainActivity.this, "action_rawdata", Toast.LENGTH_SHORT).show();
    }

    private void showPushMeeageClick() {
        Toast.makeText(MainActivity.this, "action_pushmessage", Toast.LENGTH_SHORT).show();
    }

    private void showCheersClick() {
        Toast.makeText(MainActivity.this, "action_cheers", Toast.LENGTH_SHORT).show();
    }

    private void showHealthClick() {
        Toast.makeText(MainActivity.this, "action_health", Toast.LENGTH_SHORT).show();
    }

    private void showSportsClick() {
        Toast.makeText(MainActivity.this, "action_sports", Toast.LENGTH_SHORT).show();
    }

    private void showSecurityClick() {
        Toast.makeText(MainActivity.this, "action_security", Toast.LENGTH_SHORT).show();
    }

    private void showMessageClick() {
        Toast.makeText(MainActivity.this, "action_message", Toast.LENGTH_SHORT).show();
    }

    private void showSettingClick() {
        Toast.makeText(MainActivity.this, "action_setting", Toast.LENGTH_SHORT).show();
    }

    private void showScanClick() {
        Toast.makeText(MainActivity.this, "action_scan", Toast.LENGTH_SHORT).show();
    }

    @Override public void onClick(View v) {
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
            case R.id.nav_cirI_head: {
                headerClick();
                break;
            }
            case R.id.nav_username: {
                userNameClick();
                break;
            }
            case R.id.nav_usermail: {
                userMailClick();
                break;
            }
            case R.id.image_three_arrow: {
                arrowClick();
                break;
            }
            default:
        }
    }

    private void arrowClick() {
        float resultDegrees = endDegrees - startDegrees;
        if (resultDegrees > 0){
            startDegrees = 180;
            endDegrees = 0;
//            Animation rotateAnimation  = new RotateAnimation(startDegrees, endDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//            rotateAnimation.setFillAfter(true);
//            rotateAnimation.setDuration(300);
//            rotateAnimation.setRepeatCount(0);
//            rotateAnimation.setInterpolator(new LinearInterpolator());
//            ImageView rotateImage = findViewById(R.id.image_three_arrow);
//            rotateImage.startAnimation(rotateAnimation);
//            Menu menu = navView.getMenu();
//            menu.setGroupVisible(R.id.menu_group_scan, true);
//            menu.setGroupVisible(R.id.menu_group_set, true);
//            menu.setGroupVisible(R.id.menu_group_listen, false);
//            menu.setGroupVisible(R.id.menu_group_obthers, false);
            Menu menu = navView.getMenu();
            menu.clear();
            navView.inflateMenu(R.menu.menu);
        }
        else if (resultDegrees < 0){
            startDegrees = 0;
            endDegrees = 180;
//            Menu menu = navView.getMenu();
//            menu.setGroupVisible(R.id.menu_group_scan, false);
//            menu.setGroupVisible(R.id.menu_group_set, false);
//            menu.setGroupVisible(R.id.menu_group_listen, true);
//            menu.setGroupVisible(R.id.menu_group_obthers, true);
            Menu menu = navView.getMenu();
            menu.clear();
            navView.inflateMenu(R.menu.personinfo);
        }
    }

    private void userMailClick() {
        Toast.makeText(MainActivity.this,"userMail",Toast.LENGTH_SHORT).show();
    }

    private void userNameClick() {
        Toast.makeText(MainActivity.this,"userName",Toast.LENGTH_SHORT).show();
    }

    private void headerClick() {
        Toast.makeText(MainActivity.this,"Head",Toast.LENGTH_SHORT).show();
        MenuItem();
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
        if(mTvLog.getVisibility() == View.VISIBLE){
            mTvLog.setVisibility(View.INVISIBLE);
        }else {
            mTvLog.setVisibility(View.VISIBLE);
        }
    }
    /** 命令View显示 */
    private void ShowViewCommand() {
        Toast.makeText(getApplicationContext(), "按钮被点击了", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "ShowViewCommand+进入函数");
    }



}
