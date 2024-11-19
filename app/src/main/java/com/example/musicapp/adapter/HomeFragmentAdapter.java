package com.example.musicapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.musicapp.fragment.HomeArtistFragment;
import com.example.musicapp.fragment.NewsFragment;

public class HomeFragmentAdapter extends FragmentStateAdapter {
     public HomeFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new NewsFragment();
            case 1: return new HomeArtistFragment();
            default: return new NewsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
