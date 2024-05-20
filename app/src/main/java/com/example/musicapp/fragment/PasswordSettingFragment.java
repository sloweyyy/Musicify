package com.example.musicapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.musicapp.R;
import com.example.musicapp.activities.RegisterOrSignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PasswordSettingFragment extends Fragment {
    View view;
    Button btnConfirm;
    FirebaseUser user ;
    EditText reinputNewPassword,inputNewPassword,inputOldPassword;
    ImageButton iconBack;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_password_setting, container, false);
        btnConfirm= view.findViewById(R.id.btnConfirm);
        reinputNewPassword= view.findViewById(R.id.reinputNewPassword);
        inputNewPassword= view.findViewById(R.id.inputNewPassword);
        inputOldPassword= view.findViewById(R.id.inputOldPassword);
        iconBack= view.findViewById(R.id.iconBack);
        user = FirebaseAuth.getInstance().getCurrentUser();
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  newPassword,oldPassword,reNewPassword;
                newPassword = inputNewPassword.getText().toString().trim();
                oldPassword = inputOldPassword.getText().toString().trim();
                reNewPassword = reinputNewPassword.getText().toString().trim();
                if (newPassword.isEmpty()) {
                    inputNewPassword.setError("Password is required");
                    inputNewPassword.requestFocus();
                    return;
                }
                if (oldPassword.isEmpty()) {
                    inputOldPassword.setError("Password is required");
                    inputOldPassword.requestFocus();
                    return;
                }
                if (reNewPassword.isEmpty()) {
                    reinputNewPassword.setError("Password is required");
                    reinputNewPassword.requestFocus();
                    return;
                }
                if (newPassword.length() < 6 ){
                    inputNewPassword.setError("Password must be at least 6 characters");
                    inputNewPassword.requestFocus();
                    return;
                }
                if (!newPassword.equals(reNewPassword)) {
                    reinputNewPassword.setError("Password not match");
                    reinputNewPassword.requestFocus();
                    return;
                }
                user.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "User password updated.");
                                }
                            }
                        });
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> newPass = new HashMap<>();
                newPass.put("password", newPassword);
                updateDocument(newPass);

            }
        });
        return view;

    }
    private void updateDocument(Map<String, Object> updates) {
        if (user != null) {
            DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            userDocRef.update(updates)
                    .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w("ProfileFragment", "Error updating document", e));
        }
    }
}