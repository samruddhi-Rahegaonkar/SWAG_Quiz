package com.example.swag_quiz;

import java.util.List;

public class QuestionModel {
    private String question;
    private List<String> options;
    private String docId;
    private String correctAnswer;

    public QuestionModel(String docId, String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.docId = docId;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getDocId() {
        return docId;
    }
}