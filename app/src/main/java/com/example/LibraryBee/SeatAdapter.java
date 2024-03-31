package com.example.LibraryBee;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.grid_item, parent, false);
        }

        TextView textViewSeatNumber = view.findViewById(R.id.textViewSeatNumber);
        TextView textViewSlot = view.findViewById(R.id.textViewSlot);

        Seat seat = seatsList.get(position);

        textViewSeatNumber.setText(seat.getNumber());

        List<Seat.ReserveStatus> reserveStatusList = seat.getReserveStatusList();
        if (reserveStatusList != null && !reserveStatusList.isEmpty()) {
            StringBuilder reserveStatusText = new StringBuilder();
            for (Seat.ReserveStatus reserveStatus : reserveStatusList) {
                reserveStatusText.append(reserveStatus.toString()).append(", ");
            }
            textViewSlot.setVisibility(View.VISIBLE);
            textViewSlot.setText(reserveStatusText.toString());
        } else {
            textViewSlot.setVisibility(View.GONE);
        }

        switch (seat.getStatus()) {
            case AVAILABLE:
                view.setBackgroundColor(Color.WHITE);
                break;
            case SELECTED:
                view.setBackgroundColor(Color.GREEN);
                break;
            case RESERVED:
                int backgroundColor;
                if (reserveStatusList != null && reserveStatusList.size() == 2) {
                    // Seat has both morning and evening slots, set color to purple
                    backgroundColor = Color.parseColor("#800080"); // Purple
                } else if (reserveStatusList != null && !reserveStatusList.isEmpty()) {
                    // Seat has only one slot
                    switch (reserveStatusList.get(0)) {
                        case FULL_DAY:
                            backgroundColor = Color.RED;
                            break;
                        case MORNING:
                            backgroundColor = Color.parseColor("#FFA500"); // Orange
                            break;
                        case EVENING:
                            backgroundColor = Color.BLUE;
                            break;
                        default:
                            backgroundColor = Color.WHITE; // Default color
                            break;
                    }
                } else {
                    backgroundColor = Color.WHITE; // Default color
                }
                view.setBackgroundColor(backgroundColor);
                break;
        }

        return view;
    }

}
