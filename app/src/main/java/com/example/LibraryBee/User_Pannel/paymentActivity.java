package com.example.LibraryBee.User_Pannel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LibraryBee.Admin_Pannel.Request;
import com.example.LibraryBee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class paymentActivity extends AppCompatActivity {

    Button send;

    // Constants for request status
    private static final int REQUEST_PENDING = 0;
    private static final int REQUEST_APPROVED = 1;
    private static final int REQUEST_REJECTED = 2;

    private DatabaseReference requestRef;
    private DatabaseReference subscriptionRef;
    private DatabaseReference TimestampRef;
    private DatabaseReference seatRef;
    private DatabaseReference userseatNumberRef;
    private DatabaseReference usertimingSlotRef;
    private String selectedSeatNumber;
    private String selectedSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.LibraryBee.R.layout.activity_payment);
        initializeViews();

        // Get the selected seat number from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedSeatNumber")) {
            selectedSeatNumber = intent.getStringExtra("selectedSeatNumber");
        }
        if (intent != null && intent.hasExtra("selectedSlot")) {
            selectedSlot = intent.getStringExtra("selectedSlot");
        }


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Generate request and send to admin
                generateAndSendRequest();
                showConfirmationDialog();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    // Now you have the user ID
                    // You can use this userId in your updateSubscriptionAndSeatStatus method
                    updateSubscriptionAndSeatStatus(userId);
                } else {
                    // User is not logged in
                    // Handle the scenario where there is no logged-in user
                }
            }
        });

        // Firebase initialization
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
            subscriptionRef = usersDatabase.child(userId).child("isSubscribed");
            userseatNumberRef = usersDatabase.child(userId).child("seatNumber");
            usertimingSlotRef = usersDatabase.child(userId).child("timingSlot");
            TimestampRef = usersDatabase.child(userId).child("subscriptionTimestamp");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            seatRef = database.getReference("seats");
            subscriptionRef.setValue(false);
            TimestampRef.setValue(0);

            // Initialize reference to requests node in Firebase
            requestRef = database.getReference("requests");

            // Set listener for subscription changes
            subscriptionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isSubscribed = snapshot.getValue(Boolean.class);
                    if (isSubscribed != null && isSubscribed) {
                        // User has an active subscription
                        // Perform actions accordingly
                        // For example, enable premium features
                    } else {
                        // User does not have an active subscription
                        // Handle non-subscribed state
                        // For example, prompt user to subscribe
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled
                }
            });
        } else {
            // User is not authenticated, handle accordingly (e.g., redirect to login screen)
        }
    }

    void initializeViews() {
        send = findViewById(R.id.send);
    }

    void generateAndSendRequest() {
        // Get current user information
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userName = currentUser.getDisplayName(); // Get the username from Firebase Authentication
            String userEmail = currentUser.getEmail();

            // If the username is null, you can set it to the email address or any other default value
            if (userName == null) {
                userName = userEmail;
            }

            String price;


            if ("Full Day".equals(selectedSlot)) {
                price = "1000 rs/month";
            } else {
                price = "600 rs/month";
            }

            // Create a request object with user information and other details
            Request request = new Request(userId, userName, userEmail, selectedSeatNumber, selectedSlot, price);

            // Set the key as the userID when pushing the request to Firebase
            DatabaseReference newRequestRef = requestRef.child(userId);
            newRequestRef.setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(paymentActivity.this, "Request sent to admin", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(paymentActivity.this, "Failed to send request. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Handle the case where the current user is null (not authenticated)
            // You may want to redirect the user to the login screen
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("THANKS FOR CONFIRMING YOUR SEAT\n\nYour seat has been booked temporarily for the next 12 hours. To make it permanent for a month, please go to Library Bee and make payment accordingly.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform any actions after confirming
                dialog.dismiss(); // Dismiss the dialog
            }
        });
        builder.show();
    }


    // Other methods...

    private void updateSubscriptionAndSeatStatus(String userId) {
        // Update user subscription in Firebase

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("isSubscribed");
        userRef.setValue(true); // Set the user as subscribed
        // Update seat status in Firebase

        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        userseatNumberRef = usersDatabase.child(userId).child("seatNumber");
        usertimingSlotRef = usersDatabase.child(userId).child("timingSlot");
        userseatNumberRef = usersDatabase.child(userId).child("seatNumber");
        usertimingSlotRef = usersDatabase.child(userId).child("timingSlot");

        long currentTimestamp = System.currentTimeMillis();
        DatabaseReference TimestampRef = usersDatabase.child(userId).child("subscriptionTimestamp");
        TimestampRef.setValue(currentTimestamp);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference seatRef = database.getReference("seats"); // Update seat status to reserved

        // Update seat status in Firebase
        DatabaseReference seatToUpdateRef = seatRef.child(selectedSeatNumber);
        seatToUpdateRef.child("number").setValue(selectedSeatNumber);
        seatToUpdateRef.child("status").setValue("RESERVED");
        userseatNumberRef.setValue(selectedSeatNumber);
        usertimingSlotRef.setValue(selectedSlot);

        // Perform additional actions based on selected slot (e.g., update reserve status list)
        // Remember to handle different slot types (morning, evening, full day) accordingly
        // For example:
        DatabaseReference reserveStatusListRef = seatToUpdateRef.child("reserveStatusList");
        // Update reserve status based on the user's timing slot
        switch (selectedSlot) {
            case "Morning":
                reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(true);
                break;
            case "Evening":
                reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(true);
                break;
            case "Full Day":
                reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(true);
                break;
            default:
                // Handle unknown slot
                break;
        }

        long reservationTimestamp = System.currentTimeMillis();
        seatToUpdateRef.child("reservationTimestamp").setValue(reservationTimestamp);

        // Create a ScheduledThreadPoolExecutor with a single thread
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);


        long delayInMilliseconds = 30*24*60*60*1000;
        executor.schedule(() -> {
            // Retrieve the reservation timestamp from Firebase
            seatToUpdateRef.child("reservationTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long reservedAt = dataSnapshot.getValue(Long.class);
                        if (reservedAt != null && System.currentTimeMillis() - reservedAt >= delayInMilliseconds) {
                            // More than 12 hours have passed since reservation, revert changes
                            userRef.setValue(false);
                            seatToUpdateRef.child("status").setValue("AVAILABLE");
                            reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);
                            reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false);
                            reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
                            userseatNumberRef.setValue("none");
                            usertimingSlotRef.setValue("none");
                            TimestampRef.setValue(0);

                            DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("requests");
                            requestsRef.child(userId).child("rejected").setValue(true);
                            // Also remove the reservation timestamp
                            seatToUpdateRef.child("reservationTimestamp").removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle onCancelled
                }
            });
        }, delayInMilliseconds, TimeUnit.MILLISECONDS);


    }
}
