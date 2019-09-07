package com.example.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import wxz.us.ble.central.BLEDevice;
import wxz.us.ble.central.BLEManager;
import wxz.us.ble.central.L;
import wxz.us.ble.listener.ScanListener;

import static com.baidu.mapapi.map.BitmapDescriptorFactory.fromResource;
import static com.example.location.Image.BBQ;
import static com.example.location.Image.BootImages;
import static com.example.location.Image.Broken_Heart;
import static com.example.location.Image.Craft;
import static com.example.location.Image.Daydream;
import static com.example.location.Image.Email;
import static com.example.location.Image.Fine_Dining;
import static com.example.location.Image.Home;
import static com.example.location.Image.Kids;
import static com.example.location.Image.Love;
import static com.example.location.Image.Meeting;
import static com.example.location.Image.Music;
import static com.example.location.Image.Painting;
import static com.example.location.Image.Party;
import static com.example.location.Image.Pet;
import static com.example.location.Image.Phone_Call;
import static com.example.location.Image.Pizza;
import static com.example.location.Image.Reading;
import static com.example.location.Image.Really;
import static com.example.location.Image.Romantic;
import static com.example.location.Image.Sculpture;
import static com.example.location.Image.Self;
import static com.example.location.Image.Sex;
import static com.example.location.Image.Soaking;
import static com.example.location.Image.WTF;
import static com.example.location.Image.Watch_TV;
import static com.example.location.Image.Whaaaaat;
import static com.example.location.Image.Work;
import static com.example.location.Image.Workshop;
import static com.example.location.UtilsTools.alarmToBytes;
import static com.example.location.UtilsTools.byteTo16String;
import static com.example.location.UtilsTools.bytetoarray;
import static com.example.location.UtilsTools.intArraysTobyteArrays;
import static com.example.location.UtilsTools.intToByteArray;
import static com.example.location.UtilsTools.longSitByte;
import static com.example.location.UtilsTools.nowTimeToBytes;
import static com.example.location.UtilsTools.record_date;
import static com.example.location.UtilsTools.strToByteArray;
import static com.example.location.UtilsTools.string2Unicode;
import static com.example.location.UtilsTools.userToByte;
import static java.util.Objects.requireNonNull;

/**
 * The type Main activity.
 *
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener{
    /**
     * 声明 地图视图展示层
     */
    private MapView mapView;
    /**
     * 声明 百度地图视图展示层
     */
    private BaiduMap baiduMap;
    /**
     * 声明 具体定位信息展示层
     */
    private TextView mTvLog;
    /**
     * 声明 侧拉效果视图展示层
     */
    private NavigationView navView;
    /**
     * 声明 定位相关 经纬度信息
     * mLatitude 经度
     */
    private double mLatitude;
    /**
     * 声明 定位相关 经纬度信息
     * mLongitudes 纬度
     */
    private double mLongitudes;
    /**
     * 声明 布尔值 是否是首次默认为 true
     */
    private boolean isFirstLocate = true;
    /**
     * 声明 日志的TAG
     */
    private static final String TAG = "MainActivity";
    /**
     * 声明 覆盖物
     */
    @SuppressWarnings("unused")
    private Marker marker = null;
    /**
     * 声明 百度地图定位监听类方法
     */
    private MyLocationListener myListener = new MyLocationListener();
    /**
     * 声明 百度地图定位服务客户端
     */
    private LocationClient mLocationClient;
    /**
     * 声明 百度地图方向传感器
     */
    private MyOrientationListener mMyOrientationListener;
    /**
     * 声明 检测网络变化
     */
    private NetWorkStateReceiver netWorkStateReceiver;
    /**
     *  声明 百度地图定位坐标图标——icon
     */
    private BitmapDescriptor mIconLocation = fromResource(R.drawable.arrow);
    /**
     * 声明 请求定位人员的定位时的方向
     */
    private float mcurrentx ;
    /**
     * 声明 创建驾车路线检索实例
     */
    private RoutePlanSearch mSearch;
    private DrawerLayout mDrawerLayout;
    private float startDegrees = 0;
    private float endDegrees = 180;
    /**
     * 设备的默认蓝牙适配器
     */
    private BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1001;
    /**
     * perViewPager 自定义视图
     */
    private CustomViewPager perViewPager;
    private PerAdapter<DeviceFragment> perAdapter;
    private BLEManager mBleManager;
    private ListView lv;
    private TextView tvHint;
    private ProgressBar progressBar;
    private ShowBLEAdapter mViewAdapter;
    private MenuItem itemScan;
    /**
     *  是否正在扫描
     */
    private boolean scanIng = false;
    /**
     * 每个设备Key
     */
    private List<String> addressView;
    /**
     *  当前正在操作的设备
     */
    private BLEDevice dBleDevice;
    /**
     *  操作当前的设备
     *  */
    private String dAddress;
    private ArrayList<BLEDevice> mBlueList;
    /**
     * The Is need to write file.
     */
    public boolean is_need_toWriteFile=false;
    /**
     * key is the MAC Address 多设备 每一个BLEDevice实例代表一个设备
     * 把所有的设备即BLEDevice实例放到一个集合里面，通过address 来获得对应的设备，做相应的操作
     */
    private LinkedHashMap<String, BLEDevice> mBlueDevices = new LinkedHashMap<>();
    private ArrayList<DeviceFragment> mFragments = new ArrayList<>();
    /**
     * The M handlers.
     */
    private LinkedHashMap<String, Handler> mHandlers = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> rssiMap = new LinkedHashMap<>();
    private LinkedHashMap<String, String> uuidMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        /*
          setContentView 获取主页面视图
          mDrawerLayout 获取抽屉布局视图
          myToolbar 获取工具栏视图
          myToolbar.setLogo 设置 app logo
          myToolbar.setTitle 设置 app title 标题
          myToolbar.setSubtitle 设置 app 小标题
          myToolbar.setNavigationIcon() 设置图标
          mLocationClient 获取百度地图定位服务客户端实例 展示GPS定位信息
          mLocationClient.registerLocationListener 获取到位置信息时 注册定位监听器
          mIconLocation 获取初始化图标
          mapView 获取地图实例图层
          mTvLog  获取定位详情信息图层
          navView 获取抽屉侧滑视图图层
          navView.inflateHeaderView  获取侧滑视图图层头部布局视图图
          navView.inflateMenu  获取侧滑视图图层菜单栏布局视图图
          baiduMap 获取BaiduMap实例
          myBluetoothManager 获取蓝牙权限高级管理权限服务入口
          myBluetoothAdapter 获取设备的默认蓝牙适配器
         */
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar =  findViewById(R.id.tool_bar);
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        mIconLocation = fromResource(R.drawable.navi_map_gps);
        mapView = findViewById(R.id.b_map_View);
        mTvLog = findViewById(R.id.view_attribute);
        navView = findViewById(R.id.design_nav_view);
        navView.inflateHeaderView(R.layout.nav_header);
        navView.inflateMenu(R.menu.menu);
        baiduMap = mapView.getMap();
        myToolbar.setLogo(R.mipmap.ic_launcher_round);
        myToolbar.setTitle("  野外人员管理");
