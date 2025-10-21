package com.example.artmarketplace;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



public class CustomerHomeActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);
        Button btnChatbot = findViewById(R.id.btnChatbot);

        btnChatbot.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("BOT_TYPE", "customer"); // tells ChatActivity which bot to use
            startActivity(i);
        });

    }
}
