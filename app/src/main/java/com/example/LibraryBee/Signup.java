package com.example.LibraryBee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.LibraryBee.Login;
import com.example.LibraryBee.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

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
            String email = emailEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String phoneNumber = phonenumberEditText.getText().toString().trim();
            String gender = maleRadioButton.isChecked() ? "Male" : "Female";
            String password = passwordEditText.getText().toString().trim();
            String repeatPassword = repeatPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(phoneNumber) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
                Toast.makeText(Signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(repeatPassword)) {
                Toast.makeText(Signup.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication - Create user
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Signup.this, task -> {
                        if (task.isSuccessful()) {
                            // Get the UID of the newly registered user
                            String userId = auth.getCurrentUser().getUid();

                            // Set the initial subscription status (false for a new user)
                            boolean isSubscribed = false;

                            // Set the initial last payment timestamp to the current time



                            // Save additional user data to the Firebase Realtime Database
                            User user = new User(userId, email, username, phoneNumber, gender, isSubscribed);
                            usersDatabase.child(userId).setValue(user);

                            user.setSubscriptionTimestamp(System.currentTimeMillis());

                            // Sign up successful, navigate to user dashboard
                            Intent intent = new Intent(Signup.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign up failed, display error message
                            Toast.makeText(Signup.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}


