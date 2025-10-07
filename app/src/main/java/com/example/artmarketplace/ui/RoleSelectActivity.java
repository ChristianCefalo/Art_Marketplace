package com.example.artmarketplace.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artmarketplace.R;
import com.example.artmarketplace.ui.customer.CustomerHomeActivity;
import com.example.artmarketplace.ui.provider.ProviderHomeActivity;

/**
 * Entry point that lets the user choose between customer and provider flows.
 */
public class RoleSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        Button customerButton = findViewById(R.id.button_customer);
        Button providerButton = findViewById(R.id.button_provider);

        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCustomerHome();
            }
        });

        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchProviderHome();
            }
        });
    }

    private void launchCustomerHome() {
        Intent intent = new Intent(this, CustomerHomeActivity.class);
        startActivity(intent);
    }

    private void launchProviderHome() {
        Intent intent = new Intent(this, ProviderHomeActivity.class);
        startActivity(intent);
    }
}
