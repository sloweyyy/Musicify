package com.example.musicapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.model.Playlist;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private Context context;
    private List<Playlist> playlistList;
    private String userId; // User ID associated with the playlists

    public PlaylistAdapter(Context context, List<Playlist> playlistList, String userId) {
        this.context = context;
        this.playlistList = playlistList;
        this.userId = userId;
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
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView playlistImage;
        private TextView playlistName;
        private TextView playlistCount;
        private ImageView privacyIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistImage = itemView.findViewById(R.id.playlistImage);
            playlistName = itemView.findViewById(R.id.playlistName);
            playlistCount = itemView.findViewById(R.id.playlistCount);
            privacyIcon = itemView.findViewById(R.id.privacyIcon);
        }

        public void bind(Playlist playlist) {
            playlistImage.setImageResource(playlist.getImageResource());
            playlistName.setText(playlist.getName());
            playlistCount.setText(playlist.getSongCount() + " songs");
            privacyIcon.setImageResource(playlist.getPrivacyIcon());
        }
    }

    // Method to update the playlist list
    public void updatePlaylistList(List<Playlist> playlists) {
        playlistList.clear();
        playlistList.addAll(playlists);
        notifyDataSetChanged();
    }

    // Method to fetch playlists from Firestore based on the user's ID
    public void fetchPlaylists() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Playlist> playlists = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Playlist playlist = document.toObject(Playlist.class);
                        playlists.add(playlist);
                    }
                    updatePlaylistList(playlists);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}
