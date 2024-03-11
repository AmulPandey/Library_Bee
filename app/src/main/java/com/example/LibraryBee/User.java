package com.example.LibraryBee;

public class User {
    public String userId;
    public String email;
    public String username;
    public String phoneNumber;
    public String gender;
    public boolean isSubscribed; // Make this public

    public long subscriptionTimestamp;

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
        this.subscriptionTimestamp = 0; // Initialize to 0 by default
    }

    public long getSubscriptionTimestamp() {
        return subscriptionTimestamp;
    }

    public void setSubscriptionTimestamp(long subscriptionTimestamp) {
        this.subscriptionTimestamp = subscriptionTimestamp;
    }

    public String getUsername() {
        return username;
    }
}
