package com.example.lifelink;

public class ChatMessageModel {
    private String id;
    private String sender;
    private String receiver;
    private String text;
    private String timestamp;
    private boolean read;

    public ChatMessageModel() {
        // Empty constructor required for Firestore
    }

    public ChatMessageModel(String id, String sender, String receiver, String text, String timestamp, boolean read) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
} 