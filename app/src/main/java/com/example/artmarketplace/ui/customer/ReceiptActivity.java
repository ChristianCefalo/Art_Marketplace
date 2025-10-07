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
 * Displays the final order receipt after the mock payment completes.
 */
public class ReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_receipt_order_id";
    public static final String EXTRA_ORDER_STATUS = "extra_receipt_order_status";
    public static final String EXTRA_ORDER_TOTAL = "extra_receipt_order_total";
    public static final String EXTRA_ART_TITLE = "extra_receipt_art_title";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID_SIM = "PAID_SIM";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Intent intent = getIntent();
        String orderId = intent.getStringExtra(EXTRA_ORDER_ID);
        String status = intent.getStringExtra(EXTRA_ORDER_STATUS);
        int total = intent.getIntExtra(EXTRA_ORDER_TOTAL, 0);
        String artTitle = intent.getStringExtra(EXTRA_ART_TITLE);

        if (TextUtils.isEmpty(orderId)) {
            Toast.makeText(this, R.string.receipt_error_missing, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView subtitleText = findViewById(R.id.text_receipt_subtitle);
        TextView orderIdText = findViewById(R.id.text_receipt_order_id);
        TextView totalText = findViewById(R.id.text_receipt_total);
        TextView statusText = findViewById(R.id.text_receipt_status);
        Button doneButton = findViewById(R.id.button_receipt_done);

        if (TextUtils.isEmpty(artTitle)) {
            artTitle = getString(R.string.receipt_default_art_title);
        }

        subtitleText.setText(getString(R.string.receipt_subtitle, artTitle));
        orderIdText.setText(getString(R.string.receipt_order_id, orderId));
        totalText.setText(getString(R.string.receipt_total, Formatters.formatPrice(total)));
        statusText.setText(getString(R.string.receipt_status_label, mapStatusToLabel(status)));

        doneButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, CustomerHomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private String mapStatusToLabel(@Nullable String status) {
        if (STATUS_CONFIRMED.equals(status)) {
            return getString(R.string.receipt_status_confirmed);
        } else if (STATUS_PAID_SIM.equals(status)) {
            return getString(R.string.receipt_status_paid_sim);
        } else if (STATUS_PENDING.equals(status)) {
            return getString(R.string.receipt_status_pending);
        }
        return getString(R.string.receipt_status_unknown);
    }
}
