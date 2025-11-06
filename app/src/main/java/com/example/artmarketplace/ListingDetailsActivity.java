package com.example.artmarketplace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ListingDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "LISTING_ID";

    public static void launch(Context c, String id) {
        Intent i = new Intent(c, ListingDetailsActivity.class);
        i.putExtra(EXTRA_ID, id);
        c.startActivity(i);
    }

    private Button btnBack;
    private TextView tvTitle, tvPrice, tvTags, tvDesc, tvEmail;
    private ImageView iv;

    private FirebaseFirestore db;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvTags  = findViewById(R.id.tvTags);
        tvDesc  = findViewById(R.id.tvDesc);
        tvEmail = findViewById(R.id.tvEmail);
        iv      = findViewById(R.id.ivImage);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        String id = getIntent().getStringExtra(EXTRA_ID);
        if (id == null) { finish(); return; }

        db.collection("listings").document(id).get()
                .addOnSuccessListener(d -> {
                    if (!d.exists()) { finish(); return; }
                    String title = d.getString("title");
                    Double price = d.getDouble("price");
                    List<String> tags = resolveTags(d.get("tags"));
                    String desc = d.getString("description");
                    String ownerId = d.getString("ownerId");
                    String imageUrl = resolveImageUrl(d.get("imageUrl"), d.get("imageUrls"));
                    tvTitle.setText(title == null ? "Untitled" : title);
                    tvPrice.setText(price == null ? "" :
                            "$" + String.format(java.util.Locale.US, "%.2f", price));
                    tvTags.setText((tags == null || tags.isEmpty())
                            ? ""
                            : android.text.TextUtils.join(", ", tags));
                    tvDesc.setText(desc == null ? "" : desc);

                    if (ownerId != null) {
                        db.collection("users").document(ownerId).get()
                                .addOnSuccessListener(u -> {
                                    String email = u.getString("email");
                                    tvEmail.setText(email == null ? "" : email);
                                });
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // very small loader without libs
                        Executors.newSingleThreadExecutor().execute(() -> {
                            HttpURLConnection conn = null;
                            try {
                                conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                                conn.setConnectTimeout(5000);
                                conn.setReadTimeout(7000);
                                conn.connect();
                                try (java.io.InputStream is = conn.getInputStream()) {
                                    Bitmap b = BitmapFactory.decodeStream(is);
                                    runOnUiThread(() -> iv.setImageBitmap(b));
                                }
                            } catch (Exception ignored) {
                            } finally {
                                if (conn != null) conn.disconnect();
                            }
                        });
                    }
                });
    }
    private static List<String> resolveTags(@Nullable Object raw) {
        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            List<String> cleaned = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof String) {
                    String s = ((String) o).trim();
                    if (!s.isEmpty()) cleaned.add(s);
                }
            }
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
            return null;
        }
        if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (!s.isEmpty()) {
                return Arrays.asList(s.split("\\s*,\\s*"));
            }
        }
        return null;
    }

    @Nullable
    private static String resolveImageUrl(@Nullable Object single, @Nullable Object multiple) {
        if (single instanceof String && !((String) single).trim().isEmpty()) {
            return ((String) single).trim();
        }

        if (multiple instanceof List) {
            List<?> list = (List<?>) multiple;
            for (Object o : list) {
                if (o instanceof String && !((String) o).trim().isEmpty()) {
                    return ((String) o).trim();
                }
            }
        } else if (multiple instanceof String) {
            String s = (String) multiple;
            if (!s.trim().isEmpty()) {
                String[] parts = s.split("\\s*,\\s*");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        return part.trim();
                    }
                }
            }
        }

        return null;
    }
}

