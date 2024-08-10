package com.example.LibraryBee.Admin_Pannel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.LibraryBee.R;
import com.example.LibraryBee.Request;
import com.example.LibraryBee.Seat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestHandeling extends AppCompatActivity {

    private DatabaseReference requestRef;
    private RecyclerView requestRecyclerView;
    private List<Request> requestList;
    private RequestAdapter requestAdapter;

    private String selectedSeatNumber;
    private String selectedSlot;
    private DatabaseReference userseatNumberRef;

    private  DatabaseReference usertimingSlotRef;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_handeling);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Adjust content to fit system windows
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        requestRef = FirebaseDatabase.getInstance().getReference("requests");
        requestRecyclerView = findViewById(R.id.request_recycler_view);
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(requestList,RequestHandeling.this);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestRecyclerView.setAdapter(requestAdapter);

        final TextView noRequestsTextView = findViewById(R.id.no_requests_text);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedSeatNumber")) {
            selectedSeatNumber = intent.getStringExtra("selectedSeatNumber");
        }
        if (intent != null && intent.hasExtra("selectedSlot")) {
            selectedSlot = intent.getStringExtra("selectedSlot");
        }

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // Fetch pending requests from Firebase
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    if (request != null && !request.isApproved() && !request.isRejected()) {
                        requestList.add(request);
                    }
                }
                requestAdapter.notifyDataSetChanged();

                if (requestList.isEmpty()) {
                    noRequestsTextView.setVisibility(View.VISIBLE);
                } else {
                    noRequestsTextView.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE); // Hide the progress bar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
                progressBar.setVisibility(View.GONE); // Hide the progress bar
            }
        });






        // Handle request item click
        requestRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, requestRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Request request = requestList.get(position);
                showRequestDialog(request);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // Handle long click
            }
        }));
    }

    // Method to show dialog for request approval or rejection
    private void showRequestDialog(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request from " + request.getUserName());
        builder.setMessage("Seat: " + request.getSelectedSeatNumber() + "\nSlot: " + request.getSelectedSlot());
        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                approveRequest(request);
            }
        });
        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rejectRequest(request);
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    // Method to approve the request

    private void approveRequest(final Request request) {

        // Update the 'approved' field in the request node
        DatabaseReference requestNodeRef = requestRef.child(request.getRequestId()).child("approved");
        requestNodeRef.setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = null;
                            if (currentUser != null) {
                                userId = currentUser.getUid();
                            }
                            // Update UI
                            //request.setApproved(true); // Set the request as approved locally
                            updateSubscriptionAndSeatStatus(request);

                            requestAdapter.notifyDataSetChanged(); // Notify the adapter to update the UI
                        } else {
                            // Handle failure
                            Log.e("ApproveRequest", "Failed to approve request: " + task.getException());
                        }
                    }
                });
    }


    // Method to reject the request
    private void rejectRequest(final Request request) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reject Request");
        builder.setMessage("Are you sure you want to reject this request?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestRef.child(request.getRequestId()).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updateSubscriptionAndSeatStatusonR(request);
                                    DatabaseReference requestNodeRef = requestRef.child(request.getRequestId()).child("rejected");
                                    requestNodeRef.setValue(true)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> innerTask) {
                                                    if (innerTask.isSuccessful()) {
                                                        // Update UI on main thread
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                requestAdapter.notifyDataSetChanged();
                                                            }
                                                        });
                                                    } else {
                                                        // Handle rejection update failure
                                                        Log.e("RejectRequest", "Failed to update rejection status: " + innerTask.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    // Handle deletion failure
                                    Log.e("RejectRequest", "Failed to delete request: " + task.getException());
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    // Method to update user subscription and seat status after request approval
    private void updateSubscriptionAndSeatStatus(final Request request) {
        // Update user subscription in Firebase
        final String userId = request.getUserId();

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("isSubscribed");
        userRef.setValue(true); // Set the user as subscribed
        // Update seat status in Firebase
        final String selectedSeatNumber = request.getSelectedSeatNumber();
        final String selectedSlot = request.getSelectedSlot();


        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
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


        usersDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);

                // Get the list of usernames and IDs from Firebase
                DatabaseReference usernamesRef = seatToUpdateRef.child("usernames");
                DatabaseReference userIdsRef = seatToUpdateRef.child("userIds");

                usernamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                        List<String> usernamesList = dataSnapshot.getValue(t);
                        if (usernamesList == null) {
                            usernamesList = new ArrayList<>();
                        }
                        if (!usernamesList.contains(username)) {
                            usernamesList.add(username);
                            usernamesRef.setValue(usernamesList);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });

                userIdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                        List<String> userIdsList = dataSnapshot.getValue(t);
                        if (userIdsList == null) {
                            userIdsList = new ArrayList<>();
                        }
                        if (!userIdsList.contains(userId)) {
                            userIdsList.add(userId);
                            userIdsRef.setValue(userIdsList);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });



        userseatNumberRef.setValue(selectedSeatNumber);
        usertimingSlotRef.setValue(selectedSlot);

        // Perform additional actions based on selected slot (e.g., update reserve status list)
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


