package com.example.LibraryBee.Admin_Pannel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LibraryBee.R;
import com.example.LibraryBee.User_Pannel.Seat;
import com.example.LibraryBee.User_Pannel.SeatAdapter;
import com.example.LibraryBee.User_Pannel.SeatSelectionActivity;
import com.example.LibraryBee.User_Pannel.paymentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeatManagementActivity extends AppCompatActivity {

    private GridView gridView;

    private ArrayList<Seat> seatsList;
    private SeatAdapter seatAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_management);

        gridView = findViewById(R.id.gridView);


        seatsList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String seatNumber = String.format("%03d", i);
            seatsList.add(new Seat(seatNumber, Seat.Status.AVAILABLE, 0));
        }

        seatAdapter = new SeatAdapter(this, seatsList);
        gridView.setAdapter(seatAdapter);


        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Seat selectedSeat = seatsList.get(position);

            DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference().child("seats").child(selectedSeat.getNumber());
            seatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String number = dataSnapshot.child("number").getValue(String.class);
                        String status = dataSnapshot.child("status").getValue(String.class);
                        List<String> usernames = new ArrayList<>();
                        List<String> userIds = new ArrayList<>();
                        for (DataSnapshot child : dataSnapshot.child("usernames").getChildren()) {
                            usernames.add(child.getValue(String.class));
                        }
                        for (DataSnapshot child : dataSnapshot.child("userIds").getChildren()) {
                            userIds.add(child.getValue(String.class));
                        }
                        Long reservationTimestamp = dataSnapshot.child("reservationTimestamp").getValue(Long.class);
                        Boolean isReservedForFull = dataSnapshot.child("reserveStatusList").child("FULL_DAY").getValue(Boolean.class);
                        Boolean isReservedForMor = dataSnapshot.child("reserveStatusList").child("MORNING").getValue(Boolean.class);
                        Boolean isReservedForEve = dataSnapshot.child("reserveStatusList").child("EVENING").getValue(Boolean.class);


                        // Show seat details in a dialog
                        showSeatDetailsDialog(number, status, usernames, userIds, reservationTimestamp, isReservedForFull, isReservedForMor, isReservedForEve);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors

                }
            });
        });


        ImageView imageView = findViewById(R.id.imageButton);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the dialog builder
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SeatManagementActivity.this);

                // Create an ImageView to display the image
                ImageView imageView = new ImageView(SeatManagementActivity.this);
                imageView.setImageResource(R.drawable.color_info); // Replace 'color_info' with your image resource

                // Get the drawable from the image view
                Drawable drawable = imageView.getDrawable();

                // Set the image drawable to the image view
                imageView.setImageDrawable(drawable);

                // Set the image view to the dialog builder
                dialogBuilder.setView(imageView);

                // Set the positive button for the dialog
                dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

                // Create the dialog
                AlertDialog dialog = dialogBuilder.create();

                // Retrieve the intrinsic width and height of the drawable
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();

                // Set the layout parameters for the dialog window to match the image dimensions
                dialog.getWindow().setLayout(width, height);

                // Show the dialog
                dialog.show();
            }
        });


    }

    private void showSeatDetailsDialog(String number, String status, List<String> usernames, List<String> userIds, Long reservationTimestamp, Boolean isReservedForFull, Boolean isReservedForMor, Boolean isReservedForEve) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_seat_details, null);
        builder.setView(dialogView);

        TextView seatNumberTextView = dialogView.findViewById(R.id.seatNumberTextView);
        TextView statusTextView = dialogView.findViewById(R.id.statusTextView);
        TextView reservationTimestampTextView = dialogView.findViewById(R.id.reservationTimestampTextView);
        TextView reserveStatusMorningTextView = dialogView.findViewById(R.id.reserveStatusMorningTextView);
        TextView reserveStatusEveningTextView = dialogView.findViewById(R.id.reserveStatusEveningTextView);
        TextView reserveStatusFullDayTextView = dialogView.findViewById(R.id.reserveStatusFullDayTextView);

        ListView usernamesListView = dialogView.findViewById(R.id.usernamesListView);
        ListView userIdsListView = dialogView.findViewById(R.id.useridsListView);

        seatNumberTextView.setText(number);
        statusTextView.setText("Status: " + status);
        reservationTimestampTextView.setText("Reservation Timestamp: " + (reservationTimestamp != null ? reservationTimestamp.toString() : "N/A"));
        reserveStatusMorningTextView.setText("Reserved for Morning: " + (isReservedForMor!= null? isReservedForMor.toString() : "N/A"));
        reserveStatusEveningTextView.setText("Reserved for Evening: " + (isReservedForEve!= null? isReservedForEve.toString() : "N/A"));
        reserveStatusFullDayTextView.setText("Reserved for Full Day: " + (isReservedForFull!= null? isReservedForFull.toString() : "N/A"));

        ArrayAdapter<String> usernamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernames);
        usernamesListView.setAdapter(usernamesAdapter);

        ArrayAdapter<String> userIdsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userIds);
        userIdsListView.setAdapter(userIdsAdapter);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
