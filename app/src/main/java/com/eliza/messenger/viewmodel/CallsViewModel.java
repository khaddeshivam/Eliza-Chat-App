package com.eliza.messenger.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.eliza.messenger.model.Call;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class CallsViewModel extends AndroidViewModel {
    private static final String TAG = "CallsViewModel";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseUser user;

    private final MutableLiveData<List<Call>> recentCalls;
    private final MutableLiveData<List<Call>> favorites;
    private final MutableLiveData<List<Call>> calls = new MutableLiveData<>();
    private final MutableLiveData<Call.Status> callStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSpeakerOn = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isVideoOn = new MutableLiveData<>(false);

    private boolean isMuted;

    public CallsViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        recentCalls = new MutableLiveData<>(new ArrayList<>());
        favorites = new MutableLiveData<>(new ArrayList<>());
        isMuted = false;

        // Load favorites and recent calls when ViewModel is created
        loadFavorites();
        loadRecentCalls();
    }

    public LiveData<List<Call>> getRecentCalls() {
        return recentCalls;
    }

    public LiveData<List<Call>> getFavorites() {
        return favorites;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void toggleMute() {
        isMuted = !isMuted;
    }

    public LiveData<List<Call>> getCalls() {
        return calls;
    }

    public LiveData<Call.Status> getCallStatus() {
        return callStatus;
    }

    public LiveData<Boolean> isSpeakerOn() {
        return isSpeakerOn;
    }

    public LiveData<Boolean> isVideoOn() {
        return isVideoOn;
    }

    public void toggleSpeaker() {
        Boolean current = isSpeakerOn.getValue();
        isSpeakerOn.setValue(current != null ? !current : true);
    }

    public void toggleVideo() {
        Boolean current = isVideoOn.getValue();
        isVideoOn.setValue(current != null ? !current : true);
    }

    public void selectCall(Call call) {
        // Update the selected call status
        callStatus.setValue(call.getStatus());
        
        // Update the calls list
        List<Call> currentCalls = calls.getValue();
        if (currentCalls != null) {
            for (Call c : currentCalls) {
                if (c.getId().equals(call.getId())) {
                    c.setStatus(call.getStatus());
                    break;
                }
            }
            calls.setValue(currentCalls);
        }

        // Handle initial state for new calls
        if (call.getStatus() == Call.Status.INCOMING) {
            isSpeakerOn.setValue(true);
            isVideoOn.setValue(true);
        } else if (call.getStatus() == Call.Status.ENDED) {
            isSpeakerOn.setValue(false);
            isVideoOn.setValue(false);
        }
    }

    public void startCallService() {
        // Start WebRTC service
    }

    public void stopCallService() {
        // Stop WebRTC service
    }

    public void endCall() {
        // End current call
    }

    private void loadFavorites() {
        if (user == null) {
            return;
        }

        db.collection("users")
            .document(user.getUid())
            .collection("favorites")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen failed.", e);
                    return;
                }

                List<Call> favoriteCalls = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    Call call = doc.toObject(Call.class);
                    if (call != null) {
                        favoriteCalls.add(call);
                    }
                }
                favorites.setValue(favoriteCalls);
            });
    }

    private void loadRecentCalls() {
        if (user == null) {
            return;
        }

        db.collection("calls")
            .whereEqualTo("userId", user.getUid())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    android.util.Log.w(TAG, "Listen failed.", e);
                    return;
                }

                List<Call> recentCallsList = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    Call call = doc.toObject(Call.class);
                    if (call != null) {
                        recentCallsList.add(call);
                    }
                }
                recentCalls.setValue(recentCallsList);
            });
    }

    public void addToFavorites(Call call) {
        if (user == null || call == null) {
            return;
        }

        db.collection("users")
            .document(user.getUid())
            .collection("favorites")
            .document(call.getId())
            .set(call)
            .addOnSuccessListener(aVoid -> {
                // Success
            })
            .addOnFailureListener(e -> {
                android.util.Log.w(TAG, "Error adding to favorites", e);
            });
    }

    public void removeFromFavorites(Call call) {
        if (user == null || call == null) {
            return;
        }

        db.collection("users")
            .document(user.getUid())
            .collection("favorites")
            .document(call.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Success
            })
            .addOnFailureListener(e -> {
                android.util.Log.w(TAG, "Error removing from favorites", e);
            });
    }

    public boolean isFavorite(Call call) {
        List<Call> currentFavorites = favorites.getValue();
        if (currentFavorites != null) {
            for (Call fav : currentFavorites) {
                if (fav.getId().equals(call.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}