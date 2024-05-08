package com.example.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Button btnSignIn;
    TextView btnRegisterNow;
    EditText editEmail,editPassword;
    TextView msgError;
    TextView textForgetPassword;
    boolean passwordVisible;
//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null){
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(RegisterActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSignIn = findViewById(R.id.btnSignIn);
        editEmail = findViewById(R.id.inputEmail);
        editPassword = findViewById(R.id.inputPassword);
        btnRegisterNow = findViewById(R.id.registerPath);
        btnSignIn= findViewById(R.id.btnSignIn);
        textForgetPassword = findViewById(R.id.forgetPassword);
        msgError = findViewById(R.id.msgError);

        //logic
        textForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ForgetPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        editPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Right = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= editPassword.getRight() - editPassword.getCompoundDrawables()[Right].getBounds().width()) {
                        int selection = editPassword.getSelectionEnd();
                        if (passwordVisible) {
                            // set drawable image here
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off_24dp,0);
                            // For hide password
                            editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            // set drawable image here
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.visibility_24dp, 0);
                            // For show password
                            editPassword.setTransformationMethod(null);
                            passwordVisible = true;
                        }
                        editPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });
        btnRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password;
                email = editEmail.getText().toString().trim();
                password = editPassword.getText().toString().trim();
                if(email.isEmpty()){
                    editEmail.setError("Email is required");
                    //Toast.makeText(LoginActivity.this , "Enter email", Toast.LENGTH_SHORT).show();
                    editEmail.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    editPassword.setError("Password is required");
                    editPassword.requestFocus();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    msgError.setVisibility(View.GONE);

                                } else {
                                    try {
                                        throw task.getException();
                                    }
                                    // If sign-in fails, display a message to the user.
                                    catch(FirebaseAuthInvalidUserException e) {
                                        msgError.setText("Account doesn't exist");
                                        msgError.setVisibility(View.VISIBLE);
                                    }
                                    catch(FirebaseAuthInvalidCredentialsException e) {
                                        msgError.setText("Invalid email or password");
                                        msgError.setVisibility(View.VISIBLE);
                                    }
                                    catch(Exception e) {
                                        msgError.setText("Authentication failed");
                                        msgError.setVisibility(View.VISIBLE);
                                    }
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            msgError.setVisibility(View.GONE);
                                        }
                                    }, 4000);
                                }
                            }
                        });
            }
        });

    }
}
