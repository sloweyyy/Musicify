package com.example.musicapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.manager.MediaPlayerManager;
import com.example.musicapp.manager.OnSongSelectedListener;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.adapter.SongAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import com.example.musicapp.model.Song;

public class PlaySongFragment extends Fragment implements FetchAccessToken.AccessTokenCallback , OnSongSelectedListener {

    private View view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private int currentPosition;
    private List<Song> songList;
    private PlaySongFragment playSongFragment;
    private MediaPlayerManager mediaPlayerManager;
    private String songnameValue, artistnameValue, avataValue, played_value, total_value, urlAudioValue;
    private TextView songname, artistname, duration_played, duration_total, lyric;
    private ImageView cover_art;
    private ImageButton repeateBtn, previousBtn, pauseBtn, nextBtn, shuffleBtn, show_lyricBtn;

    private LinearLayout backButtonLayout, lyricLayout;

    private Button iconBackPlaying;

    private SeekBar seekBar;
    private boolean isPlaying = false;
    private int position = -1;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;

    private ImageView heartBtn;
    private String songId, previousSongId, nextSongId;

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        getTrack(accessToken);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.play_song, container, false);
        // hide bottom navigation bar
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        mediaPlayerManager = MediaPlayerManager.getInstance();
        initializeViews();
        if (getArguments() != null) {
            songId = getArguments().getString("songId");
            previousSongId = getArguments().getString("previousSongId");
            nextSongId = getArguments().getString("nextSongId");
        }
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().seekTo(0);
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        iconBackPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.setCurrentPosition(0);
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        lyricLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
            }
        });
        show_lyricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
            }
        });

        lyric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
            }
        });

        repeateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.setCurrentPosition(0);
                mediaPlayerManager.getMediaPlayer().seekTo(0);
            }
        });

        previousBtn.setOnClickListener(v -> {
            mediaPlayerManager.getMediaPlayer().pause();
            mediaPlayerManager.setCurrentPosition(0);
            PlayPreviousSong();
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().pause();
                mediaPlayerManager.setCurrentPosition(0);
                PlayNextSong();
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().pause();
                mediaPlayerManager.setCurrentPosition(0);
                PlayRandomSong();
            }
        });


        return view;
    }

    public void setCurrentSongList(List<Song> songList, String currentSongId) {
        this.songList = songList;
        this.songId = currentSongId;
    }
    private int getCurrentSongIndex(String songId) {
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getId().equals(songId)) {
                return i;
            }
        }
        return -1;
    }
    private void PlayPreviousSong(){
        int currentIndex = getCurrentSongIndex(songId);
        String previousSongId = "";
        if (currentIndex > 0) {
            previousSongId = songList.get(currentIndex - 1).getId();
        }
        else {
            previousSongId = songList.get(songList.size()-1).getId();
        }
        Song previousSong = getSongById(previousSongId);
        if (playSongFragment == null) {
            playSongFragment = new PlaySongFragment();
            playSongFragment.setCurrentSongList(songList, previousSongId);
            Bundle args = new Bundle();
            args.putString("songId", previousSongId);
            playSongFragment.setArguments(args);
        } else {
            playSongFragment.setCurrentSongList(songList, previousSongId);
        }

        ((AppCompatActivity) requireContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, playSongFragment)
                .addToBackStack(null)
                .commit();
    }
    private void PlayNextSong(){
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        int currentIndex = getCurrentSongIndex(songId);
        String nextSongId = "";
        if (currentIndex < songList.size()-1) {
            nextSongId = songList.get(currentIndex + 1).getId();
        }
        else {
            nextSongId = songList.get(0).getId();
        }
        if (playSongFragment == null) {
            playSongFragment = new PlaySongFragment();
            playSongFragment.setCurrentSongList(songList, nextSongId);
            Bundle args = new Bundle();
            args.putString("songId", nextSongId);
            playSongFragment.setArguments(args);
        } else {
            playSongFragment.setCurrentSongList(songList, nextSongId);
        }

        ((AppCompatActivity) requireContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, playSongFragment)
                .addToBackStack(null)
                .commit();
    }
    private void PlayRandomSong(){
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        int randomIndex = (int)(Math.random() * songList.size());
        String nextSongId = songList.get(randomIndex).getId();
        if (playSongFragment == null) {
            playSongFragment = new PlaySongFragment();
            playSongFragment.setCurrentSongList(songList, nextSongId);
            Bundle args = new Bundle();
            args.putString("songId", nextSongId);
            playSongFragment.setArguments(args);
        } else {
            playSongFragment.setCurrentSongList(songList, nextSongId);
        }

        ((AppCompatActivity) requireContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, playSongFragment)
                .addToBackStack(null)
                .commit();
    }
    private Song getSongById(String songId) {
        for (Song song : songList) {
            if (song.getId().equals(songId)) {
                return song;
            }
        }
        throw new IllegalArgumentException("Song with ID " + songId + " not found.");
    }
    private void ShowLyric() {
        LyricFragment lyricFragment = new LyricFragment();
        lyricFragment.setSongId(songId);
        lyricFragment.setSongName(songnameValue);
        lyricFragment.setArtistName(artistnameValue);
        lyricFragment.setAvata(avataValue);
        lyricFragment.setPlayedDuration(played_value);
        lyricFragment.setTotalDuration(total_value);

        Bundle args = new Bundle();
        args.putString("songId", songId);
        args.putString("songName", songnameValue);
        args.putString("artistName", artistnameValue);
        args.putString("avata", avataValue);

        args.putString("playedDuration", played_value);
        args.putString("totalDuration", total_value);

        lyricFragment.setArguments(args);
        ((AppCompatActivity)requireContext()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, lyricFragment, "LyricFragment")
                .addToBackStack("LyricFragment")
                .commit();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((BottomAppBarListener) requireActivity()).showBottomAppBar();
    }
    public void setSongId(String songId) {
        this.songId = songId;
    }
    private void initializeViews() {
        songname = view.findViewById(R.id.songNamePlay);
        artistname = view.findViewById(R.id.artistNamePlay);
        duration_played = view.findViewById(R.id.played);
        duration_total = view.findViewById(R.id.total);
        repeateBtn = view.findViewById(R.id.repeateBtn);
        previousBtn = view.findViewById(R.id.previousBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        nextBtn = view.findViewById(R.id.nextBtn);
        shuffleBtn = view.findViewById(R.id.shuffleBtn);
        cover_art = view.findViewById(R.id.imageCon);
        seekBar = view.findViewById(R.id.seekbar);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        iconBackPlaying = view.findViewById(R.id.iconBackPlaying);
        heartBtn = view.findViewById(R.id.heartBtn);
        show_lyricBtn = view.findViewById(R.id.show_lyricBtn);
        lyricLayout = view.findViewById(R.id.lyricLayout);
        lyric = view.findViewById(R.id.lyric);

        checkIsLiked(songId, new OnIsLikedCallback() {
            @Override
            public void onResult(boolean isLiked) {
                if (isLiked) {
                    heartBtn.setImageResource(R.drawable.favourite_filled);
                } else {
                    heartBtn.setImageResource(R.drawable.favourite_outline);
                }
            }
        });

        heartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIsLiked(songId, new OnIsLikedCallback() {
                    @Override
                    public void onResult(boolean isLiked) {
                        if (isLiked) {
                            removeSongFromLikedSongs(songId);
                            heartBtn.setImageResource(R.drawable.favourite_outline);
                        } else {
                            addSongToLikedSongs(songId);
                            heartBtn.setImageResource(R.drawable.favourite_filled);
                        }
                    }
                });
            }
        });

    }
    private void checkIsLiked(String id, OnIsLikedCallback callback) {
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> likedSongs = (List<String>) userDoc.get("likedsong");
                        callback.onResult(likedSongs.contains(id));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                    callback.onResult(false);
                });
    }
    private void removeSongFromLikedSongs(String songId) {
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedsong", FieldValue.arrayRemove(songId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Removed from liked songs successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SongAdapter", "Failed to remove song from liked songs: " + e.getMessage());
                                });
                    } else {
                        Log.e("SongAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
    public void addSongToLikedSongs(String songId) {
        String userId = "4k4kPnoXFCTgzBAvaDNw25XVFpy1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        userDoc.getReference().update("likedsong", FieldValue.arrayUnion(songId))
                                .addOnSuccessListener(aVoid -> {

                                    Toast.makeText(requireContext(), "Add to liked songs successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error
                                    Log.e("SongAdapter", "Failed to add song to liked songs: " + e.getMessage());
                                });
                    } else {
                        Log.e("SongAdapter", "No user document found with userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
                });
    }
    private void getTrack(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApi apiService = retrofit.create(SpotifyApi.class);
        String authorization = "Bearer " + accessToken;
        Call<TrackModel> call = apiService.getTrack(authorization, songId);
        call.enqueue(new Callback<TrackModel>() {
            @Override
            public void onResponse(@NonNull Call<TrackModel> call, @NonNull Response<TrackModel> response) {
                if (response.isSuccessful()) {
                    TrackModel track = response.body();
                    if (track != null) {
                        setupTrack(track);
                    }
                } else {
                    showError(response);
                }
            }

            @Override
            public void onFailure(Call<TrackModel> call, Throwable throwable) {
                Log.e("Error fetching track", throwable.getMessage());
            }
        });
    }
    public void setupTrack(TrackModel track) {
        String songName = track.getName();
        String artistName = track.artists.get(0).getName();
        String imageUrl = track.album.images.get(0).getUrl();
        String playUrl = track.getPreview_url();

        songname.setText(songName);
        artistname.setText(artistName);
        Glide.with(getActivity()).load(imageUrl).into(cover_art);

        songnameValue = songName;
        artistnameValue = artistName;
        avataValue = imageUrl;
        urlAudioValue = playUrl;

        setupMediaPlayer(playUrl);
        setupSeekBar();
        setupPauseButton();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayerManager.getMediaPlayer() != null) {
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    public void setupMediaPlayer(String playUrl) {
        if (playUrl != null && !playUrl.isEmpty()) {
            mediaPlayerManager.setMediaSource(playUrl);
            mediaPlayerManager.setIsPlaying(true);
            if (mediaPlayerManager.getIsPlaying()) {
                mediaPlayerManager.getMediaPlayer().seekTo(mediaPlayerManager.getCurrentPosition());
                mediaPlayerManager.getMediaPlayer().start();
            }
            seekBar.setMax((int) (mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
            duration_total.setText(formattedTime(mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
            updateSeekBarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayerManager.getMediaPlayer() != null && mediaPlayerManager.getIsPlaying()) { // Check if mediaPlayer is playing
                        int CurrentPosition = (mediaPlayerManager.getMediaPlayer().getCurrentPosition() / 1000);
                        seekBar.setProgress(CurrentPosition);
                        duration_played.setText(formattedTime(CurrentPosition));
                        handler.postDelayed(this, 500); // Update every 500 milliseconds
                    }
                }
            };
            handler.postDelayed(updateSeekBarRunnable, 0);
        }
    }
    public void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                        mediaPlayerManager.getMediaPlayer().seekTo(progress * 1000);
                    }
                }
                duration_played.setText(formattedTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }
    public void setupPauseButton() {
        pauseBtn.setOnClickListener(v -> {
            if (mediaPlayerManager.getIsPlaying() == true) {
                if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().pause();
                }
                mediaPlayerManager.setIsPlaying(true);
                pauseBtn.setBackgroundResource(R.drawable.play);
            } else {
                if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().start();
                }
                pauseBtn.setBackgroundResource(R.drawable.pause);
                mediaPlayerManager.setIsPlaying(true);
            }
        });
    }
    public void showError(Response<TrackModel> response) {
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
    @SuppressLint("DefaultLocale")
    private String formattedTime(int currentPosition) {
        int minutes = currentPosition / 60;
        int seconds = currentPosition % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( mediaPlayerManager.getMediaPlayer() != null) {
//                 mediaPlayerManager.getMediaPlayer().release();
//                 mediaPlayerManager.setMediaSource(null);
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    @Override
    public void onSongSelected(String songId, String previousSongId, String nextSongId) {
        this.songId = songId;
        this.previousSongId = previousSongId;
        this.nextSongId = nextSongId;
        playPreviousSong(previousSongId);
    }
    private void playPreviousSong(String previousSongId) {
        setSongId(previousSongId);
        if (playSongFragment == null) {
            playSongFragment = new PlaySongFragment();
            playSongFragment.setSongId(previousSongId);
            Bundle args = new Bundle();
            args.putString("songId", previousSongId);
            playSongFragment.setArguments(args);
        } else {
            playSongFragment.setSongId(previousSongId);
        }

        ((AppCompatActivity) requireContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, playSongFragment)
                .addToBackStack(null)
                .commit();
    }
    public void onPreviousClicked(View view) {
        if (this instanceof OnSongSelectedListener) {
            ((OnSongSelectedListener) this).onSongSelected(songId, previousSongId, nextSongId);
        }
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    public interface SpotifyApi {
        @GET("v1/tracks/{songId}")
        Call<TrackModel> getTrack(@Header("Authorization") String authorization, @Path("songId") String songId);
    }

    public static class TrackModel {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("preview_url")
        private String preview_url;

        @SerializedName("artists")
        private List<ArtistModel> artists;

        @SerializedName("album")
        private AlbumModel album;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPreview_url() {
            return preview_url;
        }

        public static class ArtistModel {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }

        public static class AlbumModel {
            @SerializedName("images")
            private List<ImageModel> images;

            public static class ImageModel {
                @SerializedName("url")
                private String url;

                public String getUrl() {
                    return url;
                }
            }
        }
    }
    public interface OnPreviousSongClickListener {
        void onPreviousSongClick(String previousSongId);
    }

}