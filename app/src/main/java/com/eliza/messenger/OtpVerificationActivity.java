package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.eliza.messenger.model.User;
import com.eliza.messenger.util.GoogleApiHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    private String verificationId;
    private FirebaseAuth auth;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        
        // Get phone number and verification ID from intent
        Intent intent = getIntent();
        if (intent != null) {
            phoneNumber = intent.getStringExtra("phoneNumber");
            verificationId = intent.getStringExtra("verificationId");
        }

        if (phoneNumber == null || verificationId == null) {
            Toast.makeText(this, "Invalid verification data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize callbacks for resending OTP
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                verifyPhoneNumberWithCode(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(OtpVerificationActivity.this,
                    "Verification failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                  @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = vId;
                resendToken = token;
                Toast.makeText(OtpVerificationActivity.this,
                    "New OTP sent successfully",
                    Toast.LENGTH_SHORT).show();
            }
        };

        setupUI();
        setupKeypad();
        setupBackButton();
    }

    private void setupUI() {
        TextView textPhone = findViewById(R.id.text_phone);
        EditText otp1 = findViewById(R.id.otp_1);
        EditText otp2 = findViewById(R.id.otp_2);
        EditText otp3 = findViewById(R.id.otp_3);
        EditText otp4 = findViewById(R.id.otp_4);
        EditText otp5 = findViewById(R.id.otp_5);
        EditText otp6 = findViewById(R.id.otp_6);
        MaterialButton verifyButton = findViewById(R.id.button_test_code);
        MaterialButton resendButton = findViewById(R.id.button_resend);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        // Display the phone number
        textPhone.setText(phoneNumber);

        // Set up OTP input fields
        final EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < 5) {
                        otpFields[index + 1].requestFocus();
                    }
                    if (isOtpComplete(otpFields)) {
                        verifyButton.setEnabled(true);
                    } else {
                        verifyButton.setEnabled(false);
                    }
                }
            });
        }

        // Set up verify button
        verifyButton.setOnClickListener(v -> {
            String otp = getOtpFromFields(otpFields);
            if (otp.length() == 6) {
                progressBar.setVisibility(View.VISIBLE);
                verifyButton.setEnabled(false);
                
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                verifyPhoneNumberWithCode(credential);
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up resend button
        resendButton.setOnClickListener(v -> {
            // Check if Google Play Services is available
            if (!GoogleApiHelper.checkPlayServices(this)) {
                Log.e(TAG, "Google Play Services not available or outdated");
                // Try to clear Google Play Services cache to resolve potential issues
                GoogleApiHelper.clearGooglePlayServicesCache(this);
                return;
            }
            
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
            
            // Clear OTP fields
            for (EditText field : otpFields) {
                field.setText("");
            }
            otpFields[0].requestFocus();
            
            PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(resendToken)
                    .build();
            
            try {
                PhoneAuthProvider.verifyPhoneNumber(options);
                Log.d(TAG, "Resending OTP to: " + phoneNumber);
            } catch (SecurityException e) {
                // Handle security exception using our helper
                Log.e(TAG, "Security exception during OTP resend: " + e.getMessage());
                GoogleApiHelper.handleSecurityException(this, e);
            }
            
            // Hide progress after a delay
            new android.os.Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
            }, 2000);
        });
    }
    
    private void setupKeypad() {
        // Get OTP fields
        EditText otp1 = findViewById(R.id.otp_1);
        EditText otp2 = findViewById(R.id.otp_2);
        EditText otp3 = findViewById(R.id.otp_3);
        EditText otp4 = findViewById(R.id.otp_4);
        EditText otp5 = findViewById(R.id.otp_5);
        EditText otp6 = findViewById(R.id.otp_6);
        final EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
        
        // Setup number buttons
        for (int i = 0; i <= 9; i++) {
            final int number = i;
            int buttonId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            MaterialButton button = findViewById(buttonId);
            if (button != null) {
                button.setOnClickListener(v -> onNumberClick(String.valueOf(number), otpFields));
            }
        }
        
        // Setup backspace button
        MaterialButton backspaceButton = findViewById(R.id.btn_backspace);
        if (backspaceButton != null) {
            backspaceButton.setOnClickListener(v -> onBackspaceClick(otpFields));
        }
    }
    
    private void setupBackButton() {
        // Set up back button
        findViewById(R.id.button_back).setOnClickListener(v -> {
            // Go back to phone auth activity
            Intent intent = new Intent(this, PhoneAuthActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void onNumberClick(String number, EditText[] otpFields) {
        // Find the first empty field
        for (EditText field : otpFields) {
            if (field.getText().toString().isEmpty()) {
                field.setText(number);
                // Move focus to next field if not the last one
                int currentIndex = java.util.Arrays.asList(otpFields).indexOf(field);
                if (currentIndex < otpFields.length - 1) {
                    otpFields[currentIndex + 1].requestFocus();
                }
                return;
            }
        }
    }
    
    private void onBackspaceClick(EditText[] otpFields) {
        // Find the last non-empty field
        for (int i = otpFields.length - 1; i >= 0; i--) {
            if (!otpFields[i].getText().toString().isEmpty()) {
                otpFields[i].setText("");
                otpFields[i].requestFocus();
                return;
            }
        }
    }

    private boolean isOtpComplete(EditText[] otpFields) {
        for (EditText field : otpFields) {
            if (field.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getOtpFromFields(EditText[] otpFields) {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            otp.append(field.getText().toString().trim());
        }
        return otp.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Play Services resolution results
        if (GoogleApiHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Google Play Services issue resolved, try again
            Log.d(TAG, "Google Play Services issue resolved, retrying operation");
            
            // Get OTP fields to clear them
            EditText otp1 = findViewById(R.id.otp_1);
            EditText otp2 = findViewById(R.id.otp_2);
            EditText otp3 = findViewById(R.id.otp_3);
            EditText otp4 = findViewById(R.id.otp_4);
            EditText otp5 = findViewById(R.id.otp_5);
            EditText otp6 = findViewById(R.id.otp_6);
            final EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
            
            // Clear OTP fields
            for (EditText field : otpFields) {
                field.setText("");
            }
            otpFields[0].requestFocus();
            
            // Resend OTP
            PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(resendToken)
                    .build();
            
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
    }
    
    private void verifyPhoneNumberWithCode(PhoneAuthCredential credential) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        
        try {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user == null) {
                            Toast.makeText(this, "Authentication failed: User is null", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Check if user already exists in Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users")
                            .document(user.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // User already exists, proceed to home
                                    Intent intent = new Intent(OtpVerificationActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Create new user in Firestore
                                    User newUser = new User(
                                        user.getUid(),
                                        phoneNumber,
                                        user.getDisplayName() != null ? user.getDisplayName() : "",
                                        ""  // Profile picture URL will be set later
                                    );
                                    
                                    // Initialize additional fields
                                    newUser.setFirstName("");
                                    newUser.setLastName("");
                                    newUser.setStatus("Hi there!");
                                    newUser.setLastSeen(new Date());
                                    newUser.setOnline(true);
                                    newUser.setBio("");
                                    newUser.setTestAccount(false);
                                    newUser.setTestUserId(null);
                                    
                                    db.collection("users")
                                        .document(user.getUid())
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            // Navigate to profile setup
                                            Intent intent = new Intent(OtpVerificationActivity.this, ProfileSetupActivity.class);
                                            intent.putExtra("userId", user.getUid());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(OtpVerificationActivity.this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            if (e instanceof FirebaseFirestoreException) {
                                                FirebaseFirestoreException exception = (FirebaseFirestoreException) e;
                                                if (exception.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                                    Toast.makeText(OtpVerificationActivity.this, "Permission denied. Please try again.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(OtpVerificationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(OtpVerificationActivity.this, "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        String errorMessage = "Verification failed";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (SecurityException e) {
            // Handle security exception using our helper
            Log.e(TAG, "Security exception during sign in: " + e.getMessage());
            GoogleApiHelper.handleSecurityException(this, e);
            progressBar.setVisibility(View.GONE);
        }
    }
}
