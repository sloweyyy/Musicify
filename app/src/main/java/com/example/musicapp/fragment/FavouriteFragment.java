package com.example.musicapp.fragment;
import androidx.annotation.*;
import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.musicapp.R;
import com.example.musicapp.adapter.TabFavoriteAdapter;
import com.google.android.material.tabs.TabLayout;

public class FavouriteFragment extends Fragment{

    private TabLayout tabLayout;
    private ViewPager viewPager;

    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_favourite, container,false);
        tabLayout = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager);
        TabFavoriteAdapter tabFavoriteAdapter = new TabFavoriteAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tabFavoriteAdapter.addFragment(new PlaylistsFragment(),"Playlists");
        tabFavoriteAdapter.addFragment(new AlbumsFragment(),   "Albums");
        tabFavoriteAdapter.addFragment(new ArtistsFragment(),  "Artists");
        viewPager.setAdapter(tabFavoriteAdapter);
        return view;
    }
}
