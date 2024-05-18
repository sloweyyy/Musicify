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
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.Song;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context context;
    private List<Song> songList;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    public interface OnItemClickListener {
        void onItemClick(PlaylistAPI playlist);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.songTitle.setText(song.getTitle());
        holder.artistName.setText(song.getArtist());

        // Load image using Glide
        Glide.with(context)
                .load(song.getImageUrl())
                .placeholder(R.drawable.playlist_image)
                .error(R.drawable.playlist_image)
                .into(holder.songThumbnail);

        checkIsLiked(song.getId(), new OnIsLikedCallback() {
            @Override
            public void onResult(boolean isLiked) {
                if (isLiked) {
                    holder.heartBtn.setImageResource(R.drawable.favourite_filled);
                } else {
                    holder.heartBtn.setImageResource(R.drawable.favourite_outline);
                }
            }
        });
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    private void checkIsLiked(String id, OnIsLikedCallback callback) {
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> likedSongs = (List<String>) userDoc.get("likedsong");
                        callback.onResult(likedSongs.contains(id));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                    callback.onResult(false);
                });
    }
    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView songTitle;
        TextView artistName;
        ImageView heartBtn;
        ImageView songThumbnail;

        private SongAdapter.OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            songTitle = itemView.findViewById(R.id.songTitle);
            artistName = itemView.findViewById(R.id.artistName);
            heartBtn = itemView.findViewById(R.id.heartBtn);
            songThumbnail = itemView.findViewById(R.id.songThumbnail);


            heartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Song song = songList.get(position);
                        checkIsLiked(song.getId(), new OnIsLikedCallback() {
                            @Override
                            public void onResult(boolean isLiked) {
                                if (isLiked) {
                                    removeSongFromLikedSongs(song.getId());
                                    heartBtn.setImageResource(R.drawable.favourite_outline);
                                } else {
                                    addSongToLikedSongs(song.getId());
                                    heartBtn.setImageResource(R.drawable.favourite_filled);
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Song selected = songList.get(position);
                PlaySongFragment fragment = new PlaySongFragment();
                fragment.setSongId(selected.getId());
                Bundle args = new Bundle();
                args.putString("songId", selected.getId());
                fragment.setArguments(args);
                // Add the Fragment to the Activity
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        public void setOnItemClickListener(SongAdapter.OnItemClickListener listenerInput) {
            listener = listenerInput;
        }
    }



    private void removeSongFromLikedSongs(String songId) {
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedsong", FieldValue.arrayRemove(songId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Removed from liked songs successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SongAdapter", "Failed to remove song from liked songs: " + e.getMessage());
                                });
                    } else {
                        Log.e("SongAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
        public void addSongToLikedSongs(String songId) {
            String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
            FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                     if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                            userDoc.getReference().update("likedsong", FieldValue.arrayUnion(songId))
                                .addOnSuccessListener(aVoid -> {

                                Toast.makeText(context, "Add to liked songs successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                // Handle the error
                                Log.e("SongAdapter", "Failed to add song to liked songs: " + e.getMessage());
                                });
                        } else {
                            Log.e("SongAdapter", "No user document found with userId: " + userId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                    });
}
}
