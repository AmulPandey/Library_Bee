package com.example.LibraryBee;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SeatSelectionActivity extends AppCompatActivity {

    private GridView gridView;
    private Button btnConfirm;
    private ArrayList<Seat> seatsList;
    private SeatAdapter seatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        gridView = findViewById(R.id.gridView);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Initialize seats list
        seatsList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seatsList.add(new Seat(i, Seat.Status.AVAILABLE));
        }

        // Set adapter for grid view
        seatAdapter = new SeatAdapter(this, seatsList);
        gridView.setAdapter(seatAdapter);

        // Handle seat selection
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Seat selectedSeat = seatsList.get(position);
                if (selectedSeat.getStatus() == Seat.Status.AVAILABLE) {
                    // Change seat status to selected
                    selectedSeat.setStatus(Seat.Status.SELECTED);
                    // Update adapter
                    seatAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Seat is already reserved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle confirm button click
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here you can implement the logic for confirming and paying for the selected seats
                // For example, you can save the selected seats information in Firebase
                // and then navigate to the payment screen
            }
        });
    }
}
