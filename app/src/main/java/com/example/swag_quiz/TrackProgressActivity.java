package com.example.swag_quiz;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class TrackProgressActivity extends AppCompatActivity {

    private ListView progressListView;
    private ArrayAdapter<String> progressAdapter;
    private ArrayList<String> progressList;
    private FirebaseFirestore db;
    private String quizId; // Passed via Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_progress);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get quizId from Intent
        quizId = getIntent().getStringExtra("quizId");

        // Setup ListView
        progressListView = findViewById(R.id.progressListView);
        progressList = new ArrayList<>();
        progressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, progressList);
        progressListView.setAdapter(progressAdapter);

        // Fetch results if quizId is present
        if (quizId != null) {
            fetchQuizResults();
        } else {
            Toast.makeText(this, "Quiz ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchQuizResults() {
        db.collection("quizzes").document(quizId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot quizDoc = task.getResult();
                        if (quizDoc.contains("results")) {
                            progressList.clear();
                            Object resultsObj = quizDoc.get("results");

                            if (resultsObj instanceof java.util.Map) {
                                java.util.Map<String, Object> results = (java.util.Map<String, Object>) resultsObj;
                                for (String studentId : results.keySet()) {
                                    java.util.Map<String, Object> studentData = (java.util.Map<String, Object>) results.get(studentId);
                                    String name = (String) studentData.get("name");
                                    Long totalQuestions = (Long) studentData.get("totalQuestions");
                                    Long correctAnswers = (Long) studentData.get("correctAnswers");

                                    progressList.add(name + ": " + correctAnswers + "/" + totalQuestions + " correct");
                                }
                            }

                            progressAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore Error", "Failed to fetch quiz results", task.getException());
                        Toast.makeText(this, "Error loading results", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
