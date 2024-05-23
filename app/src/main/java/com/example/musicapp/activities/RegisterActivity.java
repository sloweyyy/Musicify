package com.example.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    Button  btnRegister,iconBack;
    TextView textSignInNow;
    boolean passwordVisible;
    EditText inputName,inputEmail,inputPassword;
    TextView msgError;
    //private DocumentReference mFirestore = FirebaseFirestore.getInstance().document("user/user1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        textSignInNow = findViewById(R.id.signinPath);
        btnRegister = findViewById(R.id.btnRegister);
        inputName = findViewById(R.id.inputName);
        inputPassword = findViewById(R.id.inputPassword);
        inputEmail = findViewById(R.id.inputEmail);
        msgError = findViewById(R.id.msgError);
        iconBack= findViewById(R.id.iconBack);

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterOrSignUp.class);
                startActivity(intent);
                finish();
            }
        });
        inputPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Right = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= inputPassword.getRight() - inputPassword.getCompoundDrawables()[Right].getBounds().width()) {
                        int selection = inputPassword.getSelectionEnd();

                        if (passwordVisible) {
                            inputPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off_24dp,0);
                            inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            inputPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.visibility_24dp, 0);
                            inputPassword.setTransformationMethod(null);
                            passwordVisible = true;
                        }
                        inputPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });
        textSignInNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name,email,password;
                name = inputName.getText().toString().trim();
                email = inputEmail.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                if(name.isEmpty()){
                    inputName.setError("Name is required");
                    inputName.requestFocus();
                    return;
                }
                if(email.isEmpty()){
                    inputEmail.setError("Email is required");
                    inputEmail.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    msgError.setText("Password is required");
                    msgError.setVisibility(View.VISIBLE);
                    inputPassword.requestFocus();
                    return;
                }
                if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    inputEmail.setError("Valid email is required");
                    inputEmail.requestFocus();
                    return;
                }
                if(password.length() < 6 ){
                    msgError.setText("Password must be at least 6 characters");
                    msgError.setVisibility(View.VISIBLE);
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account created.", Toast.LENGTH_SHORT).show();

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if(currentUser != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                List<String> likedSongs = new ArrayList<>();
                                List<String> likedAlbums = new ArrayList<>();
                                List<String> likedArtists = new ArrayList<>();
                                Map<String, Object> user = new HashMap<>();
                                user.put("Name", name);
                                user.put("id", currentUser.getUid());
                                user.put("password", password);
                                user.put("email", email); 
                                user.put("likedsong",likedSongs);
                                user.put("likedAlbums", likedAlbums);
                                user.put("likedArtist", likedArtists);
                                user.put("notificationCount",0);

                                DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
                                userDocRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("TAG", "User data saved successfully!");

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Failed to save user data.", e);
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}