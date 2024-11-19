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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.model.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongHomeAdapter extends RecyclerView.Adapter<SongHomeAdapter.ViewHolder> {
    int position;
    private final Context context;
    private final List<Song> songList;
    private final OnSongSelectedListener listener;

    public SongHomeAdapter(Context context, List<Song> songList, OnSongSelectedListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false);
        return new SongHomeAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.trackName.setText(song.getTitle());
        holder.artistName.setText(song.getArtist());

        Glide.with(context)
                .load(song.getImageUrl())
                .error(R.drawable.logo)
                .into(holder.artistPic);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public interface OnSongSelectedListener {
        void onSongSelected(Song song);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView trackName;
        ImageView artistPic;
        ImageButton playButton;
        TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            artistName = itemView.findViewById(R.id.artistName);
            artistPic = itemView.findViewById(R.id.artistPic);
            playButton = itemView.findViewById(R.id.playButton);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Song selected = songList.get(position);
                        PlaySongFragment fragment = new PlaySongFragment();
                        fragment.setSongId(selected.getId());
                        fragment.setCurrentSongList(songList, selected.getId());
                        Bundle args = new Bundle();
                        args.putString("songId", selected.getId());
                        args.putString("previousSongId", getPreviousSongId(position));
                        args.putString("nextSongId", getNextSongId(position));
                        fragment.setArguments(args);

                        FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                        fragment.show(fragmentManager, "PlaySongFragment");

                        updateRecentListeningSong(selected);

                        if (listener != null) {
                            listener.onSongSelected(selected);
                        }
                    }
                }
            });
        }

        private String getPreviousSongId(int currentPosition) {
            if (currentPosition == 0) {
                return null;
            } else {
                return songList.get(currentPosition - 1).getId();
            }
        }

        private String getNextSongId(int currentPosition) {
            if (currentPosition == songList.size() - 1) {
                return null;
            } else {
                return songList.get(currentPosition + 1).getId();
            }
        }

        private void updateRecentListeningSong(Song song) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String userId = mAuth.getCurrentUser().getUid();
            Map<String, Object> recentListeningSong = new HashMap<>();
            recentListeningSong.put("songName", song.getTitle());
            recentListeningSong.put("imageURL", song.getImageUrl());
            recentListeningSong.put("artistName", song.getArtist());
            recentListeningSong.put("songId", song.getId());
            db.collection("users").document(userId).update("recentListeningSong", recentListeningSong)
                    .addOnSuccessListener(aVoid -> Log.d("SongHomeAdapter", "Recent listening song updated successfully"))
                    .addOnFailureListener(e -> Log.e("SongHomeAdapter", "Failed to update recent listening song: " + e.getMessage()));
        }
    }


}

