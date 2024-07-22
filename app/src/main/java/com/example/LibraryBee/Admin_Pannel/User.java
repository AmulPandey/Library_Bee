package com.example.LibraryBee.Admin_Pannel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class User {
    public String userId;

    public String email;
    public String username;
    public String phoneNumber;
    public String gender;
    public boolean isSubscribed;
    public long subscriptionTimestamp;
    public String timingSlot;
    public String seatNumber;
    public String joiningDate;
    public String deviceToken;


    // Default constructor
    public User() {
    }

    // Constructor with parameters
    public User(String userId, String email, String username, String phoneNumber, String gender, boolean isSubscribed) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.isSubscribed = isSubscribed;
        this.subscriptionTimestamp = 0;
        this.timingSlot = "none";
        this.seatNumber = "none";
        this.joiningDate = getCurrentDate();
        this.deviceToken = "";
    }


    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
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


    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    public String getSubscriptionDateAsString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date(subscriptionTimestamp);
        return subscriptionTimestamp != 0 ? dateFormat.format(date) : "0";
    }
}
