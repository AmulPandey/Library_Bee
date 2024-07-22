package com.example.LibraryBee.Admin_Pannel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LibraryBee.Request;
import com.example.LibraryBee.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requests;
    private Context context;

    public RequestAdapter(List<Request> requests, Context context) {
        this.requests = requests;
        this.context = context;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView userNameTextView;
        private TextView userIdTextView;
        private TextView seatNumberTextView;
        private TextView slotTextView;
        private TextView amountTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name);
            userIdTextView = itemView.findViewById(R.id.user_id);
            seatNumberTextView = itemView.findViewById(R.id.seat_number);
            slotTextView = itemView.findViewById(R.id.slot);
            amountTextView = itemView.findViewById(R.id.amount);
        }

        public void bind(Request request) {
            userNameTextView.setText("Username: " + request.getUserName());
            userIdTextView.setText("UserId: " + request.getUserId());
            seatNumberTextView.setText("Seat: " + request.getSelectedSeatNumber());
            slotTextView.setText("Slot: " + request.getSelectedSlot());
            amountTextView.setText("Amount: " + request.getAmount());
        }
    }
}
