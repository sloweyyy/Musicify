package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.activities.LoginActivity;
import com.example.musicapp.activities.PrivacyPolicyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    View view;
    ImageButton notificationIcon, iconBack, modifyName;
    ImageView backgroundAvatar;
    CircleImageView avatar;
    Button logout;
    EditText Name;
    private FirebaseAuth.AuthStateListener mAuthListener;

    FrameLayout notificationDotHolder;
    TextView counterView;
    private FirebaseStorage storage;
    LinearLayout linearLayout;
    ScrollView notificationPopup;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AVATAR_IMAGE_REQUEST = 2;
    Uri selectedAvatarUri = null;
    int notificationCount;
    Uri selectedImageUri = null;
    FirebaseAuth mAuth;
    //    TextView notificationText;
    String data = "";
    FirebaseUser user;
    FirebaseFirestore db;
    TextView privacyPolicy;
    TextView notification;
    TextView modifyPassword;
    List<String> notifications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);
        Name = view.findViewById(R.id.Name);
        backgroundAvatar = view.findViewById(R.id.backgroundAvatar);
        avatar = view.findViewById(R.id.avatarImage);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        notification = view.findViewById(R.id.notification);
        counterView = view.findViewById(R.id.counters);
        notificationIcon = view.findViewById(R.id.notificationIcon);
        privacyPolicy = view.findViewById(R.id.privacyPolicy);
        logout = view.findViewById(R.id.btnLogout);
        modifyName = view.findViewById(R.id.modifyName);
        modifyPassword = view.findViewById(R.id.modifyPassword);
        user = mAuth.getCurrentUser();
        notificationPopup = view.findViewById(R.id.notificationPopup);
        if (notificationPopup != null) {
            linearLayout = notificationPopup.findViewById(R.id.linearLayout);
        } else {
            Log.e("ProfileFragment", "notificationPopup is null");
        }


        if (getArguments() != null) {
            data = getArguments().getString("data", "");
            notifications.add(data);
        }
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            if (document.contains("Name")) {
                                String name = document.getString("Name");
                                Name.setText(name);
                            }
                            if (document.contains("notificationCount")) {
                                notificationCount = document.getLong("notificationCount").intValue();
                                counterView.setText(String.valueOf(notificationCount));
                            }

                            if (document.contains("backgroundImageUrl")) {
                                String backgroundImageUrl = document.getString("backgroundImageUrl");
                                if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
                                    Glide.with(view.getContext()).load(backgroundImageUrl).into(backgroundAvatar);
                                }
                            }

                            if (document.contains("avatarUrl")) {
                                String avatarUrl = document.getString("avatarUrl");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(view.getContext()).load(avatarUrl).circleCrop().into(avatar);
                                }
                            }
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), PrivacyPolicyActivity.class);
                startActivity(intent);
                getActivity().finish();

            }
        });
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationPopup.getVisibility() == View.GONE) {
                    notificationPopup.setVisibility(View.VISIBLE);
                } else {
                    notificationPopup.setVisibility(View.GONE);
                }
            }
        });

        modifyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordSettingFragment fragment = new PasswordSettingFragment();
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationPopup.getVisibility() == View.GONE) {
                    notificationPopup.setVisibility(View.VISIBLE);
                    updateNotifications(notifications);
                } else {
                    notificationPopup.setVisibility(View.GONE);
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        modifyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(Name, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        Name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    saveNameChanges();
                    Name.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });
        Name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    saveNameChanges();
                    Name.setCursorVisible(false);
                }
            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_AVATAR_IMAGE_REQUEST);
            }
        });
        backgroundAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        return view;


    }

    private void saveNameChanges() {
        String newName = Name.getText().toString();
        updateNameInFirestore(newName);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Name.getWindowToken(), 0);
    }

    private void updateNameInFirestore(String newName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", newName);
        updateDocument(updates);
    }

    private void updateCounterInFirestore(int notificationCount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("notificationCount", notificationCount + 1);
        updateDocument(updates);
        counterView.setText(String.valueOf(notificationCount));
    }

    private void addNotificationCardToView(String notificationMessage) {
        if (linearLayout == null) {
            Log.e("ProfileFragment", "linearLayout is null");
            return;
        }

        MaterialCardView cardView = new MaterialCardView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        TextView notificationTextView = new TextView(getActivity());
        notificationTextView.setLayoutParams(new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        notificationTextView.setText(notificationMessage);
        notificationTextView.setTextColor(getResources().getColor(R.color.textColor));

        notificationTextView.setGravity(Gravity.CENTER_VERTICAL);

        cardView.addView(notificationTextView);

        linearLayout.addView(cardView);
    }

    private void updateNotifications(List<String> notifications) {
        linearLayout.removeAllViews();
        for (String message : notifications) {
            addNotificationCardToView(message);
        }
        updateCounterInFirestore(notificationCount);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Differentiate between the requests
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                backgroundAvatar.setImageURI(selectedImageUri);
                uploadToFirestore(selectedImageUri, "backgroundImageUrl");
            } else if (requestCode == PICK_AVATAR_IMAGE_REQUEST) {
                selectedAvatarUri = data.getData();
                avatar.setImageURI(selectedAvatarUri);
                uploadToFirestore(selectedAvatarUri, "avatarUrl");
            }
        }
    }

    private void uploadToFirestore(Uri fileUri, String key) {
        if (fileUri != null) {
            StorageReference fileRef = storage.getReference().child(key + "/" + UUID.randomUUID().toString());

            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(key, downloadUri.toString());
                    updateDocument(updates);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Error uploading image", e);
            });
        }
    }

    private void updateDocument(Map<String, Object> updates) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userDocRef = db.collection("users").document(user.getUid());
            userDocRef.update(updates)
                    .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w("ProfileFragment", "Error updating document", e));
        }
    }
}
