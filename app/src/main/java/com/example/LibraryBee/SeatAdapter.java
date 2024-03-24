package com.example.LibraryBee;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.grid_item, null);
        }

        TextView textView = view.findViewById(R.id.textViewSeatNumber);
        Seat seat = seatsList.get(position);

        textView.setText(String.valueOf(seat.getNumber()));

        switch (seat.getStatus()) {
            case AVAILABLE:
                view.setBackgroundColor(Color.GREEN);
                break;
            case SELECTED:
                view.setBackgroundColor(Color.RED);
                break;
            case RESERVED:
                view.setBackgroundColor(Color.BLUE);
                break;
        }

        return view;
    }
}
