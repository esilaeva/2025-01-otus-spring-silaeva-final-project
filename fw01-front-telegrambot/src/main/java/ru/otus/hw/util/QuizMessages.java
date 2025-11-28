package ru.otus.hw.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.otus.hw.model.QuizQuestion;
import ru.otus.hw.model.UserQuizSession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuizMessages {


    public static String startMessage(UserQuizSession session) {
        return """
                ✅ Great choice!
                
                📚 Topic: *%s*
                📝 Questions: *%d*
                
                Let's begin! 🚀
                """.formatted(session.getTopicName(), session.getTotalQuestions());
    }

    public static String questionMessage(UserQuizSession session, QuizQuestion question) {
        return """
                📚 Question %d/%d
                
                What is the Russian translation of:
                🔤 *%s*
                
                Type your answer below:
                """.formatted(session.getCurrentQuestionIndex() + 1,
                session.getTotalQuestions(),
                question.hebrewWord());
    }

    public static String stopMessage(int completed, int total) {
        return """
                🛑 Quiz stopped!
                
                You completed %d/%d questions.
                
                Type /start to begin a new quiz.
                
                """.formatted(completed, total);
    }

    public static String correctAnswerFeedback(QuizQuestion question, int correctAnswers, int questionNumber) {
        return """
                ✅ *Correct!*
                
                *%s* = %s
                
                Score: %d/%d
                """.formatted(
                question.hebrewWord(), question.russianTranslation(),
                correctAnswers, questionNumber
        );
    }

    public static String incorrectAnswerFeedback(QuizQuestion question, String userAnswer,
                                                 int correctAnswers, int questionNumber) {
        return """
                ❌ *Incorrect*
                
                *%s* = %s
                Your answer: %s
                
                Score: %d/%d
                """.formatted(question.hebrewWord(), question.russianTranslation(), userAnswer,
                correctAnswers, questionNumber
        );
    }

    public static String finalMessage(UserQuizSession session) {
        String emoji = session.getScore() >= 80 ? "🎉" : session.getScore() >= 60 ? "👍" : "💪";
        return """
                🏁 *Quiz Complete!*
                
                %s Great job, %s!
                
                📊 *Final Results:*
                -------------------
                📚 Topic: *%s*
                ✅ Correct: %d
                ❌ Incorrect: %d
                📈 Score: *%d%%*
                
                Type /start to try another quiz!
                """.formatted(emoji, session.getUserName(), session.getTopicName(),
                session.getCorrectAnswers(), session.getTotalQuestions() - session.getCorrectAnswers(),
                session.getScore());
    }

    public static String helpMessage() {
        return """
                ✅ *Welcome to Quiz Bot*
                
                This bot helps to enrich your vocabulary
                with words from different topics.
                
                📝 *Available Commands:*
                /start - Begin a new quiz (select topic)
                /help - Show this help message
                /status - Show bot status
                /stop - Stop current quiz
                
                🎯 *How to Play:*
                1. Type /start to begin
                2. Select a topic from the menu
                3. Answer each Hebrew word in Russian
                4. Get your final score
                
                Good luck! 🍀
                """;
    }

    public static String welcomeMessage(String userName) {
        return """
                👋 Hello, *%s*!
                
                🎯 Welcome to the Hebrew-Russian Quiz Bot!
                
                Please select a topic to start your quiz:""".formatted(userName);
    }

    public static String statusMessage(String javaVersion, String uptime, int topicsNumber) {
        return """
                ✅ Bot is running smoothly!
                
                🚀 Java Version: *%s*
                ⏰ Uptime: *%s*
                📘 Topics number: *%s*
                """.formatted(javaVersion, uptime, topicsNumber);
    }

    public static String unknownCommandMessage(String text) {
        return """
                You said:
                *%s*
                
                Type /help for available commands.
                """.formatted(text);
    }

    public static String noWordsAvailableMessage() {
        return "❌ Failed to start quiz. *No words available* for this topic.";
    }
}