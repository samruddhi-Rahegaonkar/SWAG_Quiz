package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button signUpButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        adminRadioButton = findViewById(R.id.adminRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            registerUser();
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String role = adminRadioButton.isChecked() ? "admin" : studentRadioButton.isChecked() ? "student" : "";

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty()) {
            showToast("Please fill all fields and select a role");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            progressBar.setVisibility(View.GONE);
            return;
        }

        // 🔥 Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, role);
                        }
                    } else {
                        showToast("Sign up failed: " + task.getException().getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);  // 🔒 Storing only metadata, not password

        db.collection("users").document(userId)  // 🔥 Storing user with UID as document ID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showToast("Sign up successful!");
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save user data: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
