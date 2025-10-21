package com.example.artmarketplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.VH> {

    interface OnClick { void open(@NonNull String convId); } // ← no title arg

    private List<DocumentSnapshot> data;
    private final OnClick onClick;

    ConversationsAdapter(List<DocumentSnapshot> d, OnClick oc) {
        data = (d == null) ? new ArrayList<>() : d;
        onClick = oc;
    }

    void submit(List<DocumentSnapshot> d) {
        data = (d == null) ? new ArrayList<>() : d;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, snippet;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            snippet = itemView.findViewById(R.id.tvSnippet);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final DocumentSnapshot doc = data.get(position);

        final String rawTitle    = doc.getString("title");
        final String safeTitle   = (rawTitle == null || rawTitle.trim().isEmpty()) ? "Untitled" : rawTitle.trim();
        final String last        = doc.getString("lastMessage");
        final String safeSnippet = (last == null) ? "" : last;

        h.title.setText(safeTitle);
        h.snippet.setText(safeSnippet);

        h.itemView.setOnClickListener(view -> onClick.open(doc.getId())); // ← only convId

        h.itemView.setOnLongClickListener(view -> { showMenu(view, doc); return true; });
    }

    private void showMenu(@NonNull View anchor, @NonNull DocumentSnapshot doc) {
        Context c = anchor.getContext();
        PopupMenu m = new PopupMenu(c, anchor);

        // Give items stable IDs so we don't rely on nullable titles
        final int ID_RENAME = 1;
        final int ID_DELETE = 2;

        m.getMenu().add(0, ID_RENAME, 0, "Rename");
        m.getMenu().add(0, ID_DELETE, 1, "Delete");

        m.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case ID_DELETE:
                    deleteConversation(doc);
                    return true;
                case ID_RENAME:
                    renameConversation(c, doc);
                    return true;
                default:
                    return false;
            }
        });

        m.show();
    }


    private void deleteConversation(@NonNull DocumentSnapshot doc) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference msgs = doc.getReference().collection("messages");

        msgs.limit(50).get().addOnSuccessListener(q -> {
            if (q.isEmpty()) {
                doc.getReference().delete();
            } else {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot x : q) batch.delete(x.getReference());
                batch.commit().addOnSuccessListener(ignored -> deleteConversation(doc));
            }
        });
    }

    private void renameConversation(@NonNull Context c, @NonNull DocumentSnapshot doc) {
        EditText et = new EditText(c);
        String current = doc.getString("title");
        et.setText(current == null ? "" : current);

        new AlertDialog.Builder(c)
                .setTitle("Rename chat")
                .setView(et)
                .setPositiveButton("Save", (dlg, which) -> {
                    String newTitle = et.getText().toString().trim();
                    if (newTitle.isEmpty()) {
                        Toast.makeText(c, "Title can't be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    doc.getReference().update("title", newTitle);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override public int getItemCount() { return (data == null) ? 0 : data.size(); }
}
