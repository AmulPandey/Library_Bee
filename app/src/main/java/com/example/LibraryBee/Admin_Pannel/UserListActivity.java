package com.example.LibraryBee.Admin_Pannel;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.LibraryBee.R;
import com.example.LibraryBee.User;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private DatabaseReference database;
    private ProgressBar progressBar;
    private UserAdapter adapter;
    private List<User> userList;
    private TextView textViewUserCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.LibraryBee.R.layout.activity_user_list);
        RecyclerView recyclerView = findViewById(com.example.LibraryBee.R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(com.example.LibraryBee.R.id.progressBar);
        SearchView searchView = findViewById(com.example.LibraryBee.R.id.searchView);
        textViewUserCount = findViewById(R.id.textViewUserCount);
        userList = new ArrayList<>();


        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false); // Expand the SearchView
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });


        // Show ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Initialize Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                }

                textViewUserCount.setText("Number of Users: " + userList.size());
                // Now you have userList, pass it to your RecyclerView adapter
                adapter = new UserAdapter(userList); // Assigning to the global adapter variable
                recyclerView.setAdapter(adapter);

                // Hide ProgressBar
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Hide ProgressBar
                progressBar.setVisibility(View.GONE);
                // Handle error
            }


        });


    }


}
