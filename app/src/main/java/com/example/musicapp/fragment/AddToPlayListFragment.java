package com.example.musicapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.adapter.AddToPlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.example.musicapp.viewmodel.PlaylistViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class AddToPlayListFragment extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private RecyclerView playlistsRecyclerView;
    private AddToPlaylistAdapter addToPlaylistAdapter;
    private List<Playlist> playlistList;
    private String userId;
    private String songId;
    private Uri selectedImageUri;
    private BottomSheetDialog bottomSheetDialog;
    private PlaylistViewModel playlistViewModel;

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

        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);

        addToPlaylistAdapter = new AddToPlaylistAdapter(getContext(), playlistList, userId, songId, this);
        playlistsRecyclerView.setAdapter(addToPlaylistAdapter);

        createPlaylistButton.setOnClickListener(v -> showCreatePlaylistBottomSheet());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        playlistViewModel.getPlaylists().observe(this, playlists -> {
            playlistList.clear();
            playlistList.addAll(playlists);
            addToPlaylistAdapter.notifyDataSetChanged();
        });

        playlistViewModel.fetchPlaylists(userId);

        return builder.create();
    }

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
            String playlistName = textInputEditText.getText().toString().trim();

            if (playlistName.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                return;
            }

            playlistViewModel.createPlaylist(playlistName, selectedImageUri, success -> {
                if (success) {
                    Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                    playlistViewModel.fetchPlaylists(userId);
                } else {
                    Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cancelBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());
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

    public void dismissDialog() {
        if (getDialog() != null && getDialog().isShowing()) {
            getDialog().dismiss();
        }
    }

}
