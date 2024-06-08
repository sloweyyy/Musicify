package com.example.musicapp.fragment;
 
import android.app.Dialog;
import static android.app.Activity.RESULT_OK;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.activities.LoginActivity;
import com.example.musicapp.activities.PrivacyPolicyActivity;
import com.example.musicapp.activities.TermsConditionActivity;
import com.example.musicapp.viewmodel.ProfileViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class ProfileFragment extends Fragment {
    View view;
    ImageButton  modifyName;
    ImageView backgroundAvatar;
    CircleImageView avatar;
    Button logout;
    EditText Name;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AVATAR_IMAGE_REQUEST = 2;
    Uri selectedAvatarUri = null;
    Uri selectedImageUri = null;
    private ProfileViewModel profileViewModel;

    TextView privacyPolicy;
    TextView modifyPassword;
    TextView termsAndConditions,update,feedbackError;
    Dialog dialog1,dialog2;
    Button btnLogoutCancel, btnDialogLogout;
    Button btnReportCancel, btnReportSend;
    TextInputEditText inputReport;
    TextView reportSucess;
    String email;
    String name;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);
        Name = view.findViewById(R.id.Name);
        backgroundAvatar = view.findViewById(R.id.backgroundAvatar);
        avatar = view.findViewById(R.id.avatarImage);
        // Initialize your ViewModel here
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);


//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//        storage = FirebaseStorage.getInstance();
        privacyPolicy = view.findViewById(R.id.privacyPolicy);
        logout = view.findViewById(R.id.btnLogout);
        modifyName = view.findViewById(R.id.modifyName);
        modifyPassword = view.findViewById(R.id.modifyPassword);
//        user = mAuth.getCurrentUser();
        termsAndConditions = view.findViewById(R.id.termsAndConditions);
        update = view.findViewById(R.id.update);
        feedbackError = view.findViewById(R.id.feedbackError);
        dialog1 = new Dialog(getActivity());
        dialog2 = new Dialog(getActivity());
        dialog1.setContentView(R.layout.custom_report_dialog);
        dialog2.setContentView(R.layout.custom_logout_dialog);
        if(getActivity() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            dialog1.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
//            dialog1.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_dialog));
            dialog2.getWindow().setLayout(width,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog2.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_dialog));
        }
        dialog1.setCancelable(false);
        dialog2.setCancelable(false);
        //dialog
        btnLogoutCancel = dialog2.findViewById(R.id.btnCancel);
        btnDialogLogout = dialog2.findViewById(R.id.btnDialogLogout);
        btnReportCancel = dialog1.findViewById(R.id.btnCancel);
        btnReportSend = dialog1.findViewById(R.id.btnReportSend);
        inputReport = dialog1.findViewById(R.id.inputReport);
        reportSucess = dialog1.findViewById(R.id.reportSucess);
        inputReport = dialog1.findViewById(R.id.inputReport);
        setupObservers();
        setupListeners();
        btnLogoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });
        btnReportCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
            }
        });
        btnReportSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reportContent = inputReport.getText().toString().trim();
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
//                    saveErrorReport(reportContent,email);
                    profileViewModel.saveErrorReport(reportContent,email);
                    reportSucess.setVisibility(View.VISIBLE);
                    reportSucess.setText("Thanks for giving us feedback!");
                    inputReport.setText("");



                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reportSucess.setVisibility(View.GONE);
                            //dialog1.dismiss();
                        }
                    }, 6000);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog1.dismiss();
                        }
                    },3000);
                }
            }
        });
        btnDialogLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        feedbackError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Blurry.with(getContext()).radius(10).sampling(2).onto((ViewGroup)view);
                dialog1.show();
                dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Blurry.delete((ViewGroup)view);
                    }
                });
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Blurry.with(getContext()).radius(10).sampling(2).onto((ViewGroup)view);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Nothing to Update");
                builder.setMessage("Your application is already up-to-date.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Blurry.delete((ViewGroup)view);
                    }
                });
            }
        });
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(v.getContext(), PrivacyPolicyActivity.class);
                startActivity(intent);
                getActivity().finish();

            }
        });
        termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), TermsConditionActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        modifyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordSettingFragment fragment = new PasswordSettingFragment();
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Blurry.with(getContext()).radius(10).sampling(2).onto((ViewGroup)view);
                 dialog2.show();
                dialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Blurry.delete((ViewGroup)view);
                    }
                });

            }
        });

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel.fetchCurrentUserProfile();
    }
    private void setupObservers() {
        profileViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                email = userProfile.getEmail();
                Name.setText(userProfile.getName());
                if (userProfile.getBackgroundImageUrl() != null && !userProfile.getBackgroundImageUrl().isEmpty()) {
                    Glide.with(view.getContext()).load(userProfile.getBackgroundImageUrl()).into(backgroundAvatar);
                }
                if (userProfile.getAvatarUrl() != null && !userProfile.getAvatarUrl().isEmpty()) {
                    Glide.with(view.getContext()).load(userProfile.getAvatarUrl()).circleCrop().into(avatar);
                }
            }
        });
    }
    private void setupListeners() {
        modifyName.setOnClickListener(v -> {
            String newName = Name.getText().toString();
            profileViewModel.updateUserName(newName);
            hideKeyboardAndClearFocus(Name);

        });

        avatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_AVATAR_IMAGE_REQUEST);
        });

        // Listener for background image click
        backgroundAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                profileViewModel.uploadImageAndUpdateProfile(selectedImageUri, "backgroundImageUrl");
            } else if (requestCode == PICK_AVATAR_IMAGE_REQUEST) {
                selectedAvatarUri = data.getData();
                profileViewModel.uploadImageAndUpdateProfile(selectedAvatarUri, "avatarUrl");

            }
        }
    }
    private void hideKeyboardAndClearFocus(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
        view.getRootView().clearFocus();
    }

}
