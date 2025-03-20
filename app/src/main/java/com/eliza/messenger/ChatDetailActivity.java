package com.eliza.messenger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.eliza.messenger.adapter.MessagesAdapter;
import com.eliza.messenger.databinding.ActivityChatDetailBinding;
import com.eliza.messenger.model.Message;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    
    private ActivityChatDetailBinding binding;
    private String chatId;
    private String chatName;
    private String chatPhotoUrl;
    private boolean isGroup;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private MessagesAdapter messagesAdapter;
    private List<Message> messagesList = new ArrayList<>();
    private String currentUserId;
    private String otherUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        currentUserId = auth.getCurrentUser().getUid();
        
        // Get chat ID from intent
        chatId = getIntent().getStringExtra("chatId");
        chatName = getIntent().getStringExtra("chatName");
        chatPhotoUrl = getIntent().getStringExtra("chatPhotoUrl");
        
        if (chatId == null) {
            Log.e(TAG, "No chat ID provided");
            Toast.makeText(this, "Error: Could not load chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set up toolbar with chat information
        setupToolbar();
        
        // Set up RecyclerView for messages
        setupRecyclerView();
        
        // Set up click listeners for message input
        setupClickListeners();
        
        // Load messages for this chat
        loadMessages();
        
        // Mark chat as read
        markChatAsRead();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Set toolbar info
        if (!TextUtils.isEmpty(chatName)) {
            binding.contactName.setText(chatName);
        }
        
        if (!TextUtils.isEmpty(chatPhotoUrl)) {
            Glide.with(this)
                .load(chatPhotoUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(binding.contactImage);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }
    
    private void setupRecyclerView() {
        messagesAdapter = new MessagesAdapter(this, messagesList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show newest messages at the bottom
        binding.messagesRecyclerView.setLayoutManager(layoutManager);
        binding.messagesRecyclerView.setAdapter(messagesAdapter);
    }
    
    private void setupClickListeners() {
        // Send button
        binding.sendButton.setOnClickListener(v -> {
            String messageText = binding.messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendTextMessage(messageText);
                binding.messageInput.setText("");
            }
        });
        
        // Attachment button
        binding.attachButton.setOnClickListener(v -> {
            showAttachmentOptions();
        });
    }
    
    private void loadMessages() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading messages: " + error.getMessage(), error);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                
                if (value != null) {
                    messagesList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                        Message message = doc.toObject(Message.class);
                        if (message != null) {
                            message.setId(doc.getId());
                            messagesList.add(message);
                        }
                    }
                    
                    messagesAdapter.notifyDataSetChanged();
                    if (messagesList.size() > 0) {
                        binding.messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                        binding.noMessagesText.setVisibility(View.GONE);
                    } else {
                        binding.noMessagesText.setVisibility(View.VISIBLE);
                    }
                }
                
                binding.progressBar.setVisibility(View.GONE);
            });
            
        // Get other participant info
        loadChatDetails();
    }
    
    private void loadChatDetails() {
        db.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isGroup = documentSnapshot.getBoolean("isGroup") != null && 
                              documentSnapshot.getBoolean("isGroup");
                    
                    if (isGroup) {
                        String groupName = documentSnapshot.getString("name");
                        if (!TextUtils.isEmpty(groupName)) {
                            binding.contactName.setText(groupName);
                        }
                        binding.contactStatus.setText("Group Chat");
                    } else {
                        // One-on-one chat, load other participant
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && participants.size() > 0) {
                            for (String participantId : participants) {
                                if (!participantId.equals(currentUserId)) {
                                    otherUserId = participantId;
                                    loadUserDetails(participantId);
                                    break;
                                }
                            }
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading chat details: " + e.getMessage(), e);
            });
    }
    
    private void loadUserDetails(String userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String status = documentSnapshot.getString("status");
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    
                    if (!TextUtils.isEmpty(name)) {
                        binding.contactName.setText(name);
                    }
                    
                    if (!TextUtils.isEmpty(status)) {
                        binding.contactStatus.setText(status);
                    } else {
                        binding.contactStatus.setText("Online");
                    }
                    
                    if (!TextUtils.isEmpty(photoUrl)) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .circleCrop()
                            .into(binding.contactImage);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user details: " + e.getMessage(), e);
            });
    }
    
    private void markChatAsRead() {
        db.collection("chats").document(chatId)
            .update("unreadCount", 0)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat marked as read"))
            .addOnFailureListener(e -> Log.e(TAG, "Error marking chat as read: " + e.getMessage(), e));
    }
    
    private void sendTextMessage(String messageText) {
        if (TextUtils.isEmpty(messageText)) return;
        
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", currentUserId);
        messageData.put("text", messageText);
        messageData.put("timestamp", new Timestamp(new Date()));
        messageData.put("isRead", false);
        messageData.put("type", "text");
        
        // Add message to firestore
        db.collection("chats").document(chatId)
            .collection("messages")
            .add(messageData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Message sent successfully");
                
                // Update last message in chat document
                updateChatLastMessage(messageText);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                Snackbar.make(binding.getRoot(), "Failed to send message", Snackbar.LENGTH_SHORT).show();
            });
    }
    
    private void updateChatLastMessage(String messageText) {
        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", messageText);
        chatUpdates.put("lastMessageTime", new Timestamp(new Date()));
        chatUpdates.put("lastSenderId", currentUserId);
        
        // If the other user has read all previous messages, set unread to 1
        // otherwise increment the unread count
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long unreadCount = documentSnapshot.getLong("unreadCount");
                if (unreadCount != null && currentUserId.equals(documentSnapshot.getString("lastSenderId"))) {
                    chatUpdates.put("unreadCount", unreadCount + 1);
                } else {
                    chatUpdates.put("unreadCount", 1);
                }
                
                chatRef.update(chatUpdates)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat last message updated"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating chat: " + e.getMessage(), e));
            }
        });
    }
    
    private void showAttachmentOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Send Media")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Take photo
                    if (checkCameraPermission()) {
                        takePhoto();
                    } else {
                        requestCameraPermission();
                    }
                } else if (which == 1) {
                    // Choose from gallery
                    if (checkStoragePermission()) {
                        pickImageFromGallery();
                    } else {
                        requestStoragePermission();
                    }
                }
            })
            .show();
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                REQUEST_CAMERA_PERMISSION);
    }
    
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                    REQUEST_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    REQUEST_STORAGE_PERMISSION);
        }
    }
    
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_PICK_IMAGE) {
                // For now just show a toast - in a real app would upload the image
                Toast.makeText(this, "Media sharing coming soon!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
