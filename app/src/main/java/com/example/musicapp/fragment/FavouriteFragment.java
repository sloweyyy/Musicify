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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class FavouriteFragment extends Fragment {

    private View view;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_favourite, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        List<Playlist> playlistList = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new PlaylistAdapter(getActivity(), playlistList, userId);
        recyclerView.setAdapter(adapter);
        adapter.fetchPlaylists();


        ImageView addPlaylist = view.findViewById(R.id.iconAddPlaylist);
        addPlaylist.setOnClickListener(new View.OnClickListener() {
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
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;
    }
}
