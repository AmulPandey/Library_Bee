package com.example.LibraryBee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is already authenticated
        if (auth.getCurrentUser() != null) {
            // User is already authenticated, navigate to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Close login activity
        }

        // Login button click listener
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Login.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in successful, navigate to user dashboard
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Close login activity
                        } else {
                            // Sign in failed, display error message
                            Toast.makeText(Login.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Signup button click listener
        signupButton.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
        });
    }

    public void forgotPassword(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.forgetpass, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        Button submitBtn = dialogView.findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                // Proceed with sending the password reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}
