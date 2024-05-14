package com.example.musicapp.adapter;

import android.content.Context;
import android.provider.MediaStore;
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
import com.example.musicapp.fragment.AlbumDetailFragment;
import com.example.musicapp.model.Album;
import com.example.musicapp.model.Playlist;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {
    private Context context;
    private List<Album> albumsList;
    private String userId;
    public AlbumsAdapter(Context context, List<Album> albumsList, String userId) {
        this.context = context;
        this.albumsList = albumsList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albumsList.get(position);
        holder.bind(album);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment albumDetailFragment = AlbumDetailFragment.newInstance(album.getName(), album.getImageResource(), album.getArtistName());
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, albumDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumImage;
        private TextView albumName;
        private TextView albumArtist;
        private ImageView privacyIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            albumArtist = itemView.findViewById(R.id.albumArtist);
            privacyIcon = itemView.findViewById(R.id.privacyIcon);

        }

        public void bind(Album album) {
            albumImage.setImageResource(album.getImageResource());
            albumName.setText(album.getName());
            albumArtist.setText(album.getArtistName());
        }
    }

    // Method to update the playlist list
    public void updateAlbumList(List<Album> albums) {
        albumsList.clear();
        albumsList.addAll(albums);
        notifyDataSetChanged();
    }

    // Method to fetch liked albums from Firestore based on the user's ID
    public void fetchAlbums() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("albums").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Album> albums = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Album album = document.toObject(Album.class);
                albums.add(album);
            }
            updateAlbumList(albums);
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

    // sort the playlist list by name
    public void sortAlbumByName() {
        albumsList.sort((album1, album2) -> {
            String name1 = album1.getName();
            String name2 = album2.getName();

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


    // Method to delete a liked album from Firestore
    public void unlikeAlbum(Album album) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("albums").document(album.getId()).delete().addOnSuccessListener(aVoid -> {
            fetchAlbums();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }

}
