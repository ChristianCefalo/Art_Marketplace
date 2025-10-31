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
import java.util.Objects;
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
                    //List<String> tags = (List<String>) d.get("tags");
                    List<String> tags = Arrays.asList(Objects.requireNonNull(d.getString("tags")).split("\\s*,\\s*"));
                    String desc = d.getString("description");
                    String ownerId = d.getString("ownerId");
                    //List<String> imgs = (List<String>) d.get("imageUrls");
                    String imgs = d.getString("imageUrls");
                    tvTitle.setText(title == null ? "Untitled" : title);
                    tvPrice.setText(price == null ? "" :
                            "$" + String.format(java.util.Locale.US, "%.2f", price));
                    tvTags.setText(tags == null ? "" : android.text.TextUtils.join(", ", tags));
                    tvDesc.setText(desc == null ? "" : desc);

                    if (ownerId != null) {
                        db.collection("users").document(ownerId).get()
                                .addOnSuccessListener(u -> {
                                    String email = u.getString("email");
                                    tvEmail.setText(email == null ? "" : email);
                                });
                    }

                    if (imgs != null && !imgs.isEmpty()) {
                        String url = imgs;
                        // very small loader without libs
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                                c.connect();
                                Bitmap b = BitmapFactory.decodeStream(c.getInputStream());
                                runOnUiThread(() -> iv.setImageBitmap(b));
                            } catch (Exception ignored) {}
                        });
                    }
                });
    }
}

