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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.manager.MediaPlayerManager;
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context context;
    private List<Song> songList;
    private OnSongSelectedListener listener;
    private OnLongItemClickListener longItemClickListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface OnSongSelectedListener {
        void onSongSelected(Song song);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(Song song);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.longItemClickListener = listener;
    }

    public SongAdapter(Context context, List<Song> songList, OnSongSelectedListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
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
        Glide.with(context).load(song.getImageUrl()).placeholder(R.drawable.logo).error(R.drawable.playlist_image).into(holder.songThumbnail);

        checkIsLiked(song.getId(), isLiked -> {
            if (isLiked) {
                holder.heartBtn.setImageResource(R.drawable.favourite_filled);
            } else {
                holder.heartBtn.setImageResource(R.drawable.favourite_outline);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int position1 = holder.getAdapterPosition();
            if (position1 != RecyclerView.NO_POSITION && longItemClickListener != null) {
                longItemClickListener.onLongItemClick(songList.get(position1));
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> handleSongClick(holder.getAdapterPosition()));
    }

    private void handleSongClick(int position) {
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

            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            PlaySongFragment currentFragment = (PlaySongFragment) fragmentManager.findFragmentByTag("PlaySongFragment");
            if (currentFragment != null) {
                currentFragment.dismiss();
            }

            // Show the new bottom sheet
            fragment.show(fragmentManager, "PlaySongFragment");

            // Update recent listening song
            updateRecentListeningSong(selected);

            if (listener != null) {
                listener.onSongSelected(selected);
            }
        }
    }

    private void updateRecentListeningSong(Song song) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> recentListeningSong = new HashMap<>();
        recentListeningSong.put("songName", song.getTitle());
        recentListeningSong.put("imageURL", song.getImageUrl());
        recentListeningSong.put("artistName", song.getArtist());
        recentListeningSong.put("songId", song.getId());
        db.collection("users").document(userId).update("recentListeningSong", recentListeningSong)
                .addOnSuccessListener(aVoid -> Log.d("SongAdapter", "Recent listening song updated successfully"))
                .addOnFailureListener(e -> Log.e("SongAdapter", "Failed to update recent listening song: " + e.getMessage()));
    }

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
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                List<String> likedSongs = (List<String>) userDoc.get("likedsong");
                callback.onResult(likedSongs != null && likedSongs.contains(id));
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage()));
    }

    @Override
    public int getItemCount() {
        return (songList != null) ? songList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle;
        TextView artistName;
        ImageView heartBtn;
        ImageView songThumbnail;

        private MediaPlayerManager mediaPlayerManager;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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
    }

    public void PlayFirstSong() {
        handleSongClick(0);
    }

    private void removeSongFromLikedSongs(String songId) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayRemove(songId)).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Log.e("SongAdapter", "Failed to remove song from liked songs: " + e.getMessage()));
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage()));
    }

    public void addSongToLikedSongs(String songId) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayUnion(songId)).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Log.e("SongAdapter", "Failed to add song to liked songs: " + e.getMessage()));
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage()));
    }
}
