package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.eliza.messenger.util.GoogleApiHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.eliza.messenger.model.User;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuthActivity";
    private EditText phoneNumberInput;
    private StringBuilder phoneNumber = new StringBuilder();
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        
        Log.d(TAG, "onCreate: Initializing Phone Authentication screen");

        // Enable Firebase logging for debugging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize App Check with Play Integrity
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        // Check if user is already signed in
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User already signed in, redirecting to HomeActivity");
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        phoneNumberInput = findViewById(R.id.phone_number);
        if (phoneNumberInput == null) {
            Log.e(TAG, "Phone number input field not found");
        }
        setupKeypad();
        setupContinueButton();
        setupBackButton();
        setupTestCredentialsButton();
        
        // Initialize callbacks
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "Verification completed: " + credential.getSmsCode());
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "Verification failed: " + e.getMessage());
                
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneAuthActivity.this, 
                        "Invalid phone number. Please enter a valid 10-digit number", 
                        Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseAuthException) {
                    String errorCode = ((FirebaseAuthException) e).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_TIMEOUT":
                            Toast.makeText(PhoneAuthActivity.this, 
                                "Verification timeout. Please check your internet connection and try again", 
                                Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_TOO_MANY_REQUESTS":
                            Toast.makeText(PhoneAuthActivity.this, 
                                "Too many verification attempts. Please wait a few minutes and try again", 
                                Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_NETWORK_REQUEST_FAILED":
                            Toast.makeText(PhoneAuthActivity.this, 
                                "No internet connection. Please check your network and try again", 
                                Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(PhoneAuthActivity.this, 
                                "Verification failed: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PhoneAuthActivity.this, 
                        "Verification failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = vId;
                resendToken = token;
                
                // Hide progress indicator
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                
                // Get the formatted phone number
                String formattedPhoneNumber = "+91" + phoneNumberInput.getText().toString().trim();
                
                // Start OTP verification activity
                Intent intent = new Intent(PhoneAuthActivity.this, OtpVerificationActivity.class);
                intent.putExtra("phoneNumber", formattedPhoneNumber);
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
                
                Log.d(TAG, "Code sent to: " + formattedPhoneNumber + ", verification ID: " + vId);
            }
        };
        
        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pressed, returning to OnboardingActivity");
                Intent intent = new Intent(PhoneAuthActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Add auth state listener to handle authentication state changes
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "User signed in: " + user.getUid());
                // Check if user exists in Firestore
                checkIfUserExists(user.getPhoneNumber());
            } else {
                Log.d(TAG, "User signed out");
            }
        });
    }

    private void setupContinueButton() {
        MaterialButton continueButton = findViewById(R.id.button_continue);
        phoneNumberInput = findViewById(R.id.phone_number);
        
        continueButton.setOnClickListener(v -> {
            verifyPhoneNumber();
        });

        // Add text watcher to enable/disable button based on input
        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                continueButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void verifyPhoneNumber() {
        // Get the phone number
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format phone number with country code
        String formattedPhoneNumber = "+91" + phoneNumber;

        // Show progress indicator
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        // Check if Google Play Services is available
        if (!GoogleApiHelper.checkPlayServices(this)) {
            Log.e(TAG, "Google Play Services not available or outdated");
            Toast.makeText(this, "Please enable Google Play Services", Toast.LENGTH_LONG).show();
            return;
        }

        // Configure PhoneAuthOptions
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)  // Add force resending token
            .build();

        try {
            PhoneAuthProvider.verifyPhoneNumber(options);
            Log.d(TAG, "Phone verification initiated for: " + formattedPhoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error starting phone verification: " + e.getMessage());
            Toast.makeText(this, "Error starting verification: " + e.getMessage(), Toast.LENGTH_LONG).show();
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
            
            // If the error is due to App Check, show a more specific message
            if (e.getMessage() != null && e.getMessage().contains("App attestation failed")) {
                Toast.makeText(this, "Please ensure Google Play Services is properly configured", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: Attempting to sign in with credential");
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential: Success");
                    FirebaseUser user = task.getResult().getUser();
                    
                    if (user != null) {
                        // Now that we're authenticated, check if user exists in Firestore
                        checkIfUserExists(user.getPhoneNumber());
                    }
                } else {
                    Log.w(TAG, "signInWithCredential: Failure", task.getException());
                    Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkIfUserExists(String phoneNumber) {
        Log.d(TAG, "checkIfUserExists: Checking if user exists with phone number: " + phoneNumber);
        
        // First check if user is authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Please complete phone verification first.");
            Toast.makeText(this, "Please complete phone verification first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Then check if user exists in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .whereEqualTo("phone_number", phoneNumber)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                boolean userExists = !queryDocumentSnapshots.isEmpty();
                Log.d(TAG, "User exists: " + userExists);
                
                if (userExists) {
                    // User exists, proceed to HomeActivity
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    // User doesn't exist, proceed to create new account
                    createUserInFirestore(currentUser, phoneNumber);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking user existence", e);
                Toast.makeText(this, "Error checking user existence. Please try again.", Toast.LENGTH_SHORT).show();
            });
    }

    private void createUserInFirestore(FirebaseUser user, String phoneNumber) {
        Log.d(TAG, "createUserInFirestore: Creating new user in Firestore");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User newUser = new User(
            user.getUid(),
            phoneNumber,
            "User " + user.getUid().substring(0, 8), // Generate a default username
            "" // Empty profile picture URL
        );
        
        db.collection("users")
            .document(user.getUid())
            .set(newUser)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User created successfully in Firestore");
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating user in Firestore", e);
                Toast.makeText(this, "Error creating user account. Please try again.", Toast.LENGTH_SHORT).show();
            });
    }

    private void startHomeActivity() {
        Intent intent = new Intent(PhoneAuthActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupKeypad() {
        Log.d(TAG, "Setting up keypad for phone number entry");
        // Setup number buttons
        for (int i = 0; i <= 9; i++) {
            final int number = i;
            int buttonId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            MaterialButton button = findViewById(buttonId);
            if (button != null) {
                button.setOnClickListener(v -> onNumberClick(String.valueOf(number)));
            } else {
                Log.e(TAG, "Button with ID btn_" + i + " not found");
            }
        }

        // Setup backspace button
        MaterialButton backspaceButton = findViewById(R.id.btn_backspace);
        if (backspaceButton != null) {
            backspaceButton.setOnClickListener(v -> onBackspaceClick());
        } else {
            Log.e(TAG, "Backspace button not found");
        }
    }

    private void setupBackButton() {
        Log.d(TAG, "Setting up back button");
        ImageButton backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                getOnBackPressedDispatcher().onBackPressed();
            });
        } else {
            Log.e(TAG, "Back button not found");
        }
    }

    private void setupTestCredentialsButton() {
        Log.d(TAG, "Setting up test credentials button");
        MaterialButton testButton = findViewById(R.id.button_test_credentials);
        if (testButton != null) {
            testButton.setOnClickListener(v -> {
                Log.d(TAG, "Test credentials button clicked");
                phoneNumberInput.setText("1234567890");
            });
        } else {
            Log.e(TAG, "Test credentials button not found");
        }
    }

    private void onNumberClick(String number) {
        if (phoneNumber.length() < 10) {
            phoneNumber.append(number);
            phoneNumberInput.setText(phoneNumber.toString());
            phoneNumberInput.setSelection(phoneNumber.length());
        }
    }

    private void onBackspaceClick() {
        if (phoneNumber.length() > 0) {
            phoneNumber.deleteCharAt(phoneNumber.length() - 1);
            phoneNumberInput.setText(phoneNumber.toString());
            phoneNumberInput.setSelection(phoneNumber.length());
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Play Services resolution results
        if (GoogleApiHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Google Play Services issue resolved, try again
            Log.d(TAG, "Google Play Services issue resolved, retrying operation");
            // Retry the verification process
            verifyPhoneNumber();
        }
    }
}
