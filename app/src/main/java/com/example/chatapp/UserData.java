package com.example.chatapp;
import android.net.Uri;

import java.util.ArrayList;

public class UserData {
    String name;
    String email;
    String imageUri;
    String userId;
    String groupId;
    String title;
    ArrayList<String> members;

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String status;
    private long time;
    ArrayList<String> ImageStatus = new ArrayList<>();

    public UserData(String username, String email, String uri, String toString, String ...status) {
        this.name = username;
        this.email = email;
        this.imageUri = uri;
        this.userId = toString;

        if(status != null && status.length > 0) {
            this.status = status[0];
        }
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
