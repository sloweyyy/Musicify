package com.example.musicapp.fragment;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.AlbumsAdapter;
import com.example.musicapp.model.Album;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment {
    private View view;

    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_albums, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        List<Album> albumList = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new AlbumsAdapter(getActivity(), albumList, userId);
        recyclerView.setAdapter(adapter);
        adapter.fetchAlbums();

        ImageView recentlyAddedIcon = view.findViewById(R.id.iconRecentlyAdded);
        TextView recentlyAddedText = view.findViewById(R.id.textRecentlyAdded);
        final boolean[] isRecentlyAdded = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyAdded[0] = !isRecentlyAdded[0];
                if (isRecentlyAdded[0]) {
                    recentlyAddedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyAddedText.setText("Hide Recently Played");
                    adapter.sortAlbumByName();
                } else {
                    recentlyAddedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyAddedText.setText("Recently Played");
                    adapter.sortAlbumByName();
                }
            }
        };
        recentlyAddedIcon.setOnClickListener(recentlyAddedClickListener);
        recentlyAddedText.setOnClickListener(recentlyAddedClickListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }
}