//        myToolbar.setSubtitle("  阳光");
        setSupportActionBar(myToolbar);
        /* myToolbar.setNavigationIcon(R.mipmap.head_background); */
        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, myToolbar, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        /* 蓝牙权限高级管理 */
        BluetoothManager myBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = myBluetoothManager.getAdapter();
        getPermissionMethod();
    }

    /** 首次进入权限验证 */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "You must allow all the permissions", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                requestLocation();
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                finish();
            }
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
            String[] permissions =permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        else{
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        initRoutePlan();
        initBlue();
        intiView();
        button();
        headMenuItem();
        menuItem();
        /* 开始定位，定位结果会回调到前面注册的监听器中 */
        mLocationClient.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* 开启定位 */
        baiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        /* 开启方向传感器 */
        mMyOrientationListener.start();
        /* 判断蓝牙是否开启 */
        if (myBluetoothAdapter.isEnabled()) {
            System.out.println("蓝牙已开启...");
        } else {
            enableBle();
        }
        initScanDialog();
    }

    /**
     * 开启蓝牙
     */
    public void enableBle() {
        if (!myBluetoothAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }
    /**
     * 扫描设备
     */
    private void scanLeDevice() {
        itemScan.setTitle("stop scan");
        if (mBlueList != null) {
            mBlueList.clear();
        }
        // scanIng = true;
        if (scanIng) {
            stopScan();
        }
        /* 扫描5秒 */
        mBleManager.startScan(7);
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        scanIng = false;
        mBleManager.stopScan();
    }

    /**
     * initSettingDialog 设置菜单点击弹窗
     * initMessageDialog 消息菜单点击弹窗
     * initSecurityDialog 安全菜单点击弹窗
     * initSportsDialog 运动菜单点击弹窗
     * initHealthDialog 健康菜单点击弹窗
     * initCheersDialog 水杯菜单点击弹窗
     * initTextDialog  文本命令菜单点击弹窗
     * initHeartDialog 心率菜单点击弹窗
     * initpushsettingDialog 消息推送菜单点击弹窗
     * initrawdataDialog 裸数据点击弹窗
     */
    private void initDialog() {
        initSettingDialog();
        initMessageDialog();
        initSecurityDialog();
        initSportsDialog();
        initHealthDialog();
        initCheersDialog();
        initTextDialog();
        initHeartDialog();
        initPushSettingDialog();
        initRawDataDialog();
    }
    /***
     * 添加设备，每连接一个设备就把设备添加到集合里面，方便管理
     */
    private void addDevice(final BLEDevice device) {
        final String address = device.getAddress();
        /* 扫描获取的 */
        // byte[] scanRecord = null;
        /* 扫秒是获取的 信号 */
        // int rssi = 0;
        if (!mBlueDevices.containsKey(address)) {
            mBlueDevices.put(device.getAddress(), device);
            if (addressView.size() == 0) {
                /* address = addressView.get(0);默认操作第一个 */
                dBleDevice = mBlueDevices.get(address);
                dAddress = requireNonNull(dBleDevice).getAddress();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDeviceFragment(device.getAddress(), device);
                    initDialog();
                }
            });
            addressView.add(device.getAddress());
        }
        Toast.makeText(this, getString(R.string.connect_device)+"", Toast.LENGTH_LONG).show();
        perViewPager.setCurrentItem(addressView.size());
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 不要同时连几个蓝牙设备，要等连接成功后再连接下一个 */
                connect(address);
            }
        }).start();

    }

    /**
     * 连接设备
     * @param address the address
     */
    protected void connect(String address) {
        if (scanIng) {
            /* 先判断是否正在扫描 */
            stopScan();
        }
        dBleDevice = mBlueDevices.get(address);
        requireNonNull(dBleDevice).connect();
    }

    private void addDeviceFragment(String st, BLEDevice device) {
        DeviceFragment mFragment = new DeviceFragment(device);
        mFragments.add(mFragment);
        perAdapter.setListViews(mFragments, st);
        perAdapter.notifyDataSetChanged();

    }
    /**
     * 扫描 dialog
     * scanDialog
    **/
    private AlertDialog scanDialog;
    private void initScanDialog() {
        AlertDialog.Builder scanBuilder = new AlertDialog.Builder(this);
        scanBuilder.setTitle(getString(R.string.search_device)+"");
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.device_list, null);
        mBlueList = new ArrayList<>();
        lv = view.findViewById(R.id.device_list);
        tvHint = view.findViewById(R.id.tv);
        progressBar = view.findViewById(R.id.pbar);
        progressBar.setVisibility(View.VISIBLE);
        mViewAdapter = new ShowBLEAdapter(this, mBlueList, rssiMap, uuidMap);
        lv.setAdapter(mViewAdapter);
        scanBuilder.setView(view);
        scanBuilder.setPositiveButton(getString(R.string.retry)+"",
                new DialogInterface.OnClickListener() {
                    /* 重试按钮 */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* 下面三句控制弹框的关闭 */
                        try {
                            /* stopScan(); */
                            Field field = requireNonNull(dialog.getClass().getSuperclass())
                                    .getDeclaredField("mShowing");
                            field.setAccessible(true);
                            /* true表示要关闭 */
                            field.set(dialog, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // reScanLeDevice(true);
                        // actionAlertDialog();
                        // lv.setVisibility(View.GONE);
                        tvHint.setVisibility(View.GONE);
                        scanLeDevice();
                        /* scanDialog.show(); */
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
        scanBuilder.setNegativeButton(getString(R.string.cancel)+"",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* 取消查找设备的操作 */
                        stopScan();
                        System.out.println("取消查找");
                        /* 下面三句控制弹框的关闭 */
                        try {

                            Field field = requireNonNull(dialog.getClass().getSuperclass())
                                    .getDeclaredField("mShowing");
                            field.setAccessible(true);
                            /* true表示要关闭 */
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        scanDialog.cancel();
                        /* scanDialog.dismiss(); */
                    }
                });

        scanDialog = scanBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                stopScan();
                /* 下面三句控制弹框的关闭 */
//                try {
//                    Field field = scanDialog.getClass().getSuperclass()
//                            .getDeclaredField("mShowing");
//                    field.setAccessible(true);
//                    /* true表示要关闭 */
//                    field.set(scanDialog, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                BLEDevice device = mBlueList.get(position);
                addDevice(device);
                scanDialog.dismiss();
                scanDialog.cancel();
            }
        });
    }
    /**
     * 设置dialog
     * settingDialog
     */
    private AlertDialog settingDialog;
    @SuppressWarnings("AlibabaMethodTooLong")
    private void initSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_setting)+"");
        final String[] commants = {getString(R.string.set_time)+"", getString(R.string.set_clock)+"", getString(R.string.device_alarm_list)+"",
                getString(R.string.step_goal_setting)+"", getString(R.string.user_set)+"", getString(R.string.Anti_throwing_vibration)+"",
                getString(R.string.longsit_set)+"", getString(R.string.restore_factory)+"", getString(R.string.set_name)+"",
                getString(R.string.set_picture_1)+"",  "清空图片0组", "设置图片文字1组", "清空图片文字0组", "设置图片0组",
                "设置图片文字0组", "设置图片2组", "设置图片3组", "设置图片4组", "设置图片文字2组",
                "设置图片文字3组", "设置图片文字4组", "防丢设置振动次数:1", "防丢设置振动次数:3", "定时心率测量设置(*) ",
                "抬手亮屏设置(*)-打开", "抬手亮屏设置(*)-关闭", "防丢设置振动次数:7", "防丢设置振动次数:4", "防丢设置振动次数:5", "防丢设置振动次数:6",
                "开机图片设置", "功率设置", "设备名设置", "广播设置"
        };
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("dBleDevice:" + dBleDevice.getAddress());
                // 图片设置
                // 清空图片组
                // 图片文字设置
                // 清空图片文字组
                // 图片0组
                // 图片文字设置
                // 设置图片2组 Love
                // 设置图片3组 Work
                // 设置图片文字2组
                // 设置图片文字3组
                // 设置图片文字4组
                // "防丢设置振动次数:1"
                //"防丢设置振动次数:3"
                // 定时心率测量
                // 抬手亮屏设置 , 0x00 代表关闭， 0x01 代表打开
                // 抬手亮屏设置 , 0x00 代表关闭， 0x01 代表打开
                // "防丢设置振动次数:7"
                // "防丢设置振动次数:4"
                // "防丢设置振动次数:5"
                // "防丢设置振动次数:6"
                //	"开机图片设置"
                //	"功率设置"
                //	"设备名设置"
                //	"广播设置"
                switch (which) {
                    case 0: {
                        byte[] time = nowTimeToBytes();
                        updateList(dAddress, "cmd:0x01," + commants[0] + " :"
                                + Arrays.toString(byteTo16String(time)));
                        write(dAddress, time.length, 0x01, time);
                        break;
                    }
                    case 1: {
                        byte[] result = new byte[15];
                        byte[] alarmToBytes1 = alarmToBytes(2017, 6, 13, 21, 1,
                                1, 2, 1);
                        byte[] alarmToBytes2 = alarmToBytes(2017, 2, 2, 2, 2,
                                2, 127, 1);
                        byte[] alarmToBytes3 = alarmToBytes(2017, 3, 3, 3, 3,
                                3, 127, 0);
                        System.arraycopy(alarmToBytes1, 0, result, 0,
                                alarmToBytes1.length);
                        System.arraycopy(alarmToBytes2, 0, result, 5,
                                alarmToBytes2.length);
                        System.arraycopy(alarmToBytes3, 0, result, 10,
                                alarmToBytes3.length);
                        updateList(dAddress, "cmd:0x02," + commants[1] + " :"
                                + Arrays.toString(byteTo16String(result)));
                        write(dAddress, result.length, 0x02, result);
                        break;
                    }
                    case 2: {
                        byte[] alar = {0x01};
                        updateList(dAddress, "cmd:0x03," + commants[2] + " :"
                                + Arrays.toString(byteTo16String(alar)));
                        write(dAddress, alar.length, 0x03, alar);
                        break;
                    }
                    case 3: {
                        byte[] b = intToByteArray(5000);
                        updateList(dAddress, "cmd:0x04," + commants[3] + " :"
                                + Arrays.toString(byteTo16String(b)));
                        write(dAddress, b.length, 0x04, b);
                        break;
                    }
                    case 4: {
                        byte[] user = userToByte(1, 20, 180, 60);
                        updateList(dAddress, "cmd:0x05," + commants[4] + " :"
                                + Arrays.toString(byteTo16String(user)));
                        write(dAddress, user.length, 0x05, user);
                        break;
                    }
                    case 5: {//"防丢设置振动次数:0"
                        int set0 = 0; //次数

                        int b0 = 1; //报警等级

                        byte[] d = new byte[4];
                        d[0] = (byte) (((set0 << 2) & 0xff) | b0);
                        updateList(dAddress, "cmd:0x06," + commants[5] + " :"
                                + Arrays.toString(byteTo16String(d)));
                        write(dAddress, d.length, 0x06, d);
                        break;
                    }
                    case 6: {
                        int open = 1;
                        int time1 = 2;//一般30分钟 间隔时间小朱测试 改成了2分钟

                        int interval = 1;//一般5分钟 间隔时间小朱测试 改成了1分钟


                        byte[] lgSit = longSitByte(open, time1, interval);
                        updateList(dAddress, "cmd:0x07," + commants[6] + " :"
                                + Arrays.toString(byteTo16String(lgSit)));
                        write(dAddress, lgSit.length, 0x07, lgSit);
                        break;
                    }
                    case 7: {
                        byte[] r = {0x00};
                        updateList(dAddress, "cmd:0x09," + commants[7] + " :"
                                + Arrays.toString(byteTo16String(r)));
                        write(dAddress, r.length, 0x09, r);
                        break;
                    }
                    case 8: {// 云(0) 石(1) 智(2) 能(3)
                        byte[] oneFonts = {0x40, 0x40, 0x44, 0x44, 0x44, 0x44,
                                (byte) 0xC4, (byte) 0xC4, 0x44, 0x44, 0x46, 0x46,
                                0x64, 0x60, 0x40, 0x00, 0x00, 0x20, 0x70, 0x38,
                                0x2C, 0x27, 0x23, 0x31, 0x10, 0x12, 0x14, 0x18,
                                0x70, 0x20, 0x00, 0x00};
                        byte[] twoFonts = {0x02, 0x02, 0x02, 0x02, (byte) 0x82,
                                (byte) 0xF2, 0x4E, 0x42, 0x42, 0x42, 0x42, 0x42,
                                (byte) 0xC2, 0x02, 0x02, 0x00, 0x10, 0x08, 0x04,
                                0x02, 0x01, 0x7F, 0x20, 0x20, 0x20, 0x20, 0x20,
                                0x20, 0x7F, 0x00, 0x00, 0x00};
                        byte[] threeFonts = {0x10, 0x14, 0x13, (byte) 0x92, 0x7E,
                                0x32, 0x52, (byte) 0x92, 0x00, 0x7C, 0x44, 0x44,
                                0x44, 0x7C, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00,
                                (byte) 0xFF, 0x49, 0x49, 0x49, 0x49, 0x49, 0x49,
                                (byte) 0xFF, 0x00, 0x00, 0x00, 0x00};
                        byte[] fourFonts = {0x10, (byte) 0xB8, (byte) 0x97,
                                (byte) 0x92, (byte) 0x90, (byte) 0x94, (byte) 0xB8,
                                0x10, 0x00, 0x7F, 0x48, 0x48, 0x44, 0x74, 0x20,
                                0x00, 0x00, (byte) 0xFF, 0x0A, 0x0A, 0x4A,
                                (byte) 0x8A, 0x7F, 0x00, 0x00, 0x3F, 0x44, 0x44,
                                0x42, 0x72, 0x20, 0x00};
                        updateList(dAddress, "cmd:0x0A," + commants[8] + " :"
                                + "云石智能");
                        writeName(dAddress, 0x0A, oneFonts, twoFonts, threeFonts,
                                fourFonts);
                        break;
                    }
                    case 9: {
                        int index1 = 1; // 组编号

                        byte[] data = new byte[640];
                        System.arraycopy(intArraysTobyteArrays(Home),
                                0, data, 0, 128);
                        System.arraycopy(intArraysTobyteArrays(Party),
                                0, data, 128, 128);
                        System.arraycopy(intArraysTobyteArrays(BBQ), 0,
                                data, 256, 128);
                        System.arraycopy(intArraysTobyteArrays(Reading), 0,
                                data, 384, 128);
                        System.arraycopy(intArraysTobyteArrays(Watch_TV), 0,
                                data, 512, 128);
                        updateList(dAddress, "cmd:0x0B," + commants[9] + " :"
                                + index1);
                        writeImage(dAddress, 0x0B, index1, data);
                        break;
                    }
                    case 10:{
                        byte[] index = {0x00}; // 0x00~0x09

                        updateList(dAddress, "cmd:0x0C," + commants[10] + " :"
                                + Arrays.toString(byteTo16String(index)));
                        write(dAddress, index.length, 0x0C, index);
                        break;
                    }
                    case 11: {
                        int n1 = 1; // 那一组图片的文字 0~9

                        String[] image_name = {"Home", "Party", "BBQ", "Reading",
                                "Watch TV"};
                        updateList(dAddress, "cmd:0x0D," + commants[11] + " :" + n1
                                + ",name = " + Arrays.toString(image_name));
                        writeImageName(dAddress, 0x0D, n1, image_name);
                        break;
                    }
                    case 12: {
                        byte[] indexNameClear = {0x00};
                        updateList(
                                dAddress,
                                "cmd:0x0E,"
                                        + commants[12]
                                        + " :"
                                        + Arrays.toString(byteTo16String(indexNameClear)));
                        write(dAddress, indexNameClear.length, 0x0E, indexNameClear);
                        break;
                    }
                    case 13: {
                        byte[] data13 = new byte[1024];
                        System.arraycopy(intArraysTobyteArrays(Self),
                                0, data13, 0, 128);
                        System.arraycopy(intArraysTobyteArrays(Pet), 0,
                                data13, 128, 128);
                        System.arraycopy(intArraysTobyteArrays(Kids),
                                0, data13, 256, 128);
                        System.arraycopy(intArraysTobyteArrays(Pizza),
                                0, data13, 384, 128);
                        System.arraycopy(intArraysTobyteArrays(Music),
                                0, data13, 512, 128);
                        System.arraycopy(intArraysTobyteArrays(Really),
                                0, data13, 640, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Whaaaaat), 0,
                                data13, 768, 128);
                        System.arraycopy(intArraysTobyteArrays(WTF), 0,
                                data13, 896, 128);
                        int index0 = 0;
                        updateList(dAddress, "cmd:0x0B," + commants[13] + " :"
                                + index0);
                        writeImage(dAddress, 0x0B, index0, data13);
                        break;
                    }
                    case 14: {
                        int nn = 0; // 那一组图片的文字 0~9

                        String[] image_name_0 = {"Self", "Pet", "Kids", "Pizza",
                                "Music", "Really", "Whaaaaat", "WTF"};
                        updateList(dAddress, "cmd:0x0D," + commants[14] + " :" + nn
                                + ",name = " + Arrays.toString(image_name_0));
                        writeImageName(dAddress, 0x0D, nn, image_name_0);
                        break;
                    }
                    case 15: {
                        int imageIndex2 = 2;
                        byte[] image_byte2 = new byte[768];
                        System.arraycopy(intArraysTobyteArrays(Love),
                                0, image_byte2, 0, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Broken_Heart), 0,
                                image_byte2, 128, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Fine_Dining), 0,
                                image_byte2, 256, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Romantic), 0,
                                image_byte2, 384, 128);
                        System.arraycopy(intArraysTobyteArrays(Sex), 0,
                                image_byte2, 512, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Soaking), 0,
                                image_byte2, 640, 128);

                        updateList(dAddress, "cmd:0x0B," + commants[15] + " :"
                                + imageIndex2);
                        writeImage(dAddress, 0x0B, imageIndex2, image_byte2);
                        break;
                    }
                    case 16: {
                        int imageIndex3 = 3;
                        byte[] image_byte3 = new byte[640];
                        System.arraycopy(intArraysTobyteArrays(Work),
                                0, image_byte3, 0, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Phone_Call), 0,
                                image_byte3, 128, 128);
                        System.arraycopy(intArraysTobyteArrays(Email),
                                0, image_byte3, 256, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Meeting), 0,
                                image_byte3, 384, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Daydream), 0,
                                image_byte3, 512, 128);

                        updateList(dAddress, "cmd:0x0B," + commants[16] + " :"
                                + imageIndex3);
                        writeImage(dAddress, 0x0B, imageIndex3, image_byte3);
                        break;
                    }
                    case 17: {// 设置图片4组 Workshop
                        int imageIndex4 = 4;
                        byte[] image_byte4 = new byte[512];
                        System.arraycopy(
                                intArraysTobyteArrays(Workshop), 0,
                                image_byte4, 0, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Sculpture), 0,
                                image_byte4, 128, 128);
                        System.arraycopy(
                                intArraysTobyteArrays(Painting), 0,
                                image_byte4, 256, 128);
                        System.arraycopy(intArraysTobyteArrays(Craft),
                                0, image_byte4, 384, 128);

                        updateList(dAddress, "cmd:0x0B," + commants[17] + " :"
                                + imageIndex4);
                        writeImage(dAddress, 0x0B, imageIndex4, image_byte4);
                        break;
                    }
                    case 18: {
                        int nameIndex2 = 2; // 那一组图片的文字 0~9

                        String[] image_name_2 = {"Love", "Broken", "Fine",
                                "Romantic", "Sex", "Soaking"};
                        updateList(
                                dAddress,
                                "cmd:0x0D," + commants[18] + " :" + nameIndex2
                                        + ",name = "
                                        + Arrays.toString(image_name_2));
                        writeImageName(dAddress, 0x0D, nameIndex2, image_name_2);
                        break;
                    }
                    case 19: {
                        int nameIndex3 = 3; // 那一组图片的文字 0~9

                        String[] image_name_3 = {"Work", "PhoneCal", "Email",
                                "Meeting", "Daydream"};
                        updateList(
                                dAddress,
                                "cmd:0x0D," + commants[19] + " :" + nameIndex3
                                        + ",name = "
                                        + Arrays.toString(image_name_3));
                        writeImageName(dAddress, 0x0D, nameIndex3, image_name_3);
                        break;
                    }
                    case 20: {
                        int nameIndex4 = 4; // 那一组图片的文字 0~9

                        String[] image_name_4 = {"Workshop", "Sculptur",
                                "Painting", "Craft"};
                        updateList(
                                dAddress,
                                "cmd:0x0D," + commants[20] + " :" + nameIndex4
                                        + ",name = "
                                        + Arrays.toString(image_name_4));
                        writeImageName(dAddress, 0x0D, nameIndex4, image_name_4);
                        break;
                    }
                    case 21: {
                        int set1 = 1; //次数

                        int b1 = 1; //报警等级

                        byte[] d1 = new byte[4];
                        d1[0] = (byte) (((set1 << 2) & 0xff) | b1);
                        updateList(dAddress, "cmd:0x06," + commants[21] + " :"
                                + Arrays.toString(byteTo16String(d1)));
                        write(dAddress, d1.length, 0x06, d1);
                        break;
                    }
                    case 22: {
                        int set3 = 3; //次数

                        int b3 = 1; //报警等级

                        byte[] d3 = new byte[4];
                        d3[0] = (byte) (((set3 << 2) & 0xff) | b3);
                        updateList(dAddress, "cmd:0x06," + commants[22] + " :"
                                + Arrays.toString(byteTo16String(d3)));
                        write(dAddress, d3.length, 0x06, d3);
                        break;
                    }
                    case 23: {// 定时设置的值 0或者 15，30，60。0 代表定时测量心率关闭，15 代表每 15 分钟定时测量一次心率，30
                        // 代表每隔 30 分钟定时测量一次性率，60 代表每隔 60 分钟定时测量一次心率。
                        heartDialog.show();
                        break;
                    }
                    case 24: {
                        byte[] setx = {0x01};
                        updateList(dAddress, "cmd:0x0f," + commants[24] + " :"
                                + Arrays.toString(byteTo16String(setx)));
                        write(dAddress, setx.length, 0x0f, setx);
                        break;
                    }
                    case 25: {
                        byte[] set2 = {0x00};
                        updateList(dAddress, "cmd:0x0f," + commants[25] + " :"
                                + Arrays.toString(byteTo16String(set2)));
                        write(dAddress, set2.length, 0x0f, set2);
                        break;
                    }
                    case 26: {
                        int set7 = 7; //次数

                        int b7 = 1; //报警等级

                        byte[] d7 = new byte[4];
                        d7[0] = (byte) (((set7 << 2) & 0xff) | b7);

                        updateList(dAddress, "cmd:0x06," + commants[26] + " :"
                                + Arrays.toString(byteTo16String(d7)));
                        write(dAddress, d7.length, 0x06, d7);
                        break;
                    }
                    case 27: {
                        int set4 = 4; //次数

                        int b4 = 1; //报警等级

                        byte[] d4 = new byte[4];
                        d4[0] = (byte) (((set4 << 2) & 0xff) | b4);

                        updateList(dAddress, "cmd:0x06," + commants[27] + " :"
                                + Arrays.toString(byteTo16String(d4)));
                        write(dAddress, d4.length, 0x06, d4);
                        break;
                    }
                    case 28: {
                        int set5 = 5; //次数

                        int b5 = 1; //报警等级

                        byte[] d5 = new byte[4];
                        d5[0] = (byte) (((set5 << 2) & 0xff) | b5);

                        updateList(dAddress, "cmd:0x06," + commants[28] + " :"
                                + Arrays.toString(byteTo16String(d5)));
                        write(dAddress, d5.length, 0x06, d5);
                        break;
                    }
                    case 29: {
                        int set6 = 6; //次数

                        int b6 = 1; //报警等级

                        byte[] d6 = new byte[4];
                        d6[0] = (byte) (((set6 << 2) & 0xff) | b6);
                        updateList(dAddress, "cmd:0x06," + commants[26] + " :"
                                + Arrays.toString(byteTo16String(d6)));
                        write(dAddress, d6.length, 0x06, d6);
                        break;
                    }
                    case 30:{ //					11  开机图片设置(*)  0～255  (#)  16
                        byte[] imageByte = intArraysTobyteArrays(BootImages);
                        updateList(dAddress, "cmd:0x0B," + commants[30] + ",length=" + imageByte.length);
                        writeBootImages(dAddress, 0x0B, imageByte);
                        break;
                    }
                    case 31: {
                        String subtitle = " 范围从-128 到正 127,但是实际能设置的值根据芯片不同有所差异，B2 类型手环只支持-40, -30, -20, -16,-12, -8, -4, 0, +4 这几组数字";
                        settingDialog(0x0C, commants[31], subtitle);
                        break;
                    }
                    case 32: {
                        String subtitle2 = "默认是 B2，长度不超过 2 个字节";
                        settingDialog(0x0D, commants[32], subtitle2);
                        break;
                    }
                    case 33: {
                        String subtitle3 = "设置范围为 32 到 16384，真正的时间要用设置的间隔×0.625 才可以达到，比如设置成 8000，实际代表 5000 毫秒（8000×0.625）发射一次广播。";
                        settingDialog(0x0E, commants[33], subtitle3);
                        break;
                    }
                    default:
                }
            }
        });

        settingDialog = builder.create();
    }
    private void settingDialog(final int cmd, final String title, String subtitle) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText = view.findViewById(R.id.editText);
        TextView subtitleText = view.findViewById(R.id.subtitle_text);
        subtitleText.setText(subtitle);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText.getText().toString();
                int a;
                if ("".equals(s)) {
                    return;
                }
                L.i(title + ":----------" + s + " --" + cmd);
                switch (cmd) {
                    case 0x0C: {//	"功率设置"
                        a = Integer.parseInt(s);
                        byte[] dd = {(byte) a};
                        updateList(dAddress, "cmd:0x0C, " + title + ":" + a + " , " + Arrays.toString(byteTo16String(dd)));
                        write(dAddress, dd.length, cmd, dd);
                        break;
                    }
                    case 0x0D: {//	"设备名设置"
                        byte[] t = s.getBytes();
                        updateList(dAddress, "cmd:0x0D, " + title + ":" + s + " , " + Arrays.toString(byteTo16String(t)));
                        write(dAddress, t.length, cmd, t);
                        break;
                    }
                    case 0x0E: {//	"广播设置"
                        a = Integer.parseInt(s);
                        float xx = a * 0.625f;
                        byte[] gb = intToByteArray(a);
                        updateList(dAddress, "cmd:0x0E, " + title + "毫秒:" + a + "*0.625 =" + xx + " , " + Arrays.toString(byteTo16String(gb)));
                        write(dAddress, gb.length, cmd, gb);
                        break;
                    }
                    case 0x62: {//	"定时温度测量设置"
                        a = Integer.parseInt(s);
                        byte[] ss = {(byte) a};
                        updateList(dAddress, "cmd:0x62, " + " , " + Arrays.toString(byteTo16String(ss)));
                        write(dAddress, ss.length, cmd, ss);
                        break;
                    }
                    case 0x64: {//	"跌倒灵敏度设置"
                        a = Integer.parseInt(s);
                        byte[] ssselect = {(byte) a};
                        Log.i("TAG", "ssselect:" + a);
                        updateList(dAddress, "cmd:0x64, " + "跌倒灵敏度设置 , " + Arrays.toString(byteTo16String(ssselect)));
                        write(dAddress, ssselect.length, cmd, ssselect);
                        break;
                    }
                    default:
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        builder.show();
    }
    /**
     * 消息相关命令 dialog
     * MessageDialog
     * */
    private AlertDialog messageDialog;
    private void initMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("消息相关命令");
        final String[] commants = {"手机请求设备报警", "来电提醒", "来电已接听", "来电已拒接",
                "短信提醒", "自定义短消息", "电量请求","(消息通知)来电提醒(utf-16)", "(消息通知)来电已接听(utf-16)",
                "(消息通知)来电已拒接(utf-16)","(消息通知)短信(utf-16)","(消息通知)邮件(utf-16)", "(消息通知)微信(utf-16)",
                "(消息通知)QQ(utf-16)", "(消息通知)来电提醒(utf-8)","(消息通知)来电已接听(utf-8)","(消息通知)来电已拒接(utf-8)",
                "(消息通知)短信(utf-8)", "(消息通知)whatsapp(utf-8)","(消息通知)line(utf-8)",};

        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        byte[] a = {0x02};
                        updateList(dAddress, "cmd:0x11," + commants[0] + " :"
                                + Arrays.toString(byteTo16String(a)));
                        write(dAddress, a.length, 0x11, a);
                        break;
                    }
                    case 1:{
                        String str = "+12345678910";
                        byte[] st = strToByteArray(str);
                        updateList(dAddress, "cmd:0x14," + commants[1] + " :" + str);
                        write(dAddress, st.length, 0x14, st);
                        break;
                    }
                    case 2: {
                        byte[] s = {0x00};
                        updateList(dAddress, "cmd:0x15," + commants[2] + " :"
                                + Arrays.toString(byteTo16String(s)));
                        write(dAddress, s.length, 0x15, s);
                        break;
                    }
                    case 3: {
                        byte[] j = {0x00};
                        updateList(dAddress, "cmd:0x16," + commants[3] + " :"
                                + Arrays.toString(byteTo16String(j)));
                        write(dAddress, j.length, 0x16, j);
                        break;
                    }
                    case 4: {
                        String d = "+12345678910";
                        updateList(dAddress, "cmd:0x17," + commants[4] + " :" + d);
                        byte[] dx = strToByteArray(d);
                        write(dAddress, dx.length, 0x17, dx);
                        break;
                    }
                    case 5: {//自定义短消息
                        textDialog.show();
                        break;
                    }
                    case 6: {//请求电量
                        byte[] battery = {0x00};
                        updateList(dAddress, "cmd:0x14," + commants[6] + Arrays.toString(battery));
                        write(dAddress, battery.length, 0x14, battery);
                        break;
                    }
                    case 7: {//"(消息通知)来电提醒(utf-16)"
                        Log.i(TAG, "1234567890123");
                        String phone = "小水电费水电费多少";
                        byte[] call = new byte[0];
                        try {
                            call = phone.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }//utf-16  包头有两个字节一个汉字 两个字节   utf-8 一个汉字 三个字节
                        updateList(dAddress, "cmd:0x1B," + commants[7] + " :" + phone);
                        byte[] bt = new byte[9];
                        bt[0] = 0;
                        System.arraycopy(call, 2, bt, 1, 8);
                        write_messgae(dAddress, bt.length, 0x1B, bt);//新
//                        write(dAddress, bt.length, 0x1B, bt);
                        break;
                    }
                    case 8: {//"(消息通知)来电已接听(utf-16)"
                        String phonechat = "小叔叔叔收到的";
                        byte[] callchat = new byte[0];
                        try {
                            callchat = phonechat.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updateList(dAddress, "cmd:0x1B," + commants[8] + " :" + phonechat);
                        byte[] btchat = new byte[9];
                        btchat[0] = 2;
                        System.arraycopy(callchat, 2, btchat, 1, 8);
                        write_messgae(dAddress, btchat.length, 0x1B, btchat);
                        break;
                    }
                    case 9: {//"(消息通知)来电已拒接(utf-16)"
                        String phoneat = "小叔叔叔收到的";
                        byte[] callhat = new byte[0];
                        try {
                            callhat = phoneat.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updateList(dAddress, "cmd:0x1B," + commants[9] + " :" + phoneat);
                        byte[] btcat = new byte[9];
                        btcat[0] = 2;
                        System.arraycopy(callhat, 2, btcat, 1, 8);
                        write_messgae(dAddress, btcat.length, 0x1B, btcat);
                        break;
                    }
                    case 10: {//(消息通知)短信(utf-16)
                        String phoneca = "+实打实地方是对方答复";
                        updateList(dAddress, "cmd:0x1B," + commants[10] + " :" + phoneca);
                        byte[] callcal = new byte[0];
                        try {
                            callcal = phoneca.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        byte[] btca = new byte[9];
                        btca[0] = 3;
                        System.arraycopy(callcal, 2, btca, 1, 8);
                        write_messgae(dAddress, btca.length, 0x1B, btca);
                        break;
                    }
                    case 11: {//消息通知)邮件(utf-16)"
                        String phonecal = "邮件提醒测试";
                        updateList(dAddress, "cmd:0x111," + commants[11] + " :" + phonecal);
                        byte[] callcall = new byte[0];
                        try {
                            callcall = phonecal.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        byte[] btcal = new byte[9];
                        btcal[0] = 4;
                        System.arraycopy(callcall, 2, btcal, 1, 8);
                        write_messgae(dAddress, btcal.length, 0x1B, btcal);
                        break;
                    }
                    case 12: {// "(消息通知)微信(utf-16)",
                        String phone_length = "微信提醒测试中文";
                        updateList(dAddress, "cmd:0x111," + commants[12] + " :" + phone_length);
                        byte[] callcha = new byte[0];
                        try {
                            callcha = phone_length.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        @SuppressWarnings("MismatchedReadAndWriteOfArray") byte[] btCalChat;
                        btCalChat = new byte[9];
                        btCalChat[0] = 5;
                        System.arraycopy(callcha, 2, btCalChat, 1, 8);
//                        write_messgae(dAddress, btcalchat.length, 0x1B, btcalchat);
                        write_string(dAddress, "推送微信消息内容显示的文本内容注意汉字长度");
                        break;
                    }
                    case 13: {// "(消息通知)QQ(utf-16)",
                        String QQ_length = "QQ提醒测试中文需持续";
                        updateList(dAddress, "cmd:0x111," + commants[13] + " :" + QQ_length);
                        byte[] qqcha = new byte[0];
                        try {
                            qqcha = QQ_length.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        byte[] btqq = new byte[9];
                        btqq[0] = 6;
                        System.arraycopy(qqcha, 2, btqq, 1, 8);
                        write_messgae(dAddress, btqq.length, 0x1B, btqq);
                        write_string(dAddress, "推送QQ消息内容显示的文本内容注意汉字长度");
                        break;
                    }
                    case 14:{//(消息通知)来电提醒(utf-8)"
                        String smsLength = "159894758112323";
                        byte[] smsLeng;
                        smsLeng = smsLength.getBytes(StandardCharsets.UTF_8);
                        updateList(dAddress, "cmd:0x111," + commants[14] + " :" + smsLength);
                        byte[] bttSms = new byte[12];
                        bttSms[0] = 0x07;
                        if(smsLeng.length<=11){
                            System.arraycopy(smsLeng, 0, bttSms, 1, smsLeng.length);
                        }else //noinspection ConstantConditions
                            if(11 < smsLeng.length){
                            System.arraycopy(smsLeng, 0, bttSms, 1, 11);
                        }
                        write_messgae(dAddress, bttSms.length, 0x1B, bttSms);
                        write_string(dAddress,"qqenglishpushtesttoshowlong");
                        break;
                    }
                    case 15:{/* (消息通知)来电已接听(utf-8)" */
                        String callLengthEn = "159894758112323778";
                        byte[] smsLengEn = callLengthEn.getBytes(StandardCharsets.UTF_8);
                        updateList(dAddress, "cmd:0x111," + commants[15] + " :" + callLengthEn);
                        byte[] bttCallEn = new byte[12];
                        bttCallEn[0] = 0x08;
                        if(smsLengEn.length<=11){
                            System.arraycopy(smsLengEn, 0, bttCallEn, 1, smsLengEn.length);
                        }else {
                            System.arraycopy(smsLengEn, 0, bttCallEn, 1, 11);
                        }
                        write_messgae(dAddress, bttCallEn.length, 0x1B, bttCallEn);
                        break;
                    }
                    /* (消息通知)来电已拒接(utf-8)" */
                    case 16:{
                        String callLengthEnRe = "159894758112323778";
                        byte[] smsLengEnRe;
                        smsLengEnRe = callLengthEnRe.getBytes(StandardCharsets.UTF_8);
                        updateList(dAddress, "cmd:0x111," + commants[16] + " :" + callLengthEnRe);
                        byte[] bttCallEnRe = new byte[12];
                        bttCallEnRe[0] = 0x09;
                        if(smsLengEnRe.length<=11){
                            System.arraycopy(smsLengEnRe, 0, bttCallEnRe, 1, smsLengEnRe.length);
                        }else {
                            System.arraycopy(smsLengEnRe, 0, bttCallEnRe, 1, 11);
                        }
                        write_messgae(dAddress, bttCallEnRe.length, 0x1B, bttCallEnRe);
                        break;
                    }
                    //(消息通知)短信(utf-8)"
                    case 17: {
                        String smsLengthEnRe = "smscalliningingingingingingingnignign";
                        byte[] smsLengEn;
                        smsLengEn = smsLengthEnRe.getBytes(StandardCharsets.UTF_8);
                        updateList(dAddress, "cmd:0x111," + commants[17] + " :" + smsLengthEnRe);
                        byte[] smsEnRe = new byte[12];
                        smsEnRe[0] = 0x0A;
                        if (smsLengEn.length <= 11) {
                            System.arraycopy(smsLengEn, 0, smsEnRe, 1, smsLengEn.length);
                        } else {
                            System.arraycopy(smsLengEn, 0, smsEnRe, 1, 11);
                        }
                        write_messgae(dAddress, smsEnRe.length, 0x1B, smsEnRe);
                        write_string(dAddress, "utf8_sms_messagetoshownoticethelength");
                        break;
                    }
                    /* (消息通知)whatsapp(utf-8) */
                    case 18:{
                        //noinspection SpellCheckingInspection
                        String whatsAppRe = "whatsapp_calliningingingingingingingnignign";
                        byte[] whatsAppLengEn = new byte[0];
                        try {
                            whatsAppLengEn = whatsAppRe.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updateList(dAddress, "cmd:0x111," + commants[18] + " :" + whatsAppRe);
                        byte[] whatsAppEnRe = new byte[12];
                        whatsAppEnRe[0] = 0x0B;
                        if(11 >= whatsAppLengEn.length){
                            System.arraycopy(whatsAppLengEn, 2, whatsAppEnRe, 1, whatsAppLengEn.length);
                        }else {
                            System.arraycopy(whatsAppLengEn, 2, whatsAppEnRe, 1, 11);
                        }
                        write_messgae(dAddress, whatsAppEnRe.length, 0x1B, whatsAppEnRe);
                        write_string(dAddress,"utf8_whatsapp_messagetoshownoticethelength");
                        break;
                    }
                    case 19: {//(消息通知)line(utf-8)",
                        String lineLengthEnRe = "line_sadasdasdasdsadasdsadsadsadsadasdsadsad";
                        byte[] lineLangEn = new byte[0];
                        try {
                            lineLangEn = lineLengthEnRe.getBytes("Unicode");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updateList(dAddress, "cmd:0x111," + commants[19] + " :" + lineLengthEnRe);
                        byte[] lineEnRe = new byte[12];
                        lineEnRe[0] = 0x0C;
                        if (11 >= lineLangEn.length) {
                            System.arraycopy(lineLangEn, 2, lineEnRe, 1, lineEnRe.length);
                        } else {
                            System.arraycopy(lineLangEn, 2, lineEnRe, 1, 11);
                        }
                        write_messgae(dAddress, lineEnRe.length, 0x1B, lineEnRe);
                        write_string(dAddress, "utf16_line推送中英文混合显示注意字节长度");
                        break;
                    }
                    default:
                }
            }
        });
        messageDialog = builder.create();
    }
    /**
     * 定时测量心率 dialog
     * HeartDialog
     */
    private AlertDialog heartDialog;
    private void initHeartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText2 = view.findViewById(R.id.editText);
        builder.setTitle("请输入0~60分钟数");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText2.getText().toString();
                int a = Integer.parseInt(s);
                L.i("定时测量心率:----------" + s + " --" + a);

                byte[] data = {(byte) a};
                updateList(dAddress, "cmd:0x08, 每隔 " + a + " 分钟定时测量一次心率, " + Arrays.toString(byteTo16String(data)));
                write(dAddress, data.length, 0x08, data);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        heartDialog = builder.create();
    }
    /**
     * 文本命令 dialog
     * TextDialog
     */
    private AlertDialog textDialog;
    private void initTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText = view.findViewById(R.id.editText);
        builder.setTitle("请输入不超过12个汉字的内容");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText.getText().toString();
                L.i("string:----------" + s + "--");
                L.i("unicode:----------" + string2Unicode(s) + "--");
                try {
//					byte [] t = s.getBytes("utf-16"); //因为用utf-16 和 Unicode 编码差不多，所以用两个都可以
                    byte[] t = s.getBytes("Unicode");

                    //要去掉Unicode标识头在发送
                    byte[] data = new byte[16]; //不能超过16个字节
                    byte[] data2 = new byte[16]; //不能超过16个字节
                    if(t.length <=18){
                        System.arraycopy(t, 2, data, 0, t.length - 2);
                    }else //noinspection ConstantConditions
                        if(18 < t.length && 26 >= t.length){
                        System.arraycopy(t, 2, data, 0, 16);
                        System.arraycopy(t, 18, data2, 0, t.length-18);
                    }else //noinspection ConstantConditions
                            if(26 < t.length){
                        System.arraycopy(t, 2, data, 0, 16);
                        System.arraycopy(t, 18, data2, 0, 8);
                    }
                    updateList(dAddress, "cmd:0x19, 发送内容：" + s + "；  Unicode编码 :" + string2Unicode(s) + " ,第一个包:" +
                            Arrays.toString(byteTo16String(data)));
                    write_custom(dAddress, data.length, 0x19,0, data);
                    updateList(dAddress, "cmd:0x19, 第二个包：" + Arrays.toString(byteTo16String(data2)));
                    write_custom(dAddress, data2.length, 0x19,1, data2);

                } catch (UnsupportedEncodingException e) {
                    /* TODO Auto-generated catch block */
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        textDialog = builder.create();
    }
    /**
     * 安全 dialog
     * SecurityDialog
     */
    private AlertDialog securityDialog;
    private void initSecurityDialog() {
        // 因为直接把Mac地址转换成byte[]之后的长度是 17 会超出长度，所以要去掉“ ：”符号，他的长度变成
        // 12,发送的时候要发的长度是16。
        @SuppressLint("HardwareIds") final byte[] bluAddr = BluetoothAdapter.getDefaultAdapter()
                .getAddress().replace(":", "").getBytes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_security)+"");
        final String[] commants = {getString(R.string.phone_to_disconnect)+"", getString(R.string.super_connect)+"", getString(R.string.user_login)+"",
                getString(R.string.requests_mac)+"", getString(R.string.requests_e)+"", getString(R.string.requests_bound)+"",
                getString(R.string.device_characteristic)+"",
                getString(R.string.request_disconnect)+"", getString(R.string.binding_custom)+"",
                getString(R.string.login_custom)+"",getString(R.string.request_deletebunding)+"","设置手环工作模式"};
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        updateList(dAddress, "cmd:0x22," + commants[0] + " :"
                                + Arrays.toString(byteTo16String(bluAddr)));
                        write(dAddress, 16, 0x22, bluAddr);
                        break;
                    }
                    case 1: {
                        byte[] SUPER_BOUND_DATA = {0x01, 0x23, 0x45, 0X67,
                                (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
                                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
                                0x76, 0x54, 0x32, 0x10};
                        updateList(
                                dAddress,
                                "cmd:0x24,"
                                        + commants[1]
                                        + " :"
                                        + Arrays.toString(byteTo16String(SUPER_BOUND_DATA)));
                        write(dAddress, SUPER_BOUND_DATA.length, 0x24,
                                SUPER_BOUND_DATA);
                        break;
                    }
                    case 2: {
                        updateList(dAddress, "cmd:0x23," + commants[2] + " :"
                                + Arrays.toString(byteTo16String(bluAddr)));
                        write(dAddress, 16, 0x23, bluAddr);
                        break;
                    }
                    case 3: {
                        byte[] d = {0x00};
                        updateList(dAddress, "cmd:0x26," + commants[3] + " :"
                                + Arrays.toString(d));
                        write(dAddress, 1, 0x26, d);
                        break;
                    }
                    case 4: {
                        byte[] e = {0x00};
                        updateList(dAddress, "cmd:0x25," + commants[4] + " :"
                                + Arrays.toString(e));
                        write(dAddress, 1, 0x25, e);
                        break;
                    }
                    case 5: {
                        updateList(dAddress, "cmd:0x21," + commants[5] + " :"
                                + Arrays.toString(byteTo16String(bluAddr)));
                        write(dAddress, 16, 0x21, bluAddr);
                        break;
                    }
                    case 6: {
                        byte[] t = {0x00};
                        updateList(dAddress, "cmd:0x28," + commants[6] + " :"
                                + Arrays.toString(t));
                        write(dAddress, t.length, 0x28, t);
                        break;
                    }
                    case 7: {
                        updateList(dAddress, "cmd:0x27," + commants[7]);
                        write(dAddress, 16, 0x27, bluAddr);
                        break;
                    }
                    case 8: {//"手机请求绑定(自定义)"
                        initbondDialog(0x21);
                        bondDialog.show();
                        break;
                    }
                    case 9: {//"用户登录请求(自定义)".
                        initbondDialog(0x23);
                        bondDialog.show();
                        break;
                    }
                    case 10: {//"手机请求删除绑定(自定义)"
                        initbondDialog(0x22);
                        bondDialog.show();
                        break;
                    }
                    case 11: {//设置手环工作模式
                        initWorkDialog();
                        bondDialog.show();
                        break;
                    }
                    default:
                }
            }
        });
        securityDialog = builder.create();
    }
    private void initWorkDialog() {
        String title = "设置手环工作模式:AT+MODE=0 或者AT+MODE=1";
        updateList(dAddress, title);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText2 = view.findViewById(R.id.editText);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok)+"", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText2.getText().toString();
                L.i(TAG,"s:"+s);
                byte[][] arrs= bytetoarray(s);
                write_nohead(dAddress, requireNonNull(arrs)[0].length, 0, arrs[0]);
                if(arrs.length ==2){
                    try {
                        Thread.sleep(2000);
                        write_nohead(dAddress,arrs[1].length,0,arrs[1]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel)+"", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        bondDialog = builder.create();
    }
    /**
     * bondDialog
     */
    private AlertDialog bondDialog;
    private void initbondDialog(final int cmd) {
        String title = "";
        if (cmd == 0x21) {
            title =getString(R.string.custom_binding)+"";
        } else if (cmd == 0x22) {
            title = getString(R.string.custom_delete_binding)+"";
        } else if (cmd == 0x23) {
            title = getString(R.string.custom_login)+"";
        }
        updateList(dAddress, getString(R.string.same)+"");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.ed_layout, null);
        final EditText editText2 = view.findViewById(R.id.editText);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok)+"", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String s = editText2.getText().toString();
                byte[] data = s.getBytes();
                updateList(dAddress, "cmd:" + Integer.toHexString(cmd) + ","+getString(R.string.input)+" :" + s + " ,"
                        + Arrays.toString(data));
                write(dAddress, 16, cmd, data);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel)+"", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        bondDialog = builder.create();
    }
    /**
     * SportsDialog
     */
    private AlertDialog sportsDialog;
    private void initSportsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_sports)+"");
        final String[] commants = {getString(R.string.real_sports)+"", getString(R.string.history_sports)+"",
                getString(R.string.real_atmospheric_pressure)+"",getString(R.string.move_history_data_point),
                getString(R.string.request_nearly_sleep_data)+"",getString(R.string.request_detailed_history_sleep_data)+"",
                getString(R.string.move_history_sleep_data_point)+"",getString(R.string.request_location_action_data)+"",
                getString(R.string.request_history_location_action_data)+"",getString(R.string.request_move_history_location_action_data)+"",
                getString(R.string.close_real_sports_data_synchronization)+"",getString(R.string.close_real_pressure_data_synchronization)+"",};

        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0: {
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] s = {0x01};
                        updateList(dAddress, "cmd:0x31," + commants[0] + " :"
                                + Arrays.toString(s));
                        write(dAddress, s.length, 0x31, s);
                        break;
                    }
                    case 1: {
                        byte[] sh = {0x01};
                        updateList(dAddress, "cmd:0x35," + commants[1] + " :"
                                + Arrays.toString(sh) + "," + getString(R.string.wait));
                        write(dAddress, sh.length, 0x35, sh);
                        break;
                    }
                    case 2: {
                        byte[] qy = {0x01};
                        updateList(dAddress, "cmd:0x37," + commants[2] + " :"
                                + Arrays.toString(qy));
                        write(dAddress, qy.length, 0x37, qy);
                        break;
                    }
                    case 3: { // 请求挪动历史步数数据指针

                        // 为了避免每次同步时间过长，增加请求挪动历史运动数据指针的命令，APP 传送一个时间点过来
                        // 如果发送了一个全 0 的四个数据过来，则代表将指针重置到起始位置。
                        byte[] aa = record_date(2017, 7, 10, 15); // 年，月，日，时
                        updateList(dAddress, "cmd:0x32," + commants[3] + " :"
                                + Arrays.toString(byteTo16String(aa))
                                + "，挪动时间：" + "2016-12-17 00:00");
                        write(dAddress, aa.length, 0x32, aa);
                        break;
                    }
                    case 4: {
                        byte[] sleep = {0x01};
                        updateList(dAddress, "cmd:0x33," + commants[4] + " :"
                                + Arrays.toString(byteTo16String(sleep)));
                        write(dAddress, sleep.length, 0x33, sleep);
                        break;
                    }
                    case 5: {
                        byte[] hisSleep = {0x01};
                        updateList(dAddress, "cmd:0x34," + commants[5] + " :"
                                + Arrays.toString(byteTo16String(hisSleep))
                                + "," + getString(R.string.wait));
                        write(dAddress, hisSleep.length, 0x34, hisSleep);
                        break;
                    }
                    case 6: {
                        // 请求挪动历史睡眠数据指针
                        byte[] sleepzhizhen = record_date(2016, 12, 17, 0); // 年，月，日，时
                        updateList(
                                dAddress,
                                "cmd:0x39,"
                                        + commants[6]
                                        + " :"
                                        + Arrays.toString(byteTo16String(sleepzhizhen))
                                        + "，挪动时间：" + "2016-12-17 00:00");
                        write(dAddress, sleepzhizhen.length, 0x39, sleepzhizhen);
                        break;
                    }
                    case 7: {
                        byte[] locaiton = {0x01};
                        updateList(dAddress, "cmd:0x3A," + commants[7] + " :"
                                + Arrays.toString(byteTo16String(locaiton)));
                        write(dAddress, locaiton.length, 0x3A, locaiton);
                        break;
                    }
                    case 8: {
                        byte[] hislocaiton = {0x01};
                        updateList(
                                dAddress,
                                "cmd:0x3B,"
                                        + commants[8]
                                        + " :"
                                        + Arrays.toString(byteTo16String(hislocaiton))
                                        + ",请等待，历史数据在后台请求");
                        write(dAddress, hislocaiton.length, 0x3B, hislocaiton);
                        break;
                    }
                    case 9: {
                        byte[] locaitonTime = record_date(2016, 12, 17, 0); // 年，月，日，时
                        updateList(
                                dAddress,
                                "cmd:0x3C,"
                                        + commants[9]
                                        + " :"
                                        + Arrays.toString(byteTo16String(locaitonTime))
                                        + "，挪动时间：" + "2016-12-17 00:00");
                        write(dAddress, locaitonTime.length, 0x3C, locaitonTime);
                        break;
                    }
                    case 10: {// 关闭实时运动数据同步
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] s0 = {0x00};
                        updateList(dAddress, "cmd:0x31," + commants[10] + " :"
                                + Arrays.toString(s0));
                        write(dAddress, s0.length, 0x31, s0);

                        break;
                    }
                    case 11: { // 关闭实时气压数据同步

                        byte[] qy0 = {0x00};
                        updateList(dAddress, "cmd:0x37," + commants[11] + " :"
                                + Arrays.toString(qy0));
                        write(dAddress, qy0.length, 0x37, qy0);
                        break;
                    }
                    default:
                }
            }
        });
        sportsDialog = builder.create();
    }
    /**
     * 运动 dialog
     * CheersDialog
     */
    private AlertDialog cheersDialog;
    private void  initCheersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("止血带及水杯相关命令");
        final String[] commants = {"请求药品剂量数据", "请求删除药品剂量数据", "请求设置药品类型",
                "请求实时水温数据 ", "请求实时水量数据", "请求历史水温数据", "请求历史水量数据", "请求好友列表",
                "水杯参数设置(*)"};
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        byte[] zx = {0x01};
                        updateList(dAddress, "cmd:0x59," + commants[0] + " :"
                                + Arrays.toString(byteTo16String(zx))
                                + ",请等待，历史数据在后台请求");
                        write(dAddress, zx.length, 0x59, zx);
                        break;
                    case 1:
                        // 由于蓝牙硬件的特殊性，需要断开连接才能删除，所以设备在收到 APP 指令后，会
                        // 迅速返回一个成功应答，此时 APP 必须主动断开蓝牙连接（Major Command 2, Minor
                        // Command 7, 0x27） ，才进行删除工作，由于药品剂量数据总共有 10 个页面，每个页面删
                        // 除需要 400ms，APP 在断开连接后，请等待 4 秒以上才开始重新连接。
                        byte[] del = {0x00};
                        updateList(dAddress, "cmd:0x5A," + commants[1] + " :"
                                + Arrays.toString(byteTo16String(del)));
                        write(dAddress, del.length, 0x5A, del);
                        break;
                    case 2:
                        byte[] set = {0x05}; // 代表要设置的药品类型，范围是 0～254
                        updateList(dAddress, "cmd:0x5B," + commants[2] + " :"
                                + Arrays.toString(set));
                        write(dAddress, set.length, 0x5B, set);
                        break;
                    case 3:
                    case 4:
                        break;
                }
            }
        });
        cheersDialog = builder.create();
    }
    /**
     * 健康 dialog
     * HealthDialog
     */
    private AlertDialog healthDialog;
    private void initHealthDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_health)+"");
        final String[] commants = {getString(R.string.request_real_heartrate)+"", getString(R.string.request_history_heartrate)+"",
                getString(R.string.request_real_temperature)+"",getString(R.string.request_history_temperature)+"",
                getString(R.string.request_move_history_heartrate)+"", getString(R.string.request_move_history_temperature)+"",
                getString(R.string.close_real_heartrate)+"", getString(R.string.request_allhealth_data)+"",
                getString(R.string.close_real_temperature)+"",};
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("dBleDevice:" + dBleDevice.getAddress());
                switch (which) {
                    case 0:
                        // 0x00 关闭数据实时同步，0x01 打开数据实时同步
                        byte[] dx = {0x01};
                        updateList(dAddress, "cmd:0x41," + commants[0] + " :"
                                + Arrays.toString(dx));
                        write(dAddress, dx.length, 0x41, dx);
                        break;
                    case 1:
                        byte[] hh = {0x01};
                        updateList(dAddress, "cmd:0x43," + commants[1] + " :"
                                + Arrays.toString(hh) + ",请等待，历史数据在后台请求");
                        write(dAddress, hh.length, 0x43, hh);
                        break;
                    case 2:
                        byte[] t = {0x01};
                        updateList(dAddress, "cmd:0x44," + commants[2] + " :"
                                + Arrays.toString(t));
                        write(dAddress, t.length, 0x44, t);
                        break;
                    case 3:
                        byte[] th = {0x01};
                        updateList(dAddress, "cmd:0x46," + commants[3] + " :"
                                + Arrays.toString(th) + ",请等待，历史数据在后台请求");
                        write(dAddress, th.length, 0x46, th);
                        break;
                    case 4:
                        // 请求挪动历史心率数据指针
                        byte[] ah = record_date(2016, 12, 17, 0);
                        updateList(dAddress, "cmd:0x49," + commants[4] + " :"
                                + "2016-12-17 00:00");
                        write(dAddress, ah.length, 0x49, ah);
                        break;
                    case 5:
                        // 请求挪动历史体温数据指针
                        byte[] at = record_date(2016, 12, 17, 0);
                        updateList(dAddress, "cmd:0x4A," + commants[5] + " :"
                                + "2016-12-17 00:00");
                        write(dAddress, at.length, 0x4A, at);
                        break;
                    case 6: // 关闭实时心率数据同步
                        byte[] dx0 = {0x00};
                        updateList(dAddress, "cmd:0x41," + commants[6] + " :"
                                + Arrays.toString(dx0));
                        write(dAddress, dx0.length, 0x41, dx0);
                        break;
                    case 7: // 请求全部健康数据
                        Log.i("MainActivity", "请求全部健康数据");
                        byte[] dx03 = {0x00};
                        updateList(dAddress, "cmd:0x4B," + commants[7] + " :"
                                + Arrays.toString(dx03));
                        write(dAddress, dx03.length, 0x4B, dx03);
                        break;
                    case 8: // 关闭实时体温数据
                        Log.i("MainActivity", "关闭实时体温数据");
                        byte[] offtem = {0x00};
                        updateList(dAddress, "cmd:0x44," + commants[8] + " :"
                                + Arrays.toString(offtem));
                        write(dAddress, offtem.length, 0x44, offtem);
                        break;

                }
            }
        });
        healthDialog = builder.create();
    }
    /**
     * 信息推送 dialog
     * pushsettingDialog
     */
    private AlertDialog pushSettingDialog;
    private void  initPushSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("推送相关命令");
        final String[] commants = {"推送设置", "定时温度测量设置", "跌倒灵敏度设置","振动次数设置"};

        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:
