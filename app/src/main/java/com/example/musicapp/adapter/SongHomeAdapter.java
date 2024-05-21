package com.example.musicapp.adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.fragment.PlaylistDetailAPI;
import com.example.musicapp.model.PlaylistAPI;
import com.example.musicapp.model.SimplifiedTrack;
import com.example.musicapp.model.Song;

import java.util.List;

public class SongHomeAdapter extends RecyclerView.Adapter<SongHomeAdapter.ViewHolder> {
    private Context context;
    private List<Song> songList;
    int position;
    public interface OnSongSelectedListener {
        void onSongSelected(Song song);
    }
    private OnSongSelectedListener listener;
    public SongHomeAdapter(Context context, List<Song> songList,OnSongSelectedListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false);
        return new SongHomeAdapter.ViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.trackName.setText(song.getTitle());
        holder.artistName.setText(song.getArtist());

        Glide.with(context)
                .load(song.getImageUrl())
                .error(R.drawable.playlist_image)
                .into(holder.artistPic);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder   {

        TextView trackName;
        ImageView artistPic;
        ImageButton playButton;
        TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            artistName = itemView.findViewById(R.id.artistName);
            artistPic = itemView.findViewById(R.id.artistPic);
            playButton=itemView.findViewById(R.id.playButton);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onSongSelected(songList.get(position));
                    }
                    position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Song selected = songList.get(position);
                        PlaySongFragment fragment = new PlaySongFragment();
                        fragment.setSongId(selected.getId());
                        String previousSongId = getPreviousSongId(position);
                        String nextSongId = getNextSongId(position);
                        Bundle args = new Bundle();
                        args.putString("songId", selected.getId());
                        args.putString("previousSongId", previousSongId);
                        args.putString("nextSongId", nextSongId);
                        fragment.setArguments(args);
                        ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });
        }
        private String getPreviousSongId(int currentPosition) {
            if (currentPosition == 0) {
                return null;
            } else {
                return songList.get(currentPosition - 1).getId();
            }
        }

        private String getNextSongId(int currentPosition) {
            if (currentPosition == songList.size() - 1) {
                return null;
            } else {
                return songList.get(currentPosition + 1).getId();
            }
        }


    }
}
