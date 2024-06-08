package com.example.musicapp.viewmodel;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.musicapp.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ProfileViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final FirebaseStorage storage;
    private String Name;
    private MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();

    public LiveData<UserProfile> getUserProfileLiveData() {
        return userProfileLiveData;
    }
    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    public void fetchCurrentUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        UserProfile userProfile = new UserProfile();
                        if (document.contains("Name")) {
                             Name = document.getString("Name");
                            String name = document.getString("Name");
                            String email = document.getString("email");
                            userProfile.setName(name);
                            userProfile.setEmail(email);
                        }

                        if (document.contains("backgroundImageUrl")) {
                            String backgroundImageUrl = document.getString("backgroundImageUrl");
                            userProfile.setBackgroundImageUrl(backgroundImageUrl);
                        }

                        if (document.contains("avatarUrl")) {
                            String avatarUrl = document.getString("avatarUrl");
                            userProfile.setAvatarUrl(avatarUrl);
                        }
                        userProfileLiveData.postValue(userProfile);
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            });
        }
    }
    public void updateUserName(String newName) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", newName);
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    public void uploadImageAndUpdateProfile(Uri fileUri, String key) {
        if (fileUri != null) {
            StorageReference fileRef = storage.getReference().child(key + "/" + UUID.randomUUID().toString());
            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(key, downloadUri.toString());
                    updateProfile(updates);
                });
            }).addOnFailureListener(e -> {
                // Handle error
            });
        }
    }

    private void updateProfile(Map<String, Object> updates) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    public void saveErrorReport(String reportContent, String email) {
        Map<String, Object> report = new HashMap<>();
        report.put("email", email);
        report.put("created",new Date());
        report.put("reportContent", reportContent);
        db.collection("reports").add(report)
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {
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
                String body = bodyTemplate.replace("[{Name}]", Name);
                Properties prop = new Properties();
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "587");
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.starttls.enable", "true");
                String subject = "Thanks for sending us Feedback&Error report";

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
                            InternetAddress.parse(email)
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

}
