package com.example.musicapp.adapter;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.AlbumDetailFragment;
import com.example.musicapp.fragment.ArtistDetailFragment;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FollowedArtistAdapter extends RecyclerView.Adapter<FollowedArtistAdapter.ViewHolder> {
    private Context context;
    private Map<Artist, LocalDateTime> followedArtist ;

    public FollowedArtistAdapter(Context context, Map<Artist, LocalDateTime> followedArtist ) {
        this.context = context;
        this.followedArtist  = followedArtist ;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = (Artist) followedArtist.keySet().toArray()[position];
        String artistName = artist.getName();
        String imageUrl = artist.getImages().get(0).getUrl();
        holder.artistName.setText(artistName);
        Glide.with(context).load(imageUrl).into(holder.artistImage);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView artistImage;
        private TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = itemView.findViewById(R.id.artisthumbnail);
            artistName = itemView.findViewById(R.id.artistName);
        }

        public void bind(Artist artist) {
            String artistname = artist.getName();
            String imageUrl = artist.getImages().get(0).getUrl();

            artistName.setText(artistname);
            Glide.with(context).load(imageUrl).into(artistImage);
        }
        @Override
        public void onClick(View v) {
            Log.e("Clicked On item", "hehe");
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
//                AlbumSimplified selected = likedAlbums.get(position);
                Artist selected = (Artist) followedArtist.keySet().toArray()[position];
                ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
                artistDetailFragment.setArtistId(selected.getId());
                Bundle args = new Bundle();
                args.putString("albumId", selected.getId());
                artistDetailFragment.setArguments(args);
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, artistDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
    public void sortArtistByRecentlyPlayed() {
//        List<Map.Entry<Artist, LocalDateTime>> sortedEntries = new ArrayList<>(followedArtist.entrySet());
//        Collections.sort(sortedEntries, new Comparator<Map.Entry<Artist, LocalDateTime>>() {
//            @Override
//            public int compare(Map.Entry<Artist, LocalDateTime> entry1, Map.Entry<Artist, LocalDateTime> entry2) {
//                return entry2.getValue().compareTo(entry1.getValue());
//            }
//        });
//
//        followedArtist.clear();
//        for (Map.Entry<Artist, LocalDateTime> entry : sortedEntries) {
//            followedArtist.put(entry.getKey(), entry.getValue());
//        }
        notifyDataSetChanged();
    }
    // Method to update the playlist list
    public void updateFollowedArtists(Map<Artist, LocalDateTime> artists) {
        followedArtist.clear();
        followedArtist.putAll(artists);
        notifyDataSetChanged();
    }
    // Method to delete a liked album from Firestore
    public void unfollowedArtist(String albumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("followedartists", FieldValue.arrayRemove(albumId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Removed from followed artists successfully", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FollowedArtistAdapter", "Failed to remove album from followed artists: " + e.getMessage());
                                });
                    } else {
                        Log.e("FollowedArtistAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowedArtistAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
    public void addFollowedArtist(String albumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("followedartists", FieldValue.arrayUnion(albumId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Add to followed artists successfully", Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error
                                    Log.e("FollowedArtistAdapter", "Failed to add album to followed artists: " + e.getMessage());
                                });
                    } else {
                        Log.e("FollowedArtistAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowedArtistAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }


}
