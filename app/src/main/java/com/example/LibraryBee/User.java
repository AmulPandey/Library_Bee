package com.example.LibraryBee;

public class User {
    public String userId;
    public String email;
    public String username;
    public String phoneNumber;
    public String gender;
    public boolean isSubscribed; // Make this public
    public long subscriptionTimestamp;
    public String timingSlot; // New element for timing slot
    public String seatNumber; // New element for seat number
    public String joiningDate; // New element for joining date
    public String leavingDate; // New element for leaving date


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
        this.timingSlot = ""; // Initialize to empty string by default
        this.seatNumber = ""; // Initialize to empty string by default
        this.joiningDate = ""; // Initialize to empty string by default
        this.leavingDate = ""; // Initialize to empty string by default
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

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public String getTimingSlot() {
        return timingSlot;
    }

    public void setTimingSlot(String timingSlot) {
        this.timingSlot = timingSlot;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(String leavingDate) {
        this.leavingDate = leavingDate;
    }
}
