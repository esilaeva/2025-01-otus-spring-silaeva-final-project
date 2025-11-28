package ru.otus.hw.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a quiz session for a single user
 */
@Data
public class UserQuizSession {

    private Long chatId;

    private String userName;

    private String topicName;

    private List<QuizQuestion> questions;

    private int currentQuestionIndex;

    private int correctAnswers;

    private int totalQuestions;

    public UserQuizSession(Long chatId, String topicName, String userName, Map<String, String> wordPairs) {
        this.chatId = chatId;
        this.topicName = topicName;
        this.userName = userName;
        this.currentQuestionIndex = 0;
        this.questions = new ArrayList<>();
        this.correctAnswers = 0;

        wordPairs.forEach((heWord, ruWord) ->
                questions.add(new QuizQuestion(heWord, ruWord)));

        this.totalQuestions = questions.size();
    }

    public QuizQuestion getCurrentQuestion() {
        if (hasMoreQuestions()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < totalQuestions;
    }

    public void moveToNextQuestion() {
        currentQuestionIndex++;
    }

    public void incrementCorrectAnswers() {
        correctAnswers++;
    }

    public int getScore() {
        return totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
    }
}
