package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Playlist;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PlaylistDetailFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList;
    private Button backButton;
    private Button threeDotsButton;
    private BottomSheetDialog editPlaylistDialog;
    private Playlist currentPlaylist;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;

    private static final String ARG_PLAYLIST_ID = "playlistId";
    private static final String ARG_PLAYLIST_NAME = "playlistName";
    private static final String ARG_PLAYLIST_DESCRIPTION = "playlistDescription";
    private static final String ARG_PLAYLIST_THUMBNAIL = "playlistThumbnail";
    private static final String ARG_PLAYLIST_IMAGE_URL = "playlistImageURL";
    private String mPlaylistImageURL;
    private String mPlaylistName;
    private String mPlaylistDescription;
    private ImageView thumbnailImageView;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private String mPlaylistId;


    public PlaylistDetailFragment() {
        // Required empty public constructor
    }


    public static PlaylistDetailFragment newInstance(String playlistName, String imageURL, String playlistDescription, String playlistId) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYLIST_NAME, playlistName);
        args.putString(ARG_PLAYLIST_IMAGE_URL, imageURL);
        args.putString(ARG_PLAYLIST_DESCRIPTION, playlistDescription);
        args.putString(ARG_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thumbnailImageView = view.findViewById(R.id.playlistBanner);
        nameTextView = view.findViewById(R.id.playlistName);
        descriptionTextView = view.findViewById(R.id.playlistDescription);
        recyclerView = view.findViewById(R.id.recyclerView);

        setupBackButton();
        threeDotsButton = view.findViewById(R.id.threeDots);

        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            mPlaylistName = getArguments().getString(ARG_PLAYLIST_NAME);
            mPlaylistDescription = getArguments().getString(ARG_PLAYLIST_DESCRIPTION);
            mPlaylistImageURL = getArguments().getString(ARG_PLAYLIST_IMAGE_URL);
            mPlaylistId = getArguments().getString(ARG_PLAYLIST_ID);
        }

        Glide.with(requireContext()).load(mPlaylistImageURL).placeholder(R.drawable.image_up).error(R.drawable.image_up).into(thumbnailImageView);

        nameTextView.setText(mPlaylistName);
        descriptionTextView.setText(mPlaylistDescription);

        setupRecyclerView();
        loadSongsFromSpotify();

        threeDotsButton.setOnClickListener(v -> {
            getPlaylistById(mPlaylistId, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentPlaylist = document.toObject(Playlist.class);
                        showEditPlaylistBottomSheet(currentPlaylist);
                    } else {
                        Log.d("PlaylistDetailFragment", "No such playlist");
                    }
                } else {
                    Log.d("PlaylistDetailFragment", "Get failed with ", task.getException());
                }
            });
        });

    }

    private void setupBackButton() {
        backButton = getView().findViewById(R.id.iconBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });
    }


    private void setupRecyclerView() {
        songList = new ArrayList<>();
        adapter = new SongAdapter(getContext(), songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadSongsFromSpotify() {

        adapter.notifyDataSetChanged();
    }

    private void showEditPlaylistBottomSheet(Playlist playlist) {
        editPlaylistDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_new_playlist, null);
        editPlaylistDialog.setContentView(sheetView);

        TextView titleTextView = sheetView.findViewById(R.id.createPlaylistTitle);
        EditText playlistNameEditText = sheetView.findViewById(R.id.playListName);
        Switch privacySwitch = sheetView.findViewById(R.id.privacySwitch);
        ImageView playlistImage = sheetView.findViewById(R.id.playlistImage);
        Button updateButton = sheetView.findViewById(R.id.createPlaylist);

        titleTextView.setText("Edit Playlist");
        updateButton.setText("Update");

        playlistNameEditText.setText(playlist.getName());
        privacySwitch.setChecked(playlist.getPrivacy().equals("Public"));
        if (playlist.getImageURL() != null) {
            Glide.with(requireContext()).load(playlist.getImageURL()).into(playlistImage);
            selectedImageUri = Uri.parse(playlist.getImageURL());
        } else {
            playlistImage.setImageResource(R.drawable.image_up);
        }


        updateButton.setOnClickListener(v -> {
            String newName = playlistNameEditText.getText().toString();
            String newPrivacy = privacySwitch.isChecked() ? "Public" : "Private";

            if (selectedImageUri != null && !selectedImageUri.toString().equals(playlist.getImageURL())) {
                StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
                storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageURL = uri.toString();

                        updatePlaylist(playlist, newName, newPrivacy, newImageURL, editPlaylistDialog);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("PlaylistDetailFragment", "Error uploading image", e);
                });
            } else {
                updatePlaylist(playlist, newName, newPrivacy, playlist.getImageURL(), editPlaylistDialog);
            }
        });

        playlistImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        editPlaylistDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (editPlaylistDialog != null && editPlaylistDialog.isShowing()) {
                ImageView playlistImage = editPlaylistDialog.findViewById(R.id.playlistImage);
                playlistImage.setImageURI(selectedImageUri);
            }
        }
    }

    private void updatePlaylist(Playlist playlist, String newName, String newPrivacy, String newImageURL, BottomSheetDialog bottomSheetDialog) {
        if (selectedImageUri != null && !selectedImageUri.toString().equals(playlist.getImageURL())) {
            StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
            storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uploadedImageURL = uri.toString();
                    updatePlaylistInFirestore(playlist, newName, newPrivacy, uploadedImageURL, bottomSheetDialog);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("PlaylistDetailFragment", "Error uploading image", e);
            });
        } else {
            updatePlaylistInFirestore(playlist, newName, newPrivacy, newImageURL, bottomSheetDialog);
        }
    }


    private void updatePlaylistInFirestore(Playlist playlist, String newName, String newPrivacy, String newImageURL, BottomSheetDialog bottomSheetDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).update("name", newName, "privacy", newPrivacy, "imageURL", newImageURL).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Playlist updated successfully", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();

            currentPlaylist.setName(newName);
            currentPlaylist.setPrivacy(newPrivacy);
            currentPlaylist.setImageURL(newImageURL);

            nameTextView.setText(newName);
            descriptionTextView.setText(currentPlaylist.getDescription());
            Glide.with(requireContext()).load(newImageURL).into(thumbnailImageView);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to update playlist", Toast.LENGTH_SHORT).show();
            Log.e("PlaylistDetailFragment", "Error updating playlist", e);
        });
    }

    public void getPlaylistById(String playlistId, OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistId).get().addOnCompleteListener(listener);
    }


}
