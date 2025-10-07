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
 * Shows the customer's current cart prior to checkout.
 */
public class CartActivity extends AppCompatActivity {

    public static final String EXTRA_ART_ID = "extra_cart_art_id";
    public static final String EXTRA_ART_TITLE = "extra_cart_art_title";
    public static final String EXTRA_ART_PRICE = "extra_cart_art_price";
    public static final String EXTRA_ART_DESC = "extra_cart_art_desc";
    public static final String EXTRA_PROVIDER_UID = "extra_cart_provider_uid";

    private String artId;
    private String providerUid;
    private String artTitle;
    private String artDesc;
    private int artPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Intent intent = getIntent();
        artId = intent.getStringExtra(EXTRA_ART_ID);
        providerUid = intent.getStringExtra(EXTRA_PROVIDER_UID);
        artTitle = intent.getStringExtra(EXTRA_ART_TITLE);
        artDesc = intent.getStringExtra(EXTRA_ART_DESC);
        artPrice = intent.getIntExtra(EXTRA_ART_PRICE, 0);

        if (TextUtils.isEmpty(artId) || TextUtils.isEmpty(providerUid)) {
            Toast.makeText(this, R.string.cart_error_missing_item, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView titleText = findViewById(R.id.text_cart_title);
        TextView descText = findViewById(R.id.text_cart_desc);
        TextView priceText = findViewById(R.id.text_cart_price);
        Button continueButton = findViewById(R.id.button_continue_browsing);
        Button checkoutButton = findViewById(R.id.button_proceed_checkout);

        titleText.setText(artTitle);
        if (TextUtils.isEmpty(artDesc)) {
            descText.setText(R.string.cart_no_description);
        } else {
            descText.setText(artDesc);
        }
        priceText.setText(Formatters.formatPrice(artPrice));

        continueButton.setOnClickListener(v -> finish());
        checkoutButton.setOnClickListener(v -> {
            Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
            checkoutIntent.putExtra(EXTRA_ART_ID, artId);
            checkoutIntent.putExtra(EXTRA_ART_TITLE, artTitle);
            checkoutIntent.putExtra(EXTRA_ART_DESC, artDesc);
            checkoutIntent.putExtra(EXTRA_ART_PRICE, artPrice);
            checkoutIntent.putExtra(EXTRA_PROVIDER_UID, providerUid);
            startActivity(checkoutIntent);
        });
    }
}
