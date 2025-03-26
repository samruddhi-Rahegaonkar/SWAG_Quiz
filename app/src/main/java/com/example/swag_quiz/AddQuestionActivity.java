package com.example.swag_quiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {

    private EditText editTextQuestion, editTextOptionA, editTextOptionB, editTextOptionC, editTextOptionD, editTextCorrectAnswer;
    private Button buttonAddQuestion, buttonFinish;
    private FirebaseFirestore db;
    private String quizId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("quizId");

        if (quizId == null || quizId.isEmpty()) {
            Toast.makeText(this, "Quiz ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextQuestion = findViewById(R.id.questionInput);
        editTextOptionA = findViewById(R.id.optionA);
        editTextOptionB = findViewById(R.id.optionB);
        editTextOptionC = findViewById(R.id.optionC);
        editTextOptionD = findViewById(R.id.optionD);
        editTextCorrectAnswer = findViewById(R.id.editTextCorrectAnswer);
        buttonAddQuestion = findViewById(R.id.addQuestionButton);
        buttonFinish = findViewById(R.id.submitQuizButton);

        buttonAddQuestion.setOnClickListener(v -> addQuestionToFirestore());

        buttonFinish.setOnClickListener(v -> {
            Intent intent = new Intent(AddQuestionActivity.this, PastQuizzesActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void addQuestionToFirestore() {
        String question = editTextQuestion.getText().toString().trim();
        String optionA = editTextOptionA.getText().toString().trim();
        String optionB = editTextOptionB.getText().toString().trim();
        String optionC = editTextOptionC.getText().toString().trim();
        String optionD = editTextOptionD.getText().toString().trim();
        String correctAnswer = editTextCorrectAnswer.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(optionA) || TextUtils.isEmpty(optionB) || TextUtils.isEmpty(optionC) ||
                TextUtils.isEmpty(optionD) || TextUtils.isEmpty(correctAnswer)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!correctAnswer.matches("[A-D]")) {
            Toast.makeText(this, "Correct answer must be A, B, C, or D", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = new ArrayList<>();
        options.add(optionA);
        options.add(optionB);
        options.add(optionC);
        options.add(optionD);

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("question", question);
        questionData.put("options", options);
        questionData.put("correctAnswer", correctAnswer);

        db.collection("quizzes").document(quizId).collection("questions")
                .add(questionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Question added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add question: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        editTextQuestion.setText("");
        editTextOptionA.setText("");
        editTextOptionB.setText("");
        editTextOptionC.setText("");
        editTextOptionD.setText("");
        editTextCorrectAnswer.setText("");
    }
}
