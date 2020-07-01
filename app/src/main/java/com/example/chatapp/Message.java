package com.example.chatapp;

import com.google.firebase.Timestamp;

public class Message {
    String userId;
    String recevierId;
    String textMessage;
    String imageUri;
    long createdAt;

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Message() {
        createdAt = System.currentTimeMillis() / 1000;

    }

    public String getUserId() {
        return userId;
    }

    public String getRecevierId() {
        return recevierId;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRecevierId(String recevierId) {
        this.recevierId = recevierId;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
