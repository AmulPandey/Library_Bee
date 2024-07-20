package com.example.LibraryBee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.LibraryBee.User_Pannel.Seat;
import com.example.LibraryBee.User_Pannel.home;
import com.example.LibraryBee.User_Pannel.maps;
import com.example.LibraryBee.User_Pannel.notifications;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private BottomNavigationView btnview;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid();

        DatabaseReference seatsRef = FirebaseDatabase.getInstance().getReference().child("seats");

        progressDialog.show(); // Show progress dialog before fetching data from Firebase

        // Add seats data to Firebase (if not already added)
        seatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss(); // Dismiss progress dialog after fetching data
                if (!dataSnapshot.exists()) {
                    for (int i = 1; i <= 20; i++) {
                        String seatNumber = String.format("%03d", i);
                        Seat seat = new Seat("Seat " + seatNumber, Seat.Status.AVAILABLE,0);

                        // Push seat data under the "seats" node with seat number as key
                        DatabaseReference newSeatRef = seatsRef.child(seatNumber);

                        // Set reservation timestamp
                        long reservationTimestamp = 0; // Assuming initial reservation timestamp is 0
                        newSeatRef.child("reservationTimestamp").setValue(reservationTimestamp);

                        // Set seat status and reserve status list
                        newSeatRef.setValue(seat);
                        newSeatRef.child("status").setValue(seat.getStatus().name());

                        // Create a child node for reserve status list under the seat
                        DatabaseReference reserveStatusRef = newSeatRef.child("reserveStatusList");

                        // Add reserve statuses to the list
                        reserveStatusRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false); // Assuming initially false
                        reserveStatusRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
                        reserveStatusRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);

                        // Initialize user IDs and usernames lists
                        DatabaseReference userIdsRef = newSeatRef.child("userIds");
                        userIdsRef.setValue(new ArrayList<String>()); // Initialize user IDs list

                        DatabaseReference usernamessRef = newSeatRef.child("usernames");
                        usernamessRef.setValue(new ArrayList<String>()); // Initialize usernames list

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss(); // Dismiss progress dialog if data fetching is canceled
                // Handle errors
            }
        });


        btnview = findViewById(R.id.btnview);

        btnview.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    loadfrag(new home(), false);
                } else if (id == R.id.nav_map) {
                    loadfrag(new maps(), false);
                } else {
                    // Handle other menu items
                    loadfrag(new notifications(), false);
                }
                return true;
            }
        });

        btnview.setSelectedItemId(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Close the app
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }



    // Method to load fragments
    public void loadfrag(Fragment fragment, boolean flag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (flag)
            ft.add(R.id.container, fragment);
        else
            ft.replace(R.id.container, fragment);
        ft.commit();
    }



}

