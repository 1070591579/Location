package com.example.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import wxz.us.Utils.Utils;
import wxz.us.ble.central.BLEDevice;
import wxz.us.ble.central.L;
import wxz.us.ble.listener.BLEDeviceListener;
import wxz.us.ble.listener.DeviceMessageListener;
import wxz.us.ble.listener.ErrorListener;
import wxz.us.ble.listener.HistoryDataListener;
import wxz.us.ble.listener.OtherDataListener;
import wxz.us.ble.listener.RealtimeDataListener;

public class DeviceFragment extends Fragment {
    private ViewPager device_viewpager;
    private List<Fragment> fragments;
    private FragAdapter adapter;
    private BLEDevice bleDevice;
    private View rootView = null;// 缓存Fragment com.us.view
    public DeviceFragment mDeviceFragment;
    private MainActivity mActivity;
    public UpdateChartsListener mUpdateChartsListener;
    private String TAG = "DeviceFragment";
    FileOutputStream fos;
    int[] tep = new int[3];//加速度裸数据数组
    public UpdateListListener mUpdateListListener;
    private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String text = (String) msg.obj;
            switch (msg.arg1) {
                case 1: {
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (MainActivity) context;
    }

    public DeviceFragment(BLEDevice device) {
        if (device != null) {
            this.bleDevice = device;
        }
        mDeviceFragment = this;
    }

    public void setUpdateChartsListener(UpdateChartsListener updateChartsListener) {
        if (updateChartsListener != null) {
            mUpdateChartsListener = updateChartsListener;
        }
    }

    public void setUpdateListListener(UpdateListListener updateListListener) {
        if (updateListListener != null) {
            mUpdateListListener = updateListListener;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragments = new ArrayList<Fragment>();
        fragments.add(new DeviceListFragment(bleDevice, mDeviceFragment));
        fragments.add(new ChartsFragment(bleDevice, mDeviceFragment));
        adapter = new FragAdapter(getChildFragmentManager(), fragments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.device_viewpager, container, false);
            device_viewpager = rootView.findViewById(R.id.device_viewPager);
            device_viewpager.setOffscreenPageLimit(2);
            device_viewpager.setAdapter(adapter);
//            device_viewpager.onTouchEvent()
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        //下面两个看需要具体用哪一个
        setBLEDeviceListener();
        setAnalysisListener();
        return rootView;
    }

    public void initfile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        // 获取当前时间
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        final String path = "/sdcard/ustone/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String SavePath = str + ".txt";
        File saveFile = new File(SavePath);
        try {
            fos = new FileOutputStream(path + saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置数据已经解析的接口
     */
    private void setAnalysisListener() {
        bleDevice.setErrorListener(new ErrorListener() {
            @Override
            public void onError(String address, int cmd, int errorCode) {
                String text = getString(R.string.result_order) + ":0x" + Integer.toHexString(cmd) + "," + getString(R.string.send_result) + ":" + errorCode;
//						(errorCode<bleDevice.RESPONE_STATE.length?bleDevice.RESPONE_STATE[errorCode]:"");
                Message msg = msgHandler.obtainMessage();
                msg.arg1 = 1;
                msg.obj = text;
                if (errorCode != 0x0c) {
                    msgHandler.sendMessage(msg);
                }
            }
        });
        bleDevice.setRealtimeDataListener(new RealtimeDataListener() {
            @Override
            public void onRealtimeTemperature(String address, float temperature) {
                String temp = "体温: " + temperature + "°";
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealtimeData(address, temp);
                }
            }

            @Override
            public void onRealtimeSports(String address, int step, int distance,
                                         int calory) {
                String sports = getString(R.string.steps) + ":" + step + " ，" + getString(R.string.distance) + ":" + distance + "m , " + " ，" + getString(R.string.Calorie) + ":" + calory + "cal";
                if (mUpdateListListener != null){
                    mUpdateListListener.onRealtimeData(address, sports);
                }
            }

            @Override
            public void onRealtimePressure(String address, float atmospheric,
                                           float altitude, float ambientTemperature) {
                String sports = "气压: " + atmospheric + "Kpa，海拔：" + altitude + "m , 环境温度：" + ambientTemperature + "°";
                if (mUpdateListListener != null){
                    mUpdateListListener.onRealtimeData(address, sports);
                }
            }

            @Override
            public void onRealtimeHearts(String address, int heart) {
                String sports = getString(R.string.heartrate) + ": " + heart;
                if (mUpdateListListener != null){
                    mUpdateListListener.onRealtimeData(address, sports);
                }
            }

            @Override
            public void onRecentSleep(String address, int[] startTime,
                                      int[] stopTime, int[] span) {
                String data = getString(R.string.lastday_sleep_status) + "\n " +
                        getString(R.string.start_sleep_time) + startTime[0] + "-" + startTime[1] + "-" + startTime[2] + " " + startTime[3] + ":" + startTime[4] +
                        "\n" + getString(R.string.stop_sleep_time) + stopTime[0] + "-" + stopTime[1] + "-" + stopTime[2] + " " + stopTime[3] + ":" + stopTime[4] +
                        "\n" + getString(R.string.sleep_time) + span[0] + getString(R.string.hour) + span[1] + getString(R.string.minute);
                if (mUpdateListListener != null){
                    mUpdateListListener.onRealtimeData(address, data);
                }
            }

            @Override
            public void onRealLocationAction(String address, int number,
                                             int action) {
                String data = "当前位置动作：" + number + " 基站编号," + action + " 动作编号";
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealtimeData(address, data);
                }
            }

            /**
             * @param address
             * @param electricity  电量数据
             */
            @Override
            public void onRealElectricity(String address, int electricity) {
                String data = "电量：" + electricity;
                if (mUpdateListListener != null){
                    mUpdateListListener.onRealtimeData(address, data);
                }
            }

            /**
             * @param address
             * @param Hearrate         心率
             * @param LBloodPressure   低血压
             * @param HBloodPressure   高血压
             * @param QxygenPercentsge 血氧浓度
             * @param BreateFraquency  呼吸频率
             */
            @Override
            public void onRealAllHealthData(String address, int Hearrate, int LBloodPressure, int HBloodPressure, int QxygenPercentsge, int BreateFraquency) {
                String data = "健康数据返回：" + "心率:" + Hearrate + ",低血压: " + LBloodPressure +
                        ",高血压: " + HBloodPressure + ",血氧浓度:" + QxygenPercentsge +
                        ",呼吸频率:" + BreateFraquency;
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealtimeData(address, data);
                }
            }

            /**
             * @param address
             * @param data    心率裸数据
             */
            @Override
            public void onRealRawHearrate(String address, byte[] data) {
                final byte[] tempData = new byte[16];
                System.arraycopy(data, 4, tempData, 0, 16);
                int a = 01;
                for (int i = 0; i < 4; i++) {
                    final byte[] by = new byte[4];
                    System.arraycopy(tempData, i * 4, by, 0, 4);
                    a = Utils.bytesToInt(by,0);

                }
                String dat = "心率裸数据返回：" + a;
//                    String dat = "心率裸数据返回：" + Arrays.toString(data);
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealtimeData(address, dat);
                }
            }

            /**
             * 加速度裸数据返回
             *
             * @param address
             * @param x       x轴
             * @param y       y轴
             * @param z       z轴
             */
            int temp = 0;
            @Override
            public void onRealRawAcceleration (String address,final int x, final int y,
                                               final int z){
                String data = "加速度裸数据返回：" + "x:" + x + ",y:" + y + ",z:" + z;
                if (mUpdateListListener != null){
                    if (mActivity.getneed_towrite()) {//需要写入文件
                        if (temp == 0) {
                            initfile();
                            temp++;
                        }
                        L.i(TAG, "需要写入文件");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(50);
                                    tep[0] = x;
                                    tep[1] = y;
                                    tep[2] = z;
                                    saveDatatoFile(tep);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else {
                        L.i(TAG, "不需要写入文件");
                        mUpdateListListener.onRealtimeData(address, data);
                    }}
            }

            /**
             * @param address
             * @param index   一个数据包中的第几组数据
             * @param pitch   pitch角数据
             * @param yaw     yaw角数据
             * @param row     row角数据
             */
            @Override
            public void onRealRawEulerangles (String address,int index, int pitch, int yaw,
                                              int row){
                String dat = "欧拉角数据返回：第" + index + "组" + "pitch:" + pitch + ",yaw:" + yaw + ",row:" + row;
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealtimeData(address, dat);
                }
            }

            /**
             * 历史心率波峰数据回调
             *
             * @param address
             * @param time    时间戳数组
             */
            @Override
            public void onRealRawHearRatePeak(String address, String time) {
                String dat = "历史心率波峰数据：第" + time;
                if (mUpdateListListener != null) {
                    mUpdateListListener.onRealRawHearRatePeak(address, dat);
                }
            }

            /**
             * 历史心率波峰数据指针回调
             * @param address
             * @param data
             */
            @Override
            public void onRawHearRatePeakPointer(String address, byte[] data) {
                if (data[0b100] == 0){
                    mUpdateListListener.onMoveRawHeartRatePeakPointer(address,"指针已经移动");
                } else {
                    mUpdateListListener.onMoveRawHeartRatePeakPointer(address,"操作失败，错误代码："+data[4]);
                }
            }
        });
        bleDevice.setHistoryDataListener(new HistoryDataListener() {
                                             @Override
                                             public void onHistoryTemperature (String address, ArrayList < Long > times,
                                                                               ArrayList < Float > data){
                                                 String aa = "历史体温数据长度：" + data.size();
                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onRealtimeData(address, aa);
                                                 }
                                             }
                                             @Override
                                             public void onHistorySprots (String address, ArrayList < Long > times,
                                                                          ArrayList < Integer > data){

                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onHistoryData(address, 0x35, times, data);
                                                 }
                                             }
                                             @Override
                                             public void onHistoryHeart (String address, ArrayList < Long > times,
                                                                         ArrayList < Integer > data){
                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onHistoryData(address, 0x42, times, data);
                                                 }
                                             }
                                             @Override
                                             public void onHistoryDetailedSleep (String address,
                                                                                 ArrayList < Long > times, ArrayList < Integer > data){
                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onHistoryData(address, 0x34, times, data);
                                                 }
                                             }
                                             @Override
                                             public void onHistoryTourniquet (String address,
                                                                              ArrayList < Long > times, ArrayList < Integer[]>data){
                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onHistoryDosageData(address, 0x59, times, data);
                                                 }
                                             }
                                             @Override
                                             public void onHistoryLocationAction (String address,
                                                                                  ArrayList < Long > times, ArrayList < Integer[]>data){
                                                 if (mUpdateListListener != null) {
                                                     mUpdateListListener.onHistoryDosageData(address, 0x3B, times, data);
                                                 }
                                             }
                                         }

        );
        bleDevice.setOtherDataListener(new

                                               OtherDataListener() {
                                                   @Override
                                                   public void onFunction (String address,int oxygen, int blood,
                                                                           int temperature, int heart, int sleep, int step){
                                                       String sports = "oxygen: " + oxygen + " ,blood：" + blood + ",temperature：" + temperature + ",heart:" + heart + ",sleep:" + sleep + ",step:" + step;
                                                       if (mUpdateListListener != null) {
                                                           mUpdateListListener.onRealtimeData(address, sports);
                                                       }
                                                   }

                                                   @Override
                                                   public void onAlarmList (String address, ArrayList <int[]>alarm){
                                                       // TODO Auto-generated method stub
                                                       if (mUpdateListListener != null){
                                                           mUpdateListListener.onAlarm(address, alarm);
                                                       }
                                                   }

                                                   @Override
                                                   public void onLogin (String address,boolean success){
                                                       String st;
                                                       if (success) {
                                                           st = getString(R.string.login_successful) + "";
                                                       } else {
                                                           st = "登录失败";
                                                       }
                                                       if (mUpdateListListener != null) {
                                                           mUpdateListListener.onRealtimeData(address, st);
                                                       }
                                                   }

                                                   @Override
                                                   public void onbound (String address,boolean success){
                                                       String st;
                                                       if (success) {
                                                           st = getString(R.string.bindig_Success) + "";
                                                       } else {
                                                           st = "绑定失败";
                                                       }
                                                       if (mUpdateListListener != null){
                                                           mUpdateListListener.onRealtimeData(address, st);
                                                       }
                                                   }

                                                   @Override
                                                   public void onFall (String address,int degree){
                                                       String st = getString(R.string.fall) + ": " + degree;
                                                       if (mUpdateListListener != null){
                                                           mUpdateListListener.onRealtimeData(address, st);
                                                       }
                                                   }

                                                   @Override
                                                   public void onDelbound (String address,boolean success){
                                                       String st;
                                                       if (success) {
                                                           st = getString(R.string.delete_binding_success) + "";
                                                       } else {
                                                           st = "删除绑定失败";
                                                       }
                                                       if (mUpdateListListener != null){
                                                           mUpdateListListener.onRealtimeData(address, st);
                                                       }

                                                   }

                                                   @Override
                                                   public void onSendImageAndFontsResult (String address,int cmd,
                                                                                          int progress, int group){
                                                       String data = "cmd：0x" + Integer.toHexString(cmd) + "， 发送结束：" + progress + " ,组编号" + group;
                                                       if (mUpdateListListener != null) {
                                                           mUpdateListListener.onRealtimeData(address, data);
                                                       }
                                                   }
                                               }

        );
    }

    public void saveDatatoFile(int[] by) {
        L.i(TAG, "开始写入数据." + Arrays.toString(by));
        /*
        如果外部存储可用
        写数据
        */
        if (getfile()) {
            try {
                if (fos != null) {
                    fos.write(Arrays.toString(by).getBytes());
                    fos.write("\r\n".toString().getBytes());
                    if (!bleDevice.isConnected()) {
                        fos.close();
                    }
                } else {
                    L.i(TAG, "fos ==null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Looper.prepare();
            Toast.makeText(mActivity,": 外部存储卡不可用", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    public boolean getfile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//如果外部存储可用
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    private void setBLEDeviceListener() {
        bleDevice.setBLEDeviceListener(new BLEDeviceListener() {
            @Override
            public void onDisConnected(String address) {
                if (mUpdateChartsListener != null){
                    mUpdateChartsListener.onDisConnected(address);
                }
                if (mUpdateListListener != null){
                    mUpdateListListener.onDisConnected(address);
                }
            }

            @Override
            public void onConnected(String address) {
                if (mUpdateChartsListener != null){
                    mUpdateChartsListener.onConnected(address);
                }
                if (mUpdateListListener != null){
                    mUpdateListListener.onConnected(address);
                }
            }

            @Override
            public void updateRssi(String address, int rssi) {
                if (mUpdateListListener != null){
                    mUpdateListListener.updateRssi(address, rssi);
                }

            }
        });
        /**DeviceMessageListener接口和 HistoryDataListener，RealtimeDataListener ，setOtherDataListener 这三个接口功能重复了
         *
         * DeviceMessageListener该接口返回的是未解析的数据
         *
         * HistoryDataListener，RealtimeDataListener ，setOtherDataListener 这三个接口返回的是已经解析的数据
         *
         *只要用其中一种就可以了，不用两个都用
         * */
        bleDevice.setDeviceMessageListener(new DeviceMessageListener() {


            @Override
            public void onSendResult(String address, int cmd, byte[] data) {
                Log.i("DYKDeviceFragment", address + Arrays.toString(data));
                //接收设备返回未解析的数据信息
                if (mUpdateChartsListener != null){
                    mUpdateChartsListener.onSendResult(address, cmd, data);
                }

            }

            @Override
            public void onSendHistory(String address, int cmd, List<byte[]> historyData) {
                //接收设备返回未解析的历史数据信息
//				if(mUpdateListListener !=null){
//					mUpdateListListener.onSendHistory(address, cmd, historyData);
//				}
            }
        });
    }



}

