package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        TextView textView = findViewById(R.id.splash_text);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        textView.startAnimation(fadeIn);
        textView.setVisibility(TextView.VISIBLE);

        TextView splashscreen_text = findViewById(R.id.splashscreen_text);
        Animation fadeInout = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashscreen_text.startAnimation(fadeInout);
        splashscreen_text.setVisibility(TextView.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        }, 2000); // 2 seconds
    }
}
