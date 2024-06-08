package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.PlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.example.musicapp.viewmodel.PlaylistViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlaylistsFragment extends Fragment {
    private View view;
    private TabLayout tabLayout;

    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;

    private enum SortState {
        DEFAULT, BY_NAME, BY_CREATED_AT
    }

    private SortState currentSortState = SortState.DEFAULT;

    private PlaylistViewModel playlistViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView); // Find RecyclerView after the view is inflated
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); // Set layout manager

        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);

        LinearLayout liked = view.findViewById(R.id.liked);

        List<Playlist> playlistList = new ArrayList<>();
        String userId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        adapter = new PlaylistAdapter(requireContext(), playlistList, userId);

        recyclerView.setAdapter(adapter);
        playlistViewModel.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            adapter.updatePlaylistList(playlists);
        });
        playlistViewModel.fetchPlaylists(userId);

        ImageView addPlaylistIcon = view.findViewById(R.id.iconAddPlaylist);
        TextView addPlaylistText = view.findViewById(R.id.textAddPlaylist);

        View.OnClickListener addPlaylistClickListener = v -> {
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
                String userId1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (playlistName.isEmpty()) {
                    Toast.makeText(getActivity(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedImageUri != null) {
                    playlistViewModel.uploadImage(selectedImageUri, uri -> {
                        String imageURL = uri.toString();
                        Date createdAt = new Date();
                        Playlist newPlaylist = new Playlist(userId1, playlistName, imageURL, createdAt);
                        playlistViewModel.savePlaylistToFirestore(newPlaylist, success -> {
                            if (success) {
                                Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                playlistViewModel.fetchPlaylists(userId1);
                            } else {
                                Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    Date createdAt = new Date();
                    Playlist newPlaylist = new Playlist(userId1, playlistName, null, createdAt);
                    playlistViewModel.savePlaylistToFirestore(newPlaylist, success -> {
                        if (success) {
                            Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                            playlistViewModel.fetchPlaylists(userId1);
                        } else {
                            Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            Button cancelBtn = sheetView.findViewById(R.id.cancelCreatePlaylist);
            cancelBtn.setOnClickListener(v13 -> bottomSheetDialog.dismiss());
        };

        View.OnClickListener moveToLikedSong = v -> {
            LikedSongFragment fragment = new LikedSongFragment();
            ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
            Bundle args = new Bundle();
        };

        addPlaylistIcon.setOnClickListener(addPlaylistClickListener);
        addPlaylistText.setOnClickListener(addPlaylistClickListener);

        liked.setOnClickListener(moveToLikedSong);

        ImageView recentlyPlayedIcon = view.findViewById(R.id.iconRecentlyPlayed);
        TextView recentlyPlayedText = view.findViewById(R.id.textRecentlyPlayed);

        String finalUserId = userId;
        View.OnClickListener recentlyPlayedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSortState == SortState.DEFAULT) {
                    currentSortState = SortState.BY_NAME;
                    recentlyPlayedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyPlayedText.setText("Sort by Name");
                    playlistViewModel.sortPlaylistsByName();
                } else if (currentSortState == SortState.BY_NAME) {
                    currentSortState = SortState.BY_CREATED_AT;
                    recentlyPlayedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyPlayedText.setText("Sort by Created Date");
                    playlistViewModel.sortPlaylistsbyDate();
                } else {
                    currentSortState = SortState.DEFAULT;
                    recentlyPlayedIcon.setImageResource(R.drawable.down_up_arrow);
                    recentlyPlayedText.setText("Sort by Default");
                    playlistViewModel.fetchPlaylists(finalUserId);
                }
            }
        };
        recentlyPlayedIcon.setOnClickListener(recentlyPlayedClickListener);
        recentlyPlayedText.setOnClickListener(recentlyPlayedClickListener);

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
}
