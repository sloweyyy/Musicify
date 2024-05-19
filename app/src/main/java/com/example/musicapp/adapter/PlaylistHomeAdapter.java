package com.example.musicapp.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.AlbumSimplified;
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.PlaylistSimplified;

import java.util.List;

public class PlaylistHomeAdapter extends RecyclerView.Adapter<PlaylistHomeAdapter.ViewHolder> {
    private Context context;
    List<PlaylistAPI> playlistList;

    public PlaylistHomeAdapter(Context context, List<PlaylistAPI> playlistList) {
        this.context = context;
       this.playlistList = playlistList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistHomeAdapter.ViewHolder(view);

    }
 @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
     PlaylistAPI playlist = playlistList.get(position);
     if(playlist != null) {
         if(playlist.getName() != null && holder.playlistName != null) {
             holder.playlistName.setText(playlist.getName());
         }
         if(playlist.tracks != null && holder.totalTracks != null) {
             holder.totalTracks.setText(String.valueOf(playlist.tracks.getTotal()));
         } else if (holder.totalTracks != null){
             holder.totalTracks.setText("N/A");
         }
     }
    }
    @Override
    public int getItemCount() {
        return playlistList.size();
    }
    public interface OnItemClickListener {
        void onItemClick(PlaylistAPI playlist);
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView playlistName;
        TextView totalTracks;
        ImageButton playButton, likedButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlistName);
            totalTracks = itemView.findViewById(R.id.totalTracks);
            likedButton = itemView.findViewById(R.id.likedButton);
            playButton=itemView.findViewById(R.id.playButton);
        }
        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                PlaylistAPI selected = playlistList.get(position);
                PlaylistDetailAPI fragment = new PlaylistDetailAPI();
                fragment.setPlaylistId(selected.getId());
                Bundle args = new Bundle();
                args.putString("playlistId", selected.getId());
                fragment.setArguments(args);
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }

        }

    }
}
