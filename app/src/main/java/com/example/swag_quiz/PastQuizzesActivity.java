package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;

public class PastQuizzesActivity extends AppCompatActivity {

    private ListView pastQuizzesListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> quizTitles;
    private ArrayList<String> quizIds;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_quizzes);

        db = FirebaseFirestore.getInstance();

        pastQuizzesListView = findViewById(R.id.pastQuizzesListView);
        quizTitles = new ArrayList<>();
        quizIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        pastQuizzesListView.setAdapter(adapter);

        fetchQuizzes();

        pastQuizzesListView.setOnItemClickListener((parent, view, position, id) -> {
            String quizTitle = quizTitles.get(position); // Get quiz title
            String quizId = quizIds.get(position); // Get quiz ID

            Intent intent = new Intent(PastQuizzesActivity.this, QuizQuestionsActivity.class);
            intent.putExtra("quizTitle", quizTitle); // Pass quiz title
            intent.putExtra("quizId", quizId); // Optional, in case you need it
            startActivity(intent);
        });

    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        quizTitles.clear();
                        quizIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            if (title != null) {
                                quizTitles.add(title);
                                quizIds.add(document.getId());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore Error", "Error fetching quizzes", task.getException());
                        Toast.makeText(this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
