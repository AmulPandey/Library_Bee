package com.example.LibraryBee.Admin_Pannel;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LibraryBee.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SeatManagementActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ListView mSeatListView;
    private List<String> seatDetailsList; // List to hold seat details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_management);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("seats");
        mSeatListView = findViewById(R.id.seatListView);
        seatDetailsList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, seatDetailsList);
        mSeatListView.setAdapter(adapter);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seatDetailsList.clear(); // Clear previous data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Extract seat details
                    String number = snapshot.child("number").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    // Extract reserve status
                    boolean morningReserved = false; // Default value if data is missing
                    boolean eveningReserved = false; // Default value if data is missing
                    boolean fullDayReserved = false; // Default value if data is missing

// Check if the reserveStatusList node exists
                    DataSnapshot reserveStatusSnapshot = snapshot.child("reserveStatusList");
                    if (reserveStatusSnapshot.exists()) {
                        // Extract reserve status if the node exists
                        morningReserved = reserveStatusSnapshot.child("MORNING").getValue(Boolean.class);
                        eveningReserved = reserveStatusSnapshot.child("EVENING").getValue(Boolean.class);
                        fullDayReserved = reserveStatusSnapshot.child("FULL_DAY").getValue(Boolean.class);
                    }
                    // Construct reserve status string
                    StringBuilder reserveStatusBuilder = new StringBuilder();
                    if (!fullDayReserved) {
                        reserveStatusBuilder.append("Morning: ").append(morningReserved ? "Reserved" : "Available").append(", ");
                        reserveStatusBuilder.append("\nEvening: ").append(eveningReserved ? "Reserved" : "Available");
                        reserveStatusBuilder.append("\nFull Day: ").append(fullDayReserved ? "Reserved" : "Available");
                    } else {
                        reserveStatusBuilder.append("Full Day: Reserved");
                    }


                    // Append details to list
                    String seatDetails = "Seat Number: " + number + " -->Status: " + status + "\nReserve Status:\n" + reserveStatusBuilder.toString();
                    seatDetailsList.add(seatDetails);
                }
                adapter.notifyDataSetChanged(); // Notify adapter for data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SeatManagementActivity.this, "Failed to load seat data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
