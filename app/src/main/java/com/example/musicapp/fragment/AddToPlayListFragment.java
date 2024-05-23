package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.AddToPlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddToPlayListFragment extends DialogFragment {
    private RecyclerView playlistsRecyclerView;
    private AddToPlaylistAdapter addToPlaylistAdapter;
    private List<Playlist> playlistList;
    private String userId;
    private String songId;

    public AddToPlayListFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_add_to_play_list, null);

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

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchPlaylistsFromFirestore();
        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the song ID from arguments
        if (getArguments() != null) {
            songId = getArguments().getString("songId");
        } else {
            // Handle the case where songId is not passed, maybe dismiss the dialog or show an error
        }
    }

    private void fetchPlaylistsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    playlistList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Playlist playlist = document.toObject(Playlist.class);
                        playlist.setId(document.getId());
                        playlistList.add(playlist);
                    }
                    addToPlaylistAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }
}