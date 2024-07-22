package com.example.LibraryBee.User_Pannel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LibraryBee.R;
import com.example.LibraryBee.Seat;
import com.example.LibraryBee.SeatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SeatSelectionActivity extends AppCompatActivity {

    private RadioGroup radioGroupSlots;
    private GridView gridView;
    private Button btnConfirm;
    private ArrayList<Seat> seatsList;
    private SeatAdapter seatAdapter;
    private String selectedSlot = "";
    private String selectedSeatNumber = "";
    private DatabaseReference seatsRef;
    private DatabaseReference usersRef;

    private  FirebaseUser currentUser;

    private boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        radioGroupSlots = findViewById(R.id.radioGroupSlots);
        gridView = findViewById(R.id.gridView);
        btnConfirm = findViewById(R.id.btnConfirm);

        seatsList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String seatNumber = String.format("%03d", i);
            seatsList.add(new Seat(seatNumber, Seat.Status.AVAILABLE,0));
        }

        seatAdapter = new SeatAdapter(this, seatsList);
        gridView.setAdapter(seatAdapter);

        seatsRef = FirebaseDatabase.getInstance().getReference().child("seats");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        radioGroupSlots.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = findViewById(checkedId);
            selectedSlot = radioButton.getText().toString();
            updateSeatAvailability();
        });


        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Seat selectedSeat = seatsList.get(position);

            // Check reservation status directly from Firebase
            DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference().child("seats").child(selectedSeat.getNumber());
            seatsRef.child(selectedSeat.getNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Boolean isReservedForFull = dataSnapshot.child("reserveStatusList").child("FULL_DAY").getValue(Boolean.class);
                        Boolean isReservedForMor = dataSnapshot.child("reserveStatusList").child("MORNING").getValue(Boolean.class);
                        Boolean isReservedForEve = dataSnapshot.child("reserveStatusList").child("EVENING").getValue(Boolean.class);

                        if ((isReservedForMor != null && isReservedForMor) || (isReservedForEve != null && isReservedForEve)) {
                            radioGroupSlots.findViewById(R.id.radioFullDay).setVisibility(View.GONE);
                        } else {
                            radioGroupSlots.findViewById(R.id.radioFullDay).setVisibility(View.VISIBLE);
                        }

                        if ((isReservedForFull != null && isReservedForFull) || (isReservedForMor && isReservedForEve)) {
                            // Display toast and make the button disappear
                            Toast.makeText(SeatSelectionActivity.this, "Please select a different seat, it is completely reserved", Toast.LENGTH_LONG).show();
                            btnConfirm.setClickable(false);
                        } else if ((isReservedForMor && isReservedForEve)) {
                            Toast.makeText(SeatSelectionActivity.this, "Please select a different seat, it is completely reserved", Toast.LENGTH_LONG).show();
                            btnConfirm.setClickable(false);
                        } else {
                            btnConfirm.setClickable(true);
                            // Check if selected slot matches the reserve status list slot
                            int selectedSlotId = radioGroupSlots.getCheckedRadioButtonId();
                            boolean slotMatches = false;
                            if (selectedSlotId != -1) {
                                String selectedSlotText = ((RadioButton) findViewById(selectedSlotId)).getText().toString();
                                switch (selectedSlotText) {
                                    case "Morning":
                                        slotMatches = isReservedForMor;
                                        break;
                                    case "Evening":
                                        slotMatches = isReservedForEve;
                                        break;
                                    case "Full Day":
                                        slotMatches = isReservedForFull;
                                        break;
                                }
                            }

                            if (slotMatches) {
                                Toast.makeText(SeatSelectionActivity.this, "Please select a different slot", Toast.LENGTH_LONG).show();
                            } else {
                                // Seat is not reserved completely and slot matches, proceed with selection
                                if (selectedSeat.getStatus() == Seat.Status.AVAILABLE) {
                                    deselectAllSeats();
                                    selectedSeat.setStatus(Seat.Status.SELECTED);
                                    seatAdapter.notifyDataSetChanged();
                                    selectedSeatNumber = selectedSeat.getNumber();
                                } else if (selectedSeat.getStatus() == Seat.Status.SELECTED) {
                                    selectedSeat.setStatus(Seat.Status.AVAILABLE);
                                    seatAdapter.notifyDataSetChanged();
                                    selectedSeatNumber = "";
                                }
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        });


        btnConfirm.setOnClickListener(v -> {

            if (currentUser != null) {
                checkUserSubscription();
            } else {
                Toast.makeText(SeatSelectionActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView imageView = findViewById(R.id.imageButton);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the dialog builder
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SeatSelectionActivity.this);

                // Create an ImageView to display the image
                ImageView imageView = new ImageView(SeatSelectionActivity.this);
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



    private void checkUserSubscription() {

        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Boolean isSubscribed = dataSnapshot.child("isSubscribed").getValue(Boolean.class);
                        if (isSubscribed != null && isSubscribed) {
                            // User is already subscribed
                            Toast.makeText(SeatSelectionActivity.this, "You are already subscribed", Toast.LENGTH_SHORT).show();
                        } else {
                            // User is not subscribed, proceed with booking
                            if (!selectedSlot.isEmpty() && isSeatSelected()) {
                                Intent intent = new Intent(getApplicationContext(), paymentActivity.class);
                                intent.putExtra("selectedSeatNumber", selectedSeatNumber);
                                intent.putExtra("selectedSlot", selectedSlot);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SeatSelectionActivity.this, "Please select both slot and seat", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }


    private void updateSeatAvailability() {
        for (Seat seat : seatsList) {
            seat.setStatus(Seat.Status.AVAILABLE);
        }

        Query morningEveningQuery = seatsRef.orderByChild("reserveStatus/" + selectedSlot).equalTo(true);
        morningEveningQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot seatSnapshot : dataSnapshot.getChildren()) {
                    Seat seat = seatSnapshot.getValue(Seat.class);
                    if (seat != null) {
                        Seat existingSeat = findSeatByNumber(seat.getNumber());
                        if (existingSeat != null) {
                            existingSeat.setStatus(Seat.Status.RESERVED);
                            existingSeat.addReserveStatus(getReserveStatus(selectedSlot));
                        }
                    }
                }
                seatAdapter.notifyDataSetChanged();

                Query fullDayQuery = seatsRef.orderByChild("reserveStatus/" + Seat.ReserveStatus.FULL_DAY).equalTo(true);
                fullDayQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot seatSnapshot : dataSnapshot.getChildren()) {
                            Seat seat = seatSnapshot.getValue(Seat.class);
                            if (seat != null) {
                                Seat existingSeat = findSeatByNumber(seat.getNumber());
                                if (existingSeat != null) {
                                    if (!existingSeat.hasReserveStatus(Seat.ReserveStatus.MORNING) &&
                                            !existingSeat.hasReserveStatus(Seat.ReserveStatus.EVENING)) {
                                        existingSeat.setStatus(Seat.Status.RESERVED);
                                        existingSeat.addReserveStatus(Seat.ReserveStatus.FULL_DAY);
                                    }
                                }
                            }
                        }
                        seatAdapter.notifyDataSetChanged();
                        // Check if any seat is reserved for full day, if yes, disable the button
                        boolean isAnySeatReservedForFullDay = isAnySeatReservedForFullDay();
                        btnConfirm.setEnabled(!isAnySeatReservedForFullDay);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private boolean isAnySeatReservedForFullDay() {
        for (Seat seat : seatsList) {
            if (seat.hasReserveStatus(Seat.ReserveStatus.FULL_DAY)) {
                return true;
            }
        }
        return false;
    }


    private boolean isSeatSelected() {
        for (Seat seat : seatsList) {
            if (seat.getStatus() == Seat.Status.SELECTED) {
                return true;
            }
        }
        return false;
    }

    private Seat findSeatByNumber(String seatNumber) {
        for (Seat seat : seatsList) {
            if (seat.getNumber().equals(seatNumber)) {
                return seat;
            }
        }
        return null;
    }

    private void deselectAllSeats() {
        for (Seat seat : seatsList) {
            if (seat.getStatus() == Seat.Status.SELECTED) {
                seat.setStatus(Seat.Status.AVAILABLE);
                seat.getReserveStatusList().clear();
            }
        }
    }

    private Seat.ReserveStatus getReserveStatus(String slot) {
        switch (slot) {
            case "Morning":
                return Seat.ReserveStatus.MORNING;
            case "Evening":
                return Seat.ReserveStatus.EVENING;
            default:
                return Seat.ReserveStatus.FULL_DAY;
        }
    }


}
