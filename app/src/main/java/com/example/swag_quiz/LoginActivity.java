package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private RadioButton adminRadioButton, studentRadioButton;
    private Button loginButton;
    private TextView signUpTextView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        adminRadioButton = findViewById(R.id.adminRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            animateLoginButton();
            loginUser();
        });

        signUpTextView.setOnClickListener(v -> navigateToSignUp());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter email and password");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // 🔥 Authenticate with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        showToast("Login failed: " + task.getException().getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void checkUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("admin".equals(role) && adminRadioButton.isChecked()) {
                            showToast("Login successful as Admin");
                            navigateToDashboard(AdminDashboardActivity.class);
                        } else if ("student".equals(role) && studentRadioButton.isChecked()) {
                            showToast("Login successful as Student");
                            navigateToDashboard(StudentDashboardActivity.class);
                        } else {
                            showToast("Invalid role selection");
                        }
                    } else {
                        showToast("User role not found.");
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    showToast("Error retrieving user role: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void navigateToDashboard(Class<?> destination) {
        Intent intent = new Intent(LoginActivity.this, destination);
        startActivity(intent);
        finish();
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void animateLoginButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 0.9f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }
}
