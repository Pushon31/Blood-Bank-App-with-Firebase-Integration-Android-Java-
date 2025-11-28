package com.home.bloodbankapplication.DataModels;

public class RequestDataModel {
   private String message;
   private String imageUrl;

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

    public RequestDataModel(String imageUrl, String message) {
        this.imageUrl = imageUrl;
        this.message = message;
    }
}
