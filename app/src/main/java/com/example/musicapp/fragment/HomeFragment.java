package com.example.musicapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.musicapp.R;
import com.example.musicapp.adapter.HomeFragmentAdapter;
import com.example.musicapp.adapter.TabFavoriteAdapter;
import com.google.android.material.tabs.TabLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
public class HomeFragment extends Fragment {
    private View view;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    ImageView artistImage;
    TextView recentSongArtist,recentSongName;

    private HomeFragmentAdapter homeFragmentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager);
        homeFragmentAdapter = new HomeFragmentAdapter(getActivity());
        viewPager2.setAdapter(homeFragmentAdapter);
        artistImage = view.findViewById(R.id.artistImage);
        recentSongArtist = view.findViewById(R.id.recentSongArtist);
        recentSongName = view.findViewById(R.id.recentSongName);
        if(artistImage==null)
        {
            Glide.with(this)
                    .load(R.drawable.images)
                    .apply(RequestOptions.circleCropTransform())
                    .into(artistImage);
            artistImage.setImageResource(R.drawable.images);
        }
        else {

        }

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager2.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    tabLayout.getTabAt(position).select();
                }
            });
        }
        return view;
    }
}