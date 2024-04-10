package com.example.musicapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.musicapp.adapter.MainViewPagerAdapter;
import com.example.musicapp.fragment.Fragment_Home;
import com.example.musicapp.fragment.Fragment_artistpage;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        anhxa();
        init();
    }
    private void anhxa()
    {
        tabLayout = findViewById(R.id.myTabLayout);
        viewPager = findViewById(R.id.myViewPaper);
    }
    private void init()
    {
        MainViewPagerAdapter mVPA = new MainViewPagerAdapter(getSupportFragmentManager());
        mVPA.addFragment(new Fragment_Home());
        mVPA.addFragment(new Fragment_artistpage());
        viewPager.setAdapter(mVPA);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.home2);
        tabLayout.getTabAt(1).setIcon(R.drawable.heart1);
    }
}