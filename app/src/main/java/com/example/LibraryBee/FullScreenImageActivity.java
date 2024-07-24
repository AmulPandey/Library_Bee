package com.example.LibraryBee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FullScreenImageActivity extends AppCompatActivity {
    private ImageView fullScreenImageView;
    private Button deleteButton;
    private Button changeprofilebutton;

    private Uri mImageUri;
    private String imageUrl;
    private String userId;

    private FirebaseAuth auth;

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        deleteButton = findViewById(R.id.deleteButton);
        changeprofilebutton = findViewById(R.id.changeprofileButton);

        auth = FirebaseAuth.getInstance();

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                mImageUri = uri;
                uploadImageToFirebaseStorage();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            imageUrl = intent.getStringExtra("IMAGE_URL");
            userId = intent.getStringExtra("USER_ID");

            if (imageUrl != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .into(fullScreenImageView);
            } else {
                fullScreenImageView.setImageResource(R.drawable.userprofile);
            }
        }

        deleteButton.setOnClickListener(v -> {
            deleteImage();
        });

        changeprofilebutton.setOnClickListener(v -> {
            choosePhotoFromGallery();
        });
    }

    private void deleteImage() {
        if (userId != null) {
            StorageReference imageReference = FirebaseStorage.getInstance().getReference("userprofiles").child(userId);
            imageReference.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(FullScreenImageActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(FullScreenImageActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            });
        }

        Intent home = new Intent(FullScreenImageActivity.this, MainActivity.class);
        startActivity(home);
    }

    public void choosePhotoFromGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadImageToFirebaseStorage() {
        if (mImageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(FullScreenImageActivity.this);
            progressDialog.setTitle("Uploading");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final StorageReference imageReference = FirebaseStorage.getInstance().getReference("userprofiles").child(auth.getCurrentUser().getUid());
            imageReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Intent home = new Intent(FullScreenImageActivity.this, MainActivity.class);
                        startActivity(home);
                        // Image uploaded successfully
                        // Now you can get the download URL and do something with it if needed
                        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Handle the download URL (e.g., save it to a database)
                            imageUrl = uri.toString();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        // Handle the error
                        Toast.makeText(FullScreenImageActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }


    }
}