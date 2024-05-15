package com.example.musicapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
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
import com.example.musicapp.fragment.LikedAlbumDetailFragment;
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.model.Album;
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

public class LikedAlbumAdapter extends RecyclerView.Adapter<LikedAlbumAdapter.ViewHolder> implements FetchAccessToken.AccessTokenCallback {
    private Context context;
    private List<Album> likedAlbumsList;
    private String userId;

    private FetchAccessToken fetchAccessToken;

    public LikedAlbumAdapter(Context context, List<Album> likedAlbumsList, String userId) {
        this.context = context;
        this.likedAlbumsList = likedAlbumsList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = likedAlbumsList.get(position);
        holder.bind(album);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment likedAlbumDetailFragment = LikedAlbumDetailFragment.newInstance(album.getName(), album.getImageResource(), album.getArtistName());
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, likedAlbumDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return likedAlbumsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumImage;
        private TextView albumName;
        private TextView albumArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            albumArtist = itemView.findViewById(R.id.albumArtist);
        }

        public void bind(Album album) {
            albumImage.setImageResource(album.getImageResource());
            albumName.setText(album.getName());
            albumArtist.setText(album.getArtistName());
        }

    }

    // Method to update the likedAlbum list

    public void updateLikedAlbumList(List<Album> albums) {
        likedAlbumsList.clear();
        likedAlbumsList.addAll(albums);
        notifyDataSetChanged();
    }
    // Method to fetch liked albums from Firestore based on the user's ID

    public void fetchLikedAlbums() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Album> albums = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Album album = document.toObject(Album.class);
                albums.add(album);
            }
            updateLikedAlbumList(albums);
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }
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

    public void unlikeAlbum(Album album) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(album.getId()).delete().addOnSuccessListener(aVoid -> {
            fetchLikedAlbums();
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }
    @Override
    public void onTokenReceived(String accessToken) {
        getAlbum(accessToken);
    }

    private void getAlbum (String accessToken){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();

        SpotifyApi apiService = retrofit.create(SpotifyApi.class);
        String albumId = "4aawyAB9vmqN3uQ7FjRGTy";
        String authorization = "Bearer " + accessToken;

        Call<AlbumModel> call = apiService.getAlbum(authorization, albumId);
        call.enqueue(new Callback<AlbumModel>() {
            @Override
            public void onResponse(@NonNull Call<AlbumModel> call, @NonNull Response<AlbumModel> response) {
                if (response.isSuccessful()) {
                    AlbumModel album = response.body();
                    if (album != null) {
                        setupAlbum(album);
                    }
                } else {
                    showError(response);
                }
            }

            @Override
            public void onFailure(Call<AlbumModel> call, Throwable throwable) {
                Log.e("Error fetching track", throwable.getMessage());
            }
        });
    }
    public void setupAlbum(AlbumModel album) {
        String songName = album.getName();
        String artistName = album.artists.get(0).getName();
        String imageUrl = album.images.get(0).getUrl();

        albumName.setText(songName);
        albumArtist.setText(artistName);
        Glide.with(getActivity()).load(imageUrl).into(albumImage);
    }
    public void showError(Response<AlbumModel> response) {
        try {
            assert response.errorBody() != null;
            String errorReason = response.errorBody().string();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Error");
            builder.setMessage(errorReason);
            builder.setPositiveButton("OK", null);
            builder.create().show();
        } catch (IOException e) {
            Log.e("Error handling response", e.getMessage());
        }
    }

    public interface SpotifyApi {
        @GET("v1/albums/{id}")
        Call<AlbumModel> getAlbum(@Header("Authorization") String authorization, @Path("albumId") String albumId);
    }
    public static class AlbumModel {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("artists")
        private List<ArtistModel> artists;

        @SerializedName("images")
        private List<ImageModel> images;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }


        public static class ArtistModel {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }

        public static class ImageModel {
            @SerializedName("url")
            private String url;

            public String getUrl() {
                return url;
            }
        }
    }
}