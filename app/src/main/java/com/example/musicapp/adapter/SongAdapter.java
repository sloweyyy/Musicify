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
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context context;
    private List<Song> songList;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    public void sortSongByName() {
        songList.sort((song1, song2) -> {
            String name1 = song1.getTitle();
            String name2 = song2.getTitle();

            if (name1 == null && name2 == null) {
                return 0;
            } else if (name1 == null) {
                return -1;
            } else if (name2 == null) {
                return 1;
            } else {

                return name1.compareTo(name2);
            }
        });
        notifyDataSetChanged();
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
        Glide.with(context).load(song.getImageUrl()).placeholder(R.drawable.playlist_image).error(R.drawable.playlist_image).into(holder.songThumbnail);

        checkIsLiked(song.getId(), isLiked -> {
            if (isLiked) {
                holder.heartBtn.setImageResource(R.drawable.favourite_filled);
            } else {
                holder.heartBtn.setImageResource(R.drawable.favourite_outline);
            }
        });

    }

    // Add missing methods
    public void clearSongs() {
        songList.clear();
        notifyDataSetChanged();
    }

    public List<Song> getSongs() {
        return songList;
    }

    public boolean hasSong(String songId) {
        for (Song song : songList) {
            if (song.getId().equals(songId)) {
                return true;
            }
        }
        return false;
    }

    public void addSong(Song song) {
        songList.add(song);
        notifyItemInserted(songList.size() - 1);
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    private void checkIsLiked(String id, OnIsLikedCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                List<String> likedSongs = (List<String>) userDoc.get("likedsong");
                if (likedSongs != null && likedSongs.contains(id)) {
                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
        });
    }

    @Override
    public int getItemCount() {
        return (songList != null) ? songList.size() : 0;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView songTitle;
        TextView artistName;
        ImageView heartBtn;
        ImageView songThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            songTitle = itemView.findViewById(R.id.songTitle);
            artistName = itemView.findViewById(R.id.artistName);
            heartBtn = itemView.findViewById(R.id.heartBtn);
            songThumbnail = itemView.findViewById(R.id.songThumbnail);

            heartBtn.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Song song = songList.get(position);
                    checkIsLiked(song.getId(), isLiked -> {
                        if (isLiked) {
                            heartBtn.setImageResource(R.drawable.favourite_outline);
                            removeSongFromLikedSongs(song.getId());
                        } else {
                            heartBtn.setImageResource(R.drawable.favourite_filled);
                            addSongToLikedSongs(song.getId());
                        }
                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Song selected = songList.get(position);
                String preId = (position == 0) ? songList.get(songList.size() - 1).getId() : songList.get(position - 1).getId();
                String nextId = (position == songList.size() - 1) ? songList.get(0).getId() : songList.get(position + 1).getId();

                // Open PlaySongFragment as a BottomSheet
                PlaySongFragment fragment = new PlaySongFragment();
                fragment.setSongId(selected.getId());
                fragment.setCurrentSongList(songList, selected.getId());
                Bundle args = new Bundle();
                args.putString("songId", selected.getId());
                args.putString("previousSongId", preId);
                args.putString("nextSongId", nextId);
                fragment.setArguments(args);

                fragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "PlaySongFragment");
            }
        }
    }
    public void PlayFirstSong()
    {
        int position = 0;
        if (position != RecyclerView.NO_POSITION) {
            Song selected = songList.get(position);
            String preId = songList.get(songList.size() - 1).getId();
            String nextId = songList.get(position + 1).getId();
            // Open PlaySongFragment as a BottomSheet
            PlaySongFragment fragment = new PlaySongFragment();
            fragment.setSongId(selected.getId());
            fragment.setCurrentSongList(songList, selected.getId());
            Bundle args = new Bundle();
            args.putString("songId", selected.getId());
            args.putString("previousSongId", preId);
            args.putString("nextSongId", nextId);
            fragment.setArguments(args);
            ((AppCompatActivity)context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    private void removeSongFromLikedSongs(String songId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayRemove(songId)).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to remove song from liked songs: " + e.getMessage());
                });
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
        });
    }

    public void addSongToLikedSongs(String songId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayUnion(songId)).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to add song to liked songs: " + e.getMessage());
                });
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
        });
    }
}