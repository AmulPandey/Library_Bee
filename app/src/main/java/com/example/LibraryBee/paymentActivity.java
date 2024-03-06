package com.example.LibraryBee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class paymentActivity extends AppCompatActivity {

    int  UPI_PAYMENT_REQUEST_CODE;

    private DatabaseReference subscriptionRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        Button paybButtonButton = findViewById(R.id.loda);
        paybButtonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform the subscription process (e.g., through a payment gateway)

                initiateUpiPayment("prashantdixit02-3@okicici", "Payment for a service", "1.00");
                // Update subscription status in Firebase
                //subscriptionRef.setValue(true);
            }
        });



        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Check if the user is authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is authenticated, get their UID
            String userId = currentUser.getUid();

            // Reference to the user's subscription status in Firebase
            subscriptionRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("subscription");

            // Attach a ValueEventListener to check the subscription status
            subscriptionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isSubscribed = snapshot.getValue(Boolean.class);

                    if (isSubscribed != null && isSubscribed) {
                        // User has an active subscription
                        // Perform actions accordingly
                        // For example, enable premium features
                    } else {
                        // User does not have an active subscription
                        // Handle non-subscribed state
                        // For example, prompt user to subscribe
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            });
        } else {
            // User is not authenticated, handle accordingly (e.g., redirect to login screen)
        }
    }


    private void initiateUpiPayment(String upiId, String paymentNote, String amount) {
        Toast.makeText(this, "Prakriti randi", Toast.LENGTH_SHORT).show();
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)  // Payee UPI ID
                .appendQueryParameter("pn", "Payee Name")  // Payee Name
                .appendQueryParameter("mc", "")  // Merchant Code (optional)
                .appendQueryParameter("tid", "123456")  // Transaction ID (optional)
                .appendQueryParameter("tr", "your-ref-id")  // Transaction Reference ID
                .appendQueryParameter("tn", paymentNote)  // Transaction Note
                .appendQueryParameter("am", amount)  // Transaction Amount
                .appendQueryParameter("cu", "INR")  // Currency Code
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);


        // Check if there is a UPI app available on the user's device
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, UPI_PAYMENT_REQUEST_CODE);
        } else {
            // Handle case where UPI app is not available
            // You may want to redirect the user to download a UPI app from the Play Store
            Toast.makeText(this, "randi", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK || resultCode == 11) {
                // Payment successful or pending
                // You can update the subscription status or perform further actions
                subscriptionRef.setValue(true);
            } else {
                // Payment failed or canceled
                // Handle accordingly
                Toast.makeText(this, "Payment Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
