package com.example.swag_quiz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.*;

public class CreateQuizActivity extends AppCompatActivity {

    private EditText quizTitle, questionInput, optionA, optionB, optionC, optionD;
    private Spinner correctAnswerSpinner;
    private Button addImageButton, addQuestionButton, submitQuizButton;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private List<Map<String, Object>> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        // Initialize Firestore and Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("quiz_images");

        // Initialize views
        quizTitle = findViewById(R.id.quizTitle);
        questionInput = findViewById(R.id.questionInput);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        correctAnswerSpinner = findViewById(R.id.correctAnswerSpinner);
        addImageButton = findViewById(R.id.addImageButton);
        addQuestionButton = findViewById(R.id.addQuestionButton);
        submitQuizButton = findViewById(R.id.submitQuizButton);

        // Set spinner options (A, B, C, D)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"A", "B", "C", "D"});
        correctAnswerSpinner.setAdapter(adapter);

        // Add Image Listener
        addImageButton.setOnClickListener(v -> selectImage());

        // Add Question to list
        addQuestionButton.setOnClickListener(v -> addQuestion());

        // Submit Quiz
        submitQuizButton.setOnClickListener(v -> submitQuiz());
    }

    // Select image from device
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    // Get the image URI after selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    // Add question to the list
    private void addQuestion() {
        String question = questionInput.getText().toString().trim();
        String a = optionA.getText().toString().trim();
        String b = optionB.getText().toString().trim();
        String c = optionC.getText().toString().trim();
        String d = optionD.getText().toString().trim();
        String correctAnswer = correctAnswerSpinner.getSelectedItem().toString();

        if (question.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("question", question);
        questionData.put("options", Arrays.asList(a, b, c, d));
        questionData.put("correctAnswer", correctAnswer);
        if (imageUri != null) {
            uploadImage(questionData);
        } else {
            questionList.add(questionData);
            clearFields();
            Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload image to Firebase Storage and add to question data
    private void uploadImage(Map<String, Object> questionData) {
        String fileName = "img_" + System.currentTimeMillis();
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    questionData.put("imageUrl", uri.toString());
                    questionList.add(questionData);
                    clearFields();
                    Toast.makeText(this, "Question with image added!", Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e -> Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show());
    }

    // Clear fields after adding a question
    private void clearFields() {
        questionInput.setText("");
        optionA.setText("");
        optionB.setText("");
        optionC.setText("");
        optionD.setText("");
        imageUri = null;
    }

    // Submit the entire quiz to Firestore
    private void submitQuiz() {
        String title = quizTitle.getText().toString().trim();

        if (title.isEmpty() || questionList.isEmpty()) {
            Toast.makeText(this, "Please enter a quiz title and add at least one question!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", title);
        quizData.put("questions", questionList);
        quizData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("quizzes").add(quizData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String quizTitle = title; // Get the quiz title
                        Intent intent = new Intent(CreateQuizActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("quizTitle", quizTitle);
                        startActivity(intent);
                        finish(); // Optional: Close the current activity
                    }
                    else {
                        Toast.makeText(this, "Failed to submit quiz.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
