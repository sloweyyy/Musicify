package com.example.musicapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.manager.MediaPlayerManager;
import com.example.musicapp.manager.OnSongSelectedListener;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public class PlaySongFragment extends BottomSheetDialogFragment implements FetchAccessToken.AccessTokenCallback, OnSongSelectedListener {

    private View view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private int currentPosition;
    private List<Song> songList;

    private String albumId;
    private MediaPlayerManager mediaPlayerManager;
    private String songnameValue, artistnameValue, avataValue, played_value, total_value, urlAudioValue, artistId;
    private TextView songname, artistname, duration_played, duration_total, lyric;
    private ImageView cover_art, threeDots, repeateBtn, shuffleBtn;
    private ImageButton previousBtn, pauseBtn, nextBtn, show_lyricBtn;

    private LinearLayout backButtonLayout, lyricLayout, threeDotsLayout, iconBackPlayingLayout;

    private Button iconBackPlaying;

    private SeekBar seekBar;
    private boolean isPlaying = false;
    private int position = -1;
    private FetchAccessToken fetchAccessToken;
    private String accessToken;

    private ImageView heartBtn;
    private String songId, previousSongId, nextSongId;

    private ImageView miniPlayerPlayPauseButton;

    private SongAdapter songAdapter; // Add SongAdapter here

    public interface OnPlayingStateChangeListener {
        void onPlayingStateChanged(boolean isPlaying);
    }

    public void setPlayingStateChangedListener(OnPlayingStateChangeListener listener) {
        this.playingStateChangeListener = listener;
    }

    private OnPlayingStateChangeListener playingStateChangeListener;

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        getTrack(accessToken);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.play_song, container, false);
        // hide bottom navigation bar
        fetchAccessToken = new FetchAccessToken();

        fetchAccessToken.getTokenFromSpotify(this);
        mediaPlayerManager = MediaPlayerManager.getInstance();
        initializeViews();
        if (getArguments() != null) {
            songId = getArguments().getString("songId");
            Log.d("PlaySongFragment", "onCreateView: " + songId);
            previousSongId = getArguments().getString("previousSongId");
            nextSongId = getArguments().getString("nextSongId");
        }

        // get recentlyplayed from firebase
        if (songList == null) {
            songList = new ArrayList<>();
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> recentlyPlayed = (List<String>) documentSnapshot.get("recentlyplayed");
                if (recentlyPlayed != null) {
                    for (String songId : recentlyPlayed) {
                        db.collection("songs").document(songId).get().addOnSuccessListener(documentSnapshot1 -> {
                            if (documentSnapshot1.exists()) {
                                Song song = documentSnapshot1.toObject(Song.class);
                                songList.add(song);
                            }
                        });
                    }
                }
            }
        });


        setCurrentSongList(songList, songId);
        if (getActivity() instanceof OnPlayingStateChangeListener) {
            playingStateChangeListener = (OnPlayingStateChangeListener) getActivity();
        }
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().seekTo(0);
                dismiss();
            }
        });

        iconBackPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.setCurrentPosition(0);
                dismiss();
            }
        });

        lyricLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
                view.setVisibility(View.GONE);
            }
        });
        show_lyricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
                view.setVisibility(View.GONE);
            }
        });

        lyric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLyric();
                view.setVisibility(View.GONE);
            }
        });

        repeateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mediaPlayerManager.setCurrentPosition(0);
