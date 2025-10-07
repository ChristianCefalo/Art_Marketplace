package com.example.artmarketplace.ui.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artmarketplace.R;
import com.example.artmarketplace.data.AuthRepo;
import com.example.artmarketplace.data.FirestoreRepo;
import com.example.artmarketplace.model.ArtItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider landing screen featuring quick actions and a list of owned listings.
 */
public class ProviderHomeActivity extends AppCompatActivity implements ProviderArtAdapter.OnItemActionListener {

    private final AuthRepo authRepo = new AuthRepo();
    private final FirestoreRepo firestoreRepo = new FirestoreRepo();

    private ProviderArtAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);

        Button createListingButton = findViewById(R.id.button_create_listing);
        Button viewOrdersButton = findViewById(R.id.button_view_orders);
        RecyclerView recyclerView = findViewById(R.id.recycler_provider_art);
        progressBar = findViewById(R.id.progress_listings);
        emptyStateView = findViewById(R.id.text_empty_listings);

        adapter = new ProviderArtAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProviderHomeActivity.this, CreateListingActivity.class));
            }
        });

        viewOrdersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProviderHomeActivity.this, ProviderOrdersActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadListings();
    }

    private void loadListings() {
        FirebaseUser currentUser = authRepo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_provider_not_signed_in, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        firestoreRepo.listArtByProvider(currentUser.getUid())
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        showLoading(false);
                        List<ArtItem> items = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            ArtItem artItem = snapshot.toObject(ArtItem.class);
                            if (artItem != null) {
                                artItem.setId(snapshot.getId());
                                items.add(artItem);
                            }
                        }
                        adapter.setItems(items);
                        emptyStateView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
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
                        Toast.makeText(ProviderHomeActivity.this,
                                getString(R.string.error_loading_provider_art, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditClicked(ArtItem artItem) {
        Intent intent = new Intent(this, EditListingActivity.class);
        intent.putExtra(EditListingActivity.EXTRA_ART_ID, artItem.getId());
        intent.putExtra(EditListingActivity.EXTRA_TITLE, artItem.getTitle());
        intent.putExtra(EditListingActivity.EXTRA_PRICE, artItem.getPrice());
        intent.putExtra(EditListingActivity.EXTRA_DESC, artItem.getDesc());
        intent.putExtra(EditListingActivity.EXTRA_IMAGE_URL, artItem.getImageUrl());
        intent.putExtra(EditListingActivity.EXTRA_CREATED_AT, artItem.getCreatedAt());
        intent.putExtra(EditListingActivity.EXTRA_PROVIDER_UID, artItem.getProviderUid());
        startActivity(intent);
    }
}
