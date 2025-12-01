package com.home.bloodbankapplication.DataModels;



public class BloodBank {
    private String id;
    private String name;
    private String address;
    private String contact;
    private String email;
    private double latitude;
    private double longitude;
    private boolean isAvailable;
    private String bloodTypes;

    // Default constructor required for Firestore
    public BloodBank() {}

    public BloodBank(String id, String name, String address, String contact, String email,
                     double latitude, double longitude, boolean isAvailable, String bloodTypes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
        this.bloodTypes = bloodTypes;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getBloodTypes() { return bloodTypes; }
    public void setBloodTypes(String bloodTypes) { this.bloodTypes = bloodTypes; }
}
