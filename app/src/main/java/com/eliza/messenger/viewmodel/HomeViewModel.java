package com.eliza.messenger.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.eliza.messenger.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.ArrayList;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToProfile = new MutableLiveData<>(false);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public LiveData<List<Chat>> getChats() {
        return chats;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getNavigateToProfile() {
        return navigateToProfile;
    }

    public void loadChats() {
        isLoading.setValue(true);
        String currentUserId = auth.getCurrentUser().getUid();
        
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Chat> chatList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Chat chat = document.toObject(Chat.class);
                    chat.setId(document.getId());
                    chatList.add(chat);
                }
                chats.setValue(chatList);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to load chats: " + e.getMessage());
                isLoading.setValue(false);
            });
    }

    public void deleteChat(String chatId) {
        db.collection("chats").document(chatId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                List<Chat> currentChats = chats.getValue();
                if (currentChats != null) {
                    currentChats.removeIf(chat -> chat.getId().equals(chatId));
                    chats.setValue(currentChats);
                }
            })
            .addOnFailureListener(e -> 
                errorMessage.setValue("Failed to delete chat: " + e.getMessage())
            );
    }

    public void undoDeleteChat(Chat chat) {
        db.collection("chats").document(chat.getId())
            .set(chat)
            .addOnSuccessListener(aVoid -> {
                List<Chat> currentChats = chats.getValue();
                if (currentChats != null) {
                    currentChats.add(chat);
                    chats.setValue(currentChats);
                }
            })
            .addOnFailureListener(e -> 
                errorMessage.setValue("Failed to restore chat: " + e.getMessage())
            );
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void onProfileNavigated() {
        navigateToProfile.setValue(false);
    }

    public void navigateToProfile() {
        navigateToProfile.setValue(true);
    }
}
