package com.example.LibraryBee.User_Pannel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.LibraryBee.MainActivity;
import com.example.LibraryBee.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FullScreenImageActivity extends AppCompatActivity {
    private ImageView fullScreenImageView;
    private Button deleteButton;
    private String imageUrl;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        deleteButton = findViewById(R.id.deleteButton);

        Intent intent = getIntent();
        if (intent != null) {
            imageUrl = intent.getStringExtra("IMAGE_URL");
            userId = intent.getStringExtra("USER_ID");

            Glide.with(this)
                    .load(imageUrl)
                    .into(fullScreenImageView);
        }

        deleteButton.setOnClickListener(v -> {
            deleteImage();
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
        startActivity((home));
    }
}

