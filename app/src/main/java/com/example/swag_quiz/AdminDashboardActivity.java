package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button pastQuizzes, createQuiz, trackProgress, logoutButton;
    private ImageView userLogo;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        pastQuizzes = findViewById(R.id.pastQuizzes);
        createQuiz = findViewById(R.id.createQuiz);
        trackProgress = findViewById(R.id.trackProgress);
        logoutButton = findViewById(R.id.logoutButton);

        // Fetch Admin's name (from Firebase or Intent)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String adminName = user.getDisplayName();
            if (adminName == null || adminName.isEmpty()) {
                adminName = "Admin"; // Fallback name
            }
            welcomeText.setText("Welcome " + adminName + " - Admin");
        } else {
            welcomeText.setText("Welcome Admin");
        }

        // Click listeners for navigation
        pastQuizzes.setOnClickListener(v -> openPastQuizzes());
        createQuiz.setOnClickListener(v -> openCreateQuiz());
        trackProgress.setOnClickListener(v -> openTrackProgress());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void openPastQuizzes() {
        startActivity(new Intent(this, PastQuizzesActivity.class));
    }

    private void openCreateQuiz() {
        startActivity(new Intent(this, CreateQuizActivity.class));
    }

    private void openTrackProgress() {
        startActivity(new Intent(this, TrackProgressActivity.class));
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
