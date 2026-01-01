package com.example.uob_23057989_wypf_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        MaterialToolbar topBar = findViewById(R.id.topBarLogin);
        EditText email = findViewById(R.id.email);
        EditText pass = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_btn);
        TextView registerTxt = findViewById(R.id.register_text);
        ProgressBar progressBar = findViewById(R.id.login_progress_bar); // Find the loader

        // Back button to MainActivity (home screen)
        topBar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        // Navigate to Register Screen
        registerTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Login logic
        loginBtn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = pass.getText().toString().trim();

            if (TextUtils.isEmpty(e)) {
                email.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(p)) {
                pass.setError("Password required");
                return;
            }

            // --- Show loader and disable button ---
            loginBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(e, p).addOnCompleteListener(task -> {
                // --- Hide loader and re-enable button ---
                loginBtn.setEnabled(true);
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " +
                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
