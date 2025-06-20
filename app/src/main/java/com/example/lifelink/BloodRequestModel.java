package com.example.lifelink;

public class BloodRequestModel {
    private String id;
    private String bloodType;
    private String hospitalName;
    private boolean isUrgent;
    private String timeAgo;
    private String distance;

    public BloodRequestModel() {
        // Required empty constructor for Firestore
    }

    public BloodRequestModel(String id, String bloodType, String hospitalName, boolean isUrgent, String timeAgo, String distance) {
        this.id = id;
        this.bloodType = bloodType;
        this.hospitalName = hospitalName;
        this.isUrgent = isUrgent;
        this.timeAgo = timeAgo;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
} 