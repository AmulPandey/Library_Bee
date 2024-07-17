package com.example.LibraryBee.Admin_Pannel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.LibraryBee.Admin_Pannel.Request;
import com.example.LibraryBee.R;

import java.util.List;

public class RequestAdapter extends ArrayAdapter<Request> {

    private Context context;
    private List<Request> requestList;

    public RequestAdapter(Context context, List<Request> requestList) {
        super(context, 0, requestList);
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);
        }

        Request request = requestList.get(position);

        TextView userNameTextView = listItemView.findViewById(R.id.user_name);
        TextView userIdTextView = listItemView.findViewById(R.id.user_id);
        TextView seatNumberTextView = listItemView.findViewById(R.id.seat_number);
        TextView slotTextView = listItemView.findViewById(R.id.slot);
        TextView amountTextView = listItemView.findViewById(R.id.amount);

        // Set username and userID
        String userNameWithId = request.getUserName() + " (ID: " + request.getUserId() + ")";
        userNameTextView.setText(userNameWithId);

        seatNumberTextView.setText("Seat: " + request.getSelectedSeatNumber());
        slotTextView.setText("Slot: " + request.getSelectedSlot());
        amountTextView.setText("Amount: " + request.getAmount());

        return listItemView;
    }
}
