package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizQuestionsActivity extends AppCompatActivity {

    private ListView questionsListView;
    private ArrayAdapter<String> questionsAdapter;
    private ArrayList<String> questionsList;
    private FirebaseFirestore db;
    private String quizTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get quizTitle from intent
        Intent intent = getIntent();
        quizTitle = intent != null ? intent.getStringExtra("quizTitle") : null;

        if (quizTitle == null || quizTitle.isEmpty()) {
            Log.e("QuizQuestionsActivity", "Quiz title not found in intent");
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(quizTitle); // Set activity title

        // Initialize UI components
        questionsListView = findViewById(R.id.questionsListView);
        questionsList = new ArrayList<>();
        questionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questionsList);
        questionsListView.setAdapter(questionsAdapter);

        String quizId = getIntent().getStringExtra("quizId");
        if (quizId != null) {
            fetchQuestionsByQuizId(quizId);
        } else {
            Toast.makeText(this, "No quiz ID found", Toast.LENGTH_SHORT).show();
        }
    }

        private void fetchQuestionsByQuizId(String quizId) {
            db.collection("quizzes").document(quizId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.contains("questions")) {
                                List<Map<String, Object>> questionsArray = (List<Map<String, Object>>) document.get("questions");
                                if (questionsArray != null && !questionsArray.isEmpty()) {
                                    questionsList.clear();
                                    for (Map<String, Object> questionObj : questionsArray) {
                                        String questionText = (String) questionObj.get("question");
                                        questionsList.add(questionText != null ? questionText : "Untitled Question");
                                    }
                                    questionsAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d("FetchQuestions", "No questions found in quiz.");
                                    Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("FetchQuestions", "Quiz document does not contain questions field.");
                                Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("FetchQuestions", "Failed to fetch quiz details", task.getException());
                            Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FetchQuestions", "Error fetching questions", e);
                        Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
                    });
        }



    private void fetchQuestions(String quizId) {
        db.collection("quizzes")
                .document(quizId)
                .collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            questionsList.clear();
                            for (DocumentSnapshot doc : querySnapshot) {
                                String question = doc.getString("question");
                                questionsList.add(question != null ? question : "Untitled Question");
                            }
                            questionsAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("QuizQuestionsActivity", "No questions found for quiz ID: " + quizId);
                            Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("QuizQuestionsActivity", "Failed to load questions", task.getException());
                        Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizQuestionsActivity", "Error fetching questions", e);
                    Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
                });
    }
}
