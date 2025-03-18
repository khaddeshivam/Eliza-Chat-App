package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        MaterialButton startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(v -> {
            // Navigate to phone authentication screen
            Intent intent = new Intent(this, PhoneAuthActivity.class);
            startActivity(intent);
            finish(); // Close onboarding activity
        });
    }
}
