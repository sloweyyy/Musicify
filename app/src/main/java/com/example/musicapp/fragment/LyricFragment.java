package com.example.musicapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.manager.MediaPlayerManager;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Image;

import java.util.ResourceBundle;

public class LyricFragment extends Fragment implements FetchAccessToken.AccessTokenCallback{
    private FetchAccessToken fetchAccessToken;
    private View view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    private boolean isPlaying;
    private String songId;
    private int currentPosition;
    private MediaPlayerManager mediaPlayerManager;
    private ImageView background, threeDots, artistAvata, heartBtn;
    private String songNameValue, artistNameValue, avataValue, played_value, total_value, urlAudio;
    private LinearLayout backButtonLayout;
    private Button iconBack;
    private TextView header, artistName, songName, playedDuration, totalDuration;
    private ImageButton repeateBtn, previousBtn, pauseBtn, nextBtn, shuffleBtn;
    private SeekBar seekBar;
    @Override
    public void onTokenReceived(String accessToken) {

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lyric, container, false);
        ((BottomAppBarListener) requireActivity()).hideBottomAppBar();
        mediaPlayerManager = MediaPlayerManager.getInstance();
        if (mediaPlayerManager.getIsPlaying()==true) {
            mediaPlayerManager.getMediaPlayer().start();
        }
        if (getArguments() != null) {
            songId = getArguments().getString("songId");
            songNameValue = getArguments().getString("songName");
            artistNameValue = getArguments().getString("artistName");
            avataValue = getArguments().getString("avata");
           played_value = getArguments().getString("playedDuration");
           total_value = getArguments().getString("totalDuration");

        }
        initializeViews();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayerManager.setCurrentPosition(mediaPlayerManager.getMediaPlayer().getCurrentPosition());
        mediaPlayerManager.getMediaPlayer().seekTo(mediaPlayerManager.getMediaPlayer().getCurrentPosition());
    }

    private void initializeViews() {


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

        setupMediaPlayer();
        setupSeekBar();
        setupPauseButton();

    }
    public void setSongId(String songId){
        this.songId = songId;
    }
    public void setSongName(String songName){
        this.songNameValue = songName;
    }
    public void setArtistName(String artistName){
        this.artistNameValue = artistName;
    }
    public void setAvata(String avara_url){
        this.avataValue = avara_url;
    }
    public void setPlayedDuration(String duration){
        this.played_value = duration;
    }

    public void setTotalDuration(String duration){
        this.total_value = duration;
    }

    public void setupMediaPlayer() {
        seekBar.setMax((int) (mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
        totalDuration.setText(formattedTime(mediaPlayerManager.getMediaPlayer().getDuration() / 1000));
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if ( mediaPlayerManager.getMediaPlayer()!= null && mediaPlayerManager.getMediaPlayer().isPlaying())
                { // Check if mediaPlayer is playing
                    int CurrentPosition = ( mediaPlayerManager.getMediaPlayer().getCurrentPosition() / 1000);
                    seekBar.setProgress(CurrentPosition);
                    playedDuration.setText(formattedTime(CurrentPosition));
                    handler.postDelayed(this, 500); // Update every 500 milliseconds
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
                    if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
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
            if (mediaPlayerManager.getIsPlaying()==true) {
                if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().pause();
                }
                mediaPlayerManager.setIsPlaying(false);
                pauseBtn.setBackgroundResource(R.drawable.play);
            } else {
                if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().start();
                }
                pauseBtn.setBackgroundResource(R.drawable.pause);
                mediaPlayerManager.setIsPlaying(true);
            }
        });

        pauseBtn.setOnClickListener(v -> {
            if (mediaPlayerManager.getIsPlaying()==true) {
                if ( mediaPlayerManager.getMediaPlayer() != null) { // Check if mediaPlayer is initialized
                    mediaPlayerManager.getMediaPlayer().pause();
                }
                mediaPlayerManager.setIsPlaying(false);
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
    @SuppressLint("DefaultLocale")
    private String formattedTime(int currentPosition) {
        int minutes = currentPosition / 60;
        int seconds = currentPosition % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
