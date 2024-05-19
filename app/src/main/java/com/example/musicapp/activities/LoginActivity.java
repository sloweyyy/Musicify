package com.example.musicapp.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Button btnSignIn,iconBack;
    TextView btnRegisterNow;
    EditText editEmail,editPassword;
    ImageView btnGoogle;
    TextView msgError;
    TextView textForgetPassword;
    boolean passwordVisible;
//     GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Hehe";
//     private static final int RC_SIGN_IN = 9001;
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
        iconBack=findViewById(R.id.iconBack);
        btnGoogle = findViewById(R.id.btnGoogle);
        oneTapClient = Identity.getSignInClient(this);


        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                        .setSupported(true)
                                        .setServerClientId(getString(R.string.default_web_client_id))
                                        .setFilterByAuthorizedAccounts(false)
                                        .build()
                        )
                        .setAutoSelectEnabled(false)
                        .build();
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {
                                try {
                                    startIntentSenderForResult(
                                            result.getPendingIntent().getIntentSender(),
                                            REQ_ONE_TAP, null, 0, 0, 0, null);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                            }
                        });
            }
        });
        //logic
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
                            // set drawable image here
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off_24dp, 0);
                            // For hide password
                            editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            // set drawable image here
                            editPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_24dp, 0);
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
                    //Toast.makeText(LoginActivity.this , "Enter email", Toast.LENGTH_SHORT).show();
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
                                    }
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
                                    }, 6000);
                                }

                    }
                });
            }
        });

    }

//    private void ShowAlertDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//        builder.setTitle("Email Not Verified");
//        builder.setMessage("Please verify your email now. You cannot log in without email verification.");
//        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Intent to open email app
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate with Firebase.
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign-in fails, display a message to the user.
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
