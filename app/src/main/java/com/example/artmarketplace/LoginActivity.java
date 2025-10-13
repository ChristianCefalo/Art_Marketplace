package com.example.artmarketplace;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvToRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvToRegister = findViewById(R.id.tvToRegister);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) { toast("Enter email and password"); return; }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    if (r.getUser() == null) {
                        toast("Unexpected error: user is null.");
                        return;
                    }
                    String uid = r.getUser().getUid();
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(snap -> {
                                String role = snap.getString("role");
                                if ("provider".equals(role)) {
                                    go(ProviderHomeActivity.class);
                                } else {
                                    go(CustomerHomeActivity.class);
                                }
                            })
                            .addOnFailureListener(e -> { btnLogin.setEnabled(true); toast("Failed to load profile: "+e.getMessage()); });
                })
                .addOnFailureListener(e -> { btnLogin.setEnabled(true); toast("Login failed: "+e.getMessage()); });
    }

    private void go(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
