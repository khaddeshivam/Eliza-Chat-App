package com.eliza.messenger.model;

import com.google.firebase.Timestamp;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private String mediaUrl;
    private Timestamp timestamp;
    private boolean isRead;
    private String type; // text, image, video, etc.

    // Required empty constructor for Firestore
    public Message() {
    }

    public Message(String senderId, String text, Timestamp timestamp, boolean isRead, String type) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", text='" + text + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                ", type='" + type + '\'' +
                '}';
    }
}
