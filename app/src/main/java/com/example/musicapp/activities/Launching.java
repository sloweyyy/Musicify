package com.example.musicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Launching extends AppCompatActivity {
    Handler handler = new Handler();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launching);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(Launching.this, GettingStartedActivity.class);
            startActivity(intent);
            finish();
        } else {
            handler.postDelayed(() -> {
                startActivity(new Intent(Launching.this, MainActivity.class));
                finish();
            }, 3000);
        }


    }
}
