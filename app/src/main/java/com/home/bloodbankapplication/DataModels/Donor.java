package com.home.bloodbankapplication.DataModels;

public class Donor {
    private String name;
    private String city;
    private String mobile;
    private String bloodGroup;
    private String password;

    // Default constructor (MUST for Firebase)
    public Donor() {
    }

    public Donor(String name, String city, String mobile, String bloodGroup, String password) {
        this.name = name;
        this.city = city;
        this.mobile = mobile;
        this.bloodGroup = bloodGroup;
        this.password = password;
    }

    // Getters
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getMobile() { return mobile; }
    public String getBloodGroup() { return bloodGroup; }
    public String getPassword() { return password; }

    // Setters (required for Firebase)
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setPassword(String password) { this.password = password; }
}