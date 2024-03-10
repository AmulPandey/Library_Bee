package com.example.LibraryBee;

public class User {
    public String userId;
    public String email;
    public String username;
    public String phoneNumber;
    public String gender;
    public boolean isSubscribed; // Make this public

    // Add a default constructor
    public User() {
    }

    // Your existing constructor
    public User(String userId, String email, String username, String phoneNumber, String gender, boolean isSubscribed) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.isSubscribed = isSubscribed;
    }
}
