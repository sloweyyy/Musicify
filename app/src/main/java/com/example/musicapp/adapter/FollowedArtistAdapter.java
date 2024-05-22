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
import com.example.musicapp.fragment.ArtistDetailFragment;
import com.example.musicapp.model.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FollowedArtistAdapter extends RecyclerView.Adapter<FollowedArtistAdapter.ViewHolder> {
    private Context context;
    private List<Artist> followedArtists = new ArrayList<>();
    private boolean isAscending = false;

    public FollowedArtistAdapter(Context context, List<Artist> followedArtists) {
        this.context = context;
        this.followedArtists = followedArtists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = followedArtists.get(position);
        String artistName = artist.getName();
        String imageUrl = artist.getImages().isEmpty() ? null : artist.getImages().get(0).getUrl();

        holder.artistName.setText(artistName);
        if (imageUrl != null) {
            Glide.with(context).load(imageUrl).into(holder.artistImage);
        } else {
            holder.artistImage.setImageResource(R.drawable.artist_image_demo); // A default image in case there's no image URL
        }
    }

    @Override
    public int getItemCount() {
        if (followedArtists!=null)
        return followedArtists.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView artistImage;
        private TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = itemView.findViewById(R.id.artisthumbnail);
            artistName = itemView.findViewById(R.id.artistTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Artist selected = followedArtists.get(position);
                ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
                artistDetailFragment.setArtistId(selected.getId());
                Bundle args = new Bundle();
                args.putString("artistId", selected.getId());
                artistDetailFragment.setArguments(args);
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, artistDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    public void sortArtistByName() {
        Collections.sort(followedArtists, new Comparator<Artist>() {
            @Override
            public int compare(Artist artist1, Artist artist2) {
                if (isAscending) {
                    return artist2.getName().compareToIgnoreCase(artist1.getName());
                } else {
                    return artist1.getName().compareToIgnoreCase(artist2.getName());
                }
            }
        });
        isAscending = !isAscending;
        notifyDataSetChanged();
    }
}
