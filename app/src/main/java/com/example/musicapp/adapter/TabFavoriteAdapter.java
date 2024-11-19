package com.example.musicapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.musicapp.fragment.FollowedArtistFragment;
import com.example.musicapp.fragment.LikedAlbumsFragment;
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
            case 1: return new FollowedArtistFragment();
            case 2: return new LikedAlbumsFragment();
            default: return new PlaylistsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
