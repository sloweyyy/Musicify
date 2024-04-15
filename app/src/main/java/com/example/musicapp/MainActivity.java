package com.example.musicapp;

import static com.example.musicapp.R.id.*;

import android.os.Binder;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.RoomSQLiteQuery;
import androidx.viewpager.widget.ViewPager;

import com.example.musicapp.fragment.Fragment_Home;
import com.example.musicapp.fragment.Fragment_artistpage;
import com.example.musicapp.fragment.accountFragment;
import com.example.musicapp.fragment.exploreFragmant;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);

       bottomBar.setOnItemSelectedListener(item ->
                {
                    int itemId = item.getItemId();
                    if (itemId == R.id.homeTab) {
                        replaceFragment(new Fragment_Home());
                    } else if (itemId == R.id.exploreTab) {
                        replaceFragment(new exploreFragmant());
                    } else if (itemId == R.id.accountTab) {
                        replaceFragment(new accountFragment());
                    }
                    return true;
                }
                );
        replaceFragment(new Fragment_Home());
    }
    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout,fragment);
        transaction.commit();
    }


}