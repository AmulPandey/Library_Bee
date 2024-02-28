package com.example.LibraryBee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private Button registerButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.passwordEditText_2);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Register button click listener
        registerButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String repeatPassword = repeatPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
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
                            // Sign up successful, navigate to user dashboard
                            Intent intent = new Intent(Signup.this, Login.class); // Assuming this is your login activity
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
