package com.example.musicapp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.adapter.AlbumAdapter;
import com.example.musicapp.adapter.FetchAccessToken;
import com.example.musicapp.adapter.SongAdapter;
import com.example.musicapp.model.Artist;
import com.example.musicapp.model.BottomAppBarListener;
import com.example.musicapp.model.Song;
import com.example.musicapp.viewmodel.ArtistDetailViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.wasabeef.blurry.Blurry;

public class ArtistDetailFragment extends Fragment implements FetchAccessToken.AccessTokenCallback {
    HomeFragment homeFragment;
    private RecyclerView recyclerViewAlbums;
    private ArtistDetailViewModel viewModel;
    private AlbumAdapter albumAdapter;
    private SongAdapter songAdapter;
    private RecyclerView recyclerViewSongs;
    private Artist artist;
    private final List<Artist> followedArtists = new ArrayList<>();
    private Button backButton;
    private Button moreButton;
    private View view;
    private String artistId;
    private String accessToken;
    private FetchAccessToken fetchAccessToken;
    private TextView artistName;
    private ImageView imageView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isFragmentAttached = false;

    public ArtistDetailFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isFragmentAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isFragmentAttached = false;
    }

    @Override
    public void onTokenReceived(String accessToken) {
        this.accessToken = accessToken;
        viewModel.getArtist(accessToken, artistId );
        viewModel.artistDetail.observe(getViewLifecycleOwner(), artists -> {
            if (artists!=null) {
                artistName.setText(artists.getName());
                Glide.with(requireContext()).load(artists.getImages().get(0).getUrl()).into(imageView);
            }
        });

        viewModel.getArtistAlbums(accessToken, artistId);
        viewModel.artistAlbums.observe(getViewLifecycleOwner(), albums -> {
            if (albums != null) {
                albumAdapter = new AlbumAdapter(getContext(), albums);
                recyclerViewAlbums.setAdapter(albumAdapter);
                recyclerViewAlbums.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getArtistTopSongs(accessToken, artistId);
        viewModel.artistTopSongs.observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                songAdapter = new SongAdapter(getContext(), songs, new SongAdapter.OnSongSelectedListener() {
                    @Override
                    public void onSongSelected(Song song) {
                        // Handle song selection
                    }
                });
                recyclerViewSongs.setAdapter(songAdapter);
                recyclerViewSongs.setVisibility(View.VISIBLE);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_artist_detail, container, false);
        ((BottomAppBarListener) requireActivity()).showBottomAppBar();
        recyclerViewAlbums = view.findViewById(R.id.recyclerView_Albums);
        viewModel = new ViewModelProvider(this).get(ArtistDetailViewModel.class);
        recyclerViewSongs = view.findViewById(R.id.recyclerView_Songs);
        artistName = view.findViewById(R.id.artistName);
        imageView = view.findViewById(R.id.imgArtist);
        backButton = view.findViewById(R.id.backBtn);
        moreButton = view.findViewById(R.id.moreBtn);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAlbums.setLayoutManager(horizontalLayoutManager);
        recyclerViewAlbums.setLayoutManager(horizontalLayoutManager);
        recyclerViewSongs.setLayoutManager(layoutManager);
        if (getArguments() != null) {
            artistId = getArguments().getString("artistId");
        }
        setupBackButton();
        fetchAccessToken = new FetchAccessToken();
        fetchAccessToken.getTokenFromSpotify(this);
        homeFragment = new HomeFragment();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((BottomAppBarListener) requireActivity()).showBottomAppBar();
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton();
        setupMoreButton();
    }

    private void setupBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setupMoreButton() {
        moreButton = getView().findViewById(R.id.moreBtn);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionsDialog(getContext());
            }
        });
    }

    private void showMoreOptionsDialog(Context context) {
        // Create a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.more_dialog_artist, null);

        // Set the dialog's content view
        builder.setView(dialogView);

        // Get references to the buttons in the dialog
        LinearLayout fl =dialogView.findViewById(R.id.follow);
        ImageButton flBtn = dialogView.findViewById(R.id.followButton);
        LinearLayout reportBtn = dialogView.findViewById(R.id.report);
        TextView flText = dialogView.findViewById(R.id.follow_or_not);
        Button cancel = dialogView.findViewById(R.id.cancel);
        AlertDialog dialog = builder.create();
        dialog.show();

        viewModel.checkIsFollwed(artistId, new ArtistDetailViewModel.OnIsFollowedCallback() {
            @Override
            public void onResult(boolean isFollowed) {
                if (isFollowed) {
                    flBtn.setBackgroundResource(R.drawable.follow_fill);
                    flText.setTextColor(Color.parseColor("#49A078"));
                    flText.setText("Unfollow");
                } else {
                    flBtn.setBackgroundResource(R.drawable.follow);
                    flText.setTextColor(Color.parseColor("#FFFFFF"));
                    flText.setText("Follow");
                }
            }
        });
        // Set click listeners for the buttons

        fl.setOnClickListener(v -> { if (isFragmentAttached) {
            viewModel.checkIsFollwed(artistId, isFollowed -> {
                if (isFollowed) {
                    viewModel.unfollowArtist(artistId, requireContext());
                    flBtn.setBackgroundResource(R.drawable.follow);
                    flText.setTextColor(Color.parseColor("#FFFFFF"));
                    flText.setText("Follow");
                } else {
                    viewModel.addFollowedArtist(artistId, requireContext());
                    flBtn.setBackgroundResource(R.drawable.follow_fill);
                        flText.setTextColor(Color.parseColor("#49A078"));
                        flText.setText("Unfollow");
                    }
                });
            }
        });
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialogReport = new Dialog(getActivity());
                dialogReport.setContentView(R.layout.custom_report_dialog_artist);
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

                Blurry.with(getContext()).radius(10).sampling(2).onto((ViewGroup)view);
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
                                    Blurry.delete((ViewGroup)view);
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


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Blurry.delete((ViewGroup) dialogView);
            }
        });
    }

}
