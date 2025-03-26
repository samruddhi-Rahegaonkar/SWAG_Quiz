package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SelectQuizActivity extends AppCompatActivity {

    private ListView quizListView;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        quizListView = findViewById(R.id.recyclerViewQuizzes);
        db = FirebaseFirestore.getInstance();

        fetchQuizzes();

        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedQuizId = quizIds.get(position);
            Intent intent = new Intent(SelectQuizActivity.this, AddQuestionActivity.class);
            intent.putExtra("quizId", selectedQuizId);
            startActivity(intent);
        });
    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizTitles.clear();
                    quizIds.clear();
                    for (var document : queryDocumentSnapshots) {
                        quizTitles.add(document.getString("title"));
                        quizIds.add(document.getId());
                    }
                    quizListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show());
    }
}
