package com.eliza.messenger.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Chat {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private Object lastMessageTime; // Can be either Timestamp or Long
    private String lastSenderId;
    private boolean isGroup;
    private String name;
    private String groupIcon;
    private int unreadCount;
    private String photoUrl;

    // Required empty constructor for Firestore
    public Chat() {}

    public Chat(String id, List<String> participants, String lastMessage, Object lastMessageTime,
                String lastSenderId, boolean isGroup, String name, String groupIcon, int unreadCount,
                String photoUrl) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastSenderId = lastSenderId;
        this.isGroup = isGroup;
        this.name = name;
        this.groupIcon = groupIcon;
        this.unreadCount = unreadCount;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Object getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Object lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Timestamp getLastMessageTimestamp() {
        if (lastMessageTime instanceof Timestamp) {
            return (Timestamp) lastMessageTime;
        } else if (lastMessageTime instanceof Long) {
            return new Timestamp(new Date((Long) lastMessageTime));
        }
        return null;
    }

    public void setLastMessageTimestamp(Timestamp timestamp) {
        this.lastMessageTime = timestamp;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getGroupName() {
        return this.name;
    }
}
