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
    private JobScheduler jobScheduler;
    private static final int JOB_ID = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid();

        // Reference to the user's subscription timestamp in Firebase
        DatabaseReference subscriptionTimestampRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("subscriptionTimestamp");

        progressDialog.show();
        // Fetch the subscription timestamp from Firebase
        subscriptionTimestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                progressDialog.dismiss();
                if (dataSnapshot.exists()) {

                    // Subscription timestamp exists, retrieve its value
                    long subscriptionTimestamp = dataSnapshot.getValue(Long.class);

                    // Calculate the time difference
                    long currentTime = System.currentTimeMillis();
                    long timeDifference = currentTime - subscriptionTimestamp;

                    // If payment was made within the last hour, schedule the job
                    if (timeDifference < TimeUnit.HOURS.toMillis(1)) {
                        scheduleJob(TimeUnit.HOURS.toMillis(1) - timeDifference);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
                progressDialog.dismiss();
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

    private void scheduleJob(long delay) {
        ComponentName jobServiceComponentName = new ComponentName(this, MyJobSchedulerJob.class);

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, jobServiceComponentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setMinimumLatency(delay) // Set the desired delay before running the job
                .build();

        // Schedule the job
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
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

    // Override onDestroy to cancel the job if the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jobScheduler != null) {
            jobScheduler.cancel(JOB_ID);
        }
    }
}

