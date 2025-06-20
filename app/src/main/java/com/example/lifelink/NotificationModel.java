package com.example.lifelink;

public class NotificationModel {
    private String id;
    private String title;
    private String content;
    private String timeAgo;
    private int notificationType; // 0 = info, 1 = success, 2 = warning, 3 = emergency

    public NotificationModel() {
        // Required empty constructor for Firestore
    }

    public NotificationModel(String id, String title, String content, String timeAgo, int notificationType) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timeAgo = timeAgo;
        this.notificationType = notificationType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }
} 