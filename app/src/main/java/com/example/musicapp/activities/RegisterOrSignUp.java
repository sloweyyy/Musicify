package com.example.musicapp.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.R;

public class RegisterOrSignUp extends AppCompatActivity {
    Button btnRegister,btnSignIn,iconBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_or_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
        iconBack= findViewById(R.id.iconBack);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyButtonStyle(btnRegister, R.color.btnColor, 20);
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyButtonStyle(btnSignIn, R.color.btnColor, 20);

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ThemeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void applyButtonStyle(Button button, int colorResId, float cornerRadiusDp) {
        final float scale = getResources().getDisplayMetrics().density;
        int cornerRadiusPx = (int) (cornerRadiusDp * scale + 0.5f);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(ContextCompat.getColor(this, colorResId));
        drawable.setCornerRadius(cornerRadiusPx);

        button.setBackground(drawable);

        if(colorResId == android.R.color.transparent){
            button.setTextColor(ContextCompat.getColor(this, R.color.btnColor));
        } else {
            button.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        }
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        button.startAnimation(fadeIn);
    }
}