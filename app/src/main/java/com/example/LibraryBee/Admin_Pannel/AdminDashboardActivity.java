package com.example.LibraryBee.Admin_Pannel;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.LibraryBee.Auth.Login;
import com.example.LibraryBee.User_Pannel.Message;
import com.example.LibraryBee.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    private Button userListButton;

    private Button seat;

    private  Button button4;

    private Uri mImageUri;

    private CircleImageView profileImageView;

    private String imageUrl;

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();

        fetchProfileImage();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Set toolbar navigation click listener
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set toolbar background color
        //toolbar.setBackgroundColor(getResources().getColor(R.color.black));

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setBackgroundColor(getResources().getColor(android.R.color.black));

        View headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.imageViewProfile);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                mImageUri = uri;
                uploadImageToFirebaseStorage();
            }
        });


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile_change) {
                // Handle profile settings action
                choosePhotoFromGallery();
            } else if (id == R.id.nav_logout) {
                // Handle logout action
                showLogoutDialog();
            } else if (id == R.id.nav_contact_Library) {
                // Handle contact library action
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Contact Library")
                        .setMessage("Phone: 7007084705")
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
                        .setMessage("Phone: 9936474273\nEmail: amulpandey007@gmail.com")
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
                showLogoutDialog(); // Call the method to show the logout dialog
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

        // Create a Message object
        Message message = new Message(messageText, "LibraryBee", System.currentTimeMillis());

        // Save the message to Firebase Realtime Database
        databaseReference.child(messageId).setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AdminDashboardActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();

                        // Send a push notification to all devices
                        //sendPushNotification(messageText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminDashboardActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void choosePhotoFromGallery() {
        galleryLauncher.launch("image/*");
    }




    private void uploadImageToFirebaseStorage() {
        if (mImageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final StorageReference imageReference = FirebaseStorage.getInstance().getReference("userprofiles").child(auth.getCurrentUser().getUid());
            imageReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        // Image uploaded successfully
                        // Now you can get the download URL and do something with it if needed
                        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Handle the download URL (e.g., save it to a database)
                            imageUrl = uri.toString();
                            // Update profileImageView with the uploaded image
                            fetchProfileImage();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        // Handle the error
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void fetchProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the reference to the Firebase Storage path for the user's profile image
            StorageReference imageReference = FirebaseStorage.getInstance().getReference()
                    .child("userprofiles")
                    .child(currentUser.getUid());

            // Fetch the download URL of the image
            imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Load the image into the profileImageView using Glide or Picasso
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.profile) // Placeholder image while loading
                        .error(R.drawable.profile) // Image to display in case of error
                        .into(profileImageView);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e(TAG, "Failed to fetch profile image: " + exception.getMessage());
            });
        } else {
            Log.e(TAG, "User is not authenticated.");
            // Handle the case where the user is not authenticated
        }
    }

}
