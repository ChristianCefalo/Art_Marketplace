package com.example.artmarketplace;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;


public class CustomerHomeActivity1 extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private TextView artList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home1);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        artList = findViewById(R.id.artList);
        getArtDb();
    }

    private void getArtDb() {
        mDatabase.child("art").child(auth.getUid()).get()
                .addOnSuccessListener(task -> {
                    DataSnapshot snapshot = task;
                    if (snapshot.exists()) {
                        String artName = "";
                        for (DataSnapshot artSnapshot : snapshot.getChildren()) {
                            artName += artSnapshot.child("name").getValue(String.class);
                        }
                        if (artName.length() > 0) {
                            artList.setText(artName.toString());
                        } else {
                            artList.setText("No art found.");
                        }
                        Log.d("firebase", artName.toString());
                    } else {
                        artList.setText("No art found.");
                        Log.d("firebase", "No art data at this location.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("firebase", "Error getting data", e);
                });
    }
}
