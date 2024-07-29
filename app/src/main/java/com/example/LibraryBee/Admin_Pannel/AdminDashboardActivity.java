package com.example.LibraryBee.Admin_Pannel;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.deeplabstudio.fcmsend.FCMSend;
import com.example.LibraryBee.Auth.Login;
import com.example.LibraryBee.Message;

import com.example.LibraryBee.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    private Button userListButton;

    private Button seat;

    private  Button button4;
    private View fragmentRootLayout;

    SwitchCompat darkModeToggle;



    private ShapeableImageView profileImageView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static String serverKey="" ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);

        View headerView = navigationView.getHeaderView(0);

        darkModeToggle = headerView.findViewById(R.id.dark_mode_toggle);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        String adminId = "LibraryBee";
        Admin admin = new Admin(adminId);

        DatabaseReference adminRef = databaseReference.child("admin");
        adminRef.setValue(admin);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("LibraryBeePrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        darkModeToggle.setChecked(isDarkMode);

        darkModeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Recreate the activity to apply the theme change
            recreate();
        });


        auth = FirebaseAuth.getInstance();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Set Remote Config settings
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // Fetch interval
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Fetch the remote config values
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Apply the fetched values
                        serverKey = mFirebaseRemoteConfig.getString("fcm_server_key");
                        // You can now use `serverKey` as needed
                    } else {
                        // Handle errors
                        Log.e("AdminDashboardActivity", "Failed to fetch server key", task.getException());
                    }
                });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentRootLayout = findViewById(R.id.root_layout);
        if (toolbar != null) {
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white)); // White color
        }

        userListButton = findViewById(R.id.userlistbutton);
        seat = findViewById(R.id.button2);
        button4 = findViewById(R.id.button4);

        userListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the UserListActivity
                Intent intent = new Intent(AdminDashboardActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });

        seat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the UserListActivity
                Intent intent = new Intent(AdminDashboardActivity.this , SeatManagementActivity.class);
                startActivity(intent);
            }
        });

        Button sendMessageButton = findViewById(R.id.button_send_message);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a dialog box for the admin to type the message
                openSendMessageDialog();

            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this , RequestHandeling.class);
                startActivity(intent);

            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu); // Set custom drawer icon
        toggle.syncState();

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // This method is called when the drawer is being slid
                // You can animate other views based on the slideOffset
                // For example, you might animate the toolbar's alpha
                toolbar.setAlpha(1 - slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // This method is called when the drawer is fully opened
                // Perform any action you want when the drawer opens
                // Animate the fragment's root layout
                ObjectAnimator animator = ObjectAnimator.ofFloat(fragmentRootLayout, "translationX", 0f, 50f);
                animator.setDuration(300); // Duration of the animation
                animator.start();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // This method is called when the drawer is fully closed
                // Perform any action you want when the drawer closes
                // Reverse the animation
                ObjectAnimator animator = ObjectAnimator.ofFloat(fragmentRootLayout, "translationX", 50f, 0f);
                animator.setDuration(300); // Duration of the animation
                animator.start();
            }
        });

        // Set toolbar navigation click listener
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set toolbar background color
        //toolbar.setBackgroundColor(getResources().getColor(R.color.black));


//        navigationView.setBackgroundColor(getResources().getColor(android.R.color.white));
//        // Change the text color of the menu items
//        Menu menu = navigationView.getMenu();
//        for (int i = 0; i < menu.size(); i++) {
//            MenuItem item = menu.getItem(i);
//            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
//            spanString.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.black)), 0, spanString.length(), 0);
//            item.setTitle(spanString);
//
//            if (i == 0) { // Assuming logout is the 1st item in the menu
//                item.setIcon(R.drawable.logouticon); // Replace with your logout icon
//            }
//            else {
//                item.setIcon(R.drawable.contacttoauthors);
//            }
//        }


        profileImageView = headerView.findViewById(R.id.imageViewProfile);

        profileImageView.setImageResource(R.drawable.bee_logo);


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_contact_Library) {
                // Handle contact library action
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Contact Library")
                        .setMessage("Phone: 7007084705\n\nAddress: House No.76, 3/1, Site No. 1, Juhi Kalan, Kidwai Nagar, Kanpur, Uttar Pradesh 208011")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, just close the dialog
                            }
                        });
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if (id == R.id.nav_contact_developer) {
                // Handle contact library action
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Contact Developer")
                        .setMessage("Phone: 9936474273\n\nEmail: amulpandey007@gmail.com")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, just close the dialog
                            }
                        });
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }else if (id == R.id.nav_logout) {
                // Handle logout action
                showLogoutDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Close the app
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> {
                    // Handle dialog dismiss
                    // This method is called when the dialog is dismissed
                })
                .show();
    }




    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Sign out
                    auth.signOut();
                    clearUserType();

                    // Redirect to the login page
                    Intent loginIntent = new Intent(this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish(); // Close the current activity
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User clicked Cancel, do nothing
                });

        builder.create().show();
    }

    private void clearUserType() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }


    private void openSendMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminDashboardActivity.this);
        builder.setTitle("Send Message");

        // Set up the input
        final EditText input = new EditText(AdminDashboardActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String messageText = input.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    sendMessageToServer(messageText);
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendMessageToServer(String messageText) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Create a unique key for the message
        String messageId = databaseReference.push().getKey();

        // Create a Message object (Assuming you have a Message class)
        Message message = new Message(messageText, "LibraryBee", System.currentTimeMillis());

        // Save the message to Firebase Realtime Database
        databaseReference.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDashboardActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();

                    // Send a push notification to all devices
                    sendPushNotification(messageText);

                    // Check the total number of messages
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long totalMessages = dataSnapshot.getChildrenCount();
                            if (totalMessages > 100) {
                                // Delete the oldest message
                                DataSnapshot oldestMessageSnapshot = null;
                                long oldestTimestamp = Long.MAX_VALUE;
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    long timestamp = messageSnapshot.getValue(Message.class).getTimestamp();
                                    if (timestamp < oldestTimestamp) {
                                        oldestTimestamp = timestamp;
                                        oldestMessageSnapshot = messageSnapshot;
                                    }
                                }
                                if (oldestMessageSnapshot!= null) {
                                    oldestMessageSnapshot.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(AdminDashboardActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show());
    }


    private void sendPushNotification(String messageText) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve all device tokens
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String deviceToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (deviceToken != null && !deviceToken.isEmpty()) {
                        // Send notification to the device token
                        sendNotificationToDevice(deviceToken, messageText);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to retrieve device tokens.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendNotificationToDevice(String deviceToken, String messageText) {

        FCMSend.SetServerKey(serverKey);

        FCMSend.Builder build = new FCMSend.Builder(deviceToken)
                .setBody(messageText)
                .setTitle("Library Bee");
        String result = build.send().Result();



        if (result!= null) {
            // Notification sent successfully
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AdminDashboardActivity.this, "Push notification sent successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Failed to send notification
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to send push notification.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}














