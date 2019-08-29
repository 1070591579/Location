package com.example.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wxz.us.ble.central.BLEDevice;
import wxz.us.ble.central.L;

public class DeviceListFragment extends Fragment {
    private ListView mListView;
    private List<String> mList;
    private BlueActivity mActivity;
    private MyAdapter adapter;
    String address;
    private BLEDevice mBLEDevice;
    private View rootView = null;// 缓存Fragment
    DeviceFragment mDeviceFragment;
    String tag = "DeviceListFragment";

    public DeviceListFragment(BLEDevice bleDevice, DeviceFragment deviceFragment) {
        mList = new ArrayList<String>();
        this.mBLEDevice = bleDevice;
        this.address = mBLEDevice.getAddress();
        mDeviceFragment = deviceFragment;
    }

    public void clearList() {
        if (mList != null) {
            mList.clear();
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 2: {
                    String adr = msg.obj.toString();
                    System.out.println("DeviceListFragment" + adr);
                    if (address.equals(adr)) {
                        clearList();
                    }
                    break;
                }
                case 3: {
                    String st = msg.getData().getString(address);
                    System.out.println("DeviceListFragment" + st);
                    updateListView(address, st);
                    // if(address.equals(adr)){
                    // clearList();
                    // }
                    break;
                }
                default:
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (BlueActivity) activity;
        mActivity.setHandler(address, mHandler);
    }

    public void setList(String st) {
        mList.add(st);
        adapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getBottom());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceFragment.setUpdateListListener(new UpdateListListener() {
            @Override
            public void updateRssi(String address, int rssi) {
                // TODO Auto-generated method stub
                String data = " 信号：" + rssi;
                updateListView(address, data);
            }

            @Override
            public void onSendResult(String address, int cmd, byte[] data) {
                Log.i("DYKDeviceListFragment",address+ Arrays.toString(data));
                System.out.println("DeviceListFragment onSendResult:" + address
                        + ",cmd:" + Integer.toHexString(cmd));
                String st = new StringBuilder().append("收到指令: 0x").append(Integer.toHexString(cmd)).append(",收到的数据：").append(Arrays.toString(UtilsTools.byteTo16String(data))).toString();
                updateListView(address, st);
            }

            @Override
            public void onSendHistory(String address, int cmd,
                                      List<byte[]> historyData) {
                String st = "收到指令: 0x" + Integer.toHexString(cmd) + ",收到历史数据： "
                        + historyData.size() + "条";
                updateListView(address, st);
            }

            @Override
            public void onDisConnected(String address) {
                System.out.println("DeviceListFragment onDisConnected:"
                        + address);
                String st = "设备断开连接 " + address;
                updateListView(address, st);

            }

            @Override
            public void onConnected(String address) {
                System.out.println("DeviceListFragment onConnected:" + address);
                String st = getString(R.string.connected_device) + address;
                updateListView(address, st);

            }

            @Override
            public void onRealtimeData(String address, String content) {
                Log.i("onRealtimeDatadyk1111",address+ content);
                updateListView(address, content);
            }

            @Override
            public void onAlarm(String address, ArrayList<int[]> alarm) {
                updataAlarm(address,alarm);
            }

            @Override
            public void onRealRawHearRatePeak(String address, String content) {
                Log.i("onRealtimeDatadyk1111",address+ content);
                updateListView(address, content);
            }

            @Override
            public void onMoveRawHeartRatePeakPointer(String address, String result) {
                Log.i("onRealtimeDatadyk1111",address+ result);
                updateListView(address,result);
            }

            @Override
            public void onHistoryData(String address, int cmd,
                                      ArrayList<Long> times, ArrayList<Integer> data) {
                L.i(tag, "onHistoryData cmd = " + cmd);
                switch (cmd) {
                    case 0x34:
                        String a = ","+getString(R.string.sleep_status);
                        updateHistoryDataListView(address, a, times, data);
                        break;
                    case 0x35:
                        String step = ", "+getString(R.string.stepandsleep)+": ";
                        updateHistoryDataListView(address, step, times, data);
                        break;
                    case 0x42:
                        String heart = ", 心率:";
                        updateHistoryDataListView(address, heart, times, data);
                        break;
                    default:
                        break;
                }

            }
            @Override
            public void onHistoryDosageData(String address, int cmd,
                                            ArrayList<Long> times, ArrayList<Integer[]> data) {
                updateHistoryDosageListView(address, cmd,times,  data);
            }
        });
    }
    private void updataAlarm(String address,ArrayList<int[]> alarm){
//		{year,month,day,hour,minute,id,重复日，开关}
        for (int[] integer : alarm) {
            mList.add("时间：" + integer[0] +"-"+integer[1] +"-"+integer[2] +" "+integer[3]+":"+integer[4]
                    +", id:"+integer[5]+", 重复日："+integer[6]+ ", 开关:"+integer[7]);
        }
        mListView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                mListView.setSelection(mListView.getBottom());
            }
        });
    }
    private void updateHistoryDosageListView(String address, int cmd,
                                             ArrayList<Long> times,ArrayList<Integer[]> data) {
        mList.add("长度："+data.size());
        int i = 0;
        switch (cmd) {
            case 0x59: {
                for (Integer[] integer : data) {
                    String time = UtilsTools.stampToDate(times.get(i));
                    mList.add("时间：" + time + " 规格: " + integer[0] + "，Dosage剂量: " + integer[1] + "，类型: " + integer[2] + "，实际剂量：" + integer[3]);
                    i++;
                }
                break;
            }
            case 0x3B: {
                for (Integer[] integer : data) {
                    L.i(tag, "xxxxx" + Arrays.toString(integer));
                    String time = UtilsTools.stampToDate(times.get(i));
                    mList.add("时间：" + time + " 计时: " + integer[0] + "，基站编号: " + integer[1] + "，动作编号: " + integer[2]);
                    i++;
                }
                break;
            }
            default:
        }
        mListView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                mListView.setSelection(mListView.getBottom());
            }
        });
    }


    /***
     * 更新历史数据列表
     *
     * @param address
     * @param st
     * @param times
     * @param data
     */
    private void updateHistoryDataListView(String address, String st,
                                           ArrayList<Long> times,  ArrayList<Integer> data) {
        int i = 0;
        for (Integer integer : data) {
            String time = UtilsTools.stampToDate(times.get(i));
            mList.add(getString(R.string.time)+":" + time + st + integer);
            i++;
        }
        mList.add("长度："+data.size());
        mListView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                mListView.setSelection(mListView.getBottom());
            }
        });
    }

    private void updateListView(final String address, final String data) {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                if(mList.size() >60){
                    mList.remove(0);
                }
                mList.add((address + "--" + data));
                try {
                    Thread.sleep(5);
                    adapter.notifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mListView.setSelection(mListView.getBottom());
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("DeviceListFragment ...  onCreateView");
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.list_fragment, container,
                    false);
            mListView = (ListView) rootView.findViewById(R.id.device_list_view);
            adapter = new MyAdapter(getActivity().getApplicationContext());
            mListView.setAdapter(adapter);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_ble_list, null);
                holder = new ViewHolder();
                holder.mNewsTitle = (TextView) convertView
                        .findViewById(R.id.item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mNewsTitle.setText(mList.get(position));
            return convertView;
        }

        class ViewHolder {
            private TextView mNewsTitle;
        }

    }
}

