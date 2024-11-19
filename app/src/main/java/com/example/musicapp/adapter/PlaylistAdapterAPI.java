package com.example.musicapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.PlaylistAPI;
import java.util.List;

public class PlaylistAdapterAPI extends RecyclerView.Adapter<PlaylistAdapterAPI.myViewHolder>{
    private final List<PlaylistAPI> Playlists;

    public PlaylistAdapterAPI(List<PlaylistAPI> Playlists) {
        this.Playlists = Playlists;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistAdapterAPI.myViewHolder(view, new PlaylistAdapterAPI.OnItemClickListener() {

            @Override
            public void onItemClick(PlaylistAPI playlist) {

            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        PlaylistAPI playlist = Playlists.get(position);
        holder.textViewName.setText(playlist.getName());
        holder.textViewTotal.setText(playlist.tracks.getTotal());
        Glide.with(holder.itemView.getContext())
                .load(playlist.images.get(0).getUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Playlists.size();
    }

    public interface OnItemClickListener {
        void onItemClick(PlaylistAPI playlist);
    }


    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView textViewName;
        ImageView imageView;
        ImageView anhOKhoa;
        TextView textViewTotal;
        private PlaylistAdapterAPI.OnItemClickListener listener;
        public myViewHolder(@NonNull View itemView, PlaylistAdapterAPI.OnItemClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.playlistImage);
            textViewName = itemView.findViewById(R.id.playlistName);
            textViewTotal = itemView.findViewById(R.id.playlistCount);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                PlaylistAPI selected = Playlists.get(position);
                PlaylistDetailAPI fragment = new PlaylistDetailAPI();
                fragment.setPlaylistId(selected.getId());
                Bundle args = new Bundle();
                args.putString("playlistId", selected.getId());
                fragment.setArguments(args);
                // Add the Fragment to the Activity
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        public void setOnItemClickListener(PlaylistAdapterAPI.OnItemClickListener listenerInput) {
            listener = listenerInput;
        }

    }

}
