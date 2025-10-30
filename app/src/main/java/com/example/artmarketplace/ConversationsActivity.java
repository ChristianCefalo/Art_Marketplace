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
    private Button btnBack, btnNewChat;

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

        // extras
        botType = getIntent().getStringExtra("BOT_TYPE");
        if (botType == null) botType = "customer";

        // firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        uid = user.getUid();

        // ui
        rv = findViewById(R.id.rvConvos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationsAdapter(
                items,
                this::openChat,              // tap row → open
                this::renameConversation,    // popup rename
                this::deleteConversation     // popup delete
        );
        rv.setAdapter(adapter);

        btnBack    = findViewById(R.id.btnBack);
        btnNewChat = findViewById(R.id.btnNewChat);

        btnBack.setOnClickListener(v -> {
            Intent i;
            if ("provider".equals(botType)) {
                i = new Intent(this, ProviderHomeActivity.class);
            } else {
                i = new Intent(this, CustomerHomeActivity.class);
            }
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
        btnNewChat.setOnClickListener(v -> createNewConversation());

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

    /** Rename from adapter callback */
    private void renameConversation(DocumentSnapshot doc, String newTitle) {
        String t = (newTitle == null) ? "" : newTitle.trim();
        if (t.isEmpty()) {
            Toast.makeText(this, "Title can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        doc.getReference().update("title", t)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Rename failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Delete convo (paginate message deletes then delete convo doc) */
    private void deleteConversation(DocumentSnapshot doc) {
        final var msgsRef = doc.getReference().collection("messages");
        final int PAGE = 100;

        Runnable[] step = new Runnable[1];
        step[0] = () -> msgsRef.limit(PAGE).get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        doc.getReference().delete()
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Delete conversation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        var batch = db.batch();
                        for (var d : q.getDocuments()) batch.delete(d.getReference());
                        batch.commit()
                                .addOnSuccessListener(v -> step[0].run())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Delete messages failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load messages failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        step[0].run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (convReg != null) { convReg.remove(); convReg = null; }
    }
}
