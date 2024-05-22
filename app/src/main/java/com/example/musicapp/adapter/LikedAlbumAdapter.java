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
import com.example.musicapp.fragment.AlbumDetailFragment;
import com.example.musicapp.model.AlbumSimplified;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class LikedAlbumAdapter extends RecyclerView.Adapter<LikedAlbumAdapter.ViewHolder>{
    private Context context;
    private Map<AlbumSimplified, LocalDateTime> likedAlbums ;

    public LikedAlbumAdapter(Context context, Map<AlbumSimplified, LocalDateTime> likedAlbums ) {
        this.context = context;
        this.likedAlbums  = likedAlbums ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumSimplified album = (AlbumSimplified) likedAlbums.keySet().toArray()[position];
        String songName = album.getName();
        String artistName = album.getArtists().get(0).getName();
        String imageUrl = album.getImages().get(0).getUrl();
        holder.albumName.setText(songName);
        holder.albumArtist.setText(artistName);
        Glide.with(context).load(imageUrl).into(holder.albumImage);

        checkIsLiked(album.getId(), new OnIsLikedCallback() {
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

    private void checkIsLiked(String id, OnIsLikedCallback onIsLikedCallback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> likedAlbums = (List<String>) userDoc.get("likedAlbums");
                        onIsLikedCallback.onResult(likedAlbums.contains(id));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LikedAlbumAdapter", "Failed to retrieve user document: " + e.getMessage());
                    onIsLikedCallback.onResult(false);
                });
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    public interface OnItemClickListener {
        void onItemClick(AlbumSimplified albumSimplified);
    }

    @Override
    public int getItemCount() {
        return likedAlbums .size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView albumImage;
        private TextView albumName;
        private TextView albumArtist;

        private ImageView heartBtn;
        private LikedAlbumAdapter.OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            albumImage = itemView.findViewById(R.id.albumThumbnail);
            albumName = itemView.findViewById(R.id.albumTitle);
            albumArtist = itemView.findViewById(R.id.artistName);
            heartBtn = itemView.findViewById(R.id.heartBtn);
            this.listener = listener;

            heartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        AlbumSimplified albumSimplified = (AlbumSimplified) likedAlbums.keySet().toArray()[position];
                        checkIsLiked(albumSimplified.getId(), new OnIsLikedCallback() {
                            @Override
                            public void onResult(boolean isLiked) {
                                if (isLiked) {
                                    unlikeAlbum(albumSimplified.getId());
                                    heartBtn.setImageResource(R.drawable.favourite_outline);
                                } else {
                                    addAlbumToLikedAlbums(albumSimplified.getId());
                                    heartBtn.setImageResource(R.drawable.favourite_filled);
                                }
                            }
                        });
                    }
                }
            });
        }

        public void bind(AlbumSimplified album) {
            String songName = album.getName();
            String artistName = album.getArtists().get(0).getName();
            String imageUrl = album.getImages().get(0).getUrl();

            albumName.setText(songName);
            albumArtist.setText(artistName);
            Glide.with(context).load(imageUrl).into(albumImage);
        }

        @Override
        public void onClick(View v) {
            Log.e("Clicked On item", "hehe");
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
//                AlbumSimplified selected = likedAlbums.get(position);
                AlbumSimplified selected = (AlbumSimplified) likedAlbums.keySet().toArray()[position];
                AlbumDetailFragment likedAlbumDetailFragment= new AlbumDetailFragment();
                likedAlbumDetailFragment.setAlbumId(selected.getId());
                Bundle args = new Bundle();
                args.putString("albumId", selected.getId());
                likedAlbumDetailFragment.setArguments(args);
                // Add the Fragment to the Activity
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, likedAlbumDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
        public void setOnItemClickListener(LikedAlbumAdapter.OnItemClickListener listenerInput) {
            listener = listenerInput;
        }
    }

    // Method to update the likedAlbum list
    public void updateLikedAlbumList(Map<AlbumSimplified, LocalDateTime> albums) {
        likedAlbums.clear();
        likedAlbums.putAll(albums);
        notifyDataSetChanged();
    }

    // Method to sort the album list by name
    public void sortAlbumByName() {
        List<Map.Entry<AlbumSimplified, LocalDateTime>> entryList = new ArrayList<>(likedAlbums.entrySet());

        entryList.sort((entry1, entry2) -> {
            String name1 = entry1.getKey().getName();
            String name2 = entry2.getKey().getName();

            // Check for null before comparing
            if (name1 == null && name2 == null) {
                return 0; // Both are null, no difference
            } else if (name1 == null) {
                return -1; // album1 is null, sort it before album2
            } else if (name2 == null) {
                return 1; // album2 is null, sort it before album1
            } else {
                // Both are not null, sort normally
                return name1.compareTo(name2);
            }
        });
        likedAlbums.clear();
        for (Map.Entry<AlbumSimplified, LocalDateTime> entry : entryList) {
            likedAlbums.put(entry.getKey(), entry.getValue());
        }

        notifyDataSetChanged();
    }

    public void sortAlbumByRecentlyAdded() {
        List<Map.Entry<AlbumSimplified, LocalDateTime>> sortedEntries = new ArrayList<>(likedAlbums.entrySet());
        Collections.sort(sortedEntries, new Comparator<Map.Entry<AlbumSimplified, LocalDateTime>>() {
            @Override
            public int compare(Map.Entry<AlbumSimplified, LocalDateTime> entry1, Map.Entry<AlbumSimplified, LocalDateTime> entry2) {
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        likedAlbums.clear();
        for (Map.Entry<AlbumSimplified, LocalDateTime> entry : sortedEntries) {
            likedAlbums.put(entry.getKey(), entry.getValue());
        }

        notifyDataSetChanged();
    }


    // Method to delete a liked album from Firestore
    public void unlikeAlbum(String albumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedAlbums", FieldValue.arrayRemove(albumId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Removed from liked albums successfully", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LikedAlbumAdapter", "Failed to remove album from liked albums: " + e.getMessage());
                                });
                    } else {
                        Log.e("LikedAlbumAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LikedAlbumAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
    public void addAlbumToLikedAlbums(String albumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedAlbums", FieldValue.arrayUnion(albumId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Add to liked albums successfully", Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error
                                    Log.e("LikedAlbumAdapter", "Failed to add album to liked albums: " + e.getMessage());
                                });
                    } else {
                        Log.e("LikedAlbumAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LikedAlbumAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
}