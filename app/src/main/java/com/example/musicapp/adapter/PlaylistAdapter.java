package com.example.musicapp.adapter;

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
import com.example.musicapp.fragment.PlaylistDetailFragment;
import com.example.musicapp.model.Playlist;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final Context context;
    private final List<Playlist> playlistList;
    private final String userId;

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
                Bundle bundle = new Bundle();
                bundle.putString("from", "favorite");
                Fragment playlistDetailFragment = PlaylistDetailFragment.newInstance(playlist.getName(), playlist.getImageURL(), playlist.getDescription(), playlist.getId()
                );
                Log.d("PlaylistAdapter", "Playlist ID: " + playlist.getId());
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

    // Method to update the playlist list
    public void updatePlaylistList(List<Playlist> playlists) {
        playlistList.clear();
        playlistList.addAll(playlists);
        notifyDataSetChanged();
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

            // Calculate song count here
            int songCount = playlist.getSongs().size();
            playlistCount.setText(songCount + " songs");
        }
    }


}
