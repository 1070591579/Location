package com.example.location;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * 自定义禁止左右滑动的Viewpager
 * */
public class CustomViewPager extends ViewPager {
	private boolean setTouchModel = false;

	public CustomViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (setTouchModel) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (setTouchModel) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}

}
