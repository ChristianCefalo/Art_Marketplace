package com.example.artmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ProviderHomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RecyclerView rv;
    private Button btnAdd, btnBrowse, btnLogout;

    private ListingsAdapter adapter;
    private ListenerRegistration reg;
    private String uid;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);

        rv = findViewById(R.id.rvListings);
        btnAdd = findViewById(R.id.btnAdd);
        btnBrowse = findViewById(R.id.btnBrowse);
        btnLogout = findViewById(R.id.btnLogout);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ListingsAdapter(new ArrayList<>(),
                doc -> ViewListingActivity.launch(this, doc.getId()),  // tap → view details
                this::deleteDoc);
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { finishWith("Please sign in."); return; }
        uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    if (!"provider".equals(snap.getString("role"))) {
                        finishWith("Providers only.");
                        return;
                    }
                    startListening();
                })
                .addOnFailureListener(e -> finishWith("Failed to verify role: " + e.getMessage()));

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, CreateListingActivity.class)));

        btnBrowse.setOnClickListener(v ->
                startActivity(new Intent(this, BrowseListingsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void startListening() {
        reg = db.collection("listings")
                .whereEqualTo("ownerId", uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (qs, e) -> {
                    if (e != null || qs == null) return;
                    adapter.submit(qs.getDocuments());
                });
    }

    private void deleteDoc(DocumentSnapshot doc) {
        doc.getReference().delete()
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void finishWith(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (reg != null) { reg.remove(); reg = null; }
    }
}
