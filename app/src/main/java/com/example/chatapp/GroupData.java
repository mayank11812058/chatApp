package com.example.chatapp;

import java.util.ArrayList;

public class GroupData {
    String title;
    String imageUri;
    String groupId;
    ArrayList<String> members;

    public GroupData(){
    }

    public GroupData(String title, String imageUri, String groupId, ArrayList<String> members) {
        this.title = title;
        this.imageUri = imageUri;
        this.groupId = groupId;
        this.members = members;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getGroupId() {
        return groupId;
    }

    public ArrayList<String> getMembers() {
        return members;
    }
}
