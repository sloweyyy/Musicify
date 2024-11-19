package com.example.musicapp.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.LikedAlbumAdapter;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.viewmodel.LikedAlbumViewModel;
import com.google.firebase.storage.FirebaseStorage;
import java.util.ArrayList;
import java.util.List;

public class LikedAlbumsFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private static final int PICK_IMAGE_REQUEST = 1;
    List<AlbumSimplified> albumList = new ArrayList<>();
    private View view;
    private RecyclerView recyclerView;
    private LikedAlbumViewModel viewModel;
    private LikedAlbumAdapter adapter;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private FirebaseStorage storage;
    private Uri selectedImageUri;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_liked_albums, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        viewModel = new ViewModelProvider(this).get(LikedAlbumViewModel.class);

        ImageView sortIcon = view.findViewById(R.id.sortIcon);
        TextView sortText = view.findViewById(R.id.sortText);
        final boolean[] isRecentlyAdded = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyAdded[0] = !isRecentlyAdded[0];
                if (isRecentlyAdded[0]) {
                    sortIcon.setImageResource(R.drawable.down_arrow);
                    sortText.setText("Name A-Z");
                    adapter.sortAlbumByName();
                } else {
                    sortIcon.setImageResource(R.drawable.up_arrow);
                    sortText.setText("Name Z-A");
                    adapter.sortAlbumByName();
                }
            }
        };
        sortIcon.setOnClickListener(recentlyAddedClickListener);
        sortText.setOnClickListener(recentlyAddedClickListener);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        viewModel.fetchLikedAlbums(accessToken);
        viewModel.getLikedAlbumsLiveData().observe(this, albums -> {
            adapter = new LikedAlbumAdapter(getContext(), albums);
            recyclerView.setAdapter(adapter);
        });
    }
}