package com.eliza.messenger;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.eliza.messenger.model.Chat;
import java.util.List;
import java.util.ArrayList;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> navigateToProfile = new MutableLiveData<>(false);
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public HomeViewModel() {
        Log.d(TAG, "HomeViewModel initialized");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Chat>> getChats() {
        return chats;
    }

    public LiveData<Boolean> getNavigateToProfile() {
        return navigateToProfile;
    }

    public void loadChats() {
        Log.d(TAG, "Loading chats from Firestore");
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Loading chats for user: " + currentUserId);
            
            db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chats: " + error.getMessage(), error);
                        return;
                    }

                    List<Chat> chatList = new ArrayList<>();
                    if (value != null) {
                        Log.d(TAG, "Retrieved " + value.size() + " chats");
                        for (var doc : value) {
                            try {
                                Chat chat = doc.toObject(Chat.class);
                                chat.setId(doc.getId());
                                chatList.add(chat);
                                Log.d(TAG, "Added chat: " + chat.getId());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing chat document: " + e.getMessage(), e);
                            }
                        }
                    } else {
                        Log.w(TAG, "No chats found for user");
                    }
                    chats.setValue(chatList);
                    Log.d(TAG, "Updated chats LiveData with " + chatList.size() + " chats");
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadChats: " + e.getMessage(), e);
        }
    }

    public void navigateToProfile() {
        Log.d(TAG, "Navigating to profile");
        navigateToProfile.setValue(true);
    }

    public void onProfileNavigated() {
        Log.d(TAG, "Profile navigation completed");
        navigateToProfile.setValue(false);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel cleared");
    }
}
