package com.example.artmarketplace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.VH> {

    interface OnOpen   { void open(@NonNull DocumentSnapshot doc); }
    interface OnDelete{ void delete(@NonNull DocumentSnapshot doc); }

    private List<DocumentSnapshot> data = new ArrayList<>();
    private final OnOpen onOpen;
    private final OnDelete onDelete;

    private static final ExecutorService IMG_EXEC = Executors.newFixedThreadPool(2);

    ListingsAdapter(List<DocumentSnapshot> d, OnOpen o, OnDelete del) {
        if (d != null) data = d;
        onOpen = o; onDelete = del;
    }

    void submit(List<DocumentSnapshot> d) {
        data = (d == null) ? new ArrayList<>() : d;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, tags, price;
        ImageView ivThumb;       // optional
        ImageButton btnOverflow; // optional
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            tags  = v.findViewById(R.id.tvTags);
            price = v.findViewById(R.id.tvPrice);
            ivThumb = v.findViewById(R.id.ivThumb);
            btnOverflow = v.findViewById(R.id.btnOverflow);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_listing, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        DocumentSnapshot d = data.get(i);

        String title = d.getString("title");
        if (title == null || title.trim().isEmpty()) title = "Untitled";

        String tagsStr = resolveTagsToDisplay(d);

        Double price = d.getDouble("price");
        String priceStr = (price == null) ? "" : "$" + String.format(Locale.US, "%.2f", price);

        h.title.setText(title);
        h.tags.setText(tagsStr);
        h.price.setText(priceStr);

        // click = open view screen
        h.itemView.setOnClickListener(v -> onOpen.open(d));

        // tiny thumbnail if the layout provides one
        if (h.ivThumb != null) {
            h.ivThumb.setImageResource(android.R.color.darker_gray);
            String url = resolveFirstImageUrl(d);
            loadUrlInto(h.ivThumb, url);
        }

        // Overflow “Edit / Rename / Delete”
        if (h.btnOverflow != null) {
            h.btnOverflow.setOnClickListener(v -> showRowMenu(v, d));
        } else {
            h.itemView.setOnLongClickListener(v -> { showRowMenu(v, d); return true; });
        }
    }

    private void showRowMenu(@NonNull View anchor, @NonNull DocumentSnapshot doc) {
        Context c = anchor.getContext();
        PopupMenu m = new PopupMenu(c, anchor);

        final int ID_EDIT   = 0;
        final int ID_RENAME = 1;
        final int ID_DELETE = 2;

        m.getMenu().add(0, ID_EDIT,   0, "Edit");
        m.getMenu().add(0, ID_RENAME, 1, "Rename");
        m.getMenu().add(0, ID_DELETE, 2, "Delete");

        m.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == ID_DELETE) {
                onDelete.delete(doc);
                return true;
            }
            if (id == ID_EDIT) {
                // open CreateListingActivity in edit mode
                Intent i = new Intent(c, CreateListingActivity.class);
                i.putExtra("EDIT_ID", doc.getId());
                c.startActivity(i);
                return true;
            }
            if (id == ID_RENAME) {
                EditText et = new EditText(c);
                et.setText(doc.getString("title"));
                new androidx.appcompat.app.AlertDialog.Builder(c)
                        .setTitle("Rename listing")
                        .setView(et)
                        .setPositiveButton("Save", (dlg, w) -> {
                            String t = et.getText().toString().trim();
                            if (t.isEmpty()) {
                                Toast.makeText(c, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // keep your search helpers if you use them; safe to update title only
                            doc.getReference().update(
                                    "title", t,
                                    "titleLower", t.toLowerCase(Locale.US),
                                    "updatedAt", FieldValue.serverTimestamp()
                            );
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

    // --------- helpers ---------

    private static String resolveTagsToDisplay(@NonNull DocumentSnapshot d) {
        // supports "tags" as List<String> OR a comma-separated String
        Object raw = d.get("tags");
        List<String> list = null;
        if (raw instanceof List) {
            //noinspection unchecked
            list = (List<String>) raw;
        } else if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (!s.isEmpty()) list = Arrays.asList(s.split("\\s*,\\s*"));
        }
        return (list == null || list.isEmpty())
                ? ""
                : android.text.TextUtils.join(", ", list);
    }

    private static String resolveFirstImageUrl(@NonNull DocumentSnapshot d) {
        Object one = d.get("imageUrl");     // single
        if (one instanceof String && !((String) one).isEmpty()) return (String) one;

        Object many = d.get("imageUrls");   // array or comma string
        if (many instanceof List) {
            List<?> l = (List<?>) many;
            if (!l.isEmpty() && l.get(0) instanceof String) return (String) l.get(0);
        } else if (many instanceof String) {
            String s = (String) many;
            String[] parts = s.split("\\s*,\\s*");
            if (parts.length > 0) return parts[0];
        }
        return null;
    }

    private static void loadUrlInto(@NonNull ImageView target, @Nullable String url) {
        if (url == null || url.trim().isEmpty()) return;
        IMG_EXEC.execute(() -> {
            Bitmap bmp = null;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(7000);
                try (InputStream is = conn.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(is);
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
            Bitmap finalBmp = bmp;
            target.post(() -> {
                if (finalBmp != null) target.setImageBitmap(finalBmp);
            });
        });
    }
}
