package com.example.LibraryBee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class paymentActivity extends AppCompatActivity {

    Button send;

    final int UPI_PAYMENT = 0;

    private DatabaseReference subscriptionRef;
    private DatabaseReference TimestampRef;

    private DatabaseReference seatRef;

    private DatabaseReference userseatNumberRef;

    private  DatabaseReference usertimingSlotRef;


    private String selectedSeatNumber;
    private String selectedSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initializeViews();

        // Get the selected seat number from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedSeatNumber")) {
            selectedSeatNumber = intent.getStringExtra("selectedSeatNumber");
        }
        if (intent != null && intent.hasExtra("selectedSlot")) {
            selectedSlot = intent.getStringExtra("selectedSlot");
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payUsingUpi();
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
            subscriptionRef = usersDatabase.child(userId).child("isSubscribed");
            userseatNumberRef = usersDatabase.child(userId).child("seatNumber");
            usertimingSlotRef = usersDatabase.child(userId).child("timingSlot");
            TimestampRef = usersDatabase.child(userId).child("subscriptionTimestamp");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            seatRef = database.getReference("seats");
            subscriptionRef.setValue(false);
            TimestampRef.setValue(0);

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

                }

            });
        } else {
            // User is not authenticated, handle accordingly (e.g., redirect to login screen)
        }
    }

    void initializeViews() {
        send = findViewById(R.id.send);
    }

    void payUsingUpi() {
        String staticUpiId = "8565020378@okbizaxis";
        String staticName = "Library Bee";
        String staticAmount = "1.0";

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", staticUpiId)
                .appendQueryParameter("pn", staticName)
                .appendQueryParameter("am", staticAmount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(paymentActivity.this)) {
            String str = data.get(0);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                Toast.makeText(paymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Thanks For Purchasing", Toast.LENGTH_LONG).show();

                // Update seat status in Firebase
                DatabaseReference seatToUpdateRef = seatRef.child(selectedSeatNumber);
                seatToUpdateRef.child("number").setValue(selectedSeatNumber);
                seatToUpdateRef.child("status").setValue(selectedSlot);

                // Set subscription and timestamp values
                subscriptionRef.setValue(true);
                long currentTimestamp = System.currentTimeMillis();
                TimestampRef.setValue(currentTimestamp);

                // Delay setting isSubscribed to false after one minute
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                subscriptionRef.setValue(false);
                                TimestampRef.setValue(0);
                                seatToUpdateRef.child("status").setValue("Available");
                            }
                        },
                        60000 // 60 seconds (1 minute) delay
                );


            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(paymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();

                // Update seat status in Firebase
                DatabaseReference seatToUpdateRef = seatRef.child(selectedSeatNumber);
                seatToUpdateRef.child("number").setValue(selectedSeatNumber);
                seatToUpdateRef.child("status").setValue(selectedSlot);
                userseatNumberRef.setValue(selectedSeatNumber);
                usertimingSlotRef.setValue(selectedSlot);

                // Set subscription and timestamp values
                subscriptionRef.setValue(true);
                long currentTimestamp = System.currentTimeMillis();
                TimestampRef.setValue(currentTimestamp);

                // Delay setting isSubscribed to false after one minute
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                subscriptionRef.setValue(false);
                                seatToUpdateRef.child("status").setValue("Available");
                                userseatNumberRef.setValue("none");
                                usertimingSlotRef.setValue("none");
                                TimestampRef.setValue(0);
                            }
                        },
                        60000 // 60 seconds (1 minute) delay
                );
            }
            else {
                Toast.makeText(paymentActivity.this, "Transaction failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(paymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected() && netInfo.isConnectedOrConnecting() && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}
