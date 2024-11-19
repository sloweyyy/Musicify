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
import com.example.musicapp.fragment.AddToPlayListFragment;
import com.example.musicapp.model.Playlist;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AddToPlaylistAdapter extends RecyclerView.Adapter<AddToPlaylistAdapter.ViewHolder> {

    private final Context context;
    private final List<Playlist> playlistList;
    private final String userId;
    private final String songId;
    private final AddToPlayListFragment fragment;

    public AddToPlaylistAdapter(Context context, List<Playlist> playlistList, String userId, String songId, AddToPlayListFragment fragment) {
        this.context = context;
        this.playlistList = playlistList;
        this.userId = userId;
        this.songId = songId;
        this.fragment = fragment;
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
            if (!playlist.getSongs().contains(songId)) {
                addSongToPlaylist(songId, playlistId, position);
            } else {
                Toast.makeText(context, "Song already in playlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    private void addSongToPlaylist(String songId, String playlistId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("playlists").document(playlistId)
                .update("songs", FieldValue.arrayUnion(songId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show();

                    Playlist updatedPlaylist = playlistList.get(position);
                    updatedPlaylist.getSongs().add(songId);
                    notifyItemChanged(position);

                    // Dismiss the dialog
                    fragment.dismissDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error adding to playlist", Toast.LENGTH_SHORT).show();
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView playlistImage;
        private final TextView playlistName;
        private final TextView playlistCount;

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

            int songCount = playlist.getSongs().size();
            playlistCount.setText(songCount + " songs");
        }
    }
}
