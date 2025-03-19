package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.eliza.messenger.util.TestCredentials;
import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    private EditText[] otpFields;
    private String verificationId;
    private FirebaseAuth auth;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        
        Log.d(TAG, "onCreate: Initializing OTP verification screen");

        auth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phone_number");
        Log.d(TAG, "Phone number for verification: " + phoneNumber);
        
        setupOtpFields();
        setupNumberPad();
        setupBackButton();
        setupResendButton();
        setupTestCodeButton();
        
        // Register back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back button pressed, returning to previous screen");
                finish();
            }
        });

        // Start phone number verification
        startPhoneNumberVerification();
    }

    private void setupOtpFields() {
        Log.d(TAG, "Setting up OTP input fields");
        otpFields = new EditText[]{
            findViewById(R.id.otp_1),
            findViewById(R.id.otp_2),
            findViewById(R.id.otp_3),
            findViewById(R.id.otp_4),
            findViewById(R.id.otp_5),
            findViewById(R.id.otp_6)
        };

        // Set up text watchers for auto-focus
        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                    if (isOtpComplete()) {
                        Log.d(TAG, "OTP entry complete, verifying code");
                        verifyPhoneNumberWithCode();
                    }
                }
            });
        }
    }

    private void setupNumberPad() {
        Log.d(TAG, "Setting up number pad for OTP entry");
        int[] buttonIds = {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final String number = String.valueOf(i);
            findViewById(buttonIds[i]).setOnClickListener(v -> onNumberClick(number));
        }

        findViewById(R.id.btn_backspace).setOnClickListener(v -> onBackspaceClick());
    }

    private void setupBackButton() {
        Log.d(TAG, "Setting up back button");
        findViewById(R.id.button_back).setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, returning to previous screen");
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void setupResendButton() {
        Log.d(TAG, "Setting up resend button");
        MaterialButton resendButton = findViewById(R.id.button_resend);
        if (resendButton != null) {
            resendButton.setOnClickListener(v -> {
                Log.d(TAG, "Resend button clicked");
                resendButton.setEnabled(false); // Disable to prevent multiple clicks
                
                if (TestCredentials.USE_TEST_CREDENTIALS && 
                    TestCredentials.isTestPhoneNumber(phoneNumber)) {
                    Log.d(TAG, "Using test credentials for resend");
                    Toast.makeText(this, "Test code: " + TestCredentials.TEST_VERIFICATION_CODE, 
                        Toast.LENGTH_LONG).show();
                    resendButton.setEnabled(true);
                    return;
                }

                // Start the verification process again
                startPhoneNumberVerification();
            });
        } else {
            Log.e(TAG, "Resend button not found");
        }
    }
    
    private void setupTestCodeButton() {
        MaterialButton testButton = findViewById(R.id.button_test_code);
        if (testButton != null) {
            if (TestCredentials.USE_TEST_CREDENTIALS && getResources().getBoolean(R.bool.is_debug_build)) {
                testButton.setVisibility(View.VISIBLE);
                testButton.setOnClickListener(v -> {
                    Log.d(TAG, "Using test verification code");
                    String testCode = TestCredentials.TEST_VERIFICATION_CODE;
                    // Fill in the OTP fields
                    EditText[] otpFields = {
                        findViewById(R.id.otp_1),
                        findViewById(R.id.otp_2),
                        findViewById(R.id.otp_3),
                        findViewById(R.id.otp_4),
                        findViewById(R.id.otp_5),
                        findViewById(R.id.otp_6)
                    };
                    
                    for (int i = 0; i < testCode.length() && i < otpFields.length; i++) {
                        otpFields[i].setText(String.valueOf(testCode.charAt(i)));
                    }
                });
            } else {
                testButton.setVisibility(View.GONE);
            }
        }
    }
    
    private void fillOtpFields(String otp) {
        Log.d(TAG, "Filling OTP fields with test code");
        for (int i = 0; i < otpFields.length && i < otp.length(); i++) {
            otpFields[i].setText(String.valueOf(otp.charAt(i)));
        }
    }

    private void onNumberClick(String number) {
        Log.d(TAG, "Number pad clicked: " + number);
        for (EditText field : otpFields) {
            if (field.getText().toString().isEmpty()) {
                field.setText(number);
                return;
            }
        }
    }

    private void onBackspaceClick() {
        Log.d(TAG, "Backspace clicked");
        for (int i = otpFields.length - 1; i >= 0; i--) {
            if (!otpFields[i].getText().toString().isEmpty()) {
                otpFields[i].setText("");
                if (i > 0) otpFields[i - 1].requestFocus();
                return;
            }
        }
    }

    private boolean isOtpComplete() {
        for (EditText field : otpFields) {
            if (field.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getEnteredOtp() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            otp.append(field.getText().toString());
        }
        return otp.toString();
    }

    private void startPhoneNumberVerification() {
        Log.d(TAG, "Starting phone number verification for: " + phoneNumber);
        
        // Check if this is a test phone number
        if (TestCredentials.isTestPhoneNumber(phoneNumber)) {
            Log.d(TAG, "Using test phone number - bypassing actual verification");
            Toast.makeText(this, "Test mode: Verification bypassed", Toast.LENGTH_SHORT).show();
            // For test numbers, we'll simulate receiving a verification code
            // but still go through the UI flow for testing purposes
            verificationId = "test_verification_id";
            return;
        }
        
        try {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted: Auto-verification completed");
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "onVerificationFailed: " + e.getMessage(), e);
                        
                        String errorMessage;
                        if (e.getMessage() != null && e.getMessage().contains("BILLING_NOT_ENABLED")) {
                            errorMessage = "SMS verification is not enabled. For testing, use one of these numbers:\n" +
                                          "• +15555555555\n" +
                                          "• Any number ending with 0000000000\n" +
                                          "\nTest verification code: 123456";
                        } else {
                            errorMessage = "Verification failed: " + e.getMessage();
                        }
                        
                        Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        
                        // Re-enable the resend button
                        MaterialButton resendButton = findViewById(R.id.button_resend);
                        if (resendButton != null) {
                            resendButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId,
                                        @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: Verification code sent");
                        verificationId = vId;
                        resendToken = token;
                    }
                })
                .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            Log.e(TAG, "Error in startPhoneNumberVerification: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting verification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyPhoneNumberWithCode() {
        String code = getEnteredOtp();
        Log.d(TAG, "Verifying phone number with entered code");
        
        // Check if this is a test verification code
        if (TestCredentials.isTestVerificationCode(code)) {
            Log.d(TAG, "Test verification code detected - bypassing actual verification");
            Toast.makeText(this, "Test mode: Verification successful", Toast.LENGTH_SHORT).show();
            proceedToNextScreen();
            return;
        }
        
        if (verificationId != null) {
            try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential);
            } catch (Exception e) {
                Log.e(TAG, "Error creating credential: " + e.getMessage(), e);
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "verificationId is null, cannot verify code");
            Toast.makeText(this, "Verification code not received yet. Please wait or try resending.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVerificationCode() {
        Log.d(TAG, "Resending verification code");
        
        // For test phone numbers, don't actually resend
        if (TestCredentials.isTestPhoneNumber(phoneNumber)) {
            Log.d(TAG, "Test phone number - simulating code resend");
            Toast.makeText(this, "Test mode: Code resent", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (resendToken != null) {
            try {
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            Log.d(TAG, "Resend: onVerificationCompleted");
                            signInWithPhoneAuthCredential(credential);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Log.e(TAG, "Resend: onVerificationFailed: " + e.getMessage(), e);
                            
                            String errorMessage;
                            if (e.getMessage() != null && e.getMessage().contains("BILLING_NOT_ENABLED")) {
                                errorMessage = "SMS verification is not enabled. For testing, use one of these numbers:\n" +
                                              "• +15555555555\n" +
                                              "• Any number ending with 0000000000\n" +
                                              "\nTest verification code: 123456";
                            } else {
                                errorMessage = "Verification failed: " + e.getMessage();
                            }
                            
                            Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            
                            // Re-enable the resend button
                            MaterialButton resendButton = findViewById(R.id.button_resend);
                            if (resendButton != null) {
                                resendButton.setEnabled(true);
                            }
                        }

                        @Override
                        public void onCodeSent(@NonNull String vId,
                                            @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            Log.d(TAG, "Resend: onCodeSent: New verification code sent");
                            verificationId = vId;
                            resendToken = token;
                            Toast.makeText(OtpVerificationActivity.this,
                                "Code resent successfully",
                                Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setForceResendingToken(resendToken)
                    .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            } catch (Exception e) {
                Log.e(TAG, "Error in resendVerificationCode: " + e.getMessage(), e);
                Toast.makeText(this, "Error resending code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "resendToken is null, cannot resend code");
            Toast.makeText(this, "Cannot resend code at this time. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "Signing in with phone auth credential");
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    // Check if user profile exists
                    checkUserProfile();
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(OtpVerificationActivity.this,
                        "Authentication failed: " + (task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkUserProfile() {
        Log.d(TAG, "Checking if user profile exists");
        // TODO: Check if user profile exists in Firestore
        // For now, always go to profile setup
        proceedToNextScreen();
    }
    
    private void proceedToNextScreen() {
        Log.d(TAG, "Proceeding to profile setup screen");
        startActivity(new Intent(this, ProfileSetupActivity.class));
        finish();
    }
}
