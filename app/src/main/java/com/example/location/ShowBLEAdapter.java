package com.example.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import wxz.us.ble.central.BLEDevice;

public class ShowBLEAdapter extends BaseAdapter {

    private Map<String, Integer> rssiMap;
    private Map<String, String> uuidMap;
    private List<BLEDevice> mData;
    private Context mContext;
    private LayoutInflater mInflater;

    /**
     * @param context
     * @param data
     * @param rssi
     * @param uuid
     */
    public ShowBLEAdapter(Context context, List<BLEDevice> data, Map<String, Integer> rssi, Map<String, String> uuid) {
        mData = data;
        mContext = context;
        rssiMap =rssi;
        uuidMap=uuid;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        /** TODO Auto-generated method stub */
        return position;
    }


    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        /* Peripheral peripheral = mData.get(position); */
        BLEDevice device = mData.get(position);
        if(view == null) {
            view = mInflater.inflate(R.layout.item_ble_list, null);
            TextView textView = view.findViewById(R.id.item_name);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mBLENameView = textView;
/*			Log.i("DYtAG","RSSI:"+
					+rssiMap.get(mData.get(position).getAddress())+",uuid:"+uuidMap.get(mData.get(position).getAddress()));*/
            textView.setText(new StringBuilder().append(device.getName()).append("@").append(device.getAddress()).append(",rssi:").append(rssiMap.get(mData.get(position).getAddress())).append(",uuid:").append(uuidMap.get(mData.get(position).getAddress())).toString());
            view.setTag(viewHolder);
        } else {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.mBLENameView.setText(new StringBuilder().append(device.getName()).append("@").append(device.getAddress()).append(",rssi:").append(rssiMap.get(mData.get(position).getAddress())).append(",uuid:").append(uuidMap.get(mData.get(position).getAddress())).toString());
        }
        return view;
    }

    static class ViewHolder {
        TextView mBLENameView;
    }


}

