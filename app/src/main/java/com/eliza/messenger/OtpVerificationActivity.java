package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.eliza.messenger.model.User;
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

        setupUI();
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

        textPhone.setText(phoneNumber);

        // Set up OTP input fields
        EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
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
                    }
                }
            });
        }

        verifyButton.setOnClickListener(v -> {
            String otp = getOtpFromFields(otpFields);
            if (otp.length() == 6) {
                progressBar.setVisibility(View.VISIBLE);
                verifyButton.setEnabled(false);
                
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                verifyPhoneNumberWithCode(credential);
            }
        });

        resendButton.setOnClickListener(v -> {
            Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
            PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            verifyPhoneNumberWithCode(credential);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(OtpVerificationActivity.this, "Resend failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String vId,
                                             @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            verificationId = vId;
                            Toast.makeText(OtpVerificationActivity.this, "New OTP sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();
            
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
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

    private void verifyPhoneNumberWithCode(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    
                    // Create new user in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    User newUser = new User(
                        user.getUid(),
                        phoneNumber,
                        user.getDisplayName() != null ? user.getDisplayName() : "",  // Use display name if available
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
                } else {
                    Toast.makeText(OtpVerificationActivity.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
