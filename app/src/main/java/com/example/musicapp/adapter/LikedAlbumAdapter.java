package com.example.musicapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.AlbumDetailFragment;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.fragment.LikedAlbumDetailFragment;
import com.example.musicapp.model.PlaylistAPI;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.musicapp.adapter.FetchAccessToken;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class LikedAlbumAdapter extends RecyclerView.Adapter<LikedAlbumAdapter.ViewHolder>{
    private Context context;
    private List<AlbumSimplified> likedAlbumsList;

    public LikedAlbumAdapter(Context context, List<AlbumSimplified> likedAlbumsList) {
        this.context = context;
        this.likedAlbumsList = likedAlbumsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumSimplified album = likedAlbumsList.get(position);
        String songName = album.getName();
        String artistName = album.getArtists().get(0).getName();
        String imageUrl = album.getImages().get(0).getUrl();
        holder.albumName.setText(songName);
        holder.albumArtist.setText(artistName);
        Glide.with(context).load(imageUrl).into(holder.albumImage);

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Fragment likedAlbumDetailFragment = LikedAlbumDetailFragment.newInstance(album.getName(), album.getImages(), album.getArtists());
//                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
//                FragmentTransaction transaction = fragmentManager.beginTransaction();
//                transaction.replace(R.id.frame_layout, likedAlbumDetailFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
//            }

//        });
    }
    public interface OnItemClickListener {
        void onItemClick(AlbumSimplified albumSimplified);
    }

    @Override
    public int getItemCount() {
        return likedAlbumsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView albumImage;
        private TextView albumName;
        private TextView albumArtist;
        private LikedAlbumAdapter.OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            albumImage = itemView.findViewById(R.id.albumThumbnail);
            albumName = itemView.findViewById(R.id.albumTitle);
            albumArtist = itemView.findViewById(R.id.artistName);
            this.listener = listener;
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
                AlbumSimplified selected = likedAlbumsList.get(position);
                LikedAlbumDetailFragment likedAlbumDetailFragment= new LikedAlbumDetailFragment();
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

//    public void updateLikedAlbumList(List<Album> albums) {
//        likedAlbumsList.clear();
//        likedAlbumsList.addAll(albums);
//        notifyDataSetChanged();
//    }
//    // Method to fetch liked albums from Firestore based on the user's ID

//    public void fetchLikedAlbums() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
//            List<Album> albums = new ArrayList<>();
//            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                Album album = document.toObject(Album.class);
//                albums.add(album);
//            }
//            updateLikedAlbumList(albums);
//        }).addOnFailureListener(e -> {
//            // Handle error
//        });
//    }
    // Method to sort the album list by name

    public void sortAlbumByName() {
        likedAlbumsList.sort((album1, album2) -> {
            String name1 = album1.getName();
            String name2 = album2.getName();

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
        notifyDataSetChanged();
    }
    // Method to delete a liked album from Firestore

//    public void unlikeAlbum(Album album) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(album.getId()).delete().addOnSuccessListener(aVoid -> {
//            fetchLikedAlbums();
//        }).addOnFailureListener(e -> {
//            // Handle error
//        });
//    }

    public void setupAlbum(AlbumSimplified album, TextView albumName, TextView albumArtist, ImageView albumImage) {
        String songName = album.getName();
        String artistName = album.getArtists().get(0).getName();
        String imageUrl = album.getImages().get(0).getUrl();

        albumName.setText(songName);
        albumArtist.setText(artistName);
        Glide.with(context).load(imageUrl).into(albumImage);
    }
}