package com.example.LibraryBee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {


    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private TextView signUpTextView;
    private FirebaseAuth auth;

    private Button loginAdminButton;

    TextView forgotPasswordButton;

    private void saveUserType(String userType) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("UserType", userType);
        editor.apply();
    }

    // Retrieve user type from SharedPreferences
    private String getUserType() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("UserType", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        loginAdminButton = findViewById(R.id.loginAdminButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordTextView);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is already authenticated
        if (auth.getCurrentUser() != null) {
            // User is already authenticated, navigate based on user type
            String userType = getUserType();
            if (userType.equals("Admin")) {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
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
                            // Check if the user is an admin
                            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admins").child(auth.getCurrentUser().getUid());
                            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // User is an admin but clicked login as a normal user
                                        // Direct to user dashboard
                                        saveUserType("User");
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // User is not an admin, direct to user dashboard
                                        saveUserType("User");
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                    finish(); // Close login activity
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle database error
                                    Toast.makeText(Login.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Sign in failed, display error message
                            Toast.makeText(Login.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        // Signup click listener
        signUpTextView.setOnClickListener(view -> {
            // Navigate to the Signup activity
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
        });

        // Login as Admin button click listener
        loginAdminButton.setOnClickListener(view -> {
            String adminEmail = "amulpandey007@gmail.com";
            String adminPassword = "654321";

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return; // Don't proceed with admin login if fields are empty
            }

            // Authenticate as admin using FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Login.this, task -> {
                        if (task.isSuccessful()) {
                            // Check if the admin entry exists in the database
                            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admins").child(auth.getCurrentUser().getUid());
                            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // Admin entry doesn't exist, create a new entry
                                        Admin admin = new Admin(adminEmail); // Create an Admin object with admin details
                                        adminRef.setValue(admin)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        // Admin entry created successfully
                                                        saveUserType("Admin"); // Save user type as Admin
                                                        Intent intent = new Intent(Login.this, AdminDashboardActivity.class);
                                                        startActivity(intent);
                                                        finish(); // Close login activity
                                                    } else {
                                                        // Failed to create admin entry
                                                        Toast.makeText(Login.this, "Failed to create admin entry in the database", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Admin entry already exists
                                        saveUserType("Admin"); // Save user type as Admin
                                        Intent intent = new Intent(Login.this, AdminDashboardActivity.class);
                                        startActivity(intent);
                                        finish(); // Close login activity
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle database error
                                    Toast.makeText(Login.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Sign in failed, display error message
                            Toast.makeText(Login.this, "Admin authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        forgotPasswordButton.setOnClickListener(v -> forgotPassword(v));

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
