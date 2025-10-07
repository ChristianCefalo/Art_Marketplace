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
import com.example.artmarketplace.data.FirestoreRepo;
import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.util.Validators;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;

/**
 * Allows providers to edit or delete an existing listing.
 */
public class EditListingActivity extends AppCompatActivity {

    public static final String EXTRA_ART_ID = "extra_art_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_DESC = "extra_desc";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_CREATED_AT = "extra_created_at";
    public static final String EXTRA_PROVIDER_UID = "extra_provider_uid";

    private EditText titleInput;
    private EditText priceInput;
    private EditText descInput;
    private EditText imageUrlInput;
    private ProgressBar progressBar;

    private String artId;
    private long createdAt;
    private String providerUid;

    private final FirestoreRepo firestoreRepo = new FirestoreRepo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_listing);

        titleInput = findViewById(R.id.input_title);
        priceInput = findViewById(R.id.input_price);
        descInput = findViewById(R.id.input_desc);
        imageUrlInput = findViewById(R.id.input_image_url);
        progressBar = findViewById(R.id.progress_edit_listing);
        Button saveButton = findViewById(R.id.button_update_listing);
        Button deleteButton = findViewById(R.id.button_delete_listing);

        artId = getIntent().getStringExtra(EXTRA_ART_ID);
        createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0);
        providerUid = getIntent().getStringExtra(EXTRA_PROVIDER_UID);
        if (providerUid == null) {
            providerUid = "";
        }

        titleInput.setText(getIntent().getStringExtra(EXTRA_TITLE));
        int price = getIntent().getIntExtra(EXTRA_PRICE, 0);
        priceInput.setText(String.format(Locale.getDefault(), "%.2f", price / 100f));
        descInput.setText(getIntent().getStringExtra(EXTRA_DESC));
        imageUrlInput.setText(getIntent().getStringExtra(EXTRA_IMAGE_URL));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdate();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptDelete();
            }
        });
    }

    private void attemptUpdate() {
        if (!Validators.isNonEmpty(artId)) {
            Toast.makeText(this, R.string.error_missing_listing_id, Toast.LENGTH_LONG).show();
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
        ArtItem updated = new ArtItem(artId, title, priceCents, description, imageUrl, providerUid, createdAt);
        firestoreRepo.updateArt(artId, updated)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showLoading(false);
                        Toast.makeText(EditListingActivity.this,
                                R.string.success_listing_updated,
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
                        Toast.makeText(EditListingActivity.this,
                                getString(R.string.error_updating_listing, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void attemptDelete() {
        if (!Validators.isNonEmpty(artId)) {
            Toast.makeText(this, R.string.error_missing_listing_id, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        firestoreRepo.deleteArt(artId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showLoading(false);
                        Toast.makeText(EditListingActivity.this,
                                R.string.success_listing_deleted,
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
                        Toast.makeText(EditListingActivity.this,
                                getString(R.string.error_deleting_listing, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
