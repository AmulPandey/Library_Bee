package com.example.LibraryBee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.MenuItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid();

        // Reference to the user's subscription timestamp in Firebase
        DatabaseReference subscriptionTimestampRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("subscriptionTimestamp");

        // Fetch the subscription timestamp from Firebase
        subscriptionTimestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Subscription timestamp exists, retrieve its value
                    long subscriptionTimestamp = dataSnapshot.getValue(Long.class);

                    // Calculate the time difference
                    long currentTime = System.currentTimeMillis();
                    long timeDifference = currentTime - subscriptionTimestamp;

                    // Schedule the job to run after one hour
                    long intervalInMs = TimeUnit.HOURS.toMillis(1) - timeDifference;
                    if (intervalInMs < 0) {
                        // If the calculated interval is negative, set a default interval (e.g., one hour)
                        intervalInMs = TimeUnit.HOURS.toMillis(1);
                    }

                    // Schedule the job
                    scheduleJob(intervalInMs);
                } else {
                    // Subscription timestamp doesn't exist or is null
                    // Handle this case based on your application's logic
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
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
                    loadfrag(new notifications(),false);
                }
                return true;
            }
        });

        btnview.setSelectedItemId(R.id.nav_home);
    }

    private void scheduleJob(long intervalInMs) {
        ComponentName jobServiceComponentName = new ComponentName(this, MyJobSchedulerJob.class);

        int jobId = 123;
        JobInfo jobInfo = new JobInfo.Builder(jobId, jobServiceComponentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setMinimumLatency(intervalInMs) // Set the desired scheduling interval
                .build();

        // Schedule the job
        // ... (your job scheduling logic)
    }

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
