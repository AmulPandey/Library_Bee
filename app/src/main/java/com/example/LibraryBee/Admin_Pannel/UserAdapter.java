package com.example.LibraryBee.Admin_Pannel;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LibraryBee.R;
import com.example.LibraryBee.User_Pannel.Seat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        holder.textViewUserid.setText((user.getUserId()));


        // Set click listener for "View More Info" button
        holder.btnViewMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle visibility of additional details TextViews
                if (holder.textViewPhoneNumber.getVisibility() == View.GONE) {
                    holder.textViewPhoneNumber.setVisibility(View.VISIBLE);
                    holder.textViewGender.setVisibility(View.VISIBLE);
                    holder.textViewSubscription.setVisibility(View.VISIBLE);
                    holder.textViewEmail.setVisibility(View.VISIBLE);
                    holder.textViewseatNumber.setVisibility(View.VISIBLE);
                    holder.textViewtimingSlot.setVisibility(View.VISIBLE);
                    holder.textViewjoiningDate.setVisibility(View.VISIBLE);
                    holder.textViewSubsdate.setVisibility(View.VISIBLE);

                } else {
                    holder.textViewPhoneNumber.setVisibility(View.GONE);
                    holder.textViewGender.setVisibility(View.GONE);
                    holder.textViewSubscription.setVisibility(View.GONE);
                    holder.textViewEmail.setVisibility(View.GONE);
                    holder.textViewseatNumber.setVisibility(View.GONE);
                    holder.textViewtimingSlot.setVisibility(View.GONE);
                    holder.textViewjoiningDate.setVisibility(View.GONE);
                    holder.textViewSubsdate.setVisibility(View.GONE);
                }
            }
        });

        // Set additional details if needed
        holder.textViewPhoneNumber.setText("Phone Number: " + user.getPhoneNumber());
        holder.textViewGender.setText("Gender: " + user.getGender());
        holder.textViewSubscription.setText("Subscribed: " + (user.isSubscribed() ? "Yes" : "No"));
        holder.textViewEmail.setText("email:"+user.getEmail());
        holder.textViewseatNumber.setText("Seat No:"+user.getSeatNumber());
        holder.textViewtimingSlot.setText("Slot:"+user.getTimingSlot());
        holder.textViewjoiningDate.setText("Joined In:"+user.getJoiningDate());
        holder.textViewSubsdate.setText("Last Subscription: " + user.getSubscriptionDateAsString());

    }

    @Override
    public int getItemCount() {
        return filteredList.size(); // Return size of filtered list
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername, textViewEmail, textViewPhoneNumber, textViewGender,
                textViewSubscription, textViewUserid, textViewseatNumber, textViewtimingSlot,
                textViewjoiningDate,textViewSubsdate;
        ImageView btnViewMoreInfo;
        ImageView imgcontact;
        View additionalDetailsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            textViewGender = itemView.findViewById(R.id.textViewGender);
            textViewSubscription = itemView.findViewById(R.id.textViewSubscription);
            textViewUserid = itemView.findViewById(R.id.textViewUserid);
            textViewseatNumber = itemView.findViewById(R.id.textViewseatNumber);
            textViewtimingSlot = itemView.findViewById(R.id.textViewtimingSlot);
            textViewjoiningDate = itemView.findViewById(R.id.textViewjoiningDate);
            btnViewMoreInfo = itemView.findViewById(R.id.btnViewMoreInfo);
            additionalDetailsLayout = itemView.findViewById(R.id.additionalDetailsLayout);
            textViewSubsdate = itemView.findViewById(R.id.textViewSubsdate);
            imgcontact = itemView.findViewById(R.id.imgcontact);

            // Check if additional details layout is present before accessing its child views
            if (additionalDetailsLayout != null) {
                textViewEmail = additionalDetailsLayout.findViewById(R.id.textViewEmail);
                textViewPhoneNumber = additionalDetailsLayout.findViewById(R.id.textViewPhoneNumber);
                textViewGender = additionalDetailsLayout.findViewById(R.id.textViewGender);
                textViewSubscription = additionalDetailsLayout.findViewById(R.id.textViewSubscription);
                textViewseatNumber = additionalDetailsLayout.findViewById(R.id.textViewseatNumber);
                textViewtimingSlot = additionalDetailsLayout.findViewById(R.id.textViewtimingSlot);
                textViewjoiningDate = additionalDetailsLayout.findViewById(R.id.textViewjoiningDate);
                textViewSubsdate = additionalDetailsLayout.findViewById(R.id.textViewSubsdate);
            }

            imgcontact.setOnLongClickListener(v -> {
                // Get the position of the item in the RecyclerView
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Handle long press action here
                    User user = filteredList.get(position);
                    showDeleteConfirmationDialog(itemView.getContext(), user);
                    return true; // Consume the long click event
                }
                return false;
            });


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
    } public void addUser(User user) {
        userList.add(user);
        if (filteredList.contains(user)) {
            filteredList.add(user);
        }
        notifyDataSetChanged();
    }

    public void removeUser(User user) {
        userList.remove(user);
        filteredList.remove(user);
        notifyDataSetChanged();
    }

    public User getItem(int position) {
        return filteredList.get(position);
    }


    private void showDeleteConfirmationDialog(Context context, User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to remove this user?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Reference to the user's database node
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUserId());
                DatabaseReference userseatNumberRef = userRef.child("seatNumber");
                DatabaseReference usertimingSlotRef = userRef.child("timingSlot");
                // Fetch the user's seat number and slot before deleting the user
                userRef.child("seatNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String seatNumber = dataSnapshot.getValue(String.class);
                        if (seatNumber != null) {
                            // Update the seat status in Firebase
                            DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference("seats").child(seatNumber);
                            seatRef.child("status").setValue("AVAILABLE");
                            seatRef.child("reservationTimestamp").removeValue(); // Remove reservation timestamp

                            // Update reserve status list
                            DatabaseReference reserveStatusListRef = seatRef.child("reserveStatusList");
                            reserveStatusListRef.child(Seat.ReserveStatus.MORNING.name()).setValue(false);
                            reserveStatusListRef.child(Seat.ReserveStatus.EVENING.name()).setValue(false);
                            reserveStatusListRef.child(Seat.ReserveStatus.FULL_DAY.name()).setValue(false);
                        }

                        userRef.setValue(false);
                        userseatNumberRef.setValue("none");
                        usertimingSlotRef.setValue("none");

                        // Remove the user from Firebase
                        userRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Optionally, you can also remove the user from the local list and update the adapter
                                    userList.remove(user);
                                    removeUser(user);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Please Re-Open this Page To Refresh", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to remove user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors.
                        Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



}