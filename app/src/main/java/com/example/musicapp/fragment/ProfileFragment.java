package com.example.musicapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import static android.app.Activity.RESULT_OK;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    View view;
    ImageButton notificationIcon ,iconBack,modifyName;
    ImageView backgroundAvatar;
    CircleImageView avatar;
    Button logout;
    EditText Name;
    FrameLayout notificationDotHolder;
    private FirebaseStorage storage;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AVATAR_IMAGE_REQUEST = 2;
    Uri selectedAvatarUri = null;
    Uri selectedImageUri = null;
    FirebaseAuth mAuth ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container,false);
//        notificationIcon = findViewById(R.id.notificationIcon);
//        notificationDotHolder = new FrameLayout(this);
        Name = view.findViewById(R.id.Name);
        backgroundAvatar= view.findViewById(R.id.backgroundAvatar);
        avatar = view.findViewById(R.id.avatarImage);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        logout= view.findViewById(R.id.btnLogout);
        modifyName = view.findViewById(R.id.modifyName);
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            if(document.contains("Name")){
                                String name = document.getString("Name");
                                Name.setText(name);
                            }

                            if(document.contains("backgroundImageUrl")) {
                                String backgroundImageUrl = document.getString("backgroundImageUrl");
                                if(backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
                                    Glide.with(view.getContext()).load(backgroundImageUrl).into(backgroundAvatar);
                                }
                            }

                            if(document.contains("avatarUrl")) {
                                String avatarUrl = document.getString("avatarUrl");
                                if(avatarUrl != null && !avatarUrl.isEmpty()) {
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
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        modifyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = Name.getText().toString();
                updateNameInFirestore(newName);
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

    private void updateNameInFirestore(String newName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", newName);
        updateDocument(updates);
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
