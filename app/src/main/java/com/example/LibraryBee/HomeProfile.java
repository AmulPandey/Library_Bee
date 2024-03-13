package com.example.LibraryBee;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

public class HomeProfile extends Activity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_profile);

        auth = FirebaseAuth.getInstance();

        Button logOutButton = findViewById(R.id.logout_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        // Rest of your code...
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Sign out
                    auth.signOut();

                    // Redirect to the login page
                    Intent loginIntent = new Intent(HomeProfile.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish(); // Close the current activity
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User clicked Cancel, do nothing
                });

        builder.create().show();
    }

    // Rest of your code...
}
