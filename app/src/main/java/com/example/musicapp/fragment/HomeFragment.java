package com.example.musicapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicapp.R;
import com.example.musicapp.activities.LoginActivity;
import com.example.musicapp.adapter.HomeFragmentAdapter;
import com.example.musicapp.adapter.SongHomeAdapter;
import com.example.musicapp.model.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements SongHomeAdapter.OnSongSelectedListener {
    private View view;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    ImageView artistImage;
    String songId;
    TextView recentSongArtist, recentSongName;
    String userId;
    FirebaseUser user;
    FirebaseAuth mAuth;
    ImageButton resumeBtn;
    private HomeFragmentAdapter homeFragmentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home, container, false);
        recentSongName = new TextView(requireContext());
        recentSongName.setText("You haven't listened to any song");
        recentSongArtist = new TextView(requireContext());
        recentSongArtist.setText("");
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager);
        resumeBtn = view.findViewById(R.id.resumeBtn);
        homeFragmentAdapter = new HomeFragmentAdapter(getActivity());
        viewPager2.setAdapter(homeFragmentAdapter);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            // User not logged in, navigate to login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return null;  // return null to avoid continuing in this fragment
        }

        userId = user.getUid();
        artistImage = view.findViewById(R.id.artistImage);
        recentSongArtist = view.findViewById(R.id.recentSongArtist);
        recentSongName = view.findViewById(R.id.recentSongName);

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager2.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    tabLayout.getTabAt(position).select();
                }
            });
        }
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songId != null) {
                    PlaySongFragment fragment = new PlaySongFragment();
                    fragment.setSongId(songId);
                    Bundle args = new Bundle();
                    args.putString("songId", songId);
                    fragment.setArguments(args);
                    ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "No recent song to resume", Toast.LENGTH_SHORT).show();
                }
            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error);
                    return;
                }

                if (value != null && value.exists()) {
                    Log.d("Firestore", "Current data: " + value.getData());
                    if (value.contains("recentListeningSong")) {
                        Map<String, Object> recentSongData = (Map<String, Object>) value.get("recentListeningSong");
                        if (recentSongData != null) {
                            recentSongName.setText(recentSongData.get("songName").toString());
                            recentSongArtist.setText(recentSongData.get("artistName").toString());
                            Glide.with(getContext()).load(recentSongData.get("imageURL")).apply(RequestOptions.circleCropTransform()).into(artistImage);
                            resumeBtn.setVisibility(View.VISIBLE);
                            songId = recentSongData.get("songId").toString();
//                            songId= (songId != null) ? songId.toString() : null;
                        }
                    } else {
                        recentSongName.setText("Select song to listen");
                        recentSongArtist.setText("");
                        Glide.with(getContext()).load(R.drawable.playlist_image).apply(RequestOptions.circleCropTransform()).into(artistImage);
                    }
                } else {
                    recentSongArtist.setText("");
                    recentSongName.setText("Choose song to listen");
//                    artistImage.setImageResource(R.drawable.playlist_image);
                    Glide.with(getContext()).load(R.drawable.playlist_image).apply(RequestOptions.circleCropTransform()).into(artistImage);
                }
            }
        });

        return view;
    }

    private void updateRecentSong(Song song) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        Log.d("title of song", "song title: " + song.getTitle());
        if (recentSongName != null && recentSongArtist != null) {
            recentSongName.setText(song.getTitle());
            recentSongArtist.setText(song.getArtist());
            Glide.with(this).load(song.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(artistImage);
        } else {

        }
        Map<String, Object> recentListeningSong = new HashMap<>();
        recentListeningSong.put("songName", song.getTitle());
        recentListeningSong.put("imageURL", song.getImageUrl());
        recentListeningSong.put("artistName", song.getArtist());
        recentListeningSong.put("songId", song.getId());

        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("recentListeningSong", recentListeningSong)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!");
                            } else {
                                Log.w("Firestore", "Error updating document", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("HomeFragment", "userId is null, cannot update Firestore document");
        }
    }

    @Override
    public void onSongSelected(Song song) {
        Log.d("HomeFragment", "Inside home fragment");
        updateRecentSong(song);
    }
}
