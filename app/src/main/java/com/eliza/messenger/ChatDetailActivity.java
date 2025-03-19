package com.eliza.messenger;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eliza.messenger.databinding.ActivityChatDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    private ActivityChatDetailBinding binding;
    private String chatId;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Get chat ID from intent
        chatId = getIntent().getStringExtra("CHAT_ID");
        if (chatId == null) {
            Log.e(TAG, "No chat ID provided");
            Toast.makeText(this, "Error: Could not load chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Loading chat details for chat: " + chatId);
        
        // Set up toolbar with back button
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Set up click listeners
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Load chat details
        loadChatDetails();
    }
    
    private void loadChatDetails() {
        // Load chat document from Firestore
        db.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Placeholder for now - we'll implement full chat functionality later
                    Toast.makeText(this, "Chat details loaded successfully", Toast.LENGTH_SHORT).show();
                    
                    boolean isGroup = documentSnapshot.getBoolean("isGroup") != null && 
                                      documentSnapshot.getBoolean("isGroup");
                    
                    if (isGroup) {
                        String groupName = documentSnapshot.getString("groupName");
                        binding.contactName.setText(groupName != null ? groupName : "Group Chat");
                    } else {
                        // This is a one-on-one chat, load the other user's information
                        loadOtherParticipantInfo(documentSnapshot.get("participants"));
                    }
                } else {
                    Log.e(TAG, "Chat document does not exist");
                    Toast.makeText(this, "Error: Chat not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading chat document: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading chat details", Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void loadOtherParticipantInfo(Object participants) {
        // Placeholder implementation - would normally extract the other participant's ID
        // and load their information from Firestore
        binding.contactName.setText("Chat");
        binding.contactStatus.setText("Online");
    }
}
