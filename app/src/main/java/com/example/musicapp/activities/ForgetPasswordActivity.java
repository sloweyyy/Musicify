package com.example.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    Button btnResetPassword;
    EditText inputResetEmail;
    FirebaseAuth mAuth;
    ImageView iconBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnResetPassword = findViewById(R.id.btnResetPassword);
        inputResetEmail = findViewById(R.id.inputResetEmail);
        mAuth= FirebaseAuth.getInstance();

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //btnReset
        btnResetPassword.setOnClickListener(v -> {
            String email = inputResetEmail.getText().toString().trim();
            if(email.isEmpty()){
                inputResetEmail.setError("Email is required");
                inputResetEmail.requestFocus();
                return;
            }
            else {
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ForgetPasswordActivity.this, "Reset Password link has been sent to your registered Email", Toast.LENGTH_SHORT). show();
                        Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                        startActivity (intent);
                        finish();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Error sending password reset email");
                                Toast.makeText(ForgetPasswordActivity.this, "Error sending password reset email", Toast.LENGTH_SHORT). show();
                            }
                        });
            }

        });
    }
}