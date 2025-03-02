package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

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
            finish(); // Close activity if quiz title is missing
            return;
        }

        setTitle(quizTitle); // Set activity title to quiz title

        // Initialize UI components
        questionsListView = findViewById(R.id.questionsListView);
        questionsList = new ArrayList<>();
        questionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questionsList);
        questionsListView.setAdapter(questionsAdapter);

        // Fetch questions
        fetchQuestionsByTitle();
    }

    private void fetchQuestionsByTitle() {
        Log.d("FetchQuestions", "Fetching quiz for title: " + quizTitle);

        db.collection("quizzes")
                .whereEqualTo("title", quizTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot quizDocument = task.getResult().getDocuments().get(0);
                        String quizId = quizDocument.getId();
                        Log.d("FetchQuestions", "Found quiz with ID: " + quizId);
                        fetchQuestionsByQuizId(quizId);
                    } else {
                        Log.e("FetchQuestions", "No quiz found for title: " + quizTitle);
                        Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FetchQuestions", "Failed to fetch quiz by title", e);
                    Toast.makeText(this, "Error fetching quiz", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchQuestionsByQuizId(String quizId) {
        CollectionReference questionsRef = db.collection("quizzes").document(quizId).collection("questions");

        questionsRef.get().addOnCompleteListener(task -> {
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
                    Log.d("FetchQuestions", "No questions found for quiz ID: " + quizId);
                    Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("FetchQuestions", "Failed to load questions", task.getException());
                Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("FetchQuestions", "Error fetching questions", e);
            Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
        });
    }
}
