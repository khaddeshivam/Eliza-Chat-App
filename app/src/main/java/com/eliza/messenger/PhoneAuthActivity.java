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
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.eliza.messenger.util.TestCredentials;

public class PhoneAuthActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuthActivity";
    private EditText phoneNumberInput;
    private StringBuilder phoneNumber = new StringBuilder();
    private FirebaseAuth auth;

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
            // TODO: Check if profile exists
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
        
        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pressed, returning to OnboardingActivity");
                // Navigate back to OnboardingActivity
                Intent intent = new Intent(PhoneAuthActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
        ImageButton backspaceButton = findViewById(R.id.btn_backspace);
        if (backspaceButton != null) {
            backspaceButton.setOnClickListener(v -> onBackspaceClick());
        } else {
            Log.e(TAG, "Backspace button not found");
        }
    }

    private void setupContinueButton() {
        Log.d(TAG, "Setting up continue button");
        MaterialButton continueButton = findViewById(R.id.button_continue);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                if (phoneNumber.length() < 10) {
                    Log.w(TAG, "Invalid phone number length: " + phoneNumber.length());
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fullPhoneNumber = getString(R.string.country_code) + phoneNumber.toString();
                Log.d(TAG, "Proceeding with phone number: " + fullPhoneNumber);
                
                // Check if this is a test phone number
                if (TestCredentials.isTestPhoneNumber(fullPhoneNumber)) {
                    Log.d(TAG, "Using test phone number: " + fullPhoneNumber);
                    Toast.makeText(this, "Using test credentials\nVerification code: 123456", Toast.LENGTH_LONG).show();
                }
                
                Intent intent = new Intent(this, OtpVerificationActivity.class);
                intent.putExtra("phone_number", fullPhoneNumber);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Continue button not found");
        }
    }

    private void setupBackButton() {
        Log.d(TAG, "Setting up back button");
        ImageButton backButton = findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                // Use the new back press dispatcher
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
                // Fill in test phone number
                phoneNumber = new StringBuilder("1234567890");
                updatePhoneNumberDisplay();
                updateContinueButtonState();
                Toast.makeText(this, "Test phone number loaded", Toast.LENGTH_SHORT).show();
            });
            
            // Only show in debug mode
            if (getResources().getBoolean(R.bool.is_debug_build)) {
                testButton.setVisibility(View.VISIBLE);
            } else {
                testButton.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "Test credentials button not found in layout");
        }
    }

    private void onNumberClick(String number) {
        Log.d(TAG, "Number clicked: " + number);
        if (phoneNumber.length() < 10) {
            phoneNumber.append(number);
            updatePhoneNumberDisplay();
            updateContinueButtonState();
        } else {
            Log.d(TAG, "Phone number already at maximum length");
        }
    }

    private void onBackspaceClick() {
        Log.d(TAG, "Backspace clicked");
        if (phoneNumber.length() > 0) {
            phoneNumber.deleteCharAt(phoneNumber.length() - 1);
            updatePhoneNumberDisplay();
            updateContinueButtonState();
        } else {
            Log.d(TAG, "Phone number is already empty");
        }
    }

    private void updatePhoneNumberDisplay() {
        Log.d(TAG, "Updating phone number display: " + phoneNumber.toString());
        phoneNumberInput.setText(phoneNumber.toString());
    }

    private void updateContinueButtonState() {
        MaterialButton continueButton = findViewById(R.id.button_continue);
        if (continueButton != null) {
            boolean enabled = phoneNumber.length() == 10;
            Log.d(TAG, "Updating continue button state: " + (enabled ? "enabled" : "disabled"));
            continueButton.setEnabled(enabled);
        } else {
            Log.e(TAG, "Continue button not found when updating state");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: PhoneAuthActivity resumed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: PhoneAuthActivity paused");
    }
}
