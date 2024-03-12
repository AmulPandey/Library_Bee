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

import java.sql.Timestamp;
import java.util.ArrayList;

public class paymentActivity extends AppCompatActivity {

    Button send;

    final int UPI_PAYMENT = 0;

    private DatabaseReference subscriptionRef;

    private DatabaseReference TimestampRef;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initializeViews();



//        // Check if the Intent has extras
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            // Check if the key "stuffs" is present in the extras
//            if (bundle.containsKey("stuffs")) {
//                String stuffs = bundle.getString("stuffs");
//                Toast.makeText(getApplicationContext(), "stuff" + stuffs, Toast.LENGTH_SHORT).show();
//                amountEt.setText(stuffs);
//            } else {
//                Toast.makeText(getApplicationContext(), "No 'stuffs' key in extras", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "No extras in the Intent", Toast.LENGTH_SHORT).show();
//        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting the values from the EditTexts

                // upiIdEt.setFocusable(false);
                payUsingUpi();
            }
        });

        auth = FirebaseAuth.getInstance();

        // Check if the user is authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is authenticated, get their UID
            String userId = currentUser.getUid();

            // Reference to the user's subscription status in Firebase
            DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
            subscriptionRef = usersDatabase.child(userId).child("isSubscribed");
            TimestampRef = usersDatabase.child(userId).child("subscriptionTimestamp");

            // Set the initial subscription status
            subscriptionRef.setValue(false);
            TimestampRef.setValue(0);

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

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
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
                        //Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        //Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
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
            //Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
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
                //Code to handle successful transaction here.
                Toast.makeText(paymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                // Log.d("UPI", "responseStr: "+approvalRefNo);
                Toast.makeText(this, "Thanks For Purchasing", Toast.LENGTH_LONG).show();
                subscriptionRef.setValue(true);

                long currentTimestamp = System.currentTimeMillis();
                // Set the subscription timestamp
                TimestampRef.setValue(currentTimestamp);


            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(paymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(paymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(paymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}

