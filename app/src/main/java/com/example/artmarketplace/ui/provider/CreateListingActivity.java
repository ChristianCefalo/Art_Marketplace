package com.example.artmarketplace.ui.provider;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artmarketplace.R;
import com.example.artmarketplace.data.AuthRepo;
import com.example.artmarketplace.data.FirestoreRepo;
import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.util.Validators;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

/**
 * Screen allowing providers to create new art listings.
 */
public class CreateListingActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText priceInput;
    private EditText descInput;
    private EditText imageUrlInput;
    private ProgressBar progressBar;

    private final AuthRepo authRepo = new AuthRepo();
    private final FirestoreRepo firestoreRepo = new FirestoreRepo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        titleInput = findViewById(R.id.input_title);
        priceInput = findViewById(R.id.input_price);
        descInput = findViewById(R.id.input_desc);
        imageUrlInput = findViewById(R.id.input_image_url);
        progressBar = findViewById(R.id.progress_create_listing);
        Button createButton = findViewById(R.id.button_submit_listing);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreate();
            }
        });
    }

    private void attemptCreate() {
        FirebaseUser currentUser = authRepo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_provider_not_signed_in, Toast.LENGTH_LONG).show();
            return;
        }

        String title = titleInput.getText().toString().trim();
        String priceText = priceInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (!Validators.isNonEmpty(title)) {
            titleInput.setError(getString(R.string.error_field_required));
            return;
        }

        if (!Validators.isNonEmpty(priceText)) {
            priceInput.setError(getString(R.string.error_field_required));
            return;
        }

        Integer priceCents = Validators.parsePriceToCents(priceText);
        if (priceCents == null) {
            priceInput.setError(getString(R.string.error_price_invalid));
            return;
        }

        if (!Validators.isValidUrl(imageUrl)) {
            imageUrlInput.setError(getString(R.string.error_url_invalid));
            return;
        }

        showLoading(true);
        ArtItem artItem = new ArtItem(null, title, priceCents, description, imageUrl,
                currentUser.getUid(), System.currentTimeMillis());

        firestoreRepo.createArt(artItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showLoading(false);
                        Toast.makeText(CreateListingActivity.this,
                                R.string.success_listing_created,
                                Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        String message = e.getMessage();
                        if (message == null) {
                            message = getString(R.string.error_loading_provider_art_generic);
                        }
                        Toast.makeText(CreateListingActivity.this,
                                getString(R.string.error_creating_listing, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
