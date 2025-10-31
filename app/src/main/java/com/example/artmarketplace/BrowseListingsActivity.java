package com.example.artmarketplace;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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

    private Button btnBack, btnSearch;
    private RecyclerView rv;
    private ListingsAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private @Nullable String uid;

    private @Nullable String lastQuery = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_listings);

        btnBack   = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        rv        = findViewById(R.id.rvListings);

        rv.setLayoutManager(new LinearLayoutManager(this));
        // onOpen -> open details; onLongPress -> no-op here
        adapter = new ListingsAdapter(null,
                doc -> ListingDetailsActivity.launch(this, doc.getId()),
                d -> {});
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        uid  = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> showSearchDialog());

        // initial load (no filter)
        load(null);
    }

    private void showSearchDialog() {
        final EditText input = new EditText(this);
        input.setHint("title or tag");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (!TextUtils.isEmpty(lastQuery)) input.setText(lastQuery);

        new AlertDialog.Builder(this)
                .setTitle("Search listings")
                .setView(input)
                .setPositiveButton("Search", (d, w) -> {
                    String q = input.getText().toString().trim();
                    lastQuery = q;
                    load(q);
                })
                .setNegativeButton("Clear", (d, w) -> {
                    lastQuery = null;
                    load(null);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void load(@Nullable String q) {
        Query base = db.collection("listings")
                .whereEqualTo("visibility", "public");

        if (uid != null) {
            // exclude my own listings (ok to remove if you want to see yours here)
            base = base.whereNotEqualTo("ownerId", uid);
        }

        if (q != null && !q.isEmpty()) {
            // requires you to store an array field `searchTokens` (lowercased keywords from title/tags)
            base = base.whereArrayContains("searchTokens", q.toLowerCase());
        }

        base = base.orderBy("updatedAt", Query.Direction.DESCENDING).limit(50);

        base.get().addOnSuccessListener(snap -> adapter.submit(snap.getDocuments()));
    }
}
