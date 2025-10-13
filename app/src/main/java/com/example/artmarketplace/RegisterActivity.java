package com.example.artmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etDisplayName, etEmail, etPassword, etConfirm;
    private RadioGroup rgRole;
    private RadioButton rbCustomer, rbProvider;
    private Button btnRegister;
    private TextView tvToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        rgRole = findViewById(R.id.rgRole);
        rbCustomer = findViewById(R.id.rbCustomer);
        rbProvider = findViewById(R.id.rbProvider);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvToLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String conf = etConfirm.getText().toString().trim();
        final String role;
        if (rbCustomer.isChecked()) role = "customer";
        else if (rbProvider.isChecked()) role = "provider";
        else {
            toast("Please select a role before registering.");
            return;
        }

        if (email.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            toast("Fill in all fields"); return;
        }
        if (!pass.equals(conf)) { toast("Passwords do not match"); return; }
        if (pass.length() < 6) { toast("Password must be ≥ 6 chars"); return; }

        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    if (r.getUser() == null) {
                        btnRegister.setEnabled(true);
                        toast("Unexpected error: user is null.");
                        return;
                    }

                    String uid = r.getUser().getUid();
                    Map<String, Object> u = new HashMap<>();
                    u.put("email", email);
                    if (!name.isEmpty()) u.put("displayName", name);
                    u.put("role", role);
                    u.put("createdAt", FieldValue.serverTimestamp());
                    db.collection("users").document(uid).set(u, SetOptions.merge())
                            .addOnSuccessListener(x -> routeToHome(role))
                            .addOnFailureListener(e -> { btnRegister.setEnabled(true); toast("Save profile failed: "+e.getMessage()); });
                })
                .addOnFailureListener(e -> { btnRegister.setEnabled(true); toast("Register failed: "+e.getMessage()); });
    }

    private void routeToHome(String role) {
        Intent i = new Intent(this, "provider".equals(role) ? ProviderHomeActivity.class : CustomerHomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
