package com.eliza.messenger;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eliza.messenger.adapter.ContactsAdapter;
import com.eliza.messenger.databinding.ActivityContactsBinding;
import com.eliza.messenger.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = "ContactsActivity";
    private ActivityContactsBinding binding;
    private ContactsAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private List<User> contacts = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.new_chat);
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Load contacts
        loadContacts();

        // Set up search functionality
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new ContactsAdapter(this, contacts);
        binding.contactsList.setLayoutManager(new LinearLayoutManager(this));
        binding.contactsList.setAdapter(adapter);

        adapter.setOnContactClickListener(user -> {
            createOrOpenChat(user);
        });
    }

    private void loadContacts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);

        db.collection("users")
                .whereNotEqualTo("id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    contacts.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                    } else {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            User user = documentSnapshot.toObject(User.class);
                            user.setId(documentSnapshot.getId());
                            contacts.add(user);
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading contacts: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading contacts", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.contactsList.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void setupSearch() {
        binding.searchInput.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterContacts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
    }

    private void filterContacts(String query) {
        List<User> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(contacts);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (User user : contacts) {
                if (user.getFullName().toLowerCase().contains(lowercaseQuery) ||
                        (user.getPhoneNumber() != null && user.getPhoneNumber().contains(query))) {
                    filteredList.add(user);
                }
            }
        }
        adapter.updateData(filteredList);
    }

    private void createOrOpenChat(User user) {
        // Check if a chat already exists
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(user.getId());

        binding.progressBar.setVisibility(View.VISIBLE);

        // Try to find an existing chat
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean chatExists = false;
                    String existingChatId = null;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> chatParticipants = (List<String>) document.get("participants");
                        if (chatParticipants != null && chatParticipants.contains(user.getId())) {
                            chatExists = true;
                            existingChatId = document.getId();
                            break;
                        }
                    }

                    if (chatExists && existingChatId != null) {
                        // Open existing chat
                        openChatDetail(existingChatId);
                    } else {
                        // Create a new chat
                        createNewChat(user, participants);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing chat: " + e.getMessage(), e);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error creating chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewChat(User user, List<String> participants) {
        String chatId = "chat_" + currentUserId + "_" + user.getId();
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", participants);
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", new com.google.firebase.Timestamp(System.currentTimeMillis() / 1000, 0));
        chatData.put("lastSenderId", currentUserId);
        chatData.put("isGroup", false);
        chatData.put("unreadCount", 0);

        db.collection("chats").document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    openChatDetail(chatId);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error creating new chat: " + e.getMessage(), e);
                    Toast.makeText(this, "Error creating chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void openChatDetail(String chatId) {
        android.content.Intent intent = new android.content.Intent(this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
