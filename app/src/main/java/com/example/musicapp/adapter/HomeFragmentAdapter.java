package com.example.musicapp.adapter;

import androidx.annotation.NonNull;

import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicapp.fragment.HomeAlbumFragment;
import com.example.musicapp.fragment.NewsFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeFragmentAdapter extends FragmentStateAdapter {
     public HomeFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new NewsFragment();
//            case 1: return new HomeAlbumFragment();
            default: return new NewsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

}
