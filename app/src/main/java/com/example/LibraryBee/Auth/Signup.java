package com.example.LibraryBee.Auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LibraryBee.MainActivity;
import com.example.LibraryBee.R;
import com.example.LibraryBee.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Signup extends AppCompatActivity {

    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText phonenumberEditText;
    private RadioGroup answerRadioGroup;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private Button registerButton;

    private FirebaseAuth auth;
    private DatabaseReference usersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        phonenumberEditText = findViewById(R.id.phonenumberEditText);
        answerRadioGroup = findViewById(R.id.answerRadioGroup);
        maleRadioButton = findViewById(R.id.opt1);
        femaleRadioButton = findViewById(R.id.opt2);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.passwordEditText_2);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");


        // Register button click listener
        registerButton.setOnClickListener(view -> {

            ProgressDialog progressDialog = new ProgressDialog(Signup.this);
            progressDialog.setMessage("Registering...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            String email = emailEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String phoneNumber = phonenumberEditText.getText().toString().trim();
            String gender = maleRadioButton.isChecked() ? "Male" : "Female";
            String password = passwordEditText.getText().toString().trim();
            String repeatPassword = repeatPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(phoneNumber) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
                progressDialog.dismiss();
                Toast.makeText(Signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(repeatPassword)) {
                progressDialog.dismiss();
                Toast.makeText(Signup.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email or phone number already exists
            usersDatabase.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User with the same email already exists
                        progressDialog.dismiss();
                        Toast.makeText(Signup.this, "User with this email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // No user with the same email, check for phone number
                        usersDatabase.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // User with the same phone number already exists
                                    progressDialog.dismiss();
                                    Toast.makeText(Signup.this, "User with this phone number already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    // No user with the same email or phone number, proceed with registration
                                    auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(Signup.this, task -> {
                                                progressDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    // Get the UID of the newly registered user
                                                    String userId = auth.getCurrentUser().getUid();

                                                    // Set the initial subscription status (false for a new user)
                                                    boolean isSubscribed = false;

                                                    String joiningDate = getCurrentDate();

                                                    // Get the device token
                                                    getDeviceToken(Signup.this);


                                                    // Save additional user data to the Firebase Realtime Database
                                                    User user = new User(userId, email, username, phoneNumber, gender, isSubscribed);
                                                    usersDatabase.child(userId).setValue(user);
                                                    user.setJoiningDate(joiningDate);

                                                    user.setSubscriptionTimestamp(System.currentTimeMillis());
                                                    // Sign up successful, navigate to user dashboard
                                                    Intent intent = new Intent(Signup.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // Sign up failed, display error message
                                                    progressDialog.dismiss();
                                                    Toast.makeText(Signup.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        });

    }

    private String getCurrentDate() {
        // Get current date in day/month/year format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    private void getDeviceToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("Signup", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM registration token
                    String token = task.getResult();
                    Log.d("Signup", "FCM Registration Token: " + token);

                    // Save the token or send it to your server
                    saveDeviceToken(token);
                });
    }

    private void saveDeviceToken(String token) {
        // Implement this method to save the token or send it to your server
        // For example, you can save it to the Firebase Realtime Database
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDatabase.child(userId).child("deviceToken").setValue(token);
    }

}


