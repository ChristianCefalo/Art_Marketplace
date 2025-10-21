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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Button btnNew;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String uid;
    private String botType = "customer";

    private List<DocumentSnapshot> items = new ArrayList<>();
    private ConversationsAdapter adapter;

    private @Nullable ListenerRegistration convReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_conversations);

        botType = getIntent().getStringExtra("BOT_TYPE");
        if (botType == null) botType = "customer";

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class)); // adjust if different
            finish();
            return;
        }
        uid = user.getUid();

        btnNew = findViewById(R.id.btnNewChat);
        rv = findViewById(R.id.rvConvos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // ← callback only needs convId now
        adapter = new ConversationsAdapter(items, this::openChat);
        rv.setAdapter(adapter);

        btnNew.setOnClickListener(v -> createNewConversation());

        listenForConversations();
    }

    private void listenForConversations() {
        if (convReg != null) { convReg.remove(); convReg = null; }

        convReg = db.collection("chats").document(uid).collection(botType)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (qs, e) -> {
                    if (e != null || qs == null) return;
                    items = qs.getDocuments();
                    adapter.submit(items);
                });
    }

    private void createNewConversation() {
        DocumentReference convRef = db.collection("chats").document(uid)
                .collection(botType).document();
        String convId = convRef.getId();

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", "New chat");
        meta.put("createdAt", FieldValue.serverTimestamp());
        meta.put("updatedAt", FieldValue.serverTimestamp());
        meta.put("lastMessage", "");

        convRef.set(meta)
                .addOnSuccessListener(v -> openChat(convId))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Create failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openChat(String convId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("BOT_TYPE", botType);
        i.putExtra("CONV_ID", convId);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (convReg != null) { convReg.remove(); convReg = null; }
    }
}
