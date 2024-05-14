package com.example.musicapp.fragment;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.example.musicapp.model.Category;
import com.google.gson.Gson;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.FetchAccessToken;
import com.google.gson.annotations.SerializedName;

public class PlaySongFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {

    View view;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    TextView songname, artistname, duration_played, duration_total;
    ImageView cover_art;
    ImageButton repeateBtn, previousBtn, pauseBtn, nextBtn, shuffleBtn;
    SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    int position = -1;
    private FetchAccessToken fetchAccessToken;
    @Override
    public void onTokenReceived(String accessToken) {
        getTrack(accessToken);
    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.play_song, container, false);
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        songname = view.findViewById(R.id.songNamePlay);
        artistname = view.findViewById(R.id.artistNamePlay);
        duration_played = view.findViewById(R.id.played);
        duration_total= view.findViewById(R.id.total);
        repeateBtn = view.findViewById(R.id.repeateBtn);
        previousBtn = view.findViewById(R.id.previousBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        nextBtn= view.findViewById(R.id.nextBtn);
        shuffleBtn = view.findViewById(R.id.shuffleBtn);
        cover_art = view.findViewById(R.id.imageCon);
        seekBar = view.findViewById(R.id.seekbar);

        return view;
    }

    private void getIntenMethod(){
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            position = intent.getIntExtra("position", -1);
        }
    }

    public interface SpotifyApi {
        @GET("v1/tracks/{songId}")
        Call<PlaySongFragment.TrackModel> getTrack(@Header("Authorization") String authorization, @Path("songId") String songId);
    }

    public class TrackModel
    {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("preview_url")
        private String preview_url;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getPreview_url() {return preview_url;}

        @SerializedName("artists")
        private List<ArtistModel> artists;

        public class ArtistModel {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
        @SerializedName("album")
        private albumModel album;
        public class albumModel {
            @SerializedName("images")
            private List<imageModel> images;
            @SerializedName("id")
            private String id;

        }
        public class imageModel {
            @SerializedName("url")
            private String url;
            public String getUrl() {
                return url;
            }
        }
    }
    private void getTrack(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyApi apiService = retrofit.create(SpotifyApi.class);

        String songId = "7ouMYWpwJ422jRcDASZB7P";

        String authorization = "Bearer " + accessToken;

        Call<PlaySongFragment.TrackModel> call = apiService.getTrack(authorization, songId);
        call.enqueue(new Callback<TrackModel>() {

            @Override
            public void onResponse(Call<TrackModel> call, Response<TrackModel> response) {
                if (response.isSuccessful()) {
                    TrackModel track = response.body();
                    if (track != null) {
                        String songName = track.getName();
                        String artistName = track.artists.get(0).getName();
                        String imageUrl = track.album.images.get(0).getUrl();
                        String play_url = track.getPreview_url();
                        songname.setText(songName);
                        artistname.setText(artistName);
                        Glide.with(getActivity()).load(imageUrl).into(cover_art);
                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build());

                            mediaPlayer.setDataSource(track.preview_url);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                                    duration_total.setText(formattedTime(mediaPlayer.getDuration() / 1000));
                                    mediaPlayer.start();
                                    isPlaying = true;
                                    handler.postDelayed(updateSeekBarRunnable, 0);

                                }
                            });
                        } catch (Exception e) {
                            Log.e("Error player", e.getMessage());
                        }
                        pauseBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isPlaying) {
                                    mediaPlayer.pause();
                                    isPlaying = false;
                                    pauseBtn.setBackgroundResource(R.drawable.play);
                                } else {
                                    // Resume playing
                                    mediaPlayer.start();
                                    pauseBtn.setBackgroundResource(R.drawable.pause);
                                    isPlaying = true;
                                }
                            }
                        });
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                                seekBar.setProgress(currentPosition);
                                duration_played.setText(formattedTime(progress));
                                mediaPlayer.seekTo(progress * 1000);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                                int progress = seekBar.getProgress();
                                mediaPlayer.seekTo(progress * 1000); // Đặt thời gian phát của MediaPlayer đến vị trí mới
                                handler.postDelayed(updateSeekBarRunnable, 1000); // Gọi lại runnable sau mỗi 1 giây
                            }
                        });
                        updateSeekBarRunnable = new Runnable() {
                            @Override
                            public void run() {
                                int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                                seekBar.setProgress(currentPosition);
                                duration_played.setText(formattedTime(currentPosition));
                                handler.postDelayed(this, 0);
                            }
                        };
                    }
                } else {
                    try {
                        String errorReason = response.errorBody().string();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Error");
                        builder.setMessage(errorReason);
                        builder.setPositiveButton("OK", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onFailure(Call<TrackModel> call, Throwable throwable) {

            }
        });
    }
//    private void updateDurationPlayed() {
//        runnable = new Runnable()
//        {
//            @Override
//            public void run() {
//                if (mediaPlayer != null) {
//                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
//                    seekBar.setProgress(currentPosition);
//                    duration_played.setText(formattedTime(currentPosition));
//                    handler.postDelayed(this, 1000);
//                }
//
//            }
//        };
//     handler.postDelayed(runnable, 0);
//    }


    private String formattedTime (int currentPosition) {
        int minutes = currentPosition / 60;
        int seconds = currentPosition % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
