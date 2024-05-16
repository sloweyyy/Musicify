package com.example.musicapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicapp.fragment.AlbumsFragment;
import com.example.musicapp.fragment.ArtistsFragment;
import com.example.musicapp.fragment.PlaylistsFragment;


public class TabFavoriteAdapter extends FragmentStateAdapter {

    public TabFavoriteAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PlaylistsFragment();
            case 1: return new ArtistsFragment();
            case 2: return new AlbumsFragment();
            default: return new PlaylistsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
