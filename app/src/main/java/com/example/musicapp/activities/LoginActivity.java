package com.example.musicapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Button btnSignIn, iconBack;
    TextView btnRegisterNow;
    EditText editEmail, editPassword;
    TextView msgError;
    TextView textForgetPassword;
    SharedPreferences sharedPreferences;
    boolean passwordVisible;
    private static final String TAG = "Hehe";
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;
    private SignInClient oneTapClient;

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(RegisterActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
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
        btnSignIn = findViewById(R.id.btnSignIn);
        textForgetPassword = findViewById(R.id.forgetPassword);
        msgError = findViewById(R.id.msgError);
        iconBack = findViewById(R.id.iconBack);
        oneTapClient = Identity.getSignInClient(this);

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterOrSignUp.class);
                startActivity(intent);
                finish();
            }
        });

        textForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
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
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off_24dp, 0);
                            editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_24dp, 0);
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
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = editEmail.getText().toString().trim();
                password = editPassword.getText().toString().trim();
                if (email.isEmpty()) {
                    editEmail.setError("Email is required");
                    editEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    editPassword.setError("Password is required");
                    editPassword.requestFocus();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                            saveLoginStatus();
                            msgError.setVisibility(View.GONE);
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("isNew", isNew);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                msgError.setText("Account doesn't exist");
                                msgError.setVisibility(View.VISIBLE);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                msgError.setText("Invalid email or password");
                                msgError.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                msgError.setText("Authentication failed");
                                msgError.setVisibility(View.VISIBLE);
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    msgError.setVisibility(View.GONE);
                                }
                            }, 6000);
                        }
                    }
                });
            }
        });
    }

    private void saveLoginStatus() {
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("email", editEmail.getText().toString());
        editor.putString("password", editPassword.getText().toString());
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed", e);
            }
        }
    }
}
