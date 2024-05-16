package com.example.musicapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;

public class GettingStartedActivity extends AppCompatActivity {
    TextView typingt;
    private String fullText = "Your ultimate music experience";
    private int index = 0;
    Button btnStart;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_getting_started);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        @SuppressLint({"MissingInflatedId","LocalSuppress"})
        ImageView gif= findViewById(R.id.gif);
        typingt = findViewById(R.id.typingt);
        btnStart = findViewById(R.id.btnStart);
        Glide.with(this).load(R.drawable.gif_background).into(gif);
        handler = new Handler(Looper.getMainLooper());
        typingTextEffect();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterOrSignUp.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void typingTextEffect() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                typingt.setText(fullText.substring(0, index++));
                if (index <= fullText.length()) {
                    typingTextEffect();
                } else {
                    index = 0;
                    handler.postDelayed(this, 2000);
                }
            }
        }, 150);
    }
}
