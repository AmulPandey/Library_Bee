package com.example.LibraryBee;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyJobSchedulerJob extends JobService {

    private static final String TAG = "MyJobSchedulerJob";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        // Perform your background task here (e.g., network request, data synchronization)
        new MyBackgroundTask().execute(params);

        // Return true if there is ongoing work, false if the job is completed
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopped");

        // Return true to reschedule the job if necessary
        return true;
    }

    private class MyBackgroundTask extends AsyncTask<JobParameters, Void, JobParameters> {

        private long subscriptionTimestamp; // Add this field to store the subscription timestamp

        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            // Perform background task (e.g., network request, data synchronization)
            // Note: Do not perform long-running operations on the main thread here

            // Fetch subscription timestamp and update isSubscribed accordingly
            fetchSubscriptionTimestamp();

            // Once the task is complete, call jobFinished to inform the system
            jobFinished(params[0], false);

            return params[0];
        }

        private void fetchSubscriptionTimestamp() {
            // Get the current user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // User is authenticated, get their UID
                String userId = currentUser.getUid();

                // Reference to the user's subscription timestamp in Firebase
                DatabaseReference subscriptionTimestampRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("subscriptionTimestamp");

                // Read the subscription timestamp from Firebase
                subscriptionTimestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Subscription timestamp exists, retrieve its value
                            subscriptionTimestamp = dataSnapshot.getValue(Long.class);

                            // Now you have the subscription timestamp
                            // Use it as needed in your code
                            // For example, you might use it in the checkSubscriptionStatus method
                            checkSubscriptionStatus();
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
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }

        private void checkSubscriptionStatus() {
            // Your logic to check subscription expiration
            // Update the isSubscribed status in Firebase Realtime Database accordingly
            // For example, compare current time with the subscription timestamp
            // If the difference is greater than or equal to the subscription duration, set isSubscribed to false

            long currentTime = System.currentTimeMillis();
            long subscriptionDurationMillis = 60 * 60 * 1000; // 1 hour in milliseconds

            boolean isSubscribed = currentTime - subscriptionTimestamp < subscriptionDurationMillis;

            // Update isSubscribed in Firebase
            if (!isSubscribed) {
                // Subscription has expired, update isSubscribed to false in Firebase
                updateIsSubscribed(false);
            }
        }

        private void updateIsSubscribed(boolean isSubscribed) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Reference to the user's isSubscribed status in Firebase
                DatabaseReference isSubscribedRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("isSubscribed");

                // Set the new value for isSubscribed
                isSubscribedRef.setValue(isSubscribed)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "isSubscribed updated successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to update isSubscribed", e);
                            }
                        });
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }

    }

}
