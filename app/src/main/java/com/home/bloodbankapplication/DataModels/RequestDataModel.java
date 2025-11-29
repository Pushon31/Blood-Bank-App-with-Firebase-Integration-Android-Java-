package com.home.bloodbankapplication.DataModels;

public class RequestDataModel {
    private String message;
    private String imageUrl;
    private long timestamp;

    // Public no‑arg constructor
    public RequestDataModel() {
    }

    // Optionally parameterized constructor
    public RequestDataModel(String message, String imageUrl, long timestamp) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters & setters
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
