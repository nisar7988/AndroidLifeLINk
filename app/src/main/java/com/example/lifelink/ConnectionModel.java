package com.example.lifelink;

public class ConnectionModel {
    private String id;
    private String name;
    private String bloodType;
    private String distance;
    private boolean isConnected;
    private String profileImageUrl;

    public ConnectionModel() {
        // Required empty constructor for Firestore
    }

    public ConnectionModel(String id, String name, String bloodType, String distance, boolean isConnected) {
        this.id = id;
        this.name = name;
        this.bloodType = bloodType;
        this.distance = distance;
        this.isConnected = isConnected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
} 