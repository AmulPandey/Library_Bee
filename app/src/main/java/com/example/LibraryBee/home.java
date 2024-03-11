package com.example.LibraryBee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class home extends Fragment {

    private DatabaseReference subscriptionRef;
    private FirebaseAuth auth;
    private Button btn1; // Change the type to Button

    private TextView usernameTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Correctly find the ImageView
        ImageView homeProfile = view.findViewById(R.id.homepro);
        btn1 = view.findViewById(R.id.btn1); // Initialize btn1 once
        usernameTextView = view.findViewById(R.id.usernameTextView);
        // Set click listener and start activity
        homeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeProfile.class);
                startActivity(intent);
            }
        });

        // Fetch and set the subscription status
        fetchSubscriptionStatus();
        fetchUsername();

        Button btn2 = view.findViewById(R.id.btn3);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), paymentActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void fetchSubscriptionStatus() {
        if (isAdded()) {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Reference to the user's isSubscribed status in Firebase
                DatabaseReference isSubscribedRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("isSubscribed");

                // Read the isSubscribed value from Firebase
                isSubscribedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (isAdded() && dataSnapshot.exists()) {
                            // isSubscribed value exists, retrieve its value
                            boolean isSubscribed = dataSnapshot.getValue(Boolean.class);

                            // Update the UI based on the subscription status
                            updateUI(isSubscribed);
                        } else {
                            // isSubscribed doesn't exist or is null
                            // Handle this case based on your application's logic
                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                    }
                });
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }
    }

    private void fetchUsername() {
        if (isAdded()) {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Reference to the user's username in Firebase
                DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("username");

                // Read the username value from Firebase
                usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (isAdded() && dataSnapshot.exists()) {
                            // Username exists, retrieve its value
                            String username = dataSnapshot.getValue(String.class);
                            // Set the username in the TextView
                            if (usernameTextView != null) {
                                usernameTextView.setText(username);
                            } else {
                                // Handle the case where usernameTextView is null (e.g., log an error)
                            }
                        } else {
                            // Username doesn't exist or is null
                            // Handle this case (e.g., display default message)
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                    }
                });
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }
    }


    private void updateUI(boolean isSubscribed) {
        if (isSubscribed) {
            // Set ACTIVE with green color
            btn1.setText("ACTIVE");
            btn1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // Set INACTIVE with red color
            btn1.setText("INACTIVE");
            btn1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
}


