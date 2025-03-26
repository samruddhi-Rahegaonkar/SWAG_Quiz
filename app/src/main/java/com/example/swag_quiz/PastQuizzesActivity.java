package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class PastQuizzesActivity extends AppCompatActivity {

    private ListView pastQuizzesListView;
    private ArrayAdapter<String> adapter;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_quizzes);

        db = FirebaseFirestore.getInstance();
        pastQuizzesListView = findViewById(R.id.pastQuizzesListView);
        fabAddQuestion = findViewById(R.id.fabAddQuestion);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        pastQuizzesListView.setAdapter(adapter);

        fetchQuizzes();

        // On quiz click, navigate to QuizQuestionsActivity
        pastQuizzesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedQuizId = quizIds.get(position);
            Intent intent = new Intent(PastQuizzesActivity.this, QuizQuestionsActivity.class);
            intent.putExtra("quizId", selectedQuizId);
            startActivity(intent);
        });

        // On long press, show delete dialog
        pastQuizzesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });

        // Floating action button to add questions
        fabAddQuestion.setOnClickListener(v -> {
            if (quizIds.isEmpty()) {
                Toast.makeText(PastQuizzesActivity.this, "No quizzes available to add questions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show a dialog to select a quiz for adding questions
            AlertDialog.Builder builder = new AlertDialog.Builder(PastQuizzesActivity.this);
            builder.setTitle("Select a Quiz");

            String[] quizTitlesArray = quizTitles.toArray(new String[0]);
            builder.setItems(quizTitlesArray, (dialog, which) -> {
                String selectedQuizId = quizIds.get(which);
                Intent intent = new Intent(PastQuizzesActivity.this, AddQuestionActivity.class);
                intent.putExtra("quizId", selectedQuizId);
                startActivity(intent);
            });

            builder.show();
        });
    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizTitles.clear();
                    quizIds.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        if (title != null) {
                            quizTitles.add(title);
                            quizIds.add(document.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Quiz");
        builder.setMessage("Are you sure you want to delete this quiz?");

        builder.setPositiveButton("Yes", (dialog, which) -> deleteQuiz(position));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteQuiz(int position) {
        String quizIdToDelete = quizIds.get(position);
        db.collection("quizzes").document(quizIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
                    quizTitles.remove(position);
                    quizIds.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("PastQuizzesActivity", "Failed to delete quiz", e);
                    Toast.makeText(this, "Failed to delete quiz", Toast.LENGTH_SHORT).show();
                });
    }
}
