package com.example.musicapp;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.musicapp.activities.Launching;
import com.example.musicapp.databinding.ActivityMainBinding;
import com.example.musicapp.fragment.ExploreFragment;
import com.example.musicapp.fragment.FavouriteFragment;
import com.example.musicapp.fragment.HomeFragment;
import com.example.musicapp.fragment.PlaySongFragment;
import com.example.musicapp.fragment.ProfileFragment;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomAppBarListener, PlaySongFragment.OnPlayingStateChangeListener, PlaySongFragment.MiniPlayerListener {
    private boolean isPlayingMusic;

    private static final String MUSIC_NOTIFICATION_CHANNEL_ID = "Musicify";
    private static final int NOTIFICATION_ID = 1001;
    ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String currentSongName;
    FirebaseFirestore db;
    String songId;
    FirebaseUser currentUser;
    SharedPreferences sharedPreferences;
    private TextView miniPlayerSongTitle, miniPlayerArtistName;
    private ImageView miniPlayerImage;
    private LinearLayout miniPlayerLayout;
    private int currentSongIndex = 0;
    private List<Song> songList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());

        // load login state
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, navigate to login screen
            Intent intent = new Intent(MainActivity.this, Launching.class);
            startActivity(intent);
            finish();
            return;
        }
        miniPlayerSongTitle = findViewById(R.id.mini_player_song_title);
        miniPlayerArtistName = findViewById(R.id.mini_player_artist_name);
        miniPlayerImage = findViewById(R.id.mini_player_image);
        miniPlayerLayout = findViewById(R.id.mini_player_layout);

        ImageView miniPlayerPlayPauseButton = findViewById(R.id.mini_player_play_pause_button);
        ImageView miniPlayerNextButton = findViewById(R.id.mini_player_next_button);
        ImageView miniPlayerPreviousButton = findViewById(R.id.mini_player_previous_button);
        miniPlayerPlayPauseButton.setVisibility(View.GONE);
        miniPlayerNextButton.setVisibility(View.GONE);
        miniPlayerPreviousButton.setVisibility(View.GONE);

        miniPlayerNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySongFragment playSongFragment = (PlaySongFragment) getSupportFragmentManager().findFragmentByTag("PlaySongFragment");
                if (playSongFragment != null) {
                    playSongFragment.PlayNextSong();
                    currentSongIndex = (currentSongIndex + 1) % songList.size();
                    updateMiniPlayer(songList, currentSongIndex);
                }
            }
        });
        miniPlayerPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySongFragment playSongFragment = (PlaySongFragment) getSupportFragmentManager().findFragmentByTag("PlaySongFragment");

                if (playSongFragment != null) {
                    playSongFragment.playPause();
                    Log.d("PlaySongFragment", "onClick: " + playSongFragment);
                }
            }
        });
        miniPlayerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        miniPlayerPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySongFragment playSongFragment = (PlaySongFragment) getSupportFragmentManager().findFragmentByTag("PlaySongFragment");
                if (playSongFragment != null) {
                    playSongFragment.PlayPreviousSong(); // Call the existing method
                    // Update the mini-player:
                    currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size(); // Move to the previous song, wrap around if necessary
                    updateMiniPlayer(songList, currentSongIndex);
                }
            }
        });


        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (songList != null && !songList.isEmpty() && currentSongIndex >= 0) {
//                    showPlaySongFragment(songList.get(currentSongIndex).getId(), songList);
//                } else {
//                    Log.e("MainActivity", "Cannot update mini player: songList not ready or invalid index");
//                    return;
//                }
                return;
            }
        });

        requestNotificationPermission();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        replaceFragment(new HomeFragment());
        getCurrentSongObject(currentUser.getUid());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.search) {
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.favourite) {
                replaceFragment(new FavouriteFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
        if (getIntent().getBooleanExtra("showProfileFragment", false)) {
            replaceFragment(new ProfileFragment());
        }
        handleIntent(getIntent());
    }

    public void showPlaySongFragment(String songId, List<Song> songList) {
        PlaySongFragment playSongFragment = new PlaySongFragment();
        Bundle args = new Bundle();
        args.putString("songId", songId);
        playSongFragment.setArguments(args);
        playSongFragment.show(getSupportFragmentManager(), "PlaySongFragment");
    }

    @Override
    public void updateMiniPlayer(List<Song> songList, int currentPosition) {
        this.songList = songList;
        this.currentSongIndex = currentPosition;

        if (songList != null && !songList.isEmpty() && currentPosition >= 0) {
            Song currentSong = songList.get(currentPosition);
            miniPlayerSongTitle.setText(currentSong.getTitle());
            miniPlayerArtistName.setText(currentSong.getArtist());
            Glide.with(this).load(currentSong.getImageUrl()).into(miniPlayerImage);
        } else {
            Log.e("MainActivity", "Cannot update mini player: songList not ready or invalid index");
        }
    }

    @Override
    public void showMiniPlayer() {
        miniPlayerLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideMiniPlayer() {
        miniPlayerLayout.setVisibility(View.GONE);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (intent != null) {
            if (intent.hasExtra("songId")) {
                String songId = intent.getStringExtra("songId");
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("PlaySongFragment");
                if (fragment == null || !fragment.isVisible()) {
                    PlaySongFragment playSongFragment = new PlaySongFragment();
                    Bundle args = new Bundle();
                    args.putString("songId", songId);
                    playSongFragment.setArguments(args);
                    playSongFragment.show(getSupportFragmentManager(), "PlaySongFragment");
                } else {
                }
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        if (fragment instanceof PlaySongFragment) {
            PlaySongFragment playSongFragment = (PlaySongFragment) fragment;
            playSongFragment.setPlayingStateChangedListener(this);
        }
        transaction.commit();
    }

    @Override
    public void hideBottomAppBar() {
        binding.bottomAppBar.setVisibility(View.GONE);
    }

    @Override
    public void showBottomAppBar() {
        binding.bottomAppBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }

    public void getCurrentSongObject(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        db.collection("users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firebase", "Listen failed.", error);
                    return;
                }
                if (value != null && value.exists()) {
                    Map<String, Object> songObject = (Map<String, Object>) value.get("recentListeningSong");
                    if (songObject != null) {
                        currentSongName = (String) songObject.get("songName");
                        songId = (String) songObject.get("songId");
                    }
                }

            }
        });

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
            } else {
                handleNotificationPermissionDenied();
            }
        }
    }

    private void handleNotificationPermissionDenied() {
        Toast.makeText(this, "Notification permission denied. Please enable it in settings for better experience.", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            if (isPlayingMusic)
                Log.d("inside Actitivy pause", "true");
            else
                Log.d("inside Actitivy pause", "false");
            if (isPlayingMusic) {
                sendContinueListeningNotification(currentSongName);
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    private void sendContinueListeningNotification(String currentSongName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MUSIC_NOTIFICATION_CHANNEL_ID,
                    "Music Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MUSIC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications_24dp_fill0_wght400_grad0_opsz24)
                .setContentTitle("Continue Listening")
                .setContentText("Tap to resume " + currentSongName)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            // Handle permission denial case
        }

    }

    @Override
    public void onPlayingStateChanged(boolean isPlaying) {
        isPlayingMusic = isPlaying;
        if (isPlayingMusic)
            Log.d("isPlaying", "true");
        else
            Log.d("isPlaying", "false");
    }
}