package com.example.Library_Bee.demo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirebaseUpdater {

    private final DatabaseReference databaseReference;
    private final DatabaseReference seatsReference;

    String selectedSlot = "xyz";
    String username = "Shaktiman";
    String seatNumber = "A1";

    public FirebaseUpdater() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
        seatsReference = database.getReference("seats");
    }

    @Scheduled(fixedRate = 1000*60*60) // update every 1 hour
    public void updateLastUpdated() {

        //users subscription update
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    Long subscriptionTimestamp = userSnapshot.child("subscriptionTimestamp").getValue(Long.class);
                    if (subscriptionTimestamp != null) {
                        Long currentTime = System.currentTimeMillis();
                        Long timeDiff = currentTime - subscriptionTimestamp;
                        if (timeDiff >= 30*24*60*60*1000) { // 1 minute in milliseconds

                            String selectedSlot = userSnapshot.child("timingSlot").getValue(String.class);
                            System.out.println("Selected Slot: " + selectedSlot);
                            String username = userSnapshot.child("username").getValue(String.class);
                            System.out.println(username);
                            System.out.println(userId);
                            String seatNumber = userSnapshot.child("seatNumber").getValue(String.class);
                            System.out.println(seatNumber);

                            updateSeatStatus(seatNumber, userId, username, selectedSlot);

                            // ...

                            userSnapshot.getRef().child("lastUpdated").setValue(System.currentTimeMillis(),
                                    new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    }
                            );

                            userSnapshot.getRef().child("isSubscribed").setValue(false,
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });

                            userSnapshot.getRef().child("seatNumber").setValue("none",
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });

                            userSnapshot.getRef().child("timingSlot").setValue("none",
                                    new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }



    private void updateSeatStatus(String seatNumber, String userId, String username, String selectedSlot) {
        // Retrieve the seat node from the Firebase Realtime Database
        DatabaseReference seatRef = seatsReference.child(seatNumber);

        // Get the userIds and usernames lists from the seat node
        seatRef.child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userIdsList = (List<String>) dataSnapshot.getValue();
                if (userIdsList != null && userIdsList.contains(userId)) {
                    // Remove the user's ID from the userIds list
                    userIdsList.remove(userId);
                    seatRef.child("userIds").setValue(userIdsList,
                            new DatabaseReference.CompletionListener() {

                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        seatRef.child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> usernamesList = (List<String>) dataSnapshot.getValue();
                if (usernamesList != null && usernamesList.contains(username)) {
                    // Remove the user's username from the usernames list
                    usernamesList.remove(username);
                    seatRef.child("usernames").setValue(usernamesList,
                            new DatabaseReference.CompletionListener() {

                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Update seats node
        seatsReference.child(seatNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (selectedSlot != null) {
                    DatabaseReference reserveStatusListRef = dataSnapshot.getRef().child("reserveStatusList");
                    switch (selectedSlot) {
                        case "Morning":
                            reserveStatusListRef.child("MORNING").setValue(false,
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });
                            break;
                        case "Evening":
                            reserveStatusListRef.child("EVENING").setValue(false,
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });
                            break;
                        case "Full Day":
                            reserveStatusListRef.child("FULL_DAY").setValue(false,
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });
                            break;
                        default:
                            // Handle unknown or invalid slot selection
                            System.out.println("Error: Invalid slot selection: " + selectedSlot);
                            break;
                    }
                }
                if(dataSnapshot.exists())
                dataSnapshot.getRef().child("status").setValue("AVAILABLE",
                        new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }




}




