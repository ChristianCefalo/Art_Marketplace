package com.example.artmarketplace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewListingActivity extends AppCompatActivity {
    public static void launch(android.content.Context c, String id) {
        android.content.Intent i = new android.content.Intent(c, ViewListingActivity.class);
        i.putExtra("LISTING_ID", id);
        c.startActivity(i);
    }

    private FirebaseFirestore db;

    private ImageView ivImage;
    private TextView tvTitle, tvPrice, tvTags, tvDesc, tvSellerName, tvSellerEmail, tvUpdatedAt;
    private Button btnEmailSeller, btnBack;

    private String listingId;
    private String ownerEmail;
    private static final ExecutorService IMG_EXEC = Executors.newFixedThreadPool(2);

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_listing);

        db = FirebaseFirestore.getInstance();

        ivImage = findViewById(R.id.vl_image);
        tvTitle = findViewById(R.id.vl_title);
        tvPrice = findViewById(R.id.vl_price);
        tvTags  = findViewById(R.id.vl_tags);
        tvDesc  = findViewById(R.id.vl_desc);
        tvSellerName  = findViewById(R.id.vl_seller_name);
        tvSellerEmail = findViewById(R.id.vl_seller_email);
        tvUpdatedAt   = findViewById(R.id.vl_updated);
        btnEmailSeller= findViewById(R.id.vl_email_button);
        btnBack       = findViewById(R.id.vl_back);

        btnBack.setOnClickListener(v -> finish());
        btnEmailSeller.setOnClickListener(v -> openEmail());

        listingId = getIntent().getStringExtra("LISTING_ID");
        if (TextUtils.isEmpty(listingId)) {
            Toast.makeText(this, "Invalid listing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadListing();
    }

    private void loadListing() {
        android.util.Log.d("ViewListing", "Opening listing: " + listingId);
        db.collection("listings").document(listingId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { Toast.makeText(this, "Listing not found", Toast.LENGTH_LONG).show(); finish(); return; }

                    String title = doc.getString("title");
                    tvTitle.setText(TextUtils.isEmpty(title) ? "Untitled" : title);

                    Double price = doc.getDouble("price");
                    tvPrice.setText(price == null ? "—" : "$" + String.format(Locale.US, "%.2f", price));




                    String desc = doc.getString("description");
                    tvDesc.setText(desc == null ? "" : desc);


                    tvTags.setText(resolveTags(doc.get("tags")));
                    Timestamp ts = doc.getTimestamp("updatedAt");
                    if (ts != null) {
                        String d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(ts.toDate());
                        tvUpdatedAt.setText("Updated: " + d);
                    } else {
                        tvUpdatedAt.setText("");
                    }


                    String url = null;
                    Object im = doc.get("imageUrls");
                    if (im instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> imgs = (java.util.List<String>) im;
                        if (imgs != null && !imgs.isEmpty()) url = imgs.get(0);
                    }
                    if (url == null) {
                        String single = doc.getString("imageUrl");
                        if (single != null && !single.isEmpty()) url = single;
                    }
                    if (url != null) {
                        loadImageInto(ivImage, url);
                    } else {
                        ivImage.setImageResource(android.R.color.darker_gray);
                    }

                    String ownerId = doc.getString("ownerId");
                    if (!TextUtils.isEmpty(ownerId)) loadSeller(ownerId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void loadSeller(String ownerId) {
        db.collection("users").document(ownerId).get()
                .addOnSuccessListener(user -> {
                    String name = user.getString("displayName");
                    ownerEmail = user.getString("email");

                    tvSellerName.setText(TextUtils.isEmpty(name) ? "Seller" : name);
                    tvSellerEmail.setText(TextUtils.isEmpty(ownerEmail) ? "(no email)" : ownerEmail);

                    btnEmailSeller.setEnabled(!TextUtils.isEmpty(ownerEmail));
                })
                .addOnFailureListener(e -> {
                    tvSellerName.setText("Seller");
                    tvSellerEmail.setText("(email unavailable)");
                    btnEmailSeller.setEnabled(false);
                });
    }

    private void openEmail() {
        if (TextUtils.isEmpty(ownerEmail)) return;
        String subject = "Inquiry about your art listing";
        String uri = "mailto:" + Uri.encode(ownerEmail) + "?subject=" + Uri.encode(subject);
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        startActivity(Intent.createChooser(i, "Email seller"));
    }

    // minimal image loader (no libraries)
    private void loadImageInto(ImageView target, String url) {
        target.setImageResource(android.R.color.darker_gray);
        IMG_EXEC.execute(() -> {
            Bitmap bmp = null;
            HttpURLConnection conn = null;
            try {
                URL u = new URL(url);
                conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(7000);
                try (InputStream is = conn.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(is);
                }
            } catch (Exception ignored) {}
            finally { if (conn != null) conn.disconnect(); }
            final Bitmap ready = bmp;
            target.post(() -> {
                if (ready != null) target.setImageBitmap(ready);
            });
        });
    }
    private static String resolveTags(@Nullable Object raw) {
        if (raw instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) raw;
            List<String> parts = new ArrayList<>();
            for (Object o : rawList) {
                if (o instanceof String) {
                    String v = ((String) o).trim();
                    if (!v.isEmpty()) parts.add(v);
                }
            }
            return parts.isEmpty() ? "" : TextUtils.join(", ", parts);
        }

        if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (s.isEmpty()) return "";
            String[] tokens = s.split("\\s*,\\s*");
            List<String> parts = new ArrayList<>();
            for (String token : tokens) {
                String v = token.trim();
                if (!v.isEmpty()) parts.add(v);
            }
            return parts.isEmpty() ? "" : TextUtils.join(", ", parts);
        }

        return "";
    }
}

