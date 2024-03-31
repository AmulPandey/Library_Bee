package com.example.LibraryBee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private BottomNavigationView btnview;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        Seat seat = new Seat("Seat " + seatNumber, Seat.Status.AVAILABLE);
                        // Push seat data under the "seats" node with seat number as key
                        seatsRef.child(seatNumber).setValue(seat);
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

