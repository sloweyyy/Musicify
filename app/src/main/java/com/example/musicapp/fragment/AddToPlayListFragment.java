package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.AddToPlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddToPlayListFragment extends DialogFragment {
    private RecyclerView playlistsRecyclerView;
    private AddToPlaylistAdapter addToPlaylistAdapter;
    private List<Playlist> playlistList;
    private String userId;
    private String songId;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private BottomSheetDialog bottomSheetDialog;
    private StorageReference storageReference;


    public AddToPlayListFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_add_to_play_list, null);
        AppCompatButton createPlaylistButton = dialogView.findViewById(R.id.createPlaylist);
        Bundle bundle = getArguments();
        if (bundle != null) {
            songId = bundle.getString("songId");
        }


        builder.setView(dialogView);

        playlistList = new ArrayList<>();
        playlistsRecyclerView = dialogView.findViewById(R.id.playlistsRecyclerView);
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("AddToPlayListFragment", "onCreateDialog: " + songId);

        addToPlaylistAdapter = new AddToPlaylistAdapter(getContext(), playlistList, userId, songId);
        playlistsRecyclerView.setAdapter(addToPlaylistAdapter);


        createPlaylistButton.setOnClickListener(v -> showCreatePlaylistBottomSheet());

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchPlaylistsFromFirestore();
        return builder.create();
    }

    // Show the bottom sheet for creating a new playlist
    private void showCreatePlaylistBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_new_playlist, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        ImageView playlistImage = sheetView.findViewById(R.id.playlistImage);
        playlistImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        EditText textInputEditText = sheetView.findViewById(R.id.playListName);
        Button createBtn = sheetView.findViewById(R.id.createPlaylist);
        Button cancelBtn = sheetView.findViewById(R.id.cancelCreatePlaylist);

        createBtn.setOnClickListener(v -> {
            String playlistName = textInputEditText.getText().toString().trim(); // Trim for whitespace

            if (playlistName.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                return; // Don't proceed if the name is empty
            }

            createPlaylist(playlistName, selectedImageUri);
        });

        cancelBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    // Method to create a new playlist
    private void createPlaylist(String playlistName, Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storage = FirebaseStorage.getInstance().getReference();

        if (imageUri != null) {
            // Upload image to storage
            StorageReference imageRef = storage.child("playlist_images/" + UUID.randomUUID().toString());
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get download URL after successful upload
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            savePlaylistToFirestore(new Playlist(userId, playlistName, "Description", imageUrl));
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        Log.e("AddToPlayListFragment", "Image upload failed", e);
                        // Handle image upload failure (e.g., save playlist without image)
                    });
        } else {
            // No image selected, save playlist without image URL
            savePlaylistToFirestore(new Playlist(userId, playlistName, "Description", null));
        }
    }


    // Save the playlist data to Firestore
    private void savePlaylistToFirestore(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
                .add(playlist)
                .addOnSuccessListener(documentReference -> {
                    String playlistId = documentReference.getId();
                    playlist.setId(playlistId); // Set the generated ID

                    // Update the document with the ID
                    documentReference.set(playlist)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                fetchPlaylistsFromFirestore(); // Refresh the playlist list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to update playlist with ID", Toast.LENGTH_SHORT).show();
                                Log.e("AddToPlayListFragment", "Playlist update failed", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                    Log.e("AddToPlayListFragment", "Playlist creation failed", e);
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the song ID from arguments (already done in onCreateDialog)
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView playlistImage = bottomSheetDialog.findViewById(R.id.playlistImage);
            if (playlistImage != null) {
                playlistImage.setImageURI(selectedImageUri);
            }
        }
    }

    // Fetch the user's playlists from Firestore
    public void fetchPlaylistsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    playlistList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Playlist playlist = new Playlist(document.getId(), userId, document.getString("name"), document.getString("description"), document.getString("imageURL"));
                        playlist.setSongs((List<String>) document.get("songs"));
                        playlistList.add(playlist);
                    }
                    addToPlaylistAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to fetch playlists", Toast.LENGTH_SHORT).show();
                    Log.e("AddToPlayListFragment", "Playlist fetching failed", e);
                });
    }
}