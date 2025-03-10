package com.example.swag_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuizListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private List<String> quizIds = new ArrayList<>();
    private List<String> quizTitles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        quizAdapter = new QuizAdapter(quizTitles, quizIds);
        recyclerView.setAdapter(quizAdapter);

        fetchQuizzes();
    }

    private void fetchQuizzes() {
        db.collection("quizzes")
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        quizIds.clear();
                        quizTitles.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            quizIds.add(document.getId()); // Get quiz ID (e.g., CP2hEfZi2nxFIV4U1h20)
                            quizTitles.add(document.getString("title")); // Get quiz title
                        }
                        quizAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(QuizListActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show());
    }

    private class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
        private List<String> titles;
        private List<String> ids;

        QuizAdapter(List<String> titles, List<String> ids) {
            this.titles = titles;
            this.ids = ids;
        }

        @NonNull
        @Override
        public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            return new QuizViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
            holder.quizTitle.setText(titles.get(position));
            holder.itemView.setOnClickListener(v -> {
                String selectedQuizId = ids.get(position); // Get selected quiz ID
                Intent intent = new Intent(QuizListActivity.this, QuizQuestionsActivity.class);
                intent.putExtra("quizId", selectedQuizId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        class QuizViewHolder extends RecyclerView.ViewHolder {
            TextView quizTitle;

            QuizViewHolder(@NonNull View itemView) {
                super(itemView);
                quizTitle = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}