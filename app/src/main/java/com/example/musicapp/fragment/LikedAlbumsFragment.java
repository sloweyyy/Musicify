package com.example.musicapp.fragment;

import android.os.Bundle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.LikedAlbumAdapter;
import com.example.musicapp.model.Album;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.example.musicapp.adapter.FetchAccessToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LikedAlbumsFragment extends Fragment  {
    private View view;
    private RecyclerView recyclerView;
    private LikedAlbumAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_albums, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        List<Album> albumList = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new LikedAlbumAdapter(getActivity(), albumList, userId);
        recyclerView.setAdapter(adapter);
        adapter.fetchLikedAlbums();

        ImageView recentlyAddedIcon = view.findViewById(R.id.iconRecentlyAdded);
        TextView recentlyAddedText = view.findViewById(R.id.textRecentlyAdded);
        final boolean[] isRecentlyAdded = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyAdded[0] = !isRecentlyAdded[0];
                if (isRecentlyAdded[0]) {
                    recentlyAddedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyAddedText.setText("Hide Recently Added");
                    adapter.sortAlbumByName();
                } else {
                    recentlyAddedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyAddedText.setText("Recently Added");
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