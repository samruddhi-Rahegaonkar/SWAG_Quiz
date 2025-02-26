package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button signUpButton;
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

        saveUserToFirestore(email, password, role);
    }

    private void saveUserToFirestore(String email, String password, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("role", role);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    showToast("Sign up successful!");
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to sign up: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
