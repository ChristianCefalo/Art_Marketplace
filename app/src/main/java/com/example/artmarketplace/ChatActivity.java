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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private TextView tvTitle;
    private RecyclerView rv;
    private EditText et;
    private Button btn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String uid;
    private String botType;           // "customer" or "provider"
    private String convId = "default";

    private CollectionReference msgsRef;
    private ListenerRegistration msgReg;

    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;


    private boolean typingShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tvTitle = findViewById(R.id.tvTitle);
        rv      = findViewById(R.id.rvMessages);
        et      = findViewById(R.id.etInput);
        btn     = findViewById(R.id.btnSend);

        adapter = new MessageAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        rv.setItemAnimator(null);
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        uid = auth.getCurrentUser().getUid();
        botType = getIntent().getStringExtra("BOT_TYPE");
        if (botType == null) botType = "customer";

        String passedConv = getIntent().getStringExtra("CONV_ID");
        if (passedConv != null) convId = passedConv;

        tvTitle.setText(botType.equals("provider") ? "Provider Chat" : "Customer Chat");

        msgsRef = db.collection("chats").document(uid)
                .collection(botType).document(convId)
                .collection("messages");

        attachMessagesListener();

        btn.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendUserMessage(text);
            et.setText("");
        });

        ensureSystemMessage();
    }

    private void attachMessagesListener() {
        if (msgReg != null) { msgReg.remove(); msgReg = null; }

        msgReg = msgsRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (qs, e) -> {
                    if (e != null || qs == null) return;

                    List<Message> fresh = new ArrayList<>();
                    boolean sawResponse = false;

                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Message base = d.toObject(Message.class);
                        if (base == null) continue;
                        if ("system".equals(base.sender)) continue;

                        // user row (prompt)
                        if (base.prompt != null && !base.prompt.isEmpty()) {
                            Message u = new Message();
                            u.id = d.getId() + "#u";
                            u.sender = "user";
                            u.prompt = base.prompt;
                            u.createdAt = (base.createdAt != null) ? base.createdAt : Timestamp.now();
                            fresh.add(u);
                        }
                        // ai row (response)
                        if (base.response != null && !base.response.isEmpty()) {
                            Message a = new Message();
                            a.id = d.getId() + "#a";
                            a.sender = "ai";
                            a.response = base.response;
                            a.createdAt = (base.createdAt != null) ? base.createdAt : Timestamp.now();
                            fresh.add(a);
                            sawResponse = true;
                        }
                    }


                    typingShown = typingShown && !sawResponse;

                    messages.clear();
                    messages.addAll(fresh);


                    if (typingShown) {
                        Message t = new Message();
                        t.id = "typing";
                        t.sender = "ai";
                        t.response = "…";
                        t.createdAt = Timestamp.now();
                        messages.add(t);
                    }

                    adapter.submitList(new ArrayList<>(messages));
                    rv.scrollToPosition(Math.max(messages.size() - 1, 0));
                });
    }

    private void sendUserMessage(String text) {

        Message echo = new Message();
        echo.sender = "user";
        echo.prompt = text;
        echo.createdAt = Timestamp.now();
        messages.add(echo);
        adapter.submitList(new ArrayList<>(messages));
        rv.scrollToPosition(Math.max(messages.size()-1, 0));


        if (!typingShown) {
            Message t = new Message();
            t.id = "typing";
            t.sender = "ai";
            t.response = "…";
            t.createdAt = Timestamp.now();
            messages.add(t);
            typingShown = true;
            adapter.submitList(new ArrayList<>(messages));
            rv.scrollToPosition(Math.max(messages.size()-1, 0));
        }


        Map<String, Object> msg = new HashMap<>();
        msg.put("prompt", text);
        msg.put("sender", "user");
        msg.put("createdAt", FieldValue.serverTimestamp());
        msgsRef.add(msg).addOnFailureListener(e ->
                Toast.makeText(this, "Send failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());


        DocumentReference convRef = db.collection("chats").document(uid)
                .collection(botType).document(convId);
        Map<String, Object> up = new HashMap<>();
        up.put("lastMessage", text);
        up.put("updatedAt", FieldValue.serverTimestamp());
        convRef.set(up, SetOptions.merge());
        convRef.get().addOnSuccessListener(s -> {
            String curTitle = s.getString("title");
            if (!s.exists() || curTitle == null || "New chat".equals(curTitle)) {
                String title = text.length() > 30 ? text.substring(0, 30) + "…" : text;
                convRef.update("title", title);
            }
        });
    }

    private void ensureSystemMessage() {
        msgsRef.whereEqualTo("sender", "system").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Map<String, Object> sys = new HashMap<>();
                        sys.put("sender", "system");
                        sys.put("createdAt", FieldValue.serverTimestamp());
                        sys.put("prompt", botType.equals("provider")
                                ? "You are a concise seller-operations assistant for an online art marketplace."
                                : "You are a friendly customer support assistant for an online art marketplace.");
                        msgsRef.add(sys);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msgReg != null) { msgReg.remove(); msgReg = null; }
    }
}
