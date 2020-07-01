package com.example.chatapp;
import android.net.Uri;

import java.util.ArrayList;

public class UserData {
    String name;
    String email;
    String imageUri;
    String userId;
    String status;
    private long time;
    ArrayList<String> ImageStatus = new ArrayList<>();

    public UserData(String username, String email, String uri, String toString, String ...status) {
        this.name = username;
        this.email = email;
        this.imageUri = uri;
        this.userId = toString;
        this.status = status[0];
    }

    public UserData(){
    }

    public String getEmail() {
        return email;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public void addImageStatus(String imageUri){
        ImageStatus.add(imageUri);
    }

    public ArrayList<String> getImageStatus(){
        return ImageStatus;
    }

    public void setImageStatus(ArrayList<String> ImageStatus){
        this.ImageStatus = ImageStatus;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
