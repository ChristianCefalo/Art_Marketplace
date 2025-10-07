package com.example.artmarketplace.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artmarketplace.R;
import com.example.artmarketplace.util.Formatters;

/**
 * Displays details for a single art item.
 */
public class ArtDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ART_ID = "extra_art_id";
    public static final String EXTRA_ART_TITLE = "extra_art_title";
    public static final String EXTRA_ART_PRICE = "extra_art_price";
    public static final String EXTRA_ART_DESC = "extra_art_desc";
    public static final String EXTRA_ART_IMAGE_URL = "extra_art_image_url";
    public static final String EXTRA_ART_PROVIDER_UID = "extra_art_provider_uid";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_detail);

        TextView titleText = findViewById(R.id.text_detail_title);
        TextView priceText = findViewById(R.id.text_detail_price);
        TextView descriptionText = findViewById(R.id.text_detail_description);
        Button addToCartButton = findViewById(R.id.button_add_to_cart);

        String artId = getIntent().getStringExtra(EXTRA_ART_ID);
        String title = getIntent().getStringExtra(EXTRA_ART_TITLE);
        int price = getIntent().getIntExtra(EXTRA_ART_PRICE, 0);
        String desc = getIntent().getStringExtra(EXTRA_ART_DESC);
        String providerUid = getIntent().getStringExtra(EXTRA_ART_PROVIDER_UID);

        if (TextUtils.isEmpty(artId) || TextUtils.isEmpty(providerUid)) {
            Toast.makeText(this, R.string.cart_error_missing_item, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        titleText.setText(title);
        priceText.setText(Formatters.formatPrice(price));
        if (TextUtils.isEmpty(desc)) {
            descriptionText.setText(R.string.cart_no_description);
        } else {
            descriptionText.setText(desc);
        }

        final int finalPrice = price;
        final String finalDesc = desc;
        addToCartButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra(CartActivity.EXTRA_ART_ID, artId);
            intent.putExtra(CartActivity.EXTRA_ART_TITLE, title);
            intent.putExtra(CartActivity.EXTRA_ART_PRICE, finalPrice);
            intent.putExtra(CartActivity.EXTRA_ART_DESC, finalDesc);
            intent.putExtra(CartActivity.EXTRA_PROVIDER_UID, providerUid);
            startActivity(intent);
        });
    }
}
