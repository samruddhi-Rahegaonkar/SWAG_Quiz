package com.example.swag_quiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class QuestionAdapter extends BaseAdapter {
    private Context context;
    private List<QuestionModel> questions;

    public QuestionAdapter(Context context, List<QuestionModel> questions) {
        this.context = context;
        this.questions = questions;
    }

    @Override
    public int getCount() {
        return questions.size();
    }

    @Override
    public Object getItem(int position) {
        return questions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.question_item, parent, false);
        }

        TextView questionTextView = convertView.findViewById(R.id.questionTextView);
        TextView optionsTextView = convertView.findViewById(R.id.optionsTextView);
        TextView answerTextView = convertView.findViewById(R.id.answerTextView);

        QuestionModel question = questions.get(position);
        questionTextView.setText("Q: " + question.getQuestion());

        StringBuilder optionsBuilder = new StringBuilder();
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            optionsBuilder.append((char) ('A' + i)).append(") ").append(options.get(i)).append("\n");
        }
        optionsTextView.setText(optionsBuilder.toString());

        answerTextView.setText("Correct Answer: " + question.getCorrectAnswer());

        return convertView;
    }
}
