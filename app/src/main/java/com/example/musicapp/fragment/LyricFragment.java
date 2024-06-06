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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.manager.MediaPlayerManager;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class LyricFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    private FetchAccessToken fetchAccessToken;
    private View view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    private static final String API_KEY = "63a9e2c4de53b2981cc9b3a8df6b9f32";
    private static final String API_BASE_URL = "https://api.musixmatch.com/ws/1.1/";

    private boolean isPlaying;
    private String songIdhMusixmatc;

    private List<Song> songList;
    private String songId;
    private int currentPosition;

    private boolean isNullLyric;
    private MediaPlayerManager mediaPlayerManager;
    PlaySongFragment playSongFragment = new PlaySongFragment();
    private ImageView background, threeDots, artistAvata, heartBtn;
    private String songNameValue, artistNameValue, avataValue, played_value, total_value, urlAudioValue, albumId, artistId;
    private LinearLayout backButtonLayout;
    private Button iconBack;
    private TextView header, artistName, songName, playedDuration, totalDuration, lyric;
    private ImageButton repeateBtn, previousBtn, pauseBtn, nextBtn, shuffleBtn;
    private SeekBar seekBar;
    private MusixmatchApi musixmatchApi;
    private String commondId;


    @Override
    public void onTokenReceived(String accessToken) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lyric, container, false);
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        mediaPlayerManager = MediaPlayerManager.getInstance();
        if (mediaPlayerManager.getIsPlaying() == true) {
            mediaPlayerManager.getMediaPlayer().start();
        }
        if (getArguments() != null) {
            songNameValue = getArguments().getString("songName");
            artistNameValue = getArguments().getString("artistName");
            avataValue = getArguments().getString("avata");
            played_value = getArguments().getString("playedDuration");
            total_value = getArguments().getString("totalDuration");
            albumId = getArguments().getString("albumId");
            urlAudioValue = getArguments().getString("urlAudio");
            artistId = getArguments().getString("artistId");

        }
        initializeViews();
        searchTrack();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.setCurrentPosition(0);
                ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.setCurrentPosition(0);
                ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();

            }
        });

        repeateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().seekTo(0);
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().pause();
                mediaPlayerManager.setCurrentPosition(0);
                mediaPlayerManager.getMediaPlayer().seekTo(0);
                ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
                PlayPreviousSong();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().pause();
                mediaPlayerManager.setCurrentPosition(0);
                mediaPlayerManager.getMediaPlayer().seekTo(0);
                ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
                PlayNextSong();
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.getMediaPlayer().pause();
                mediaPlayerManager.setCurrentPosition(0);
                mediaPlayerManager.getMediaPlayer().seekTo(0);
                ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
                PlayRandomSong();
            }
        });
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
                if(getActivity() != null) {
                    int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
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
                        }
                        else{
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
                        },3000);
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

                dialog.dismiss(); // Dismiss your previous dialog if needed
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        mediaPlayerManager.setCurrentPosition(mediaPlayerManager.getMediaPlayer().getCurrentPosition());
        mediaPlayerManager.getMediaPlayer().seekTo(mediaPlayerManager.getMediaPlayer().getCurrentPosition());
    }
    public void MoveToArtistDetail (String artistId)
    {
        ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
        artistDetailFragment.setArtistId(artistId);
        Bundle args = new Bundle();
        args.putString("artistId", artistId);
        artistDetailFragment.setArguments(args);
        ((AppCompatActivity)getContext()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, artistDetailFragment)
                .addToBackStack(null)
                .commit();

    }
    public void MoveToAlbumDetail(String albumId){
        AlbumDetailFragment likedAlbumDetailFragment= new AlbumDetailFragment();
        likedAlbumDetailFragment.setAlbumId(albumId);
        Bundle args = new Bundle();
        args.putString("albumId",albumId);
        likedAlbumDetailFragment.setArguments(args);
        // Add the Fragment to the Activity
        ((AppCompatActivity)getContext()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, likedAlbumDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void initializeViews() {

        lyric = view.findViewById(R.id.lyric);
        songName = view.findViewById(R.id.songName);
        songName.setText(songNameValue);

        background = view.findViewById(R.id.background);
        Glide.with(getActivity()).load(avataValue).into(background);

        artistName = view.findViewById(R.id.artistName);
        artistName.setText(artistNameValue);

        artistAvata = view.findViewById(R.id.artistAvata);
        Glide.with(getActivity()).load(avataValue).into(artistAvata);

        playedDuration = view.findViewById(R.id.played);
        playedDuration.setText(played_value);

        totalDuration = view.findViewById(R.id.total);
        totalDuration.setText(total_value);

        repeateBtn = view.findViewById(R.id.repeateBtn);
        previousBtn = view.findViewById(R.id.previousBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        nextBtn = view.findViewById(R.id.nextBtn);
        shuffleBtn = view.findViewById(R.id.shuffleBtn);
        seekBar = view.findViewById(R.id.seekbar);
        backButtonLayout = view.findViewById(R.id.backButtonLayout);
        iconBack = view.findViewById(R.id.iconBack);
        heartBtn = view.findViewById(R.id.heartBtn);
        threeDots = view.findViewById(R.id.threeDots);

        threeDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog(getContext());
            }
        });
        checkIsLiked(songId,new OnIsLikedCallback() {
            @Override
            public void onResult(boolean isLiked) {
                if (isLiked) {
                    heartBtn.setImageResource(R.drawable.favourite_filled);
                } else {
                    heartBtn.setImageResource(R.drawable.heart_green_ouline);
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
        artistName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoveToArtistDetail(artistId);
            }
        });
        setupMediaPlayer();
        setupSeekBar();
        setupPauseButton();

    }

    public void setCurrentSongList(List<Song> songList, String currentSongId) {
        this.songList = songList;
        this.songId = currentSongId;
    }

    public void setSongName(String songName) {
        this.songNameValue = songName;
    }

    public void setArtistName(String artistName) {
        this.artistNameValue = artistName;
    }

    public void setAvata(String avara_url) {
        this.avataValue = avara_url;
    }

    public void setPlayedDuration(String duration) {
        this.played_value = duration;
    }

    public void setTotalDuration(String duration) {
        this.total_value = duration;
    }

    public void setupMediaPlayer() {
        seekBar.setMax((int) (mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
        totalDuration.setText(formattedTime(mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayerManager.getMediaPlayer() != null && mediaPlayerManager.getMediaPlayer().isPlaying()) { // Check if mediaPlayer is playing
                    int CurrentPosition = (mediaPlayerManager.getMediaPlayer().getCurrentPosition() / 1000);
                    seekBar.setProgress(CurrentPosition);
                    playedDuration.setText(formattedTime(CurrentPosition));
                    handler.postDelayed(this, 500);
                    if (CurrentPosition == mediaPlayerManager.getMediaPlayer().getDuration()/1000){
                        mediaPlayerManager.getMediaPlayer().pause();
                        mediaPlayerManager.setCurrentPosition(0);
                        mediaPlayerManager.getMediaPlayer().seekTo(0);
                        ((BottomAppBarListener) requireActivity()).showBottomAppBar();
                        FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager.popBackStack();
                        PlayNextSong();// Update every 500 milliseconds
                    }
                }
            }
        };
        handler.postDelayed(updateSeekBarRunnable, 0);
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
                playedDuration.setText(formattedTime(progress));
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
        backButtonLayout.setOnClickListener(v -> {
            if (mediaPlayerManager.getIsPlaying() == true) {
                if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().pause();
                }
                mediaPlayerManager.setIsPlaying(false);
                pauseBtn.setBackgroundResource(R.drawable.play);
            } else {
                if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().start();
                }
                pauseBtn.setBackgroundResource(R.drawable.pause);
                mediaPlayerManager.setIsPlaying(true);
                handler.postDelayed(updateSeekBarRunnable, 0);
            }
        });

        pauseBtn.setOnClickListener(v -> {
            if (mediaPlayerManager.getIsPlaying() == true) {
                if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().pause();
                }
                mediaPlayerManager.setIsPlaying(false);
                pauseBtn.setBackgroundResource(R.drawable.play);
            } else {
                if (mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().start();
                }
                pauseBtn.setBackgroundResource(R.drawable.pause);
                mediaPlayerManager.setIsPlaying(true);
                handler.postDelayed(updateSeekBarRunnable,0);
            }
        });
    }
    @SuppressLint("DefaultLocale")
    private String formattedTime(int currentPosition) {
        int minutes = currentPosition / 60;
        int seconds = currentPosition % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public void searchTrack() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        musixmatchApi = retrofit.create(MusixmatchApi.class);
        Call<MusixmatchSearchResponse> call = musixmatchApi.searchTrack(songNameValue, artistNameValue, API_KEY);
        call.enqueue(new Callback<MusixmatchSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<MusixmatchSearchResponse> call, @NonNull Response<MusixmatchSearchResponse> response) {
                if (response.isSuccessful()) {
                    MusixmatchSearchResponse searchResponse = response.body();
                    if (searchResponse != null) {
                        if (searchResponse.getMessageBody().getBody().getTrackList() != null) {
                            songIdhMusixmatc = searchResponse.getMessageBody().getBody().getTrackList().getTrackInfo().getId();
                            commondId = searchResponse.getMessageBody().getBody().getTrackList().getTrackInfo().getIdCommon();
                            getLyric(songIdhMusixmatc);
                            if (isNullLyric == true)
                                getLyric(commondId);
                        } else {
                            lyric.setText("Don't have lyrics");
                        }

                    } else {
                        Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MusixmatchSearchResponse> call, Throwable t) {
            }
        });
    }
    public void getLyric(String songId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        musixmatchApi = retrofit.create(MusixmatchApi.class);
        Call<LyricsResponse> call = musixmatchApi.getLyrics(songId, API_KEY);
        call.enqueue(new Callback<LyricsResponse>() {
            @Override
            public void onResponse(@NonNull Call<LyricsResponse> call, @NonNull Response<LyricsResponse> response) {
                if (response.isSuccessful()) {
                    LyricsResponse lyricResponse = response.body();
                    if (lyricResponse != null) {
                        String lyricText = lyricResponse.getMessageBody().getBody().getLyric_container().getLyric();
                        if (lyricText != null) {
                            isNullLyric = false;
                            lyric.setText(lyricText);
                        } else {
                            isNullLyric = true;
                        }
                    } else {
                        Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LyricsResponse> call, Throwable t) {
            }
        });
    }

    public void PlayPreviousSong() {
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        int currentIndex = getCurrentSongIndex(songId);
        String previousSongId = "";
        if (currentIndex > 0) {
            previousSongId = songList.get(currentIndex - 1).getId();
        } else {
            previousSongId = songList.get(songList.size() - 1).getId();
        }
        updateCurrentSong(previousSongId);
    }

    private void PlayNextSong() {
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        int currentIndex = getCurrentSongIndex(songId);
        String nextSongId = "";
        if (currentIndex < songList.size() - 1) {
            nextSongId = songList.get(currentIndex + 1).getId();
        } else {
            nextSongId = songList.get(0).getId();
        }
        updateCurrentSong(nextSongId);
    }

    private int getCurrentSongIndex(String songId) {
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getId().equals(songId)) {
                return i;
            }
        }
        return -1;
    }
    private void PlayRandomSong() {
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        int randomIndex = (int) (Math.random() * songList.size());
        String nextSongId = songList.get(randomIndex).getId();
        updateCurrentSong(nextSongId);
    }
    private void updateCurrentSong(String newSongId) {
        if (newSongId != null && !newSongId.isEmpty() && !newSongId.equals(songId)) {
            this.songId = newSongId;
            PlaySongFragment fragment = new PlaySongFragment();
            fragment.setSongId(songId);
            fragment.setCurrentSongList(songList, songId);
            Bundle args = new Bundle();
            args.putString("songId", songId);
            fragment.setArguments(args);
            fragment.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(), "PlaySongFragment");
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

    private interface OnIsLikedCallback {
        void onResult(boolean isLiked);
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

    public interface MusixmatchApi {
        @GET("track.search")
        Call<MusixmatchSearchResponse> searchTrack(
                @Query("q_track") String trackTitle,
                @Query("q_artist") String trackArtist,
                @Query("apikey") String apiKey
        );

        @GET("track.lyrics.get")
        Call<LyricsResponse> getLyrics(
                @Query("track_id") String trackId,
                @Query("apikey") String apiKey
        );
    }

    public static class MusixmatchSearchResponse {
        @SerializedName("message")
        private MessageContainer messageBody;

        public MessageContainer getMessageBody() {
            return messageBody;
        }

        public static class MessageContainer {
            @SerializedName("body")
            private MessageBody body;

            public MessageBody getBody() {
                return body;
            }

            public static class MessageBody {
                @SerializedName("track_list")
                public List<TrackWrapper> trackList;

                public TrackWrapper getTrackList() {
                    if (trackList != null && !trackList.isEmpty()) {
                        return trackList.get(0);
                    } else {
                        return null;
                    }
                }

                public static class TrackWrapper {
                    @SerializedName("track")
                    private trackInfo track_Info;

                    public trackInfo getTrackInfo() {
                        return track_Info;
                    }

                    public static class trackInfo {
                        @SerializedName("track_id")
                        public String id;
                        @SerializedName("commontrack_id")
                        public String idCommon;

                        public String getId() {
                            return id;
                        }

                        public String getIdCommon() {
                            return idCommon;
                        }
                    }
                }
            }
        }


    }

    public static class LyricsResponse {
        @SerializedName("message")
        private MessageContainer messageBody;

        public MessageContainer getMessageBody() {
            return messageBody;
        }

        public static class MessageContainer {
            @SerializedName("body")
            private MessageBody body;

            public MessageBody getBody() {
                return body;
            }

            public static class MessageBody {
                @SerializedName("lyrics")
                private lyricContainer lyric_container;

                public lyricContainer getLyric_container() {
                    return lyric_container;
                }

                public static class lyricContainer {
                    @SerializedName("lyrics_body")
                    private String lyric;

                    public String getLyric() {
                        return lyric;
                    }
                }
            }
        }
    }
}
