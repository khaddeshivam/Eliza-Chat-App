package com.eliza.messenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.eliza.messenger.model.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView profileImage;
    private TextInputEditText nameInput;
    private TextInputEditText bioInput;
    private TextInputEditText statusInput;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String currentPhotoUrl;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        setupViews();
        loadUserProfile();
    }

    private void setupViews() {
        profileImage = findViewById(R.id.profile_image);
        nameInput = findViewById(R.id.name_input);
        bioInput = findViewById(R.id.bio_input);
        statusInput = findViewById(R.id.status_input);

        findViewById(R.id.button_save).setOnClickListener(v -> saveProfile());
        findViewById(R.id.button_add_photo).setOnClickListener(v -> changeProfilePhoto());
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            nameInput.setText(user.getDisplayName());
                            bioInput.setText(user.getBio());
                            statusInput.setText(user.getStatus());
                            currentPhotoUrl = user.getProfilePictureUrl();

                            if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(currentPhotoUrl)
                                        .placeholder(R.drawable.ic_person_placeholder)
                                        .error(R.drawable.ic_person_placeholder)
                                        .into(profileImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile", e);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();
        String status = statusInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("display_name", name);
        updates.put("bio", bio);
        updates.put("status", status);
        updates.put("last_seen", new Date());
        updates.put("is_online", true);

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(updates);
        } else {
            saveProfileData(updates);
        }
    }

    private void uploadImageAndSaveProfile(Map<String, Object> updates) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("profile_images/" + currentUser.getUid() + ".jpg");

        imageRef.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updates.put("profile_picture_url", imageUrl);
                    saveProfileData(updates);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading image", e);
                    Toast.makeText(this, "Error uploading profile image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileData(Map<String, Object> updates) {
        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // Update local user data if needed
                    currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(updates.get("display_name").toString())
                                    .build())
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "User profile updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating user profile", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile", e);
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void changeProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }
}
