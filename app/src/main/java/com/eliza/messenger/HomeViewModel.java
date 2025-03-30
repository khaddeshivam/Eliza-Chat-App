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
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void loadChats() {
        Log.d(TAG, "Loading chats from Firestore");
        isLoading.setValue(true);
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Loading chats for user: " + currentUserId);
            
            db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chats: " + error.getMessage(), error);
                        errorMessage.setValue("Failed to load chats: " + error.getMessage());
                        isLoading.setValue(false);
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
                                errorMessage.setValue("Error loading chat data");
                            }
                        }
                    } else {
                        Log.w(TAG, "No chats found for user");
                    }
                    chats.setValue(chatList);
                    isLoading.setValue(false);
                    Log.d(TAG, "Updated chats LiveData with " + chatList.size() + " chats");
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadChats: " + e.getMessage(), e);
            errorMessage.setValue("Failed to load chats");
            isLoading.setValue(false);
        }
    }

    public void deleteChat(String chatId) {
        if (chatId == null) return;
        
        isLoading.setValue(true);
        db.collection("chats").document(chatId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Chat successfully deleted: " + chatId);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting chat: " + e.getMessage(), e);
                errorMessage.setValue("Failed to delete chat");
                isLoading.setValue(false);
            });
    }

    public void undoDeleteChat(Chat chat) {
        if (chat == null) return;
        
        isLoading.setValue(true);
        db.collection("chats").document(chat.getId())
            .set(chat)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Chat successfully restored: " + chat.getId());
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error restoring chat: " + e.getMessage(), e);
                errorMessage.setValue("Failed to restore chat");
                isLoading.setValue(false);
            });
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
