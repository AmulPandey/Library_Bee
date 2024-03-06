package com.example.LibraryBee;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    public String userId;
    public String email;
    public String username;
    public String phoneNumber;
    public String gender;
    private boolean isSubscribed;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String email, String username, String phoneNumber, String gender, boolean isSubscribed) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.isSubscribed = isSubscribed;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    // Example code to set the initial subscription status in Firebase
    public void setInitialSubscriptionStatus(String userId) {
        // Reference to the user's subscription status in Firebase
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference subscriptionRef = usersDatabase.child(userId).child("isSubscribed");

        // Set the initial subscription status
        subscriptionRef.setValue(false);
    }
}
