package com.example.musicapp.adapter;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;


public class MainViewPagerAdapter extends PagerAdapter {
    private ArrayList<Fragment> arrayFragment = new ArrayList<>();

    public MainViewPagerAdapter (FragmentManager fm)
    {
        super ();
    }
    public Fragment getItem(int position)
    {
        return arrayFragment.get(position);
    }
    @Override
    public int getCount() {
        return arrayFragment.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return false;
    }
    public void addFragment(Fragment fragment)
    {
        arrayFragment.add(fragment);
    }
}
