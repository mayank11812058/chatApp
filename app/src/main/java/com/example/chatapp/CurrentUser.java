package com.example.chatapp;

public class CurrentUser {
    String username;
    String imageUri;
    String email;
    String userId;
    String status;

    public CurrentUser() {
    }

    public CurrentUser(String username, String imageUri, String email, String userId, String ...status) {
        this.username = username;
        this.imageUri = imageUri;
        this.email = email;
        this.userId = userId;

        if(status.length > 0) {
            this.status = status[0];
        }else{
            this.status = "";
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }
}
