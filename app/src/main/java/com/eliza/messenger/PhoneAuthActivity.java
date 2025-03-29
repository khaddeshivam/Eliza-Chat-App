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
import com.google.firebase.FirebaseException;
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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

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
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                  @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = vId;
                resendToken = token;
                
                // Start OTP verification activity
                Intent intent = new Intent(PhoneAuthActivity.this, OtpVerificationActivity.class);
                intent.putExtra("phoneNumber", (CharSequence) phoneNumber);
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
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
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                    .whereEqualTo("phone_number", user.getPhoneNumber())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // User already exists, proceed to home
                            startHomeActivity();
                        } else {
                            // User doesn't exist, create a new user
                            createUserInFirestore(user.getUid(), user.getPhoneNumber());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PhoneAuthActivity.this, "Failed to check user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        
        // Validate phone number format
        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format phone number
        String formattedPhoneNumber = phoneNumber;
        
        // Add Indian country code
        formattedPhoneNumber = "+91" + formattedPhoneNumber;

        // Check if user exists in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String finalFormattedPhoneNumber = formattedPhoneNumber;
        
        // Log the phone number being queried
        Log.d(TAG, "Checking phone number: " + finalFormattedPhoneNumber);
        
        db.collection("users")
            .whereEqualTo("phone_number", finalFormattedPhoneNumber)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(this, "This phone number is already registered", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start phone number verification
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    finalFormattedPhoneNumber,  // Phone number to verify
                    60,                          // Timeout duration
                    TimeUnit.SECONDS,            // Unit of timeout
                    this,                       // Activity (for callback binding)
                    callbacks                    // OnVerificationStateChangedCallbacks
                );
            })
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException exception = (FirebaseFirestoreException) e;
                    if (exception.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Toast.makeText(this, "Error checking phone number: Please ensure you've entered a valid 10-digit number with Indian country code (+91)", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error checking phone number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (e instanceof FirebaseException) {
                    FirebaseException firebaseException = (FirebaseException) e;
                    if (firebaseException.getMessage() != null && 
                        firebaseException.getMessage().contains("TOO_MANY_ATTEMPTS")) {
                        Toast.makeText(this, "Too many attempts. Please wait and try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error checking phone number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    
                    // Create new user in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    User newUser = new User(
                        user.getUid(),
                        phoneNumberInput.getText().toString(),
                        "", // displayName will be set later
                        ""  // profilePictureUrl will be set later
                    );
                    
                    db.collection("users")
                        .document(user.getUid())
                        .set(newUser)
                        .addOnSuccessListener(aVoid -> {
                            // Navigate to profile setup
                            Intent intent = new Intent(PhoneAuthActivity.this, ProfileSetupActivity.class);
                            intent.putExtra("userId", user.getUid());
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PhoneAuthActivity.this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(PhoneAuthActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void createUserInFirestore(String userId, String phoneNumber) {
        User newUser = new User();
        newUser.setId(userId);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setDisplayName("User" + System.currentTimeMillis());
        newUser.setProfilePictureUrl("default_profile.png");
        newUser.setLastSeen(new Date());
        newUser.setOnline(true);
        newUser.setTestAccount(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .document(userId)
            .set(newUser)
            .addOnSuccessListener(aVoid -> {
                // User created successfully, proceed to home
                startHomeActivity();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(PhoneAuthActivity.this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
