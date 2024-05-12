package com.example.musicapp.adapter;

import android.content.Context;
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

import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaylistDetailFragment;
import com.example.musicapp.model.Playlist;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private Context context;
    private List<Playlist> playlistList;
    private String userId;

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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment playlistDetailFragment = PlaylistDetailFragment.newInstance(playlist.getName(), playlist.getImageResource(), playlist.getDescription());
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, playlistDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
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
        db.collection("playlists").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Playlist> playlists = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Playlist playlist = document.toObject(Playlist.class);
                playlists.add(playlist);
            }
            updatePlaylistList(playlists);
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // sort the playlist list by name
    public void sortPlaylistByName() {
        playlistList.sort((playlist1, playlist2) -> {
            String name1 = playlist1.getName();
            String name2 = playlist2.getName();

            // Kiểm tra null trước khi so sánh
            if (name1 == null && name2 == null) {
                return 0; // Cả hai đều là null, không có sự khác biệt
            } else if (name1 == null) {
                return -1; // playlist1 null, sắp xếp trước playlist2
            } else if (name2 == null) {
                return 1; // playlist2 null, sắp xếp trước playlist1
            } else {
                // Cả hai không phải là null, sắp xếp bình thường
                return name1.compareTo(name2);
            }
        });
        notifyDataSetChanged();
    }


    // sort the playlist by privacy
    public void sortPlaylistByPrivacy() {
        playlistList.sort((playlist1, playlist2) -> {
            // Kiểm tra xem cả hai playlist đều không phải là null trước khi so sánh privacy
            if (playlist1 != null && playlist2 != null) {
                String privacy1 = playlist1.getPrivacy();
                String privacy2 = playlist2.getPrivacy();

                // Kiểm tra xem privacy1 và privacy2 có phải là null không trước khi so sánh
                if (privacy1 != null && privacy2 != null) {
                    return privacy1.compareTo(privacy2); // Trả về kết quả so sánh
                }
            }
            return 0; // Trả về 0 nếu bất kỳ đối tượng nào là null
        });
        notifyDataSetChanged();
    }


    // Method to delete a playlist from Firestore
    public void deletePlaylist(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).delete().addOnSuccessListener(aVoid -> {
            fetchPlaylists();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // Method to update the privacy of a playlist in Firestore
    public void updatePlaylistPrivacy(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).update("privacy", playlist.getPrivacy()).addOnSuccessListener(aVoid -> {
            fetchPlaylists();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // Method to update the name of a playlist in Firestore
    public void updatePlaylistName(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).update("name", playlist.getName()).addOnSuccessListener(aVoid -> {
            fetchPlaylists();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // Method to update the song count of a playlist in Firestore
    public void updatePlaylistSongCount(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).update("songCount", playlist.getSongCount()).addOnSuccessListener(aVoid -> {
            fetchPlaylists();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // Method to update the image of a playlist in Firestore
    public void updatePlaylistImage(Playlist playlist) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId()).update("imageResource", playlist.getImageResource()).addOnSuccessListener(aVoid -> {
            fetchPlaylists();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }


}
