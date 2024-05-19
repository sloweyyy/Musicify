package com.example.musicapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicapp.activities.NotificationActivity;
import com.example.musicapp.fragment.FavouriteFragment;
import com.example.musicapp.fragment.HomeFragment;
import com.example.musicapp.fragment.LikedSongFragment;
import com.example.musicapp.fragment.ProfileFragment;
import com.example.musicapp.fragment.ExploreFragment;
import com.example.musicapp.fragment.PlaySongFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.musicapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth mAuth ;
    String name;
    Boolean isNew;
    FirebaseFirestore db ;
    FirebaseUser user;
    DocumentReference docRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
         user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
         docRef = db.collection("users").document(user.getUid());
         isNew = getIntent().getBooleanExtra("isNew",false);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        if(document.contains("Name")){
                             name = document.getString("Name");

                        }
                    }

                }
            }
        });


        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);
        checkIfFirstTime();
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

    }
    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout,fragment);
        transaction.commit();
    }
    private void checkIfFirstTime()
    {
        if (isNew) {
            sendNotification("Welcome " + name + ", enjoy your experience!");
        } else {
            sendNotification("Welcome back " + name + ", enjoy your experience!");
        }
    }
    private void sendNotification(String message){
        String channelID = "CHANNEL_ID_NOTIFICATION";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelID);

        builder.setSmallIcon(R.drawable.notifications_24dp_fill0_wght400_grad0_opsz24)
                .setContentTitle("Musicify")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("data", message);
        profileFragment.setArguments(bundle);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("showProfileFragment", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID,
                        "Some description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }

}