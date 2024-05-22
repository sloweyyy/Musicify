package com.example.musicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicapp.activities.Launching;
import com.example.musicapp.activities.LoginActivity;
import com.example.musicapp.databinding.ActivityMainBinding;
import com.example.musicapp.fragment.ExploreFragment;
import com.example.musicapp.fragment.FavouriteFragment;
import com.example.musicapp.fragment.HomeFragment;
import com.example.musicapp.fragment.ProfileFragment;
import com.example.musicapp.model.BottomAppBarListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements BottomAppBarListener {
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // load login state
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, navigate to login screen
            Intent intent = new Intent(MainActivity.this, Launching.class);
            startActivity(intent);
            finish();
            return;
        }

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.search) {
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.favourite) {
                replaceFragment(new FavouriteFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
        if (getIntent().getBooleanExtra("showProfileFragment", false)) {
            replaceFragment(new ProfileFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }

    @Override
    public void hideBottomAppBar() {
        binding.bottomAppBar.setVisibility(View.GONE);
    }

    @Override
    public void showBottomAppBar() {
        binding.bottomAppBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }
}
