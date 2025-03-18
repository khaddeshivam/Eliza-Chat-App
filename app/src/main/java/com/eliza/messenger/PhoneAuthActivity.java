package com.eliza.messenger;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class PhoneAuthActivity extends AppCompatActivity {
    private EditText phoneNumberInput;
    private StringBuilder phoneNumber = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        phoneNumberInput = findViewById(R.id.phone_number);
        setupKeypad();
        setupContinueButton();
    }

    private void setupKeypad() {
        // Setup number buttons
        for (int i = 0; i <= 9; i++) {
            final int number = i;
            int buttonId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            MaterialButton button = findViewById(buttonId);
            button.setOnClickListener(v -> onNumberClick(String.valueOf(number)));
        }

        // Setup backspace button
        ImageButton backspaceButton = findViewById(R.id.btn_backspace);
        backspaceButton.setOnClickListener(v -> onBackspaceClick());
    }

    private void setupContinueButton() {
        MaterialButton continueButton = findViewById(R.id.button_continue);
        continueButton.setOnClickListener(v -> {
            String fullPhoneNumber = getString(R.string.country_code) + phoneNumber.toString();
            // TODO: Implement phone number verification
            // For now, just show a toast with the number
            android.widget.Toast.makeText(this, "Verifying: " + fullPhoneNumber, 
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void onNumberClick(String number) {
        if (phoneNumber.length() < 10) {
            phoneNumber.append(number);
            updatePhoneNumberDisplay();
        }
    }

    private void onBackspaceClick() {
        if (phoneNumber.length() > 0) {
            phoneNumber.deleteCharAt(phoneNumber.length() - 1);
            updatePhoneNumberDisplay();
        }
    }

    private void updatePhoneNumberDisplay() {
        phoneNumberInput.setText(phoneNumber.toString());
    }
}
