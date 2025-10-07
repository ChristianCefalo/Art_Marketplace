package com.example.artmarketplace.ui.customer;

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
import com.example.artmarketplace.data.FirestoreRepo;
import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.ui.chat.ChatActivity;
import com.example.artmarketplace.util.SeedDataUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays available art listings to customers.
 */
public class CustomerHomeActivity extends AppCompatActivity implements ArtAdapter.OnItemClickListener {

    private FirestoreRepo firestoreRepo;
    private ArtAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        firestoreRepo = new FirestoreRepo();

        RecyclerView recyclerView = findViewById(R.id.recycler_art);
        progressBar = findViewById(R.id.progress_art_loading);
        emptyView = findViewById(R.id.text_empty_state);
        Button chatButton = findViewById(R.id.button_chat_ai);

        adapter = new ArtAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerHomeActivity.this, ChatActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SeedDataUtil.seedIfEmpty(this);
        loadArt();
    }

    private void loadArt() {
        showLoading(true);
        firestoreRepo.listArt()
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
                        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        String message = e.getMessage();
                        if (message == null) {
                            message = getString(R.string.error_loading_art_generic);
                        }
                        Toast.makeText(CustomerHomeActivity.this,
                                getString(R.string.error_loading_art, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClicked(ArtItem item) {
        Intent intent = new Intent(this, ArtDetailActivity.class);
        intent.putExtra(ArtDetailActivity.EXTRA_ART_ID, item.getId());
        intent.putExtra(ArtDetailActivity.EXTRA_ART_TITLE, item.getTitle());
        intent.putExtra(ArtDetailActivity.EXTRA_ART_PRICE, item.getPrice());
        intent.putExtra(ArtDetailActivity.EXTRA_ART_DESC, item.getDesc());
        intent.putExtra(ArtDetailActivity.EXTRA_ART_IMAGE_URL, item.getImageUrl());
        intent.putExtra(ArtDetailActivity.EXTRA_ART_PROVIDER_UID, item.getProviderUid());
        startActivity(intent);
    }
}