//        ******This part of logic present in springboot server.


//        // Create a ScheduledThreadPoolExecutor with a single thread
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        // Delay setting isSubscribed to false after 30 days
        long delayInMilliseconds =30*24*60*60*1000;
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
                             userseatNumberRef.setValue("none");
                             usertimingSlotRef.setValue("none");



                            seatToUpdateRef.child("status").setValue("AVAILABLE");



                            switch (selectedSlot) {
                                  case "Morning":
                                          reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false);
                                          break;
                                  case "Evening":
                                          reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
                                          break;
                                  case "Full Day":
                                          reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);
                                         break;
                                  default:
                                         // Handle unknown or invalid slot selection
                                          Log.e("Error", "Invalid slot selection: " + selectedSlot);
                                          break;
                            }


                            // Remove userId and username from seat node
                            seatToUpdateRef.child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                                    List<String> usernamesList = dataSnapshot.getValue(t);
                                    if (usernamesList != null) {
                                        // Retrieve the username from the Firebase Realtime Database
                                        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("username");
                                        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String username = dataSnapshot.getValue(String.class);
                                                if (username != null) {
                                                    usernamesList.remove(username);
                                                    seatToUpdateRef.child("usernames").setValue(usernamesList);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle error
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle error
                                }
                            });
                            seatToUpdateRef.child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                                    List<String> userIdsList = dataSnapshot.getValue(t);
                                    if (userIdsList != null) {
                                        userIdsList.remove(userId);
                                        seatToUpdateRef.child("userIds").setValue(userIdsList);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle error
                                }
                            });



                            DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("requests");
                            requestsRef.child(userId).child("rejected").setValue(true);

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


    private void updateSubscriptionAndSeatStatusonR(final Request request) {
        // Update user subscription in Firebase
        final String userId = request.getUserId();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("isSubscribed");
        //userRef.setValue(true); // Set the user as subscribed
        // Update seat status in Firebase
        final String selectedSeatNumber = request.getSelectedSeatNumber();
        final String selectedSlot = request.getSelectedSlot();



        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        userseatNumberRef = usersDatabase.child(userId).child("seatNumber");
        usertimingSlotRef = usersDatabase.child(userId).child("timingSlot");


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference seatRef = database.getReference("seats"); // Update seat status to reserved

        // Update seat status in Firebase
        DatabaseReference seatToUpdateRef = seatRef.child(selectedSeatNumber);


        // Perform additional actions based on selected slot (e.g., update reserve status list)
        DatabaseReference reserveStatusListRef = seatToUpdateRef.child("reserveStatusList");

        userRef.setValue(false);
        userseatNumberRef.setValue("none");
        usertimingSlotRef.setValue("none");


        seatToUpdateRef.child("status").setValue("AVAILABLE");

        switch (selectedSlot) {
            case "Morning":
                reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false);
                break;
            case "Evening":
                reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
                break;
            case "Full Day":
                reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);
                break;
            default:
                // Handle unknown or invalid slot selection
                Log.e("Error", "Invalid slot selection: " + selectedSlot);
                break;
        }
        // Remove userId and username from seat node
        seatToUpdateRef.child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> usernamesList = dataSnapshot.getValue(t);
                if (usernamesList != null) {
                    // Retrieve the username from the Firebase Realtime Database
                    DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("username");
                    usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.getValue(String.class);
                            if (username != null) {
                                usernamesList.remove(username);
                                seatToUpdateRef.child("usernames").setValue(usernamesList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
        seatToUpdateRef.child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> userIdsList = dataSnapshot.getValue(t);
                if (userIdsList != null) {
                    userIdsList.remove(userId);
                    seatToUpdateRef.child("userIds").setValue(userIdsList);
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        requestsRef.child(userId).child("rejected").setValue(true);


    }

}
