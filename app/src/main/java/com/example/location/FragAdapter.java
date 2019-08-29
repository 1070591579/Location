package com.example.location;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @author Administrator
 */
public class FragAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public FragAdapter(FragmentManager fm, List<Fragment> mfra) {
        super(fm);
        this.mFragments = mfra;
    }
    @Override
    public Fragment getItem(int arg0) {
        return mFragments.get(arg0);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    /**
     * @param container 视图容器
     * @param position  位置信息
     * @param object	对象
     */
    @Override
    public void destroyItem(View container, int position, Object object) {
    }
}
