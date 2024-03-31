package com.example.LibraryBee;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
                    String statusString = snapshot.child("status").getValue(String.class);

                    // Append details to list
                    seatDetailsList.add("Seat Number: " + number + ", Status: " + statusString);
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
