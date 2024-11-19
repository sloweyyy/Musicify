package com.example.musicapp.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.PlaylistAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PlaylistHomeAdapter extends RecyclerView.Adapter<PlaylistHomeAdapter.ViewHolder> {
    List<PlaylistAPI> playlistList;
    String userId;
    FirebaseUser user;
    FirebaseAuth mAuth;
    private final Context context;

    public PlaylistHomeAdapter(Context context, List<PlaylistAPI> playlistList) {
        this.context = context;
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = "A0QCduiu5FPJJHrmLiRXdCU5pmE2";
        }
        return new PlaylistHomeAdapter.ViewHolder(view);

    }

    private void checkIsLiked(String playlistId, OnIsLikedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userId == null) {
            Log.e("PlaylistHomeAdapter", "User ID is null");
            callback.onResult(false);
            return;
        }
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedPlaylist = (List<String>) documentSnapshot.get("likedPlaylist");
                        callback.onResult(likedPlaylist != null && likedPlaylist.contains(playlistId));
                    } else {
                        Log.e("PlaylistHomeAdapter", "User document does not exist");
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PlaylistHomeAdapter", "Failed to retrieve user document: " + e.getMessage());
                    callback.onResult(false);
                });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistAPI playlist = playlistList.get(position);
        if (playlist != null) {
            if (playlist.getName() != null && holder.playlistName != null) {
                holder.playlistName.setText(playlist.getName());
            }
            if (playlist.tracks != null && holder.totalTracks != null) {
                holder.totalTracks.setText(String.valueOf(playlist.tracks.getTotal()));
            } else if (holder.totalTracks != null) {
                holder.totalTracks.setText("N/A");
            }
        }
        checkIsLiked(playlist.getId(), isLiked -> {
            if (isLiked) {
                holder.heartBtn.setImageResource(R.drawable.favourite_filled);
            } else {
                holder.heartBtn.setImageResource(R.drawable.favourite_outline);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView playlistName;
        TextView totalTracks;
        ImageButton playButton;
        ImageView heartBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlistName);
            totalTracks = itemView.findViewById(R.id.totalTracks);
            heartBtn = itemView.findViewById(R.id.heartBtn);
            playButton = itemView.findViewById(R.id.playButton);
            itemView.setOnClickListener(this);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PlaylistAPI selected = playlistList.get(position);
                        PlaylistDetailAPI fragment = new PlaylistDetailAPI();
                        fragment.setPlaylistId(selected.getId());
                        Bundle args = new Bundle();
                        args.putString("playlistId", selected.getId());
                        fragment.setArguments(args);
                        ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });
            heartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PlaylistAPI playlist = playlistList.get(position);

                        checkIsLiked(playlist.getId(), isLiked -> {
                            if (isLiked) {
                                heartBtn.setImageResource(R.drawable.favourite_outline);
                                removePlaylistFromLikedPlaylists(playlist.getId());
                            } else {
                                heartBtn.setImageResource(R.drawable.favourite_filled);
                                addPlaylistToLikedPlaylists(playlist.getId());
                            }
                        });
                    }
                }
            });
        }

        public void addPlaylistToLikedPlaylists(String playlistId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .update("likedPlaylist", FieldValue.arrayUnion(playlistId))
                    .addOnSuccessListener(aVoid -> Log.d("PlaylistHomeAdapter", "Playlist added to liked playlist"))
                    .addOnFailureListener(e -> Log.e("PlaylistHomeAdapter", "Error adding playlist to liked playlist", e));
        }

        public void removePlaylistFromLikedPlaylists(String playlistId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .update("likedPlaylist", FieldValue.arrayRemove(playlistId))
                    .addOnSuccessListener(aVoid -> Log.d("PlaylistHomeAdapter", "Playlist remove to liked playlist"))
                    .addOnFailureListener(e -> Log.e("PlaylistHomeAdapter", "Error remove playlist to liked playlist", e));
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                PlaylistAPI selected = playlistList.get(position);
                PlaylistDetailAPI fragment = new PlaylistDetailAPI();
                fragment.setPlaylistId(selected.getId());
                Bundle args = new Bundle();
                args.putString("playlistId", selected.getId());
                fragment.setArguments(args);
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
