package com.eliza.messenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.Timestamp;
import com.eliza.messenger.util.TestCredentials;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import android.widget.ProgressBar;

public class ProfileSetupActivity extends AppCompatActivity {
    private static final String TAG = "ProfileSetupActivity";
    private ShapeableImageView profileImage;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        
        // Check if user is authenticated
        if (auth.getCurrentUser() == null && !TestCredentials.USE_TEST_CREDENTIALS) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(this, "Please authenticate first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupClickListeners();
        
        // Set test data if using test credentials
        if (TestCredentials.USE_TEST_CREDENTIALS) {
            setupTestProfile();
        }
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        ImageButton addPhotoButton = findViewById(R.id.button_add_photo);
        MaterialButton saveButton = findViewById(R.id.button_save);
        ImageButton backButton = findViewById(R.id.button_back);
        
        if (profileImage == null || firstNameInput == null || lastNameInput == null || 
            addPhotoButton == null || saveButton == null || backButton == null) {
            Log.e(TAG, "One or more views not found during initialization");
        }
    }

    private void setupClickListeners() {
        // Setup image picker
        setupImagePicker();

        // Setup click listeners
        ImageButton addPhotoButton = findViewById(R.id.button_add_photo);
        if (addPhotoButton != null) {
            addPhotoButton.setOnClickListener(v -> {
                Log.d(TAG, "Add photo button clicked");
                imagePickerLauncher.launch("image/*");
            });
        }
        
        MaterialButton saveButton = findViewById(R.id.button_save);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                Log.d(TAG, "Save button clicked");
                saveProfile();
            });
        }
        
        ImageButton backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        }
        
        // Setup test profile button
        setupTestProfileButton();
    }

    private void setupImagePicker() {
        Log.d(TAG, "Setting up image picker");
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d(TAG, "Image selected: " + uri);
                    selectedImageUri = uri;
                    profileImage.setImageURI(uri);
                } else {
                    Log.d(TAG, "No image selected");
                }
            }
        );
    }
    
    private void setupTestProfileButton() {
        Log.d(TAG, "Setting up test profile button");
        MaterialButton testButton = findViewById(R.id.button_test_profile);
        if (testButton != null) {
            testButton.setOnClickListener(v -> {
                Log.d(TAG, "Test profile button clicked");
                fillTestProfileData();
                Toast.makeText(this, "Test profile data filled", Toast.LENGTH_SHORT).show();
            });
            
            // Only show in debug mode
            if (getResources().getBoolean(R.bool.is_debug_build)) {
                testButton.setVisibility(View.VISIBLE);
            } else {
                testButton.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "Test profile button not found in layout");
            // For testing, we could add it programmatically here if needed
        }
    }
    
    private void fillTestProfileData() {
        Log.d(TAG, "Filling test profile data");
        if (firstNameInput != null) {
            firstNameInput.setText(TestCredentials.TEST_USER_NAME.split(" ")[0]);
        }
        
        if (lastNameInput != null) {
            String[] nameParts = TestCredentials.TEST_USER_NAME.split(" ");
            if (nameParts.length > 1) {
                lastNameInput.setText(nameParts[1]);
            } else {
                lastNameInput.setText("User");
            }
        }
        
        // We could also set a test image here if needed
    }

    private void setupTestProfile() {
        Log.d(TAG, "Setting up test profile");
        fillTestProfileData();
    }

    private void saveProfile() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        
        Log.d(TAG, "Saving profile with name: " + firstName + " " + lastName);

        if (firstName.isEmpty()) {
            Log.w(TAG, "First name is empty");
            firstNameInput.setError("First name is required");
            return;
        }

        // Disable save button to prevent multiple submissions
        MaterialButton saveButton = findViewById(R.id.button_save);
        if (saveButton != null) {
            saveButton.setEnabled(false);
        }

        // Check if we're using test credentials
        String phoneNumber = auth.getCurrentUser() != null ? 
            auth.getCurrentUser().getPhoneNumber() : "";
        
        if (TestCredentials.USE_TEST_CREDENTIALS && 
            (TestCredentials.isTestPhoneNumber(phoneNumber) || getResources().getBoolean(R.bool.is_debug_build))) {
            Log.d(TAG, "Using test credentials - bypassing image upload");
            // For test users, skip the image upload and use a placeholder
            saveProfileData(firstName, lastName, "https://ui-avatars.com/api/?name=" + 
                firstName + "+" + lastName + "&background=random");
            return;
        }

        if (selectedImageUri != null) {
            Log.d(TAG, "Profile image selected, uploading image first");
            uploadImageAndSaveProfile(firstName, lastName);
        } else {
            Log.d(TAG, "No profile image selected, saving profile without image");
            saveProfileData(firstName, lastName, null);
        }
    }

    private void uploadImageAndSaveProfile(String firstName, String lastName) {
        String imageFileName = "profile_images/" + UUID.randomUUID().toString();
        Log.d(TAG, "Uploading image to: " + imageFileName);
        
        StorageReference imageRef = storage.getReference().child(imageFileName);

        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image upload successful, getting download URL");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Image URL retrieved: " + uri);
                    saveProfileData(firstName, lastName, uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                    MaterialButton saveButton = findViewById(R.id.button_save);
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                    }
                    Toast.makeText(this, "Failed to process uploaded image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                MaterialButton saveButton = findViewById(R.id.button_save);
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                }
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void saveProfileData(String firstName, String lastName, String imageUrl) {
        String userId;
        String phoneNumber;
        boolean isTestMode = TestCredentials.USE_TEST_CREDENTIALS;
        
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            phoneNumber = auth.getCurrentUser().getPhoneNumber();
        } else if (isTestMode) {
            userId = TestCredentials.TEST_USER_ID;
            phoneNumber = TestCredentials.TEST_PHONE_NUMBER;
            Log.d(TAG, "Using test credentials - ID: " + userId + ", Phone: " + phoneNumber);
        } else {
            Log.e(TAG, "No authenticated user and not in test mode");
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "Saving profile data for user: " + userId);

        // Create a user profile map
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phoneNumber", phoneNumber);
        user.put("createdAt", Timestamp.now());
        user.put("updatedAt", Timestamp.now());
        
        if (imageUrl != null) {
            user.put("profileImage", imageUrl);
        }
        
        // Add test account flags if in test mode
        if (isTestMode) {
            user.put("testAccount", true);
            user.put("testUserId", userId);
            user.put("bio", TestCredentials.TEST_USER_BIO);
        }
        
        Log.d(TAG, "Profile data prepared: " + user);

        // Show loading state
        MaterialButton saveButton = findViewById(R.id.button_save);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        
        if (saveButton != null) {
            saveButton.setEnabled(false);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // For test mode, sign in with email/password first
        if (isTestMode && auth.getCurrentUser() == null) {
            auth.signInWithEmailAndPassword(TestCredentials.TEST_EMAIL, TestCredentials.TEST_PASSWORD)
                .addOnSuccessListener(result -> saveUserToFirestore(userId, user, saveButton, progressBar))
                .addOnFailureListener(e -> {
                    // If sign in fails, create the test account
                    auth.createUserWithEmailAndPassword(TestCredentials.TEST_EMAIL, TestCredentials.TEST_PASSWORD)
                        .addOnSuccessListener(createResult -> saveUserToFirestore(userId, user, saveButton, progressBar))
                        .addOnFailureListener(createError -> handleSaveError(createError, saveButton, progressBar));
                });
        } else {
            saveUserToFirestore(userId, user, saveButton, progressBar);
        }
    }

    private void saveUserToFirestore(String userId, Map<String, Object> userData, 
                                   MaterialButton saveButton, ProgressBar progressBar) {
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Profile saved successfully");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                // Navigate to HomeActivity
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> handleSaveError(e, saveButton, progressBar));
    }

    private void handleSaveError(Exception e, MaterialButton saveButton, ProgressBar progressBar) {
        Log.e(TAG, "Failed to save profile: " + e.getMessage(), e);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        String errorMessage;
        if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
            errorMessage = "Unable to save profile. Please ensure you have an active internet connection and try again.";
        } else {
            errorMessage = "Failed to save profile: " + e.getMessage();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        if (saveButton != null) {
            saveButton.setEnabled(true);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Profile setup screen resumed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Profile setup screen paused");
    }
}
