package com.example.LibraryBee.Admin_Pannel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.app.AlertDialog;
import android.widget.TextView;

import com.example.LibraryBee.R;
import com.example.LibraryBee.User_Pannel.Seat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestHandeling extends AppCompatActivity {

    private DatabaseReference requestRef;
    private ListView requestListView;
    private List<Request> requestList;
    private RequestAdapter requestAdapter;

    private String selectedSeatNumber;
    private String selectedSlot;
    private DatabaseReference userseatNumberRef;

    private  DatabaseReference usertimingSlotRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_handeling);
        requestRef = FirebaseDatabase.getInstance().getReference("requests");
        requestListView = findViewById(R.id.request_list);
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestList);
        requestListView.setAdapter(requestAdapter);

        final TextView noRequestsTextView = findViewById(R.id.no_requests_text);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedSeatNumber")) {
            selectedSeatNumber = intent.getStringExtra("selectedSeatNumber");
        }
        if (intent != null && intent.hasExtra("selectedSlot")) {
            selectedSlot = intent.getStringExtra("selectedSlot");
        }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });



        // Handle request item click
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Request request = requestList.get(i);
                showRequestDialog(request);
            }
        });
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
                                // Assuming requestNodeRef is already initialized
                            }
                            // Update UI
                            //request.setApproved(true); // Set the request as approved locally
                            // Perform additional actions if needed (e.g., update user subscription, seat status)
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


    private void updateSubscriptionAndSeatStatusonR(final Request request) {
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

        userRef.setValue(false);
        seatToUpdateRef.child("status").setValue("AVAILABLE");
        reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);
        reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false);
        reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
        userseatNumberRef.setValue("none");
        usertimingSlotRef.setValue("none");

        // Also remove the reservation timestamp
        seatToUpdateRef.child("reservationTimestamp").removeValue();

    }

}
