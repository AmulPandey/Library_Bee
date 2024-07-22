package com.example.LibraryBee;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.LibraryBee.R;
import com.example.LibraryBee.Seat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Seat> seatsList;



    public SeatAdapter(Context context, ArrayList<Seat> seatsList) {
        this.context = context;
        this.seatsList = seatsList;
    }

    @Override
    public int getCount() {
        return seatsList.size();
    }

    @Override
    public Object getItem(int position) {
        return seatsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.grid_item, parent, false);
        } else {
            view = convertView;
        }

        TextView textViewSeatNumber = view.findViewById(R.id.textViewSeatNumber);
        TextView textViewSlot = view.findViewById(R.id.textViewSlot);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.VISIBLE);

        Seat seat = seatsList.get(position);

        textViewSeatNumber.setText(seat.getNumber());


        DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference().child("seats").child(seat.getNumber());

        seatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null) {
                        List<Seat.ReserveStatus> reserveStatusList = new ArrayList<>();

                        switch (status) {
                            case "AVAILABLE":
                                if (seat.getStatus() == Seat.Status.SELECTED) {
                                    view.setBackgroundColor(Color.GREEN);
                                } else {
                                    view.setBackgroundColor(Color.WHITE);
                                }
                                break;
                            default:
                                view.setBackgroundColor(Color.WHITE);
                                break;
                        }

                        for (DataSnapshot snapshot : dataSnapshot.child("reserveStatusList").getChildren()) {
                            boolean reserved = snapshot.getValue(Boolean.class);
                            if (reserved) {
                                switch (snapshot.getKey()) {
                                    case "MORNING":
                                        reserveStatusList.add(Seat.ReserveStatus.MORNING);
                                        break;
                                    case "EVENING":
                                        reserveStatusList.add(Seat.ReserveStatus.EVENING);
                                        break;
                                    case "FULL_DAY":
                                        reserveStatusList.add(Seat.ReserveStatus.FULL_DAY);
                                        break;
                                }
                            }
                        }

                        updateUI(view,reserveStatusList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                progressBar.setVisibility(View.GONE);
            }
        });

        progressBar.setVisibility(View.GONE);

        return view;
    }


    // Method to update UI based on reserve status list
    private void updateUI(View view, List<Seat.ReserveStatus> reserveStatusList) {

        if (reserveStatusList != null) {
            for (Seat.ReserveStatus status : reserveStatusList) {
                switch (status) {
                    case MORNING:
                        view.setBackgroundColor(Color.parseColor("#FFA500")); // Orange
                        break;
                    case EVENING:
                        view.setBackgroundColor(Color.BLUE);
                        break;
                    case FULL_DAY:
                        view.setBackgroundColor(Color.RED);
                        break;
                    default:
                        view.setBackgroundColor(Color.WHITE);
                        break;
                }
            }

            boolean isReservedForFullDay = reserveStatusList.contains(Seat.ReserveStatus.FULL_DAY);
            boolean isReservedForMorning = reserveStatusList.contains(Seat.ReserveStatus.MORNING);
            boolean isReservedForEvening = reserveStatusList.contains(Seat.ReserveStatus.EVENING);

             //Make the seat unclickable if reserved for full day or both morning and evening

            if (isReservedForMorning && isReservedForEvening) {
                    view.setBackgroundColor(Color.parseColor("#800080")); // Purple

            }


            } else {
            // Set default color for available seats
            view.setBackgroundColor(Color.WHITE);
        }
    }

}
