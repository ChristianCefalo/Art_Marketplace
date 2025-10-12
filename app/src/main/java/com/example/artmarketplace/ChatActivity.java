package com.example.artmarketplace;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText et;
    private Button btn;
    private TextView tvTitle;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String uid;
    private String botType; // "customer" or "provider"
    private String convId = "default";

    private CollectionReference msgsRef;
    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tvTitle = findViewById(R.id.tvTitle);
        rv = findViewById(R.id.rvMessages);
        et = findViewById(R.id.etInput);
        btn = findViewById(R.id.btnSend);

        adapter = new MessageAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Require an authenticated session (email/password or anonymous)
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in (or init anonymous auth) before opening chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        uid = auth.getCurrentUser().getUid();
        botType = getIntent().getStringExtra("BOT_TYPE"); // passed from main screen
        if (botType == null) botType = "customer";

        tvTitle.setText(botType.equals("provider") ? "Provider Chat" : "Customer Chat");

        msgsRef = db.collection("chats").document(uid)
                .collection(botType).document(convId)
                .collection("messages");

        // Listen to message history ordered by createdAt
        msgsRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (qs, e) -> {
                    if (e != null || qs == null) return;
                    messages.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Message m = d.toObject(Message.class);
                        if (m != null) { m.id = d.getId(); messages.add(m); }
                    }
                    adapter.submitList(new ArrayList<>(messages));
                    rv.scrollToPosition(Math.max(messages.size() - 1, 0));
                });

        btn.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendUserMessage(text);
            et.setText("");
        });

        // Ensure a single system context message exists at the top
        ensureSystemMessage();
    }

    private void ensureSystemMessage() {
        msgsRef.whereEqualTo("sender", "system").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Map<String, Object> sys = new HashMap<>();
                        sys.put("sender", "system");
                        sys.put("createdAt", FieldValue.serverTimestamp());
                        sys.put("prompt", botType.equals("provider")
                                ? "You are a concise seller-operations assistant for an online art marketplace. " +
                                "Be action-oriented and brief (≤ 80 words). Use only the given context."
                                : "You are a friendly customer support assistant for an online art marketplace. " +
                                "Answer clearly and briefly (≤ 80 words). If unsure, say you'll check.");
                        msgsRef.add(sys);
                    }
                });
    }

    private void sendUserMessage(String text) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("prompt", text);
        msg.put("sender", "user");
        msg.put("createdAt", FieldValue.serverTimestamp());

        // Adding a doc triggers the Extension; it will add "response" on this doc
        msgsRef.add(msg).addOnFailureListener(e ->
                Toast.makeText(this, "Send failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

