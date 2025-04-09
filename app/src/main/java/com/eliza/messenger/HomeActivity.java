package com.eliza.messenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eliza.messenger.adapter.ChatAdapter;
import com.eliza.messenger.databinding.ActivityHomeBinding;
import com.eliza.messenger.model.Chat;
import com.eliza.messenger.util.SwipeToDeleteCallback;
import com.eliza.messenger.viewmodel.HomeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ActivityHomeBinding binding;
    private ChatAdapter chatAdapter;
    private HomeViewModel viewModel;
    private List<Chat> filteredChatList = new ArrayList<>();
    
    private String[] requiredPermissions = {
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            // Redirect to login flow
            startActivity(new Intent(this, OnboardingActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set up UI components
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Request permissions
        if (!checkPermissions()) {
            requestPermissions();
        }

        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        setupSearchView();
        setupSwipeActions();

        // Observe ViewModel data
        observeViewModel();

        // For testing purposes - generate chats if none exist
        loadOrGenerateTestData();
    }
    
    private boolean checkPermissions() {
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions were denied. Some features may not work properly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        chatAdapter = new ChatAdapter(this);
        chatAdapter.setOnChatClickListener(chat -> {
            // Navigate to chat detail screen
            Intent intent = new Intent(HomeActivity.this, ChatDetailActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("chatName", chat.getName());
            intent.putExtra("chatPhotoUrl", chat.getPhotoUrl());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.chatList.setAdapter(chatAdapter);
        binding.chatList.setLayoutManager(new LinearLayoutManager(this));

        // Add scroll listener for FAB show/hide behavior
        binding.chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && binding.fabNewChat.isShown()) {
                    binding.fabNewChat.hide();
                } else if (dy < 0 && !binding.fabNewChat.isShown()) {
                    binding.fabNewChat.show();
                }
            }
        });

        // Apply layout animation when the list is first shown
        binding.chatList.scheduleLayoutAnimation();
    }

    private void setupClickListeners() {
        // Set up FAB click listener
        binding.fabNewChat.setOnClickListener(v -> {
            // Apply a quick scale animation to the FAB
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            
            // Navigate to contacts selection
            Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navChats) {
                // Already on chats screen
                return true;
            } else if (itemId == R.id.navCalls) {
                // Navigate to calls screen
                Intent intent = new Intent(this, CallsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.navProfile) {
                // Navigate to profile screen
                Intent intent = new Intent(this, ProfileSetupActivity.class);
                intent.putExtra("fromHome", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });

        // Set the initial selected item
        binding.bottomNavigation.setSelectedItemId(R.id.navChats);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterChats(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterChats(newText);
                return true;
            }
        });
    }

    private void setupSwipeActions() {
        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Chat swipedChat = chatAdapter.getChatAt(position);

                // Remove item from adapter immediately for better UX
                if (filteredChatList.size() > position) {
                    filteredChatList.remove(position);
                    chatAdapter.updateData(filteredChatList);
                }

                // Show undo snackbar
                Snackbar.make(binding.getRoot(), R.string.chat_deleted, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.bottomNavigation)
                        .setAction(R.string.undo, v -> {
                            // Restore the chat in the database
                            viewModel.undoDeleteChat(swipedChat);

                            // Add it back to our filtered list
                            if (!filteredChatList.contains(swipedChat)) {
                                filteredChatList.add(position, swipedChat);
                                chatAdapter.updateData(filteredChatList);
                                checkEmptyState(filteredChatList);
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    // Only delete if user didn't press UNDO
                                    viewModel.deleteChat(swipedChat.getId());
                                }
                            }
                        })
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(binding.chatList);
    }

    private void observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers");

        viewModel.getNavigateToProfile().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                Log.d(TAG, "Navigating to profile screen");
                startActivity(new Intent(this, ProfileSetupActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                viewModel.onProfileNavigated();
            }
        });

        viewModel.getChats().observe(this, chats -> {
            Log.d(TAG, "Received chat list update with " + (chats != null ? chats.size() : 0) + " chats");

            // If search is active, filter the results
            if (binding.searchView.getQuery().length() > 0) {
                filterChats(binding.searchView.getQuery().toString());
            } else {
                chatAdapter.updateData(chats);
                // Re-run layout animation when data changes
                binding.chatList.scheduleLayoutAnimation();
                checkEmptyState(chats);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMsg, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.bottomNavigation)
                        .setAction(R.string.retry, v -> viewModel.loadChats())
                        .show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void checkEmptyState(List<Chat> chats) {
        boolean isEmpty = chats == null || chats.isEmpty();

        if (isEmpty) {
            binding.chatList.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);

            // Add animation to empty state
            binding.emptyStateLayout.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.fade_in));
        } else {
            binding.chatList.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void filterChats(String query) {
        if (viewModel.getChats().getValue() == null) return;

        List<Chat> allChats = viewModel.getChats().getValue();
        filteredChatList.clear();

        if (query.isEmpty()) {
            filteredChatList.addAll(allChats);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (Chat chat : allChats) {
                if ((chat.getName() != null && chat.getName().toLowerCase().contains(lowercaseQuery)) ||
                        (chat.getLastMessage() != null && chat.getLastMessage().toLowerCase().contains(lowercaseQuery))) {
                    filteredChatList.add(chat);
                }
            }
        }

        chatAdapter.updateData(filteredChatList);
        checkEmptyState(filteredChatList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshChats();
            return true;
        } else if (id == R.id.action_sign_out) {
            showSignOutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshChats() {
        // Show progress indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        // Apply rotate animation to the refresh icon if possible
        try {
            MenuItem refreshItem = binding.toolbar.getMenu().findItem(R.id.action_refresh);
            if (refreshItem != null && refreshItem.getIcon() != null) {
                refreshItem.getIcon().setTint(getResources().getColor(R.color.primary, getTheme()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error animating refresh icon", e);
        }

        // Reload chats from database
        viewModel.loadChats();

        // Hide progress indicator after a delay
        new Handler().postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Reset icon tint if needed
            try {
                MenuItem refreshItem = binding.toolbar.getMenu().findItem(R.id.action_refresh);
                if (refreshItem != null && refreshItem.getIcon() != null) {
                    refreshItem.getIcon().setTintList(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error resetting refresh icon", e);
            }
        }, 1500);
    }

    private void showSignOutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_confirmation)
                .setPositiveButton(R.string.sign_out, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, OnboardingActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadOrGenerateTestData() {
        // Check if we already have chats
        db.collection("chats")
            .whereArrayContains("participants", auth.getCurrentUser().getUid())
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isEmpty()) {
                    // No chats found, generate test data
                    generateTestData();
                } else {
                    // Chats exist, just load them
                    viewModel.loadChats();
                }
            });
    }

    private void generateTestData() {
        // Show a loading toast
        Toast.makeText(this, "Generating test data...", Toast.LENGTH_SHORT).show();

        // Create test contacts and chats
        createTestChats();
    }

    private void createTestChats() {
        List<Map<String, Object>> testContacts = new ArrayList<>();

        // Create a variety of realistic test contacts
        Map<String, Object> contact1 = new HashMap<>();
        contact1.put("name", "Alice Johnson");
        contact1.put("phone", "+1234567890");
        contact1.put("photoUrl", "https://randomuser.me/api/portraits/women/44.jpg");
        contact1.put("status", "Living every moment");
        testContacts.add(contact1);

        Map<String, Object> contact2 = new HashMap<>();
        contact2.put("name", "Bob Smith");
        contact2.put("phone", "+1987654321");
        contact2.put("photoUrl", "https://randomuser.me/api/portraits/men/32.jpg");
        contact2.put("status", "At work");
        testContacts.add(contact2);

        Map<String, Object> contact3 = new HashMap<>();
        contact3.put("name", "Carol Taylor");
        contact3.put("phone", "+1555666777");
        contact3.put("photoUrl", "https://randomuser.me/api/portraits/women/67.jpg");
        contact3.put("status", "Busy");
        testContacts.add(contact3);
        
        Map<String, Object> contact4 = new HashMap<>();
        contact4.put("name", "David Miller");
        contact4.put("phone", "+1333444555");
        contact4.put("photoUrl", "https://randomuser.me/api/portraits/men/75.jpg");
        contact4.put("status", "Available");
        testContacts.add(contact4);
        
        Map<String, Object> contact5 = new HashMap<>();
        contact5.put("name", "Emma Wilson");
        contact5.put("phone", "+1222888999");
        contact5.put("photoUrl", "https://randomuser.me/api/portraits/women/22.jpg");
        contact5.put("status", "Hey there! I'm using Eliza");
        testContacts.add(contact5);
        
        Map<String, Object> contact6 = new HashMap<>();
        contact6.put("name", "Frank Davis");
        contact6.put("phone", "+1777888999");
        contact6.put("photoUrl", "https://randomuser.me/api/portraits/men/41.jpg");
        contact6.put("status", "In a meeting");
        testContacts.add(contact6);
        
        Map<String, Object> contact7 = new HashMap<>();
        contact7.put("name", "Family Group");
        contact7.put("phone", "group:family123");
        contact7.put("photoUrl", "https://img.icons8.com/color/96/000000/conference-call.png");
        contact7.put("isGroup", true);
        contact7.put("status", "Family discussions");
        testContacts.add(contact7);

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : db.collection("users").document().getId();

        // Last messages for realistic conversation snippets
        String[] lastMessages = {
            "Hello there! How are you doing today?",
            "Can we meet tomorrow for coffee?",
            "I'll send you the documents soon",
            "Did you see the match last night?",
            "Happy Birthday! \uD83C\uDF82\uD83C\uDF89",
            "Here's the location for our meeting",
            "Check out this photo I took yesterday",
            "Thanks for your help!",
            "Let me know when you're free to talk",
            "I just sent you an email about the project",
            "Don't forget about our lunch tomorrow",
            "Have a great weekend! \uD83D\uDE0A",
            "Are you coming to the party tonight?",
            "I missed your call, I'll call you back"
        };

        // Random timeframes for last message times
        long[] timeOffsets = {
            1000 * 60 * 5,       // 5 minutes ago
            1000 * 60 * 30,      // 30 minutes ago
            1000 * 60 * 60,      // 1 hour ago
            1000 * 60 * 60 * 3,  // 3 hours ago
            1000 * 60 * 60 * 12, // 12 hours ago
            1000 * 60 * 60 * 24, // 1 day ago
            1000 * 60 * 60 * 48  // 2 days ago
        };

        for (Map<String, Object> contact : testContacts) {
            // Create a unique ID for this test contact
            String contactId = db.collection("users").document().getId();

            // Add contact to users collection
            db.collection("users").document(contactId)
                    .set(contact)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Test contact created: " + contact.get("name")))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating test contact", e));

            // Create a chat between current user and this contact
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("name", contact.get("name"));
            chatData.put("photoUrl", contact.get("photoUrl"));
            chatData.put("participants", new ArrayList<String>() {{
                add(currentUserId);
                add(contactId);
            }});
            
            // Generate random last message and time
            String lastMessage = lastMessages[new Random().nextInt(lastMessages.length)];
            long lastMessageTime = System.currentTimeMillis() - timeOffsets[new Random().nextInt(timeOffsets.length)];
            int unreadCount = new Random().nextInt(6); // 0-5 unread messages
            
            chatData.put("lastMessage", lastMessage);
            chatData.put("lastMessageTime", lastMessageTime);
            chatData.put("unreadCount", unreadCount);
            chatData.put("isGroup", contact.containsKey("isGroup") && (boolean)contact.get("isGroup"));

            db.collection("chats").document()
                    .set(chatData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Test chat created with " + contact.get("name"));
                        // After creating all test data, refresh the UI
                        viewModel.loadChats();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating test chat", e));
        }
        
        // Show success message after brief delay
        new Handler().postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), "Test conversations created", Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNavigation)
                    .show();
        }, 1500);
    }
}
