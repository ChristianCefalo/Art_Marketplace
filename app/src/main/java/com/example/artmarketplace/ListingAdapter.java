package com.example.artmarketplace;

import android.content.Context;
import android.view.*;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.VH> {

    interface OnOpen   { void open(@NonNull DocumentSnapshot doc); }
    interface OnDelete{ void delete(@NonNull DocumentSnapshot doc); }

    private List<DocumentSnapshot> data = new ArrayList<>();
    private final OnOpen onOpen;
    private final OnDelete onDelete;

    ListingsAdapter(List<DocumentSnapshot> d, OnOpen o, OnDelete del) {
        if (d != null) data = d;
        onOpen = o; onDelete = del;
    }

    void submit(List<DocumentSnapshot> d) { data = (d == null) ? new ArrayList<>() : d; notifyDataSetChanged(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, tags, price;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            tags  = v.findViewById(R.id.tvTags);
            price = v.findViewById(R.id.tvPrice);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_listing, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        DocumentSnapshot d = data.get(i);

        String title = d.getString("title");
        if (title == null || title.trim().isEmpty()) title = "Untitled";

        List<String> tags = Arrays.asList(Objects.requireNonNull(d.getString("tags")).split("\\s*,\\s*"));
        // List<String> tags2 = (List<String>) d.get("tags");
        String tagsStr = (tags.isEmpty()) ? "" : android.text.TextUtils.join(", ", tags);

        Double price = d.getDouble("price");
        String priceStr = (price == null) ? "" : "$" + String.format(java.util.Locale.US, "%.2f", price);

        h.title.setText(title);
        h.tags.setText(tagsStr);
        h.price.setText(priceStr);

        h.itemView.setOnClickListener(v -> onOpen.open(d));
        h.itemView.setOnLongClickListener(v -> { showMenu(v, d); return true; });
    }

    private void showMenu(@NonNull View anchor, @NonNull DocumentSnapshot doc) {
        Context c = anchor.getContext();
        PopupMenu m = new PopupMenu(c, anchor);

        final int ID_RENAME = 1;
        final int ID_DELETE = 2;

        m.getMenu().add(0, ID_RENAME, 0, "Rename");
        m.getMenu().add(0, ID_DELETE, 1, "Delete");

        m.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == ID_DELETE) {
                onDelete.delete(doc);
                return true;
            }
            if (id == ID_RENAME) {
                EditText et = new EditText(c);
                et.setText(doc.getString("title"));
                new androidx.appcompat.app.AlertDialog.Builder(c)
                        .setTitle("Rename listing")
                        .setView(et)
                        .setPositiveButton("Save", (dLg, w) -> {
                            String t = et.getText().toString().trim();
                            if (t.isEmpty()) {
                                Toast.makeText(c, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            doc.getReference().update(
                                    "title", t,
                                    "titleLower", t.toLowerCase(java.util.Locale.US),
                                    "updatedAt", FieldValue.serverTimestamp(),
                                    "searchTokens", SearchUtil.buildTokens(t, (Arrays.asList(Objects.requireNonNull(doc.getString("tags")).split("\\s*,\\s*")))
                            ));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        m.show();
    }

    @Override public int getItemCount() { return (data == null) ? 0 : data.size(); }
}
