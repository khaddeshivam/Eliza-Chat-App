package com.eliza.messenger;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.eliza.messenger.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ActivityPrivacyPolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Policy & Terms");
        }

        loadPrivacyPolicy();
    }

    private void loadPrivacyPolicy() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.loadUrl("file:///android_asset/privacy_policy.html");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}