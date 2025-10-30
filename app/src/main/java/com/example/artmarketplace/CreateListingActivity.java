package com.example.artmarketplace;


import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class CreateListingActivity extends AppCompatActivity {

    private EditText etTitle, etDesc, etTags, etPrice;
    private SwitchCompat swPublic;
    private Button btnSave, btnPickImage, btnBack;
    private ImageView ivPreview;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri pickedImageUri;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    ivPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        btnBack = findViewById(R.id.btnBack);
        etTitle = findViewById(R.id.etTitle);
        etDesc  = findViewById(R.id.etDesc);
        etTags  = findViewById(R.id.etTags);
        etPrice = findViewById(R.id.etPrice);
        swPublic = findViewById(R.id.swPublic);
        btnSave = findViewById(R.id.btnSave);
        btnPickImage = findViewById(R.id.btnPickImage);
        ivPreview = findViewById(R.id.ivPreview);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        btnBack.setOnClickListener(v -> finish());
        btnPickImage.setOnClickListener(v -> pickImage.launch("image/*"));
        btnSave.setOnClickListener(v -> saveListing());
    }

    private void saveListing() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String tags = etTags.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> listing = new HashMap<>();
        listing.put("title", title);
        listing.put("description", desc);
        listing.put("tags", tags);
        listing.put("price", price);
        listing.put("visibility", swPublic.isChecked() ? "public" : "private");
        listing.put("ownerId", uid);
        listing.put("createdAt", FieldValue.serverTimestamp());
        listing.put("updatedAt", FieldValue.serverTimestamp());

        if (pickedImageUri != null) {
            uploadImageAndSave(listing);
        } else {
            saveToFirestore(listing);
        }
    }

    private void uploadImageAndSave(Map<String, Object> listing) {
        String uid = auth.getCurrentUser().getUid();
        StorageReference ref = storage.getReference()
                .child("listings/" + uid + "/" + System.currentTimeMillis() + ".jpg");

        ref.putFile(pickedImageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            listing.put("imageUrl", uri.toString());
                            saveToFirestore(listing);
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveToFirestore(Map<String, Object> listing) {
        db.collection("listings")
                .add(listing)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Listing added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
