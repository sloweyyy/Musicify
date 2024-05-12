package com.example.musicapp.fragment;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.PlaylistAdapter;
import com.example.musicapp.model.Playlist;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {
    private View view;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_playlists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        List<Playlist> playlistList = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new PlaylistAdapter(getActivity(), playlistList, userId);
        recyclerView.setAdapter(adapter);
        adapter.fetchPlaylists();


        ImageView addPlaylistIcon = view.findViewById(R.id.iconAddPlaylist);
        TextView addPlaylistText = view.findViewById(R.id.textAddPlaylist);

        View.OnClickListener addPlaylistClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_new_playlist, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                EditText textInputEditText = view.findViewById(R.id.playListName);
                Button createBtn = view.findViewById(R.id.createPlaylist);
                Switch privacySwitch = view.findViewById(R.id.privacySwitch);
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String playlistName = textInputEditText.getText().toString();
                        String privacy = privacySwitch.isChecked() ? "Public" : "Private";

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Playlist newPlaylist = new Playlist(userId, playlistName, "Description", privacy, R.drawable.playlist_image);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();


                        db.collection("playlists").add(newPlaylist).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getActivity(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                adapter.fetchPlaylists();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                                Log.e("FavouriteFragment", "Error adding playlist", e);
                            }
                        });
                    }
                });


                Button cancelBtn = view.findViewById(R.id.cancelCreatePlaylist);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


            }
        };

        addPlaylistIcon.setOnClickListener(addPlaylistClickListener);
        addPlaylistText.setOnClickListener(addPlaylistClickListener);


        ImageView recentlyPlayedIcon = view.findViewById(R.id.iconRecentlyPlayed);
        TextView recentlyPlayedText = view.findViewById(R.id.textRecentlyPlayed);
        final boolean[] isRecentlyPlayed = {false};

        View.OnClickListener recentlyPlayedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyPlayed[0] = !isRecentlyPlayed[0];
                if (isRecentlyPlayed[0]) {
                    recentlyPlayedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyPlayedText.setText("Hide Recently Played");
                    adapter.sortPlaylistByName();
                } else {
                    recentlyPlayedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyPlayedText.setText("Recently Played");
                    adapter.sortPlaylistByPrivacy();
                }
            }
        };

        recentlyPlayedIcon.setOnClickListener(recentlyPlayedClickListener);
        recentlyPlayedText.setOnClickListener(recentlyPlayedClickListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;
    }


}