//                mediaPlayerManager.getMediaPlayer().seekTo(0);
                mediaPlayerManager.setIsRepeat(!mediaPlayerManager.getIsRepeat());
                checkIsRepeat(new OnRepeatCallback() {
                    @Override
                    public void onResult(boolean isRepeat) {
                        if (isRepeat){
                            repeateBtn.setImageResource(R.drawable.repeate_green);
                        } else {
                            repeateBtn.setImageResource(R.drawable.repeate);
                        }
                    }
                });
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

        threeDotsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog(getContext());
                Log.d("PlaySongFragment", "onClick: Three dots clicked" + songId);
            }
        });
        iconBackPlayingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        miniPlayerPlayPauseButton = requireActivity().findViewById(R.id.mini_player_play_pause_button);
        MiniPlayerListener miniPlayerListener = (MiniPlayerListener) requireActivity();
        miniPlayerListener.updateMiniPlayer(songList, getCurrentSongIndex(songId));
        miniPlayerListener.showMiniPlayer();
        return view;
    }

    private void showMoreOptionsDialog(Context context) {
        // Create a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.more_dialog_song, null);

        // Set the dialog's content view
        builder.setView(dialogView);

        LinearLayout addToPlaylist = dialogView.findViewById(R.id.addToPlaylist);
        LinearLayout album = dialogView.findViewById(R.id.album);
        LinearLayout share = dialogView.findViewById(R.id.share);
        LinearLayout report = dialogView.findViewById(R.id.report);
        Button cancel = dialogView.findViewById(R.id.cancel);
        AlertDialog dialog = builder.create();
        dialog.show();

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialogReport = new Dialog(getActivity());
                dialogReport.setContentView(R.layout.custom_report_dialog_2);
                if (getActivity() != null) {
                    int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                    dialogReport.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                dialogReport.setCancelable(false);
                Button btnCancel = dialogReport.findViewById(R.id.btnCancel);
                Button btnReportSend = dialogReport.findViewById(R.id.btnReportSend);
                TextView inputReport = dialogReport.findViewById(R.id.inputReport);
                TextView reportSucess = dialogReport.findViewById(R.id.reportSucess);
                String reportContent = inputReport.getText().toString();

                //Blurry.with(getContext()).radius(10).sampling(2).onto((ViewGroup)view);
                dialogReport.show();
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogReport.dismiss();

                    }
                });
                btnReportSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (reportContent.isEmpty()) {
                            reportSucess.setText("Please give us feedback.");
                            reportSucess.setTextColor(Color.RED);
                            reportSucess.setVisibility(View.VISIBLE);
                            inputReport.requestFocus();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    reportSucess.setVisibility(View.GONE);
                                }
                            }, 6000);
                        } else {
                            Map<String, Object> updates = new HashMap<>();
                            String subject = "Thanks for sending us Feedback&Error report";
                            updates.put("reportContent", reportContent);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("reports_1")
                                    .add(updates)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("saveErrorReport", "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    //Blurry.delete((ViewGroup)view);
                                }
                            });
                        }
                        reportSucess.setVisibility(View.VISIBLE);
                        reportSucess.setText("Thanks for giving us report! We hope you decide again");
                        dialogReport.dismiss();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogReport.dismiss();
                            }
                        }, 3000);
                    }
                });
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.putExtra(Intent.EXTRA_TEXT, urlAudioValue);
                textIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(textIntent, "Send to");
                startActivity(shareIntent);
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoveToAlbumDetail(albumId);
                view.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        addToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddToPlayListFragment addToPlayListFragment = new AddToPlayListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("songId", songId);
                addToPlayListFragment.setArguments(bundle);
                addToPlayListFragment.show(getChildFragmentManager(), "AddToPlayListFragment");

                dialog.dismiss();
            }
        });
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

    public void PlayPreviousSong() {
        int currentIndex = getCurrentSongIndex(songId);
        String previousSongId = "";
        if (currentIndex > 0) {
            previousSongId = songList.get(currentIndex - 1).getId();
        } else {
            previousSongId = songList.get(songList.size() - 1).getId();
        }
        updateCurrentSong(previousSongId);
        MiniPlayerListener miniPlayerListener = (MiniPlayerListener) requireActivity();
        miniPlayerListener.updateMiniPlayer(songList, getCurrentSongIndex(songId));
    }

    public void PlayNextSong() {
        int currentIndex = getCurrentSongIndex(songId);
        String nextSongId = "";
        if (currentIndex < songList.size() - 1) {
            nextSongId = songList.get(currentIndex + 1).getId();
        } else {
            nextSongId = songList.get(0).getId();
        }
        updateCurrentSong(nextSongId);
        MiniPlayerListener miniPlayerListener = (MiniPlayerListener) requireActivity();
        miniPlayerListener.updateMiniPlayer(songList, getCurrentSongIndex(songId));
    }

    private void PlayRandomSong() {
        int randomIndex = (int) (Math.random() * songList.size());
        String nextSongId = songList.get(randomIndex).getId();
        updateCurrentSong(nextSongId);
    }

    private Song getSongById(String songId) {
        if (songList != null) {
            for (Song song : songList) {
                if (song.getId().equals(songId)) {
                    return song;
                }
            }
        }
        Log.e("PlaySongFragment", "Song with ID " + songId + " not found.");
        return null;
    }


    private void ShowLyric() {
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        LyricFragment lyricFragment = new LyricFragment();
        lyricFragment.setSongName(songnameValue);
        lyricFragment.setArtistName(artistnameValue);
        lyricFragment.setAvata(avataValue);
        lyricFragment.setPlayedDuration(played_value);
        lyricFragment.setTotalDuration(total_value);
        lyricFragment.setCurrentSongList(songList, songId);
        Bundle args = new Bundle();
        args.putString("songId", songId);
        args.putString("songName", songnameValue);
        args.putString("artistName", artistnameValue);
        args.putString("avata", avataValue);
        args.putString("albumId", albumId);
        args.putString("urlAudio", urlAudioValue);
        args.putString("playedDuration", played_value);
        args.putString("totalDuration", total_value);
        args.putString("artistId", artistId);

        lyricFragment.setArguments(args);
        ((AppCompatActivity) requireContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, lyricFragment, "LyricFragment")
                .addToBackStack("LyricFragment")
                .commit();
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
        threeDots = view.findViewById(R.id.threeDots);
        threeDotsLayout = view.findViewById(R.id.threeDotsLayout);
        seekBar = view.findViewById(R.id.seekbar);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        iconBackPlayingLayout = view.findViewById(R.id.iconBackPlayingLayout);
        iconBackPlaying = view.findViewById(R.id.iconBackPlaying);
        heartBtn = view.findViewById(R.id.heartBtn);
        show_lyricBtn = view.findViewById(R.id.show_lyricBtn);
        lyricLayout = view.findViewById(R.id.lyricLayout);
        lyric = view.findViewById(R.id.lyric);

        checkIsRepeat(new OnRepeatCallback() {
            @Override
            public void onResult(boolean isRepeat) {
                if (isRepeat)
                    repeateBtn.setImageResource(R.drawable.repeate_green);
                else
                    repeateBtn.setImageResource(R.drawable.repeate);
            }
        });

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
        threeDotsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog(getContext());
            }
        });

    }


    private void checkIsLiked(String id, OnIsLikedCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                List<String> likedSongs = (List<String>) userDoc.get("likedsong");
                if (likedSongs != null && likedSongs.contains(id)) {
                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
            callback.onResult(false);
        });
    }

    private void checkIsRepeat(OnRepeatCallback callback)
    {
        if (mediaPlayerManager.getIsRepeat() == true)
            callback.onResult(true);
        else
            callback.onResult(false);
    }

    private void removeSongFromLikedSongs(String songId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayRemove(songId)).addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Removed from liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e("SongAdapter", "Failed to remove song from liked songs: " + e.getMessage());
                });
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
        });
    }

    public void addSongToLikedSongs(String songId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                userDoc.getReference().update("likedsong", FieldValue.arrayUnion(songId)).addOnSuccessListener(aVoid -> {

                    Toast.makeText(requireContext(), "Add to liked songs successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    // Handle the error
                    Log.e("SongAdapter", "Failed to add song to liked songs: " + e.getMessage());
                });
            } else {
                Log.e("SongAdapter", "No user document found with userId: " + userId);
            }
        }).addOnFailureListener(e -> {
            Log.e("SongAdapter", "Failed to retrieve user document: " + e.getMessage());
        });
    }

    private void getTrack(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spotify.com/").addConverterFactory(GsonConverterFactory.create()).build();

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

        albumId = track.album.getId();
        songnameValue = songName;
        artistnameValue = artistName;
        avataValue = imageUrl;
        urlAudioValue = playUrl;
        artistId = track.artists.get(0).getId();
        setupMediaPlayer(playUrl);
        mediaPlayerManager.getMediaPlayer().seekTo(0);
        setupSeekBar();
        setupPauseButton();
        artistname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoveToArtistDetail(track.artists.get(0).getId());
                view.setVisibility(View.GONE);
            }
        });
        MiniPlayerListener miniPlayerListener = (MiniPlayerListener) requireActivity();
        Log.d("Song list", songList.toString() + " " + getCurrentSongIndex(songId));
        miniPlayerListener.updateMiniPlayer(songList, getCurrentSongIndex(songId));
        miniPlayerListener.showMiniPlayer();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayerManager.getMediaPlayer() != null) {
            mediaPlayerManager.setLastPlaybackPosition(mediaPlayerManager.getMediaPlayer().getCurrentPosition());
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
//                mediaPlayerManager.getMediaPlayer().seekTo(mediaPlayerManager.getLastPlaybackPosition());
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
                        if (CurrentPosition == mediaPlayerManager.getMediaPlayer().getDuration() / 1000) {
                            if (mediaPlayerManager.getIsRepeat()) {
                                mediaPlayerManager.getMediaPlayer().seekTo(0);
                                mediaPlayerManager.setCurrentPosition(0);
                            }
                            else
                                PlayNextSong();
                        }
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
                    if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
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
        mediaPlayerManager.setIsPlaying(true);
        isPlaying = true;
        if (playingStateChangeListener != null) {
            playingStateChangeListener.onPlayingStateChanged(isPlaying);
        }
        pauseBtn.setOnClickListener(v -> {
            if (mediaPlayerManager.getIsPlaying() == true) {
                if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    //currentPosition = mediaPlayerManager.getMediaPlayer().getCurrentPosition();
                    mediaPlayerManager.getMediaPlayer().pause();

                }
                mediaPlayerManager.setIsPlaying(false);
                isPlaying = false;
                if (playingStateChangeListener != null) {
                    playingStateChangeListener.onPlayingStateChanged(isPlaying);
                }
                pauseBtn.setBackgroundResource(R.drawable.ic_play);
            } else {
                if (mediaPlayerManager.getMediaPlayer() != null) {
                    mediaPlayerManager.getMediaPlayer().start();
                }
                pauseBtn.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayerManager.setIsPlaying(true);
                isPlaying = true;
                if (playingStateChangeListener != null) {
                    playingStateChangeListener.onPlayingStateChanged(isPlaying);
                }
                handler.postDelayed(updateSeekBarRunnable, 0);
            }
        });
    }

    public void MoveToArtistDetail(String artistId) {
        ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
        artistDetailFragment.setArtistId(artistId);
        Bundle args = new Bundle();
        args.putString("artistId", artistId);
        artistDetailFragment.setArguments(args);
        ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, artistDetailFragment).addToBackStack(null).commit();

    }

    public void MoveToAlbumDetail(String albumId) {
        AlbumDetailFragment likedAlbumDetailFragment = new AlbumDetailFragment();
        likedAlbumDetailFragment.setAlbumId(albumId);
        Bundle args = new Bundle();
        args.putString("albumId", albumId);
        likedAlbumDetailFragment.setArguments(args);
        // Add the Fragment to the Activity
        ((AppCompatActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, likedAlbumDetailFragment)
                .addToBackStack(null)
                .commit();
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
        if (mediaPlayerManager.getMediaPlayer() != null) {
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
        // Instead of calling playPreviousSong here, update the current song data in PlaySongFragment
        updateCurrentSong(songId);
    }

    private void updateCurrentSong(String newSongId) {
        if (newSongId != null && !newSongId.isEmpty() && !newSongId.equals(songId)) {
            this.songId = newSongId;
            this.previousSongId = getPreviousSongId(newSongId); // Implement getPreviousSongId()
            this.nextSongId = getNextSongId(newSongId); // Implement getNextSongId()

            getTrack(accessToken);
        }
    }

    private String getPreviousSongId(String currentSongId) {
        int currentIndex = getCurrentSongIndex(currentSongId);
        if (currentIndex > 0) {
            return songList.get(currentIndex - 1).getId();
        } else {
            return songList.get(songList.size() - 1).getId();
        }
    }

    private String getNextSongId(String currentSongId) {
        int currentIndex = getCurrentSongIndex(currentSongId);
        if (currentIndex < songList.size() - 1) {
            return songList.get(currentIndex + 1).getId();
        } else {
            return songList.get(0).getId();
        }
    }

    public void onPreviousClicked(View view) {
        if (this instanceof OnSongSelectedListener) {
            ((OnSongSelectedListener) this).onSongSelected(songId, previousSongId, nextSongId);
        }
    }

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
    }

    private interface OnRepeatCallback {
        void onResult(boolean isRepeat);
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
            @SerializedName("id")
            private String id;

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }
        }

        public static class AlbumModel {
            @SerializedName("images")
            private List<ImageModel> images;
            @SerializedName("id")
            private String id;

            public String getId() {
                return id;
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

    public void updatePlayPauseButton() {
        if (mediaPlayerManager.getIsPlaying()) {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    public void playPause() {
        if (mediaPlayerManager.getIsPlaying()) {
            mediaPlayerManager.getMediaPlayer().pause();
        } else {
            mediaPlayerManager.getMediaPlayer().start();
        }
        mediaPlayerManager.setIsPlaying(!mediaPlayerManager.getIsPlaying());
        updatePlayPauseButton();
    }

    public interface OnPreviousSongClickListener {
        void onPreviousSongClick(String previousSongId);
    }

    public interface MiniPlayerListener {
        void updateMiniPlayer(List<Song> songList, int currentPosition);

        void showMiniPlayer();

        void hideMiniPlayer();
    }

}