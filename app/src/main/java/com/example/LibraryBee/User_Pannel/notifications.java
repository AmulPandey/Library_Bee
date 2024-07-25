package com.example.LibraryBee.User_Pannel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.LibraryBee.Message;
import com.example.LibraryBee.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class notifications extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseReference databaseReference;
    private TextView noNotificationsTextView;


    public notifications() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificatios, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Initialize message list
        messageList = new ArrayList<>();

        // Set up message adapter
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        noNotificationsTextView = view.findViewById(R.id.noNotificationsTextView);

        // Listen for new messages
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                List<Message> tempList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        // Convert timestamp to a readable format
                        long timestamp = message.getTimestamp();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(timestamp));

                        // Update the message object with the formatted timestamp
                        message.setFormattedTimestamp(formattedDate);

                        tempList.add(message);
                    }
                }

                // Sort messages by timestamp
                Collections.sort(tempList, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return Long.compare(m2.getTimestamp(), m1.getTimestamp());
                    }
                });



                // Update the adapter and visibility of RecyclerView and TextView
                messageList.addAll(tempList);
                messageAdapter.notifyDataSetChanged();

                if (messageList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noNotificationsTextView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noNotificationsTextView.setVisibility(View.GONE);
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
