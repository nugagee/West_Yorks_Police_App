package com.example.uob_23057989_wypf_app;

import android.content.Intent;import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // Import specific exception
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Button registerBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find all views
        MaterialToolbar topBar = findViewById(R.id.topBarRegister);
        EditText email = findViewById(R.id.reg_email);
        EditText pass = findViewById(R.id.reg_pass);
        EditText confirm = findViewById(R.id.reg_confirm_pass);
        CheckBox isAdminCheckbox = findViewById(R.id.chk_is_admin);
        registerBtn = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.register_progress_bar);
        TextView loginText = findViewById(R.id.login_text);

        // Back button -> go to Welcome/Main screen
        topBar.setNavigationOnClickListener(v -> {
            // Replaced with finish() to go back to the previous screen naturally
            finish();
        });

        // Already registered? Navigate to Login
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // --- Register user ---
        registerBtn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = pass.getText().toString().trim();
            String c = confirm.getText().toString().trim();
            boolean isAdmin = isAdminCheckbox.isChecked();

            // --- Validation ---
            if (TextUtils.isEmpty(e)) {
                email.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(p)) {
                pass.setError("Password required");
                return;
            }
            if (p.length() < 6) {
                pass.setError("Password must be at least 6 characters");
                return;
            }
            if (!p.equals(c)) {
                confirm.setError("Passwords do not match");
                return;
            }

            setLoadingState(true);

            // --- Firebase Auth User Creation ---
            auth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Auth successful, now save user role to Firestore
                            String userId = auth.getCurrentUser().getUid();
                            saveUserRole(userId, e, isAdmin);
                        } else {
                            // Auth failed, hide loader and show a more specific error
                            setLoadingState(false);

                            // --- START: IMPROVED ERROR HANDLING ---
                            Exception exception = task.getException();
                            String errorMessage;

                            if (exception instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "An account with this email already exists.";
                            } else if (exception instanceof SecurityException && exception.getMessage() != null && exception.getMessage().contains("com.google.android.gms")) {
                                // This specifically targets the "broker" error
                                errorMessage = "Could not connect to Google Services. Please check your connection and try again.";
                            } else if (exception != null) {
                                // For all other errors
                                errorMessage = "Registration failed: " + exception.getLocalizedMessage();
                            } else {
                                // Fallback message
                                errorMessage = "Registration failed. Please try again.";
                            }

                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            // --- END: IMPROVED ERROR HANDLING ---
                        }
                    });
        });
    }

    /**
     * Saves the user's details to Firestore. Hides the loader on completion.
     */
    private void saveUserRole(String userId, String email, boolean isAdmin) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId); // <<< FIX: Added userId to the document
        user.put("email", email);
        user.put("role", isAdmin ? "admin" : "user");

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Firestore write was successful, hide loader and navigate
                    setLoadingState(false);
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                    // Go to Dashboard directly after successful registration
                    startActivity(new Intent(this, DashboardActivity.class));
                    finishAffinity(); // Clears all previous activities from the back stack
                })
                .addOnFailureListener(e -> {
                    // Firestore write failed, hide loader and show error
                    setLoadingState(false);
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Helper method to toggle the loading state of the UI.
     * @param isLoading True to show loader and disable button, false otherwise.
     */
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            registerBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }
}
