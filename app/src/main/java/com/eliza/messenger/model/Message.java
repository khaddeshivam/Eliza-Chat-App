package com.eliza.messenger.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import java.util.Objects;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private String mediaUrl;
    private Timestamp timestamp;
    private String status; // sent, delivered, read
    private boolean isRead;
    private boolean isTyping;
    private String typingUserId;
    private String type; // text, image, video, etc.

    // Required empty constructor for Firestore
    public Message() {
    }

    public Message(String senderId, String text, Timestamp timestamp, boolean isRead, String type, String status, boolean isTyping, String typingUserId) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.type = type;
        this.status = status;
        this.isTyping = isTyping;
        this.typingUserId = typingUserId;
    }

    private Message(Builder builder) {
        this.id = builder.id;
        this.senderId = builder.senderId;
        this.text = builder.text;
        this.mediaUrl = builder.mediaUrl;
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.isRead = builder.isRead;
        this.isTyping = builder.isTyping;
        this.typingUserId = builder.typingUserId;
        this.type = builder.type;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @NonNull
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

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    @Nullable
    public String getTypingUserId() {
        return typingUserId;
    }

    public void setTypingUserId(String typingUserId) {
        this.typingUserId = typingUserId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", text='" + text + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", isRead=" + isRead +
                ", isTyping=" + isTyping +
                ", typingUserId='" + typingUserId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return isRead == message.isRead &&
                isTyping == message.isTyping &&
                Objects.equals(id, message.id) &&
                Objects.equals(senderId, message.senderId) &&
                Objects.equals(text, message.text) &&
                Objects.equals(mediaUrl, message.mediaUrl) &&
                Objects.equals(timestamp, message.timestamp) &&
                Objects.equals(status, message.status) &&
                Objects.equals(typingUserId, message.typingUserId) &&
                Objects.equals(type, message.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, text, mediaUrl, timestamp, status, isRead, isTyping, typingUserId, type);
    }

    public static class Builder {
        private String id;
        private String senderId;
        private String text;
        private String mediaUrl;
        private Timestamp timestamp;
        private String status;
        private boolean isRead;
        private boolean isTyping;
        private String typingUserId;
        private String type;

        public Builder(String senderId, Timestamp timestamp) {
            this.senderId = senderId;
            this.timestamp = timestamp;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Builder isTyping(boolean isTyping) {
            this.isTyping = isTyping;
            return this;
        }

        public Builder typingUserId(String typingUserId) {
            this.typingUserId = typingUserId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}
