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


    private void initiateUpiPayment(String upiId, String paymentNote, String amountt) {











        // Replace placeholders with your actual values
        String payeeVpa = "zomato-order@paytm"; // Replace with your merchant VPA
        String payeeName = "Zomato Ltd"; // Replace with your merchant name
        String transactionRefId = "505101156972"; // Replace with unique transaction ID
        String transactionNote = "Buying Russian"; // Replace with transaction description
        double amount = 100.00; // Replace with transaction amount
        String currency = "INR"; // Replace with currency code (usually INR for India)

// Build the URI for the UPI intent
        Uri uri = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", payeeVpa)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("mc", "") // Merchant code (optional)
                .appendQueryParameter("tr", transactionRefId)
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", String.valueOf(amount))
                .appendQueryParameter("cu", currency)
                .build();

// Create the UPI intent
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

// Check if there's a UPI app installed
        if (upiPayIntent.resolveActivity(getPackageManager()) != null) {
            // Create a chooser intent to allow the user to select a UPI app
            Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
            startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE); // Replace with your request code
        } else {
            // No UPI app found, inform the user
            Toast.makeText(this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
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
