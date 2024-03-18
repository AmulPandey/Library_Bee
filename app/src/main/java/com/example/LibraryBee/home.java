package com.example.LibraryBee;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class home extends Fragment {

    private Toolbar toolbar;

    private DrawerLayout drawerLayout;



    private DatabaseReference subscriptionRef;
    private FirebaseAuth auth;
    private Button btn1; // Change the type to Button

    private TextView usernameTextView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        drawerLayout = view.findViewById(R.id.drawer_layout);


        NavigationView navigationView = view.findViewById(R.id.navigation_view);
        navigationView.setBackgroundColor(getResources().getColor(android.R.color.black));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation drawer item clicks
                int id = item.getItemId();
                if (id == R.id.nav_profile_settings) {
                    // Handle profile settings action
                    Toast.makeText(getActivity(), "Profile Settings clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    // Handle logout action
                    showLogoutDialog(); // Call the method to show the logout dialog
                }

                // Close the drawer after handling the click
                drawerLayout.closeDrawers();
                return true;
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
               // navigationView.setBackgroundColor(getResources().getColor(android.R.color.black));
                getActivity().invalidateOptionsMenu(); // If you have action items to hide/show
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu(); // If you have action items to hide/show
            }
        };
        drawerLayout.addDrawerListener(toggle);

// Setting custom toggle icon
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu); // Set your custom drawable here
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });


        // Correctly find the ImageView
        btn1 = view.findViewById(R.id.btn1); // Initialize btn1 once
        usernameTextView = view.findViewById(R.id.usernameTextView);
        // Set click listener and start activity


        // Fetch and set the subscription status
        fetchSubscriptionStatus();
        fetchUsername();

        Button btn2 = view.findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), paymentActivity.class);
                startActivity(intent);
            }
        });

        registerForContextMenu(toolbar);

        return view;
    }


    private void fetchSubscriptionStatus() {
        if (isAdded()) {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Reference to the user's isSubscribed status in Firebase
                DatabaseReference isSubscribedRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("isSubscribed");

                // Read the isSubscribed value from Firebase
                isSubscribedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (isAdded() && dataSnapshot.exists()) {
                            // isSubscribed value exists, retrieve its value
                            boolean isSubscribed = dataSnapshot.getValue(Boolean.class);

                            // Update the UI based on the subscription status
                            updateUI(isSubscribed);
                        } else {
                            // isSubscribed doesn't exist or is null
                            // Handle this case based on your application's logic
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                    }
                });
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }
    }

    private void fetchUsername() {
        if (isAdded()) {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Reference to the user's username in Firebase
                DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("username");

                // Read the username value from Firebase
                usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (isAdded() && dataSnapshot.exists()) {
                            // Username exists, retrieve its value
                            String username = dataSnapshot.getValue(String.class);
                            // Set the username in the TextView
                            if (usernameTextView != null) {
                                usernameTextView.setText(username);
                            } else {
                                // Handle the case where usernameTextView is null (e.g., log an error)
                            }
                        } else {
                            // Username doesn't exist or is null
                            // Handle this case (e.g., display default message)
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors here
                    }
                });
            } else {
                // User is not authenticated, handle accordingly (e.g., redirect to login screen)
            }
        }
    }

    private void updateUI(boolean isSubscribed) {
        if (isSubscribed) {
            // Set ACTIVE with green color
            btn1.setText("ACTIVE");
            btn1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // Set INACTIVE with red color
            btn1.setText("INACTIVE");
            btn1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }


    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Sign out
                    auth.signOut();

                    // Redirect to the login page
                    Intent loginIntent = new Intent(getActivity(), Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    getActivity().finish(); // Close the current activity
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User clicked Cancel, do nothing
                });

        builder.create().show();
    }


}


