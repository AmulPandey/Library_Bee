package com.example.LibraryBee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Button btnViewMoreInfo;

    private List<User> userList;
    private List<User> filteredList; // New list to hold filtered items

    public UserAdapter(List<User> userList) {
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList); // Initialize filtered list with all items
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = filteredList.get(position); // Fetch from filtered list
        holder.textViewUsername.setText(user.getUsername());
        holder.textViewEmail.setText(user.getEmail());

        // Set click listener for "View More Info" button
        holder.btnViewMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle visibility of additional details TextViews
                if (holder.textViewPhoneNumber.getVisibility() == View.GONE) {
                    holder.textViewPhoneNumber.setVisibility(View.VISIBLE);
                    holder.textViewGender.setVisibility(View.VISIBLE);
                    holder.textViewSubscription.setVisibility(View.VISIBLE);
                } else {
                    holder.textViewPhoneNumber.setVisibility(View.GONE);
                    holder.textViewGender.setVisibility(View.GONE);
                    holder.textViewSubscription.setVisibility(View.GONE);
                }
            }
        });

        // Set additional details if needed
        holder.textViewPhoneNumber.setText("Phone Number: " + user.getPhoneNumber());
        holder.textViewGender.setText("Gender: " + user.getGender());
        holder.textViewSubscription.setText("Subscribed: " + (user.isSubscribed() ? "Yes" : "No"));
    }

    @Override
    public int getItemCount() {
        return filteredList.size(); // Return size of filtered list
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername, textViewEmail,textViewPhoneNumber,textViewGender,textViewSubscription;
        ImageView btnViewMoreInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            textViewGender = itemView.findViewById(R.id.textViewGender);
            textViewSubscription = itemView.findViewById(R.id.textViewSubscription);
            btnViewMoreInfo = itemView.findViewById(R.id.btnViewMoreInfo);
            // Initialize other TextViews here
        }
    }

    public void filter(String text) {
        filteredList.clear(); // Clear previous filtered items
        if (text.isEmpty()) {
            filteredList.addAll(userList); // If search query is empty, show all items
        } else {
            text = text.toLowerCase();
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(text) ||
                        user.getEmail().toLowerCase().contains(text)) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }
}



