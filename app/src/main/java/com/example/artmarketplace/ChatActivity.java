package com.example.artmarketplace;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private TextView tvTitle;
    private RecyclerView rv;
    private EditText et;
    private Button btnSend, btnAttach;

    // header buttons
    private Button btnBack, btnChats, btnNew;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private String uid;
    private String botType;           // "customer" or "provider"
    private String convId = "default";

    private CollectionReference msgsRef;
    private ListenerRegistration msgReg;

    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;



    // ---------- Attachment pickers ----------
    private ActivityResultLauncher<String> pickImage;
    private ActivityResultLauncher<String> pickVideo;
    private ActivityResultLauncher<String> pickAny;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // header
        btnBack  = findViewById(R.id.btnBack);
        btnChats = findViewById(R.id.btnChats);
        btnNew   = findViewById(R.id.btnNew);

        tvTitle  = findViewById(R.id.tvTitle);
        rv       = findViewById(R.id.rvMessages);
        et       = findViewById(R.id.etInput);
        btnSend  = findViewById(R.id.btnSend);
        btnAttach= findViewById(R.id.btnAttach);

        adapter = new MessageAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        rv.setItemAnimator(null);
        rv.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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

        // register attachment pickers BEFORE wiring click
        registerPickers();

        attachMessagesListener();

        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendUserMessage(text);
            et.setText("");
        });

        btnAttach.setOnClickListener(v -> showAttachChooser());

        // header nav
        btnBack.setOnClickListener(v -> finish());

        btnChats.setOnClickListener(v -> {
            Intent i = new Intent(this, ConversationsActivity.class);
            i.putExtra("BOT_TYPE", botType);
            startActivity(i);
        });

        btnNew.setOnClickListener(v -> createAndOpenNewConversation());

        ensureSystemMessage();
    }

    // ----------------- Attachments -----------------

    private void registerPickers() {
        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadAttachment(uri, "image/*");
        });
        pickVideo = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadAttachment(uri, "video/*");
        });
        pickAny = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadAttachment(uri, "*/*");
        });
    }

    private void showAttachChooser() {
        final CharSequence[] items = {"Image", "Video", "File"};
        new AlertDialog.Builder(this)
                .setTitle("Attach")
                .setItems(items, (DialogInterface dlg, int which) -> {
                    if (which == 0) pickImage.launch("image/*");
                    else if (which == 1) pickVideo.launch("video/*");
                    else pickAny.launch("*/*");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadAttachment(Uri uri, String requestedMime) {
        try {
            // Resolve actual mime + safe filename
            ContentResolver cr = getContentResolver();
            String mime = cr.getType(uri);
            if (mime == null) mime = requestedMime;

            String name = queryDisplayName(uri);
            if (name == null || name.trim().isEmpty()) {
                String ext = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(mime);
                if (ext == null) ext = "bin";
                name = "file_" + System.currentTimeMillis() + "." + ext;
            }

            // Make them effectively final for lambdas
            final String safeMime = mime;
            final String safeName = name;

            // Storage path
            String path = "chat_uploads/" + uid + "/" + botType + "/" + convId + "/" + safeName;
            StorageReference ref = storage.getReference().child(path);

            try (InputStream is = cr.openInputStream(uri)) {
                if (is == null) throw new FileNotFoundException("openInputStream returned null");

                ref.putStream(is)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return ref.getDownloadUrl();
                        })
                        .addOnSuccessListener(downloadUri -> {
                            // Firestore message with attachment
                            Map<String, Object> msg = new HashMap<>();
                            msg.put("sender", "user");
                            msg.put("createdAt", FieldValue.serverTimestamp());

                            Map<String, Object> att = new HashMap<>();
                            att.put("url", downloadUri.toString());
                            att.put("name", safeName);
                            att.put("mime", safeMime);
                            msg.put("attachment", att);

                            msgsRef.add(msg).addOnFailureListener(e ->
                                    Toast.makeText(this, "Attach write failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                            // bump conversation meta
                            DocumentReference convRef = db.collection("chats").document(uid)
                                    .collection(botType).document(convId);
                            Map<String, Object> up = new HashMap<>();
                            up.put("lastMessage", "[attachment]");
                            up.put("updatedAt", FieldValue.serverTimestamp());
                            convRef.set(up, SetOptions.merge());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        } catch (Exception e) {
            Toast.makeText(this, "Pick/upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private @Nullable String queryDisplayName(Uri uri) {
        try (android.database.Cursor c = getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ----------------- Conversations helpers -----------------

    private void createAndOpenNewConversation() {
        DocumentReference convRef = db.collection("chats").document(uid)
                .collection(botType).document();
        final String newId = convRef.getId();

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", "New chat");
        meta.put("createdAt", FieldValue.serverTimestamp());
        meta.put("updatedAt", FieldValue.serverTimestamp());
        meta.put("lastMessage", "");

        convRef.set(meta)
                .addOnSuccessListener(x -> {
                    Intent i = new Intent(this, ChatActivity.class);
                    i.putExtra("BOT_TYPE", botType);
                    i.putExtra("CONV_ID", newId);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ----------------- Messaging flow -----------------

    private void attachMessagesListener() {
        if (msgReg != null) { msgReg.remove(); msgReg = null; }

        msgReg = msgsRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (qs, e) -> {
                    if (e != null || qs == null) return;

                    List<Message> fresh = new ArrayList<>();

                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Message base = d.toObject(Message.class);
                        if (base == null) continue;
                        if ("system".equals(base.sender)) continue;

                        if (base.prompt != null && !base.prompt.isEmpty()) {
                            Message u = new Message();
                            u.id = d.getId() + "#u";
                            u.sender = "user";
                            u.prompt = base.prompt;
                            u.createdAt = (base.createdAt != null) ? base.createdAt : Timestamp.now();
                            fresh.add(u);
                        }
                        if (base.response != null && !base.response.isEmpty()) {
                            Message a = new Message();
                            a.id = d.getId() + "#a";
                            a.sender = "ai";
                            a.response = base.response;
                            a.createdAt = (base.createdAt != null) ? base.createdAt : Timestamp.now();
                            fresh.add(a);
                        }
                    }

                    // 👉 Derive typing: if the last real message is from user, show "…"
                    boolean shouldShowTyping = !fresh.isEmpty()
                            && "user".equals(fresh.get(fresh.size() - 1).sender);

                    messages.clear();
                    messages.addAll(fresh);

                    if (shouldShowTyping) {
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
        // local echo
        Message echo = new Message();
        echo.sender = "user";
        echo.prompt = text;
        echo.createdAt = Timestamp.now();
        messages.add(echo);
        adapter.submitList(new ArrayList<>(messages));
        rv.scrollToPosition(Math.max(messages.size() - 1, 0));

        // write to Firestore
        Map<String, Object> msg = new HashMap<>();
        msg.put("prompt", text);
        msg.put("sender", "user");
        msg.put("createdAt", FieldValue.serverTimestamp());
        msgsRef.add(msg).addOnFailureListener(e ->
                Toast.makeText(this, "Send failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // bump conversation meta
        DocumentReference convRef = db.collection("chats").document(uid)
                .collection(botType).document(convId);
        Map<String, Object> up = new HashMap<>();
        up.put("lastMessage", text);
        up.put("updatedAt", FieldValue.serverTimestamp());
        convRef.set(up, SetOptions.merge());
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
