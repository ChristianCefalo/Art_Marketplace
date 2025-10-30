package com.example.artmarketplace;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;



public class BrowseListingsActivity extends AppCompatActivity {

    private Button btnBack;
    private EditText etSearch;
    private RecyclerView rv;
    private ListingsAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_listings);

        btnBack = findViewById(R.id.btnBack);
        etSearch= findViewById(R.id.etSearch);
        rv      = findViewById(R.id.rvListings);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingsAdapter(null, this::openDetails, d -> {});
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        uid  = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        btnBack.setOnClickListener(v -> finish());

        // initial load
        load(null);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { load(s.toString().trim()); }
        });
    }

    private void load(@Nullable String q) {
        Query base = db.collection("listings")
                .whereEqualTo("visibility", "public");
        if (uid != null) {
            base = base.whereNotEqualTo("ownerId", uid);
        }
        if (q != null && !q.isEmpty()) {
            String term = q.toLowerCase();
            base = base.whereArrayContains("searchTokens", term);
        }
        base.orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> adapter.submit(snap.getDocuments()));
    }

    private void openDetails(com.google.firebase.firestore.DocumentSnapshot doc) {
        ListingDetailsActivity.launch(this, doc.getId());
    }
}
