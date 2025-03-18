package com.eliza.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.eliza.messenger.util.TestCredentials;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView chatList;
    private FloatingActionButton newChatButton;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        Log.d(TAG, "onCreate: Initializing Home screen");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        Log.d(TAG, "ViewModel initialized");

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup bottom navigation
        setupBottomNavigation();

        // Observe view model
        observeViewModel();

        // Load chat list
        viewModel.loadChats();
        Log.d(TAG, "Requested chat list loading");
        
        // Check if using test credentials and show indicator if needed
        checkTestCredentials();
    }
    
    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        chatList = findViewById(R.id.chat_list);
        if (chatList == null) {
            Log.e(TAG, "Chat list RecyclerView not found");
        }
        
        newChatButton = findViewById(R.id.fab_new_chat);
        if (newChatButton == null) {
            Log.e(TAG, "New chat FAB not found");
        }
    }
    
    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        if (newChatButton != null) {
            newChatButton.setOnClickListener(v -> {
                Log.d(TAG, "New chat button clicked");
                // TODO: Implement new chat functionality
            });
        }
    }
    
    private void setupBottomNavigation() {
        Log.d(TAG, "Setting up bottom navigation");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Log.d(TAG, "Bottom navigation item selected: " + item.getTitle());
                
                if (itemId == R.id.nav_chats) {
                    Log.d(TAG, "Chats tab selected");
                    // Already on chats
                    return true;
                } else if (itemId == R.id.nav_calls) {
                    Log.d(TAG, "Calls tab selected");
                    // TODO: Implement calls screen
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Log.d(TAG, "Profile tab selected, navigating to profile");
                    viewModel.navigateToProfile();
                    return true;
                }
                return false;
            });
        } else {
            Log.e(TAG, "Bottom navigation view not found");
        }
    }

    private void observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers");
        
        viewModel.getNavigateToProfile().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                Log.d(TAG, "Navigating to profile screen");
                startActivity(new Intent(this, ProfileSetupActivity.class));
                viewModel.onProfileNavigated();
            }
        });

        viewModel.getChats().observe(this, chats -> {
            Log.d(TAG, "Received chat list update with " + chats.size() + " chats");
            // TODO: Update RecyclerView adapter with chats
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show test options menu in debug builds
        if (getResources().getBoolean(R.bool.is_debug_build)) {
            getMenuInflater().inflate(R.menu.menu_home, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_generate_test_data) {
            generateTestData();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void checkTestCredentials() {
        if (auth.getCurrentUser() != null) {
            String phoneNumber = auth.getCurrentUser().getPhoneNumber();
            if (TestCredentials.USE_TEST_CREDENTIALS && TestCredentials.isTestPhoneNumber(phoneNumber)) {
                Log.d(TAG, "Using test credentials with phone: " + phoneNumber);
                Toast.makeText(this, getString(R.string.using_test_credentials), Toast.LENGTH_SHORT).show();
                
                // If we're using test credentials and there are no chats, generate some test data
                if (getResources().getBoolean(R.bool.is_debug_build)) {
                    viewModel.getChats().observe(this, chats -> {
                        if (chats.isEmpty()) {
                            generateTestData();
                        }
                    });
                }
            }
        }
    }
    
    private void generateTestData() {
        Log.d(TAG, "Generating test data");
        Toast.makeText(this, "Generating test data...", Toast.LENGTH_SHORT).show();
        
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot generate test data: User not logged in");
            return;
        }
        
        String currentUserId = auth.getCurrentUser().getUid();
        
        // Create test contacts
        for (String[] contact : TestCredentials.TEST_CONTACTS) {
            String name = contact[0];
            String phoneNumber = contact[1];
            
            // Create a test user document
            String userId = "test_" + phoneNumber.replaceAll("[^0-9]", "");
            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", name.split(" ")[0]);
            userData.put("lastName", name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "");
            userData.put("phoneNumber", phoneNumber);
            userData.put("profileImage", TestCredentials.getTestProfileImageUrl(
                    name.split(" ")[0], 
                    name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : ""));
            
            db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test user created: " + name);
                    
                    // Create a test chat between current user and this test user
                    String chatId = currentUserId.compareTo(userId) < 0 ? 
                            currentUserId + "_" + userId : userId + "_" + currentUserId;
                    
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("participants", new String[]{currentUserId, userId});
                    chatData.put("lastMessage", "Hello from " + name);
                    chatData.put("lastMessageTime", System.currentTimeMillis());
                    chatData.put("createdAt", System.currentTimeMillis());
                    
                    db.collection("chats").document(chatId)
                        .set(chatData)
                        .addOnSuccessListener(aVoid2 -> {
                            Log.d(TAG, "Test chat created with: " + name);
                            
                            // Add a test message
                            Map<String, Object> messageData = new HashMap<>();
                            messageData.put("senderId", userId);
                            messageData.put("text", "Hello from " + name);
                            messageData.put("timestamp", System.currentTimeMillis());
                            
                            db.collection("chats").document(chatId)
                                .collection("messages")
                                .add(messageData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Test message added to chat with: " + name);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding test message", e);
                                });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error creating test chat", e);
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating test user", e);
                });
        }
        
        // Refresh the chat list
        viewModel.loadChats();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Checking authentication state");
        
        // Check if user is signed in
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User not signed in, redirecting to Onboarding");
            // Navigate to OnboardingActivity
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        } else {
            Log.d(TAG, "User is signed in: " + auth.getCurrentUser().getUid());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Home screen resumed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Home screen paused");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Home screen destroyed");
    }
}
