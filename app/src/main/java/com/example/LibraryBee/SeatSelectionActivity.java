package com.example.LibraryBee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatSelectionActivity extends AppCompatActivity {

    private RadioGroup radioGroupSlots;
    private GridView gridView;
    private Button btnConfirm;
    private ArrayList<Seat> seatsList;
    private SeatAdapter seatAdapter;
    private String selectedSlot = "";
    private String selectedSeatNumber = "";
    private DatabaseReference seatsRef;

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
            seatsList.add(new Seat(seatNumber, Seat.Status.AVAILABLE));
        }

        seatAdapter = new SeatAdapter(this, seatsList);
        gridView.setAdapter(seatAdapter);

        seatsRef = FirebaseDatabase.getInstance().getReference().child("seats");

        radioGroupSlots.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                selectedSlot = radioButton.getText().toString();
                updateSeatAvailability();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Seat selectedSeat = seatsList.get(position);
                if (selectedSeat.getStatus() == Seat.Status.AVAILABLE) {
                    // Deselect all seats
                    deselectAllSeats();
                    // Select the clicked seat
                    selectedSeat.setStatus(Seat.Status.SELECTED);
                    seatAdapter.notifyDataSetChanged();
                    selectedSeatNumber = selectedSeat.getNumber();
                } else if (selectedSeat.getStatus() == Seat.Status.SELECTED) {
                    // Deselect the clicked seat
                    selectedSeat.setStatus(Seat.Status.AVAILABLE);
                    seatAdapter.notifyDataSetChanged();
                    selectedSeatNumber = "";
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Seat is already reserved", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedSlot.isEmpty() && isSeatSelected()) {
                    Intent intent = new Intent(getApplicationContext(), paymentActivity.class);
                    intent.putExtra("selectedSeatNumber", selectedSeatNumber);
                    intent.putExtra("selectedSlot", selectedSlot);
                    startActivity(intent);
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Please select both slot and seat", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSeatAvailability() {
        for (Seat seat : seatsList) {
            seat.setStatus(Seat.Status.AVAILABLE);
        }

        Query query = seatsRef.orderByChild("reserveStatus/" + selectedSlot).equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot seatSnapshot : dataSnapshot.getChildren()) {
                    Seat seat = seatSnapshot.getValue(Seat.class);
                    if (seat != null) {
                        Seat existingSeat = findSeatByNumber(seat.getNumber());
                        if (existingSeat != null) {
                            existingSeat.setStatus(Seat.Status.RESERVED);
                        }
                    }
                }
                seatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void updateSeatStatus(Seat seat) {
        seat.setStatus(Seat.Status.SELECTED);
        seatsRef.child(seat.getNumber()).setValue(seat);
        seatAdapter.notifyDataSetChanged();
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

    private void updateSeatReservationStatus(Seat seat) {
        DatabaseReference seatsRef = FirebaseDatabase.getInstance().getReference().child("seats");
        // Find the seat in Firebase based on its number
        Query query = seatsRef.orderByChild("number").equalTo(seat.getNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot seatSnapshot : dataSnapshot.getChildren()) {
                    // Update the seat with the new data
                    seatSnapshot.getRef().setValue(seat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

}
