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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.PlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PlaylistsFragment extends Fragment {
    private View view;
    private TabLayout tabLayout;


    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;

    private enum SortState {
        DEFAULT, BY_NAME, BY_PRIVACY
    }

    private SortState currentSortState = SortState.DEFAULT;

    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private BottomSheetDialog bottomSheetDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView); // Find RecyclerView after the view is inflated
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); // Set layout manager
        storage = FirebaseStorage.getInstance();

        LinearLayout liked = view.findViewById(R.id.liked);

        List<Playlist> playlistList = new ArrayList<>();
        String userId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        adapter = new PlaylistAdapter(requireContext(), playlistList, userId);

        recyclerView.setAdapter(adapter);
        adapter.fetchPlaylists();

        ImageView addPlaylistIcon = view.findViewById(R.id.iconAddPlaylist);
        TextView addPlaylistText = view.findViewById(R.id.textAddPlaylist);

        View.OnClickListener addPlaylistClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_new_playlist, null);
                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();

                ImageView playlistImage = sheetView.findViewById(R.id.playlistImage);
                playlistImage.setOnClickListener(v1 -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                });

                EditText textInputEditText = sheetView.findViewById(R.id.playListName);
                Button createBtn = sheetView.findViewById(R.id.createPlaylist);

                createBtn.setOnClickListener(v12 -> {
                    String playlistName = textInputEditText.getText().toString();

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


                    if (selectedImageUri != null) {
                        StorageReference storageRef = storage.getReference().child("playlist/" + UUID.randomUUID().toString());
                        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageURL = uri.toString();
                                Playlist newPlaylist = new Playlist(userId, playlistName, "Description", imageURL);
                                savePlaylistToFirestore(newPlaylist, bottomSheetDialog);
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            Log.e("PlaylistsFragment", "Error uploading image", e);
                        });
                    } else {
                        Playlist newPlaylist = new Playlist(userId, playlistName, "Description", null);
                        savePlaylistToFirestore(newPlaylist, bottomSheetDialog);

                    }

                });

                Button cancelBtn = sheetView.findViewById(R.id.cancelCreatePlaylist);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        };

        View.OnClickListener moveToLikedSong = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LikedSongFragment fragment = new LikedSongFragment();
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
                Bundle args = new Bundle();
            }
        };

        addPlaylistIcon.setOnClickListener(addPlaylistClickListener);
        addPlaylistText.setOnClickListener(addPlaylistClickListener);

        liked.setOnClickListener(moveToLikedSong);


        ImageView recentlyPlayedIcon = view.findViewById(R.id.iconRecentlyPlayed);
        TextView recentlyPlayedText = view.findViewById(R.id.textRecentlyPlayed);
        final boolean[] isRecentlyPlayed = {false};

        View.OnClickListener recentlyPlayedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSortState == SortState.DEFAULT) {
                    currentSortState = SortState.BY_NAME;
                    recentlyPlayedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyPlayedText.setText("Sort by Name");
                    adapter.sortPlaylistByName();
                } else if (currentSortState == SortState.BY_NAME) {
                    currentSortState = SortState.BY_PRIVACY;
                    recentlyPlayedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyPlayedText.setText("Sort by Privacy");
                    adapter.sortPlaylistByPrivacy();
                } else {
                    currentSortState = SortState.DEFAULT;
                    recentlyPlayedIcon.setImageResource(R.drawable.down_up_arrow);
                    recentlyPlayedText.setText("Recently Played");
                    adapter.fetchPlaylists();
                }
            }
        };
        recentlyPlayedIcon.setOnClickListener(recentlyPlayedClickListener);
        recentlyPlayedText.setOnClickListener(recentlyPlayedClickListener);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView playlistImage = bottomSheetDialog.findViewById(R.id.playlistImage);
            playlistImage.setImageURI(selectedImageUri);
        }
    }


    private void savePlaylistToFirestore(Playlist newPlaylist, BottomSheetDialog bottomSheetDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
                .add(newPlaylist)
                .addOnSuccessListener(documentReference -> {
                    String playlistId = documentReference.getId();

                    Playlist playlist = new Playlist(playlistId, newPlaylist.getUserId(), newPlaylist.getName(),
                            newPlaylist.getDescription(), newPlaylist.getImageURL());

                    documentReference.set(playlist)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                adapter.fetchPlaylists();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to update playlist with ID", Toast.LENGTH_SHORT).show();
                                Log.e("FavouriteFragment", "Error updating playlist", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                    Log.e("FavouriteFragment", "Error adding playlist", e);
                });
    }

}