package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsActivity extends AppCompatActivity {

    private ListView questionsListView;
    private QuestionAdapter questionAdapter;
    private ArrayList<QuestionModel> questionsList;
    private FirebaseFirestore db;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get quizId from intent
        quizId = getIntent().getStringExtra("quizId");

        if (quizId == null || quizId.isEmpty()) {
            Log.e("QuizQuestionsActivity", "Quiz ID not found in intent");
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        questionsListView = findViewById(R.id.questionsListView);
        questionsList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(this, questionsList);
        questionsListView.setAdapter(questionAdapter);

        fetchQuestions();

        // Long click to delete a question
        questionsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    // ✅ Fetch Questions Using Subcollection
    private void fetchQuestions() {
        db.collection("quizzes").document(quizId).collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            questionsList.clear();
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                String question = doc.getString("question");
                                List<String> options = (List<String>) doc.get("options");
                                String correctAnswer = doc.getString("correctAnswer");
                                String docId = doc.getId();

                                questionsList.add(new QuestionModel(docId, question, options, correctAnswer));
                            }
                            questionAdapter.notifyDataSetChanged();
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

    // ✅ Delete Question from Firestore
    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Question");
        builder.setMessage("Are you sure you want to delete this question?");

        builder.setPositiveButton("Yes", (dialog, which) -> deleteQuestion(position));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteQuestion(int position) {
        String docId = questionsList.get(position).getDocId();
        DocumentReference questionRef = db.collection("quizzes").document(quizId)
                .collection("questions").document(docId);

        questionRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Question deleted successfully", Toast.LENGTH_SHORT).show();
                    questionsList.remove(position);
                    questionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizQuestionsActivity", "Failed to delete question", e);
                    Toast.makeText(this, "Failed to delete question", Toast.LENGTH_SHORT).show();
                });
    }
}
