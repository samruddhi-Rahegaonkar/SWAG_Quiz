package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private ListView quizListView, historyListView;
    private ArrayAdapter<String> quizAdapter, historyAdapter;
    private ArrayList<String> quizTitles, historyTitles;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Guest";

        welcomeTextView = findViewById(R.id.welcomeTextView);
        quizListView = findViewById(R.id.quizListView);
        historyListView = findViewById(R.id.historyListView);

        quizTitles = new ArrayList<>();
        historyTitles = new ArrayList<>();

        quizAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyTitles);

        quizListView.setAdapter(quizAdapter);
        historyListView.setAdapter(historyAdapter);

        welcomeTextView.setText("Welcome " + studentEmail);

        fetchAvailableQuizzes();
        fetchQuizHistory();

        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            String quizTitle = quizTitles.get(position);
            Intent intent = new Intent(StudentDashboardActivity.this, QuizQuestionsActivity.class);
            intent.putExtra("quizTitle", quizTitle);
            startActivity(intent);
        });
    }

    private void fetchAvailableQuizzes() {
        db.collection("quizzes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                quizTitles.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    quizTitles.add(document.getString("title"));
                }
                quizAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuizHistory() {
        db.collection("quizAttempts").whereEqualTo("studentEmail", studentEmail)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historyTitles.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String quizTitle = document.getString("quizTitle");
                            long score = document.getLong("score");
                            historyTitles.add(quizTitle + " - Score: " + score);
                        }
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load quiz history", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
