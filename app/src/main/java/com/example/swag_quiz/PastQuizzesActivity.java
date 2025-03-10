package com.example.swag_quiz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class PastQuizzesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView quizListView;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_quizzes); // Make sure this matches your actual XML filename

        db = FirebaseFirestore.getInstance();
        quizListView = findViewById(R.id.pastQuizzesListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        quizListView.setAdapter(adapter);

        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            String quizTitle = quizTitles.get(position); // Get quiz title
            String quizId = quizIds.get(position); // Get quiz ID

            Intent intent = new Intent(PastQuizzesActivity.this, QuizQuestionsActivity.class);
            intent.putExtra("quizTitle", quizTitle); // Pass quiz title
            intent.putExtra("quizId", quizId); // Optional, in case you need it
            startActivity(intent);

        });

        fetchQuizzes();

        // Long press to delete a quiz
        quizListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmation(quizIds.get(position), position);
            return true;
        });

    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizIds.clear();
                    quizTitles.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        quizIds.add(document.getId());
                        quizTitles.add(document.getString("title"));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PastQuizzesActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation(String quizId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Quiz")
                .setMessage("Are you sure you want to delete this quiz?")
                .setPositiveButton("Yes", (dialog, which) -> deleteQuiz(quizId, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteQuiz(String quizId, int position) {
        db.collection("quizzes").document(quizId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PastQuizzesActivity.this, "Quiz deleted", Toast.LENGTH_SHORT).show();
                    quizTitles.remove(position);
                    quizIds.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PastQuizzesActivity.this, "Failed to delete quiz", Toast.LENGTH_SHORT).show());
    }
}