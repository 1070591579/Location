package com.example.location;

import android.text.SpannableStringBuilder;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wxz.us.ble.central.BLEDevice;

public class PerAdapter<DeviceFragment> extends FragmentPagerAdapter {
	private Map<String, BLEDevice> mBleDevice;
	/** content */
	private List<DeviceFragment> listViews;
	private List<String> addressTitle;
	public int size;
	private FragmentManager mFragmentManager;
	public PerAdapter(Map<String, BLEDevice> mBleDevice, FragmentManager fm) {
		super(fm);
		this.mBleDevice = mBleDevice;
		this.mFragmentManager = fm;
	}
	public PerAdapter(FragmentManager fm,
			List<DeviceFragment> mFragments) {
		super(fm);
		if(mFragments != null){
			this.listViews = mFragments;
		}else{
			this.listViews  = new ArrayList<DeviceFragment>();
		}
		addressTitle = new ArrayList<>();
	}

    public void setListViews( List<DeviceFragment> mFragments,String st) {
		this.listViews = mFragments;
		addressTitle.add(st);
	}
	@Override
	public int getCount() {
		return listViews.size();
	}
	@Override
	public CharSequence getPageTitle(int position) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(" "
				+ addressTitle.get(position));
		return ssb;
	}
	@Override
	public Fragment getItem(int arg0) {
		return (Fragment) listViews.get(arg0);
	}
	@Override
	public void destroyItem(View container, int position, Object object) {
	}

}

