package com.example.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.fragment.ProfileFragment;

public class PrivacyPolicyActivity extends AppCompatActivity {
    ImageButton iconBack;
    ScrollView tvPolicyContent;
    ProfileFragment profileFragment;
    Boolean isTextExpanded= true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        imageButtonToggle = findViewById(R.id.imageButton_toggle);
        iconBack = findViewById(R.id.iconBack);
        tvPolicyContent= findViewById(R.id.tv_policy_content);
        profileFragment=new ProfileFragment();

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivacyPolicyActivity.this, MainActivity.class);
                intent.putExtra("showProfileFragment", true);
                startActivity(intent);
                finish();

            }
        });


    }
}