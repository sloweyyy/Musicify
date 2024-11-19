package com.example.musicapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.musicapp.R;
import com.example.musicapp.adapter.TabFavoriteAdapter;
import com.google.android.material.tabs.TabLayout;

public class FavouriteFragment extends Fragment {

    private View view;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TabFavoriteAdapter tabFavoriteAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_favourite, container, false);

        tabLayout = view.findViewById(R.id.tablayout);
        viewPager2 = view.findViewById(R.id.viewpager);
        tabFavoriteAdapter = new TabFavoriteAdapter(getActivity());
        viewPager2.setAdapter(tabFavoriteAdapter);

        // Đặt nội dung cho tab đầu tiên ngay từ ban đầu
        TabLayout.Tab firstTab = tabLayout.getTabAt(0);
        if (firstTab != null) {
            String firstTabText = firstTab.getText().toString();
            TextView textView = view.findViewById(R.id.textView);
            textView.setText(firstTabText);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                // Lấy nội dung của TabItem được chọn
                String selectedTabText = tab.getText().toString();

                // Cập nhật nội dung của TextView
                TextView textView = view.findViewById(R.id.textView);
                textView.setText(selectedTabText);
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
        return view;
    }
}
