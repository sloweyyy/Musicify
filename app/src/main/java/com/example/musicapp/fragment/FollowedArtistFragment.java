package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.FollowedArtistAdapter;
import com.example.musicapp.model.Artist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class FollowedArtistFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private View view;
    private RecyclerView recyclerView;
    private FollowedArtistAdapter adapter;
    private List<Artist> followedArtists;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;
    private FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_artists, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        storage = FirebaseStorage.getInstance();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        ImageView recentlyPlayedIcon = view.findViewById(R.id.iconSortArtist);
        TextView recentlyPlayedText = view.findViewById(R.id.textRecentlyPlayed);
        final boolean[] isRecentlyPlayed = {false};

        View.OnClickListener recentlyAddedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecentlyPlayed[0] = !isRecentlyPlayed[0];
                if (isRecentlyPlayed[0]) {
                    recentlyPlayedIcon.setImageResource(R.drawable.up_arrow);
                    recentlyPlayedText.setText("Name A-Z");
                    adapter.sortArtistByName(); // Call sortArtistByName()
                } else {
                    recentlyPlayedIcon.setImageResource(R.drawable.down_arrow);
                    recentlyPlayedText.setText("Name Z-A");
                    adapter.sortArtistByName();
                }
            }
        };
        recentlyPlayedIcon.setOnClickListener(recentlyAddedClickListener);
        recentlyPlayedText.setOnClickListener(recentlyAddedClickListener);
        return view;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                if (userDoc.contains("likedArtist")) {
                    List<String> likedArtists = (List<String>) userDoc.get("likedArtist");
                    followedArtists = new ArrayList<>(); // Initialize followedArtists here
                    for (String artistId : likedArtists) {
                        getArtist(accessToken, artistId);
                    }
                }
                adapter = new FollowedArtistAdapter(requireContext(), followedArtists);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    public interface SpotifyApi {
        @GET("v1/artists/{id}")
        Call<Artist> getArtist(@Header("Authorization") String authorization, @Path("id") String artistId);
    }

    private void getArtist(String accessToken, String artistId) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();
        SpotifyApi apiService = retrofit.create(SpotifyApi.class);
        String authorization = "Bearer " + accessToken;

        Call<Artist> call = apiService.getArtist(authorization, artistId);
        call.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(@NonNull Call<Artist> call, @NonNull Response<Artist> response) {
                if (response.isSuccessful()) {
                    Artist artist = response.body();
                    followedArtists.add(artist);
                    adapter.notifyItemInserted(followedArtists.size() - 1);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Cảnh báo");
                    builder.setMessage(response.message());
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<Artist> call, Throwable throwable) {
                Log.e("Error fetching:", throwable.getMessage());
            }
        });
    }
}
