package com.eliza.messenger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eliza.messenger.adapter.ContactsAdapter;
import com.eliza.messenger.databinding.ActivityContactsBinding;
import com.eliza.messenger.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = "ContactsActivity";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ActivityContactsBinding binding;
    private ContactsAdapter adapter;
    private List<User> contacts = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.new_chat);
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Check and request permissions
        if (checkAndRequestPermissions()) {
            loadContacts();
        }

        // Set up search functionality
        setupSearch();
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_CONTACTS}, 
                    PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Contacts permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

        List<User> deviceContacts = new ArrayList<>();
        
        // Get content resolver
        Cursor cursor = getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            },
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    
                    // Clean up phone number (remove spaces and special characters)
                    phoneNumber = phoneNumber.replaceAll("\\D", "");
                    
                    if (phoneNumber.length() > 0) { // Only add contacts with valid phone numbers
                        User user = new User();
                        user.setFullName(name);
                        user.setPhoneNumber(phoneNumber);
                        deviceContacts.add(user);
                    }
                }
            } finally {
                cursor.close(); // Always close the cursor
            }
        }

        if (deviceContacts.isEmpty()) {
            showEmptyState();
        } else {
            contacts.clear();
            contacts.addAll(deviceContacts);
            adapter.notifyDataSetChanged();
            binding.progressBar.setVisibility(View.GONE);
        }
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
        participants.add(user.getPhoneNumber());

        binding.progressBar.setVisibility(View.VISIBLE);

        // Try to find an existing chat
        // ... (rest of the createOrOpenChat method remains the same)
    }

    private void createNewChat(User user, List<String> participants) {
        String chatId = "chat_" + currentUserId + "_" + user.getPhoneNumber();
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", participants);
        chatData.put("lastMessage", "");
        chatData.put("lastMessageTime", new com.google.firebase.Timestamp(System.currentTimeMillis() / 1000, 0));
        chatData.put("lastSenderId", currentUserId);
        chatData.put("isGroup", false);
        chatData.put("unreadCount", 0);

        // ... (rest of the createNewChat method remains the same)
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
