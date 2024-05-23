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
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.activities.LoginActivity;
import com.example.musicapp.activities.PrivacyPolicyActivity;
import com.example.musicapp.activities.TermsConditionActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class ProfileFragment extends Fragment {
    View view;
    ImageButton iconBack, modifyName;
    ImageView backgroundAvatar;
    CircleImageView avatar;
    Button logout;
    EditText Name;
    private FirebaseAuth.AuthStateListener mAuthListener;

    TextView counterView;
    private FirebaseStorage storage;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AVATAR_IMAGE_REQUEST = 2;
    Uri selectedAvatarUri = null;
    int notificationCount;
    Uri selectedImageUri = null;
    FirebaseAuth mAuth;
    String data = "";
    FirebaseUser user;
    FirebaseFirestore db;
    TextView privacyPolicy;
    TextView notification;
    TextView modifyPassword;
    TextView termsAndConditions,update,feedbackError;
    List<String> notifications = new ArrayList<>();
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
        iconBack = view.findViewById(R.id.iconBack);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        privacyPolicy = view.findViewById(R.id.privacyPolicy);
        logout = view.findViewById(R.id.btnLogout);
        modifyName = view.findViewById(R.id.modifyName);
        modifyPassword = view.findViewById(R.id.modifyPassword);
        user = mAuth.getCurrentUser();
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
                    saveErrorReport(reportContent,email);
                    reportSucess.setText("Thanks for giving us feedback!");
                    reportSucess.setVisibility(View.VISIBLE);

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

        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            if (document.contains("Name")) {
                                name = document.getString("Name");
                                email = document.getString("email");
                                Name.setText(name);
                            }

                            if (document.contains("backgroundImageUrl")) {
                                String backgroundImageUrl = document.getString("backgroundImageUrl");
                                if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
                                    Glide.with(view.getContext()).load(backgroundImageUrl).into(backgroundAvatar);
                                }
                            }

                            if (document.contains("avatarUrl")) {
                                String avatarUrl = document.getString("avatarUrl");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(view.getContext()).load(avatarUrl).circleCrop().into(avatar);
                                }
                            }
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
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
//                toggleTextViewAppearance(modifyPassword, R.drawable.admin_panel_settings_24dp_fill1_wght400_grad0_opsz24, R.drawable.admin_panel_settings_24dp_fill0_wght400_grad0_opsz24);
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
        modifyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(Name, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        Name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    saveNameChanges();
                    Name.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });
        Name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    saveNameChanges();
                    Name.setCursorVisible(false);
                }
            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_AVATAR_IMAGE_REQUEST);
            }
        });
        backgroundAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        return view;
    }
    private void saveErrorReport(  String reportContent,String recipientEmail) {
        Map<String, Object> updates = new HashMap<>();
        String subject = "Thanks for sending us Feedback&Error report";
        updates.put("email", email);
        updates.put("reportContent", reportContent);
        db.collection("reports")
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
        ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        emailExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String username = "musicifya@gmail.com";
                final String password = "kyza gvnf kcwg mijp";
                String bodyTemplate = "Dear [{Name}],\n\n"
                        + "We have received your feedback regarding our app. Thank you for taking the time to share your thoughts and experiences with us. "
                        + "Your input is invaluable as it helps us to continuously improve our service.\n\n"
                        + "Our team will review your feedback and take the necessary actions. We will contact you directly if we require any additional information. "
                        + "We are committed to providing you with the best possible experience, and your feedback is a key part of that effort.\n\n"
                        + "Thank you once again for your contribution.\n\n"
                        + "Best regards,\n"
                        + "[Nguyen Thi Bich Gau - Manager Of Fake App]\n"
                        + "[Musicify]";
                String body = bodyTemplate.replace("[{Name}]", name);
                Properties prop = new Properties();
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "587");
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(prop,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("musicifya@gmail.com"));
                    message.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(recipientEmail)
                    );
                    message.setSubject(subject);
                    message.setText(body);

                    Transport.send(message);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Email sent successfully");
                        }
                    });

                } catch (MessagingException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Failed to send email");
                        }
                    });
                }
            }
        });


    }

    private void saveNameChanges() {
        String newName = Name.getText().toString();
        updateNameInFirestore(newName);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Name.getWindowToken(), 0);
    }

    private void updateNameInFirestore(String newName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", newName);
        updateDocument(updates);
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Differentiate between the requests
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                backgroundAvatar.setImageURI(selectedImageUri);
                uploadToFirestore(selectedImageUri, "backgroundImageUrl");
            } else if (requestCode == PICK_AVATAR_IMAGE_REQUEST) {
                selectedAvatarUri = data.getData();
                avatar.setImageURI(selectedAvatarUri);
                uploadToFirestore(selectedAvatarUri, "avatarUrl");
            }
        }
    }

    private void uploadToFirestore(Uri fileUri, String key) {
        if (fileUri != null) {
            StorageReference fileRef = storage.getReference().child(key + "/" + UUID.randomUUID().toString());

            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(key, downloadUri.toString());
                    updateDocument(updates);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Error uploading image", e);
            });
        }
    }

    private void updateDocument(Map<String, Object> updates) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userDocRef = db.collection("users").document(user.getUid());
            userDocRef.update(updates)
                    .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w("ProfileFragment", "Error updating document", e));
        }
    }
}
