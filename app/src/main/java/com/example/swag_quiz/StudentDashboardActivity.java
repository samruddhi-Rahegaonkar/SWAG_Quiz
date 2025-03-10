package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private ListView quizListView;
    private ArrayAdapter<String> quizAdapter;
    private ArrayList<String> quizTitles, historyTitles;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    Button logout;
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Guest";

        welcomeTextView = findViewById(R.id.welcomeText);
        quizListView = findViewById(R.id.availableQuizzesListView);

        logout= findViewById(R.id.logoutButton);

        quizTitles = new ArrayList<>();

        quizAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);

        quizListView.setAdapter(quizAdapter);

        welcomeTextView.setText("Welcome " + studentEmail);

        fetchAvailableQuizzes();;

        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            String quizTitle = quizTitles.get(position);
            Intent intent = new Intent(StudentDashboardActivity.this, QuizQuestionsActivity.class);
            intent.putExtra("quizTitle", quizTitle);
            startActivity(intent);
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Toast.makeText(StudentDashboardActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
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

    }

