package com.example.musicapp.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaylistDetailFragment;
import com.example.musicapp.model.Playlist;
import com.example.musicapp.model.PlaylistSimplified;
import com.example.musicapp.service.SpotifyApiService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LikedPlaylistAdapter extends RecyclerView.Adapter<LikedPlaylistAdapter.ViewHolder> {

    private Context context;
    private List<String> likedPlaylistIds = new ArrayList<>();
    private List<Playlist> likedPlaylists = new ArrayList<>();
    private FirebaseFirestore db;
    private SpotifyApiService spotifyApiService;
    private String userId;
    private String accessToken;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public LikedPlaylistAdapter(Context context, String userId) {
        this.context = context;
        this.userId = userId;
        db = FirebaseFirestore.getInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        spotifyApiService = retrofit.create(SpotifyApiService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = likedPlaylists.get(position);
        holder.bind(playlist);
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("from", "favorite");
            Fragment playlistDetailFragment = PlaylistDetailFragment.newInstance(playlist.getName(), playlist.getImageURL(), playlist.getDescription(), playlist.getId());
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, playlistDetailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return likedPlaylists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView playlistImage;
        private TextView playlistName;
        private TextView playlistCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistImage = itemView.findViewById(R.id.playlistImage);
            playlistName = itemView.findViewById(R.id.playlistName);
            playlistCount = itemView.findViewById(R.id.playlistCount);
        }

        public void bind(Playlist playlist) {
            Glide.with(context).load(playlist.getImageURL())
                    .placeholder(R.drawable.image_up).error(R.drawable.image_up)
                    .into(playlistImage);

            playlistName.setText(playlist.getName());

            // Calculate song count here
            int songCount = playlist.getSongs().size();
            playlistCount.setText(songCount + " songs");
        }
    }

    public void updateLikedPlaylists(List<Playlist> playlists) {
        likedPlaylists.clear();
        likedPlaylists.addAll(playlists);
        notifyDataSetChanged();
    }

    public void fetchLikedPlaylists() {
        if (userId == null) {
            return;
        }
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> likedPlaylistIds = (List<String>) documentSnapshot.get("likedPlaylist");
                    if (likedPlaylistIds != null) {
                        fetchPlaylistsByIds(likedPlaylistIds);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Log.e("LikedPlaylistAdapter", "Failed to fetch liked playlists", e);
                });
    }

    private void fetchPlaylistsByIds(List<String> likedPlaylistIds) {
        for (String playlistId : likedPlaylistIds) {
            spotifyApiService.getPlaylistDetails("Bearer " + accessToken, playlistId).enqueue(new Callback<PlaylistSimplified>() {
                @Override
                public void onResponse(Call<PlaylistSimplified> call, Response<PlaylistSimplified> response) {
                    if (response.isSuccessful()) {
                        PlaylistSimplified playlistSimplified = response.body();
                        Playlist playlist = new Playlist(
                                playlistSimplified.getId(),
                                playlistSimplified.getName(),
                                playlistSimplified.getDescription(),
                                playlistSimplified.images.get(0).getUrl()
                        );
                        likedPlaylists.add(playlist);
                        notifyDataSetChanged();
                    } else {
                        Log.e("LikedPlaylistAdapter", "Failed to fetch playlist details: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<PlaylistSimplified> call, Throwable t) {
                    Log.e("LikedPlaylistAdapter", "Failed to fetch playlist details", t);
                }
            });
        }
    }


}