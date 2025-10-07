package com.example.artmarketplace.ui.provider;

import android.os.Bundle;
import android.view.View;
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
import com.example.artmarketplace.model.Order;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a provider's incoming orders.
 */
public class ProviderOrdersActivity extends AppCompatActivity {

    private final AuthRepo authRepo = new AuthRepo();
    private final FirestoreRepo firestoreRepo = new FirestoreRepo();

    private ProviderOrdersAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_orders);

        RecyclerView recyclerView = findViewById(R.id.recycler_provider_orders);
        progressBar = findViewById(R.id.progress_orders);
        emptyStateView = findViewById(R.id.text_empty_orders);

        adapter = new ProviderOrdersAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadOrders();
    }

    private void loadOrders() {
        FirebaseUser currentUser = authRepo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_provider_not_signed_in, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        firestoreRepo.listOrdersByProvider(currentUser.getUid())
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        showLoading(false);
                        List<Order> orders = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            Order order = snapshot.toObject(Order.class);
                            if (order != null) {
                                order.setId(snapshot.getId());
                                orders.add(order);
                            }
                        }
                        emptyStateView.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.setItems(orders);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        String message = e.getMessage();
                        if (message == null) {
                            message = getString(R.string.error_loading_orders_generic);
                        }
                        Toast.makeText(ProviderOrdersActivity.this,
                                getString(R.string.error_loading_orders, message),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
