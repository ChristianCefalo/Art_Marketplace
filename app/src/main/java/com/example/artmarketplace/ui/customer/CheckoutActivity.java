package com.example.artmarketplace.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artmarketplace.R;
import com.example.artmarketplace.data.AuthRepo;
import com.example.artmarketplace.data.FirestoreRepo;
import com.example.artmarketplace.model.Order;
import com.example.artmarketplace.util.Formatters;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

/**
 * Handles order creation and the mock payment sequence.
 */
public class CheckoutActivity extends AppCompatActivity {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID_SIM = "PAID_SIM";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private FirestoreRepo firestoreRepo;
    private AuthRepo authRepo;

    private Button placeOrderButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView totalText;

    private String artId;
    private String providerUid;
    private String artTitle;
    private int artPrice;

    private boolean isProcessing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        firestoreRepo = new FirestoreRepo();
        authRepo = new AuthRepo();

        Intent intent = getIntent();
        artId = intent.getStringExtra(CartActivity.EXTRA_ART_ID);
        providerUid = intent.getStringExtra(CartActivity.EXTRA_PROVIDER_UID);
        artTitle = intent.getStringExtra(CartActivity.EXTRA_ART_TITLE);
        artPrice = intent.getIntExtra(CartActivity.EXTRA_ART_PRICE, 0);

        TextView titleText = findViewById(R.id.text_checkout_title);
        totalText = findViewById(R.id.text_checkout_total);
        statusText = findViewById(R.id.text_checkout_status);
        placeOrderButton = findViewById(R.id.button_place_order);
        progressBar = findViewById(R.id.progress_checkout);

        if (TextUtils.isEmpty(artId) || TextUtils.isEmpty(providerUid)) {
            Toast.makeText(this, R.string.checkout_error_missing, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        titleText.setText(artTitle);
        totalText.setText(Formatters.formatPrice(artPrice));
        statusText.setText(R.string.checkout_status_ready);

        placeOrderButton.setOnClickListener(v -> beginCheckout());
    }

    private void beginCheckout() {
        if (isProcessing) {
            return;
        }
        if (TextUtils.isEmpty(artId) || TextUtils.isEmpty(providerUid)) {
            Toast.makeText(this, R.string.checkout_error_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText(R.string.checkout_processing);
        setProcessing(true);

        FirebaseUser currentUser = authRepo.getCurrentUser();
        if (currentUser != null) {
            createOrder(currentUser.getUid());
        } else {
            authRepo.signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user = authResult.getUser();
                            if (user == null) {
                                handleFailure(new IllegalStateException("Missing user"));
                                return;
                            }
                            createOrder(user.getUid());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleFailure(e);
                        }
                    });
        }
    }

    private void createOrder(String buyerUid) {
        Order order = new Order(null, artId, buyerUid, providerUid, STATUS_PENDING, artPrice,
                System.currentTimeMillis());
        firestoreRepo.createOrder(order)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        totalText.setText(Formatters.formatPrice(artPrice));
                        statusText.setText(R.string.checkout_status_pending);
                        advanceToSimulatedPayment(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleFailure(e);
                    }
                });
    }

    private void advanceToSimulatedPayment(final String orderId) {
        firestoreRepo.setOrderStatus(orderId, STATUS_PAID_SIM)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        statusText.setText(R.string.checkout_status_paid);
                        confirmOrder(orderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleFailure(e);
                    }
                });
    }

    private void confirmOrder(final String orderId) {
        firestoreRepo.setOrderStatus(orderId, STATUS_CONFIRMED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        statusText.setText(R.string.checkout_status_confirmed);
                        setProcessing(false);
                        launchReceipt(orderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleFailure(e);
                    }
                });
    }

    private void launchReceipt(String orderId) {
        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra(ReceiptActivity.EXTRA_ORDER_ID, orderId);
        intent.putExtra(ReceiptActivity.EXTRA_ORDER_STATUS, STATUS_CONFIRMED);
        intent.putExtra(ReceiptActivity.EXTRA_ORDER_TOTAL, artPrice);
        intent.putExtra(ReceiptActivity.EXTRA_ART_TITLE, artTitle);
        startActivity(intent);
        finish();
    }

    private void handleFailure(@NonNull Exception e) {
        String message = e.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.checkout_error_generic_fallback);
        }
        Toast.makeText(this, getString(R.string.checkout_error_generic, message), Toast.LENGTH_LONG)
                .show();
        statusText.setText(R.string.checkout_status_ready);
        setProcessing(false);
    }

    private void setProcessing(boolean processing) {
        isProcessing = processing;
        placeOrderButton.setEnabled(!processing);
        progressBar.setVisibility(processing ? View.VISIBLE : View.GONE);
    }
}
