package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eliza.messenger.databinding.ActivityOnboardingBinding;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        
        Log.d(TAG, "onCreate: Initializing Onboarding screen");

        MaterialButton startButton = findViewById(R.id.button_start);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                Log.d(TAG, "Start button clicked, navigating to PhoneAuthActivity");
                // Navigate to phone authentication screen
                Intent intent = new Intent(this, PhoneAuthActivity.class);
                startActivity(intent);
                finish(); // Close onboarding activity
            });
        } else {
            Log.e(TAG, "Start button not found");
        }

        @NonNull ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textTerms.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Onboarding screen resumed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Onboarding screen paused");
    }
}
