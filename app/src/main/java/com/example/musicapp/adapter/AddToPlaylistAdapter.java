package com.example.musicapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.model.Playlist;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AddToPlaylistAdapter extends RecyclerView.Adapter<AddToPlaylistAdapter.ViewHolder> {

    private Context context;
    private List<Playlist> playlistList;
    private String userId;
    private String songId;

    public AddToPlaylistAdapter(Context context, List<Playlist> playlistList, String userId, String songId) {
        this.context = context;
        this.playlistList = playlistList;
        this.userId = userId;
        this.songId = songId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.bind(playlist);

        holder.itemView.setOnClickListener(v -> {
            String playlistId = playlist.getId();
            addSongToPlaylist(songId, playlistId, position); // Pass position here
        });
    }


    @Override
    public int getItemCount() {
        return playlistList.size();
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

    private void addSongToPlaylist(String songId, String playlistId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("playlists").document(playlistId)
                .update("songs", FieldValue.arrayUnion(songId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show();

                    // Update the song count locally and in the UI
                    Playlist updatedPlaylist = playlistList.get(position);
                    updatedPlaylist.getSongs().add(songId);
                    notifyItemChanged(position); // Tell the adapter to refresh the item
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error adding to playlist", Toast.LENGTH_SHORT).show();
                });
    }
}