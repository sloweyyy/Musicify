package com.example.musicapp.adapter;
//ArtistHomeAdapter

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.model.Artist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtitstHomeAdapter extends RecyclerView.Adapter<ArtitstHomeAdapter.ViewHolder> {

    private Context context;
    private List<Artist> artistList;
    String userId;
    FirebaseUser user;
    FirebaseAuth mAuth;

    public ArtitstHomeAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
        Log.d("BLEBLEBKE", "Token: ");
    }

    private void checkIsLiked(String artistId, OnIsLikedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedArtists = (List<String>) documentSnapshot.get("likedArtist");
                        if (likedArtists != null && likedArtists.contains(artistId)) {
                            callback.onResult(true);
                        } else {
                            callback.onResult(false);
                        }
                    } else {
                        Log.e("ArtitstHomeAdapter", "User document does not exist");
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ArtitstHomeAdapter", "Failed to retrieve user document: " + e.getMessage());
                    callback.onResult(false);
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ViewHolder", "onCreateViewHolder was called");
        View view = LayoutInflater.from(context).inflate(R.layout.artist_item, parent, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.totalFollowers.setText(String.valueOf(artist.getFollowers().getTotal()));
        holder.artistName.setText(artist.getName());

        if (!artist.getImages().isEmpty()) {
            Glide.with(context)
                    .load(artist.getImages().get(0).getUrl())
                    .error(R.drawable.playlist_image)
                    .into(holder.cicleArtistImg);
        } else {
            holder.cicleArtistImg.setImageResource(R.drawable.playlist_image);
        }
        checkIsLiked(artist.getId(), isLiked -> {
            if (isLiked) {
                holder.heartBtn.setImageResource(R.drawable.favourite_filled);
            } else {
                holder.heartBtn.setImageResource(R.drawable.favourite_outline);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    //thieu implement onclick
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView artistName, totalFollowers;
        ImageView heartBtn;
        CircleImageView cicleArtistImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d("BLULUBLUBLU", "Token: ");
            heartBtn = itemView.findViewById(R.id.heartBtn);
            artistName = itemView.findViewById(R.id.artistName);

            totalFollowers = itemView.findViewById(R.id.totalFollowers);
            cicleArtistImg = itemView.findViewById(R.id.cicleArtistImg);

            heartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Artist artist = artistList.get(position);

                        checkIsLiked(artist.getId(), isLiked -> {
                            if (isLiked) {
                                heartBtn.setImageResource(R.drawable.favourite_outline);
                                removeArtistFromLikedSongs(artist.getId());
                            } else {
                                heartBtn.setImageResource(R.drawable.favourite_filled);
                                addArtistToLikedSongs(artist.getId());
                            }
                        });
                    }
                }
            });

        }

        public void addArtistToLikedSongs(String artistId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .update("likedArtist", FieldValue.arrayUnion(artistId))
                    .addOnSuccessListener(aVoid -> Log.d("ArtitstHomeAdapter", "Artist added to liked artists"))
                    .addOnFailureListener(e -> Log.e("ArtitstHomeAdapter", "Error adding artist to liked artists", e));
        }

        public void removeArtistFromLikedSongs(String artistId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .update("likedArtist", FieldValue.arrayRemove(artistId))
                    .addOnSuccessListener(aVoid -> Log.d("ArtitstHomeAdapter", "Artist removed from liked artists"))
                    .addOnFailureListener(e -> Log.e("ArtitstHomeAdapter", "Error removing artist from liked artists", e));
        }
    }

}
