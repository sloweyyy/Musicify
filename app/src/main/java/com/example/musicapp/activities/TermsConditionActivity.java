package com.example.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.fragment.ProfileFragment;

public class TermsConditionActivity extends AppCompatActivity {
    ImageButton  iconBack;
    ScrollView tvPolicyContent;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms_condition);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iconBack = findViewById(R.id.iconBack);
        tvPolicyContent= findViewById(R.id.tv_policy_content);
        profileFragment=new ProfileFragment();

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsConditionActivity.this, MainActivity.class);
                intent.putExtra("showProfileFragment", true);
                startActivity(intent);
                finish();

            }
        });

    }
}