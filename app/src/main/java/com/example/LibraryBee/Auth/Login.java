package com.example.LibraryBee.Auth;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.LibraryBee.Admin_Pannel.AdminDashboardActivity;
import com.example.LibraryBee.R;
import com.example.LibraryBee.User_Pannel.UserDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class Login extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private Button loginAdminButton;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth auth;

    private TextView forgotPasswordTextView;
    private LinearLayout loginLayout;

    private static final int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Adjust content to fit system windows
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        initializeViews();
        auth = FirebaseAuth.getInstance();
        checkCurrentUser();
        if (isLoggedInAsAdmin()) {
            navigateToDashboard("Admin");
            finish();
        } else {
            checkCurrentUser();
        }
        setListeners();

        // Initialize Firebase Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Set default Remote Config parameter values
        //mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        // Fetch Remote Config parameters
        fetchRemoteConfig();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            requestPermissions();
        } else {
            // Permissions are already granted, proceed with your app's logic
        }

    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signUpTextView = findViewById(R.id.signUpTextView);
        loginAdminButton = findViewById(R.id.loginAdminButton);
        loginLayout = findViewById(R.id.loginLayout);

    }

    private void checkCurrentUser() {
        if (auth.getCurrentUser() != null) {
            String userType = getUserType();
            navigateToDashboard(userType);
            finish();
        }
    }

    private boolean isLoggedInAsAdmin() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = prefs.getString("UserType", "");
        return "Admin".equals(userType);
    }

    private void setListeners() {
        loginButton.setOnClickListener(view -> {
            loginUser();
        });

        forgotPasswordTextView.setOnClickListener(view -> {
            // Handle the click on "Forgot Password?" TextView
            forgotPassword();
        });

        signUpTextView.setOnClickListener(view -> navigateToSignup());

        loginAdminButton.setOnClickListener(view -> {
            loginAsAdmin();
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please enter email and password");
            return;
        }

        loginLayout.setVisibility(View.GONE);
        // Show overlay with Lottie animation
        FrameLayout overlay = findViewById(R.id.overlay);
        overlay.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        checkAdminStatus();
                    } else {
                        showToast("Authentication failed. " + task.getException().getMessage());
                        overlay.setVisibility(View.GONE);
                        loginLayout.setVisibility(View.VISIBLE);

                    }
                });
    }



    private void checkAdminStatus() {
        String adminEmail = mFirebaseRemoteConfig.getString("admin_email");
        String adminPassword = mFirebaseRemoteConfig.getString("admin_password");
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.equals(adminEmail) && password.equals(adminPassword)) {
            saveUserType("Admin");
            navigateToDashboard("Admin");
            finish();
        } else {
            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admins")
                    .child(auth.getCurrentUser().getUid());
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userType = snapshot.exists() ? "Admin" : "User";
                    saveUserType(userType);
                    navigateToDashboard(userType);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Database error: " + error.getMessage());
                }
            });
        }
    }

    private void fetchRemoteConfig() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // Fetch interval in seconds
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        Log.d("RemoteConfig", "Config params updated: " + updated);
                    } else {
                        Log.d("RemoteConfig", "Fetch failed");
                    }
                });
    }

    private void loginAsAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please enter email and password");
            return;
        }

        String adminEmail = mFirebaseRemoteConfig.getString("admin_email");
        String adminPassword = mFirebaseRemoteConfig.getString("admin_password");

        if (!email.equals(adminEmail) || !password.equals(adminPassword)) {
            showToast("Invalid admin credentials");
            return;
        }

        saveUserType("Admin");
        navigateToDashboard("Admin");
        finish();
    }

    private void navigateToSignup() {
        startActivity(new Intent(Login.this, Signup.class));
    }

    private void navigateToDashboard(String userType) {
        Intent intent = userType.equals("Admin") ?
                new Intent(Login.this, AdminDashboardActivity.class) :
                new Intent(Login.this, UserDashboardActivity.class);
        startActivity(intent);
    }

    private void saveUserType(String userType) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("UserType", userType).apply();
    }

    private String getUserType() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("UserType", "");
    }

    private void showToast(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }

    public void forgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.forgetpass, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        Button submitBtn = dialogView.findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Password reset email sent");
                            } else {
                                showToast("Failed to send reset email");
                            }
                        });
                dialog.dismiss();
            } else {
                showToast("Please enter your email");
            }
        });

        dialog.show();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS
        }, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions are granted, proceed with your app's logic
            } else {
                // Permissions are denied, show a message to the user
            }
        }
    }
}