//						byte[] s = { 0x01 };
//						updateList(dAddress, "cmd:0x61," + commants[0] + " :"
//								+ Arrays.toString(s));
//						write(dAddress, s.length, 0x61, s);
                        break;
                    case 1:
                        String subtitle = "请输入整数. 0 代表定时测量体温关闭，默认设置是0。 比如 1 代表每 5 秒钟定时测量一次体温,5代表每25秒钟测量一次体温 ";
                        settingDialog(0x62, commants[1], subtitle);
//						byte[] sh = { 0x01 };
//						updateList(dAddress, "cmd:0x62," + commants[1] + " :"
//								+ Arrays.toString(sh) + ",请等待");
//						write(dAddress, sh.length, 0x62, sh);
                        break;
                    case 2:
                        String subtitleselect = "请输入整数.数值为1,2,3三种。1的灵敏度最低,3的灵敏度最高 ";
                        settingDialog(0x64, commants[2], subtitleselect);
//						byte[] sh = { 0x01 };
//						updateList(dAddress, "cmd:0x62," + commants[1] + " :"
//								+ Arrays.toString(sh) + ",请等待");
//						write(dAddress, sh.length, 0x62, sh);
                        break;
                    case 3:
                        byte[] sh = new byte[16];
                        sh[0] =2;//来电提醒
                        sh[1] =3;//短信
                        sh[2] =4;//邮件
                        sh[3] =5;//久坐
                        sh[4] =6;//自定义
                        sh[5] =7;//qq
                        sh[6] =8;//微信
                        sh[7] =9;//闹钟
                        sh[8] =10;//whatsapp
                        updateList(dAddress, "cmd:0x65," + commants[3] + " :"
                                + Arrays.toString(sh) + ",请等待");
                        write(dAddress, sh.length, 0x65, sh);
                        break;

                }
            }
        });
        pushSettingDialog = builder.create();
    }
    /**
     * 裸数据 dialog
     * rawDataDialog
     */
    private AlertDialog rawDataDialog;
    private void initRawDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("裸数据请求相关命令");
        final String[] commants = {"请求加速度裸数据", "请求心率裸数据",
                "请求关闭加速度裸数据实时同步", "请求关闭心率裸数据实时同步","请求加速度裸数据写入文件",
                "请求欧拉角裸数据","请求关闭欧拉角裸数据","请求历史心率波峰数据","请求挪动历史心率波峰数据指针"};
        builder.setItems(commants, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // System.out.println("dBleDevice:" + dBleDevice.getAddres());
                switch (which) {
                    case 0:
                        byte[] s = {0x01};
                        updateList(dAddress, "cmd:0x71," + commants[0] + " :"
                                + Arrays.toString(s));
                        is_need_toWriteFile=false;
                        write(dAddress, s.length, 0x71, s);
                        break;
                    case 1:
                        byte[] sh = {0x01};
                        updateList(dAddress, "cmd:0x73," + commants[1] + " :"
                                + Arrays.toString(sh) + ",请等待");
                        write(dAddress, sh.length, 0x73, sh);
                        break;
                    case 2:
                        byte[] shi = {0x00};
                        is_need_toWriteFile=false;
                        updateList(dAddress, "cmd:0x71," + commants[2] + " :"
                                + Arrays.toString(shi));
                        write(dAddress, shi.length, 0x71, shi);
                        break;
                    case 3:
                        byte[] shii = {0x00};
                        updateList(dAddress, "cmd:0x73," + commants[3] + " :"
                                + Arrays.toString(shii) + ",请等待");
                        write(dAddress, shii.length, 0x73, shii);
                        break;

                    case 4:
                        byte[] ss = {0x01};
                        is_need_toWriteFile=true;
                        updateList(dAddress, "cmd:0x71," + commants[4] + " :"
                                + Arrays.toString(ss));
                        write(dAddress, ss.length, 0x71, ss);
                        break;
                    case 5:
                        byte[] ssss = {0x01};
                        updateList(dAddress, "cmd:0x75," + commants[5] + " :"
                                + Arrays.toString(ssss));
                        write(dAddress, ssss.length, 0x75, ssss);
                        break;
                    case 6:
                        byte[] cl = {0x00};
                        updateList(dAddress, "cmd:0x75," + commants[6] + " :"
                                + Arrays.toString(cl));
                        write(dAddress, cl.length, 0x75, cl);
                        break;
                    case 7:
                        byte[] ccl = {0x00};
                        updateList(dAddress, "cmd:0x77" + commants[7] + ":"
                                + Arrays.toString(ccl));
                        write(dAddress, ccl.length, 0x77, ccl);
                        break;
                    case 8:
                        byte[] ddd = {0x00,0x00,0x00,0x00};
                        updateList(dAddress,"cmd:0x78"+commants[8] + ":" + Arrays.toString(byteTo16String(ddd)));
                        write(dAddress,ddd.length,0x78,ddd);
                        break;
                }
            }
        });
        rawDataDialog = builder.create();
    }

    private void writeImageName(String address, @SuppressWarnings("SameParameterValue") int cmd, int number, String[] name) {
        // number 设置的那一组
        // 一张最多可以设置八张图片的名字
        // 每张图片的文字可以是 8 个英文或者 4 个中文 （中文后续支持），不能超过8个字节
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).writeImageName(cmd, number, name);
    }

    private void writeName(String address, @SuppressWarnings("SameParameterValue") int cmd, byte[] oneFonts, byte[] twoFonts, byte[] threeFonts, byte[] fourFonts) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).wirteUserName(cmd, oneFonts, twoFonts, threeFonts, fourFonts);
    }

    /**
     * @param address
     * @param cmd
     * @param index   发送那一组图片
     */
    @SuppressWarnings("JavaDoc")
    private void writeImage(String address, @SuppressWarnings("SameParameterValue") int cmd, int index, byte[] data) {
        // int n = 8; //要发多少张图片 1=< n <= 8;
        // byte[] data = new byte [128*n];
        // for(int i =0;i<n;i++){
        // System.arraycopy(Image,0,data,128*i,Image.length);
        // }
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).writeImage(cmd, index, data); // 发送第一组图片
    }

    /**
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public boolean getneed_towrite(){
        return is_need_toWriteFile;
    }
    /***
     * 接收选择的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* TODO Auto-generated method stub */
        super.onActivityResult(requestCode, resultCode, data);
        L.i("data数据:----------" + data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH: {
                //noinspection StatementWithEmptyBody
                if (resultCode == RESULT_OK) {
                    // 刚打开蓝牙实际还不能立马就能用
                } else {
                    Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
    }
    /**
     * 发送开机图片
     */
    private void writeBootImages(String address, @SuppressWarnings("SameParameterValue") int cmd, byte[] data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).writeBootImages(cmd, data);
    }

    private void write(String address, int length, int cmd, byte[] data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).write(length, cmd, data);
    }

    private void write_nohead(String address, int length, @SuppressWarnings("SameParameterValue") int cmd, byte[] data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).write_nohead(length, cmd, data);
    }

    private void write_custom(String address, int length, @SuppressWarnings("SameParameterValue") int cmd, int sid, byte[] data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).write_custon(length, cmd, sid,data);
    }

    private void write_messgae(String address, int length, @SuppressWarnings("SameParameterValue") int cmd, byte[] data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).write_messsge(length, cmd, data);
    }

    private void write_string(String address, String data) {
        BLEDevice bleDevice = mBlueDevices.get(address);
        requireNonNull(bleDevice).setValue( data);
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
        /* 停止定位 */
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        /* 停止方向传感器 */
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
     * @param address
     * @param mHandler
     */
    @SuppressWarnings("JavaDoc")
    public void setHandler(String address, Handler mHandler) {
        System.out.println("setHandler :" + address);
        if (!mHandlers.containsKey(address)) {
            mHandlers.put(address, mHandler);
        }
    }
    /**
     * 更新列表打印信息
     *
     * @param address the address
     * @param value   the value
     */
    public void updateList(String address, String value) {
        Handler cHandler = mHandlers.get(address);
        Message msg = new Message();
        // msg.obj = address;
        Bundle b = new Bundle();
        b.putString(address, value);
        msg.setData(b);
        msg.what = 3;
        if (cHandler != null){
            cHandler.sendMessage(msg);
        }
        System.out.println("clearList");
    }

    /**
     * 清除列表信息
     *
     * @param address 地址信息字符串
     */
    private void clearList(String address) {
        System.out.println("clearList :" + address);
        Handler cHandler = mHandlers.get(address);
        Message msg = new Message();
        msg.obj = address;
        Bundle b = new Bundle();
        msg.setData(b);
        msg.what = 2;
        if (cHandler != null) {
            cHandler.sendMessage(msg);
        }
        System.out.println("clearList");
    }
    /**
     * The type My location listener.
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            /* Log.d(TAG, "BDLocationListener -> onReceiveLocation"); */
            /* mapView 销毁后不在处理新接收的位置 */
            if (bdLocation == null || mapView == null) {
                Log.d(TAG, "BDLocation or mapView is null");
                mTvLog.setText("定位失败...");
                return;
            }
            /* isFirstLocate变量为了防止多次调用animateMapStatus()方法，因为将地图移动到当前位置只需要在程序第一次定位时调用即可 */
            if(isFirstLocate){
                /* 设置地图缩放级别和将地图移动到当前经纬度 */
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
            mLongitudes = bdLocation.getLongitude();
            /* 获取定位精度，默认值为0.0f */
            @SuppressWarnings("unused") float radius = bdLocation.getRadius();
            /* 获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明 */
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
            textViewAddText(mTvLog,"    纬度: "+ mLatitude +"\t\t经度: " + mLongitudes);
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
        textView.setText(String.format("%s\n%s", textView.getText(), s));
    }

    private void initBlue() {
        /* 打印设备连接，写入，接收数据的信息 */
        L.isDebug = true;
        mBleManager = new BLEManager(this);
        mBleManager.setScanListener(new ScanListener() { // 扫描回调监听器
            @Override
            public void onScanResult(final int result,
                                     final BLEDevice bleDevice, final int rssi,
                                     final byte[] scanRecord, final String deviceUUID) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        /* 正在扫描 */
                        if (result == 0) {
                            scanIng = true;
                            System.out.println("找到了device:"
                                    + bleDevice.getName() + " Rssi : "
                                    + rssi + "" + "Address : "
                                    + bleDevice.getAddress() + ",uuid:" + deviceUUID);
                            if (bleDevice.getName() == null) {
                                return;
                            }
                            if (mBlueList.size() == 0) {
                                mBlueList.add(bleDevice);
                                rssiMap.put(bleDevice.getAddress(), rssi);
                                uuidMap.put(bleDevice.getAddress(), deviceUUID);
                            }
                            for (int i = 0; i < mBlueList.size(); i++) {
                                if ((mBlueList.get(i).getAddress()).equals(bleDevice.getAddress())) {
                                    break;
                                } else if (i == mBlueList.size() - 1) {
                                    if (!(mBlueList.get(i).getAddress()).equals(bleDevice.getAddress())) {
                                        mBlueList.add(bleDevice);
                                        rssiMap.put(bleDevice.getAddress(), rssi);
                                        uuidMap.put(bleDevice.getAddress(), deviceUUID);
                                    }
                                }
                            }
//									if (!mBlueList.contains(bleDevice)) {
//										mBlueList.add(bleDevice);
//									}
                            lv.setVisibility(View.VISIBLE);
                            mViewAdapter.notifyDataSetChanged();
                        } else {
                            /* 扫描结束 */
                            scanIng = false;
                            itemScan.setTitle("scan");
                            if (mBlueList.size() <= 0) {
                                tvHint.setText(getString(R.string.device_no) + "");
                                tvHint.setVisibility(View.VISIBLE);
                            } else { //noinspection SingleStatementInBlock
                                tvHint.setVisibility(View.GONE);
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        addressView = new ArrayList<>();
        addressView.clear();
    }

    @SuppressWarnings("deprecation")
    private void intiView() {
        perViewPager = findViewById(R.id.per_viewPager);
        perViewPager.setOffscreenPageLimit(4);
        perAdapter = new PerAdapter<>(getSupportFragmentManager(), mFragments);
        perViewPager.setAdapter(perAdapter);
        perViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                dAddress = addressView.get(arg0);
                if (mBlueDevices.get(dAddress) != null) {
                    dBleDevice = mBlueDevices.get(dAddress);
                    L.i(TAG, "当前设备：" + requireNonNull(dBleDevice).getAddress());
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

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
        /* 按钮处理 */
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
    private void menuItem() {
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

    /**
     * @param item
     * @return
     */
    @SuppressWarnings({"AlibabaMethodTooLong", "SingleElementAnnotation"})
    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_scan: {
                itemScan = item;
                showScanClick();
                break;
            }
            case R.id.action_setting: {
                if (dBleDevice != null) {
                    showSettingClick();
                }
                break;
            }
            case R.id.action_message: {
                if (dBleDevice != null) {
                    showMessageClick();
                }
                break;
            }
            case R.id.action_security: {
                if (dBleDevice != null) {
                    showSecurityClick();
                }
                break;
            }
            case R.id.action_sports: {
                if (dBleDevice != null) {
                    showSportsClick();
                }
                break;
            }
            case R.id.action_health: {
                if (dBleDevice != null) {
                    showHealthClick();
                }
                break;
            }
            case R.id.action_cheers: {
                if (dBleDevice != null) {
                    showCheersClick();
                }
                break;
            }
            case R.id.action_pushmessage: {
                if (dBleDevice != null) {
                    showPushMeeageClick();
                }
                break;
            }
            case R.id.action_rawdata: {
                if (dBleDevice != null) {
                    showRawDataClick();
                }
                break;
            }
            case R.id.action_clear: {
                showClearClick();
                break;
            }
            case R.id.action_disconn: {
                updateList(dAddress, "断开连接...");
                if (dBleDevice != null) {
                    showDisconnectClick();
                }
                break;
            }
            case R.id.action_connect: {
                updateList(dAddress, "开始连接...");
                if (dBleDevice != null) {
                    showConnectClick();
                }
                break;
            }
            case R.id.action_rssi: {
                updateList(dAddress, "信号强度...");
                if (dBleDevice != null) {
                    showRssiClick();
                }
                break;
            }
            default:
            //关闭滑动菜单
            mDrawerLayout.closeDrawers();
        }
        return false;
    }

    private void showRssiClick() {
        dBleDevice.getRssi();
    }

    private void showConnectClick() {
        dBleDevice.connect();
    }

    private void showDisconnectClick() {
        dBleDevice.disconnect();
    }

    private void showClearClick() {
        clearList(dAddress);
    }

    private void showRawDataClick() {
        rawDataDialog.show();
    }

    private void showPushMeeageClick() {
        pushSettingDialog.show();
    }

    private void showCheersClick() {
        cheersDialog.show();
    }

    private void showHealthClick() {
        healthDialog.show();
    }

    private void showSportsClick() {
        sportsDialog.show();
    }

    private void showSecurityClick() {
        securityDialog.show();
    }

    private void showMessageClick() {
        messageDialog.show();
    }

    private void showSettingClick() {
        settingDialog.show();
    }

    private void showScanClick() {
        scanDialog.show();
        scanLeDevice();
    }

    @Override public void onClick(View v) {
        SDKInitializer.initialize(this.getApplicationContext());
        switch (v.getId()) {
            case R.id.but_Loc: {
                centerToMyLocation(mLatitude, mLongitudes);
                break;
            }
//            case R.id.but_RoutrPlan: {
//                starRoute();
//                break;
//            }
            case R.id.but_Attribute: {
                showViewAttribute();
                break;
            }
            case R.id.but_Command: {
                showViewCommand();
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
        menuItem();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        /*
        * 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        * LocationMode.Hight_Accuracy：高精度
        * LocationMode. Battery_Saving：低功耗
        * LocationMode. Device_Sensors：仅使用设备
        */

        option.setCoorType("bd09ll");
        /*
        * 可选，设置返回经纬度坐标类型，默认GCJ02
        * GCJ02：国测局坐标；
        * BD09ll：百度经纬度坐标；
        * BD09：百度墨卡托坐标；
        * 海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        */

        int span = 6000;
        option.setScanSpan(span);
        /* 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的 */

        option.setIsNeedAddress(true);
        /* 可选，设置是否需要地址信息，默认不需要 */

        option.setOpenGps(true);
        /* 可选，默认false,设置是否使用gps */

        option.setLocationNotify(true);
        /* 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果 */

        option.setIsNeedLocationDescribe(true);
        /* 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近” */

        option.setIsNeedLocationPoiList(true);
        /* 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到 */

        option.setIgnoreKillProcess(false);
        /* 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死 */

        option.SetIgnoreCacheException(false);
        /* 可选，默认false，设置是否收集CRASH信息，默认收集 */

        option.setEnableSimulateGps(false);
        /*
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
        LatLng mLastLocationData = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLastLocationData);
        baiduMap.animateMapStatus(msu);
    }
    /** 开始规划 */
    private void starRoute() {
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
    private void showViewAttribute() {
        if(mTvLog.getVisibility() == View.VISIBLE){
            mTvLog.setVisibility(View.INVISIBLE);
        }else {
            mTvLog.setVisibility(View.VISIBLE);
        }
    }
    /** 命令View显示 */
    private void showViewCommand() {
        Toast.makeText(getApplicationContext(), "按钮被点击了", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "ShowViewCommand+进入函数");
    }


}
