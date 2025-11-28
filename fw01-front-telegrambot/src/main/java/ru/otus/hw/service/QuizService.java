package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.otus.hw.model.QuizQuestion;
import ru.otus.hw.model.UserQuizSession;
import ru.otus.hw.util.CommonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing quiz sessions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final GrpcDicClientService clientService;

    // Map to store active quiz sessions for each user
    private final Map<Long, UserQuizSession> activeSessions = new ConcurrentHashMap<>();


    /**
     * Start a new quiz session for a user
     *
     * @param chatId Telegram chat ID
     * @param userName User's first name
     * @param topicName Selected topic name
     * @return UserQuizSession if successful, null if no words available
     */
    public UserQuizSession startQuiz(Long chatId, String userName, String topicName) {
        log.info("🎯 Starting quiz for user {} (chatId: {}) on topic: {}", userName, chatId, topicName);

        // Fetch word pairs from gRPC client service
        Map<String, String> wordPairs = clientService.getWordPairsByTopicName(topicName);
        if (wordPairs.isEmpty()) {
            log.warn("⚠️ No word pairs found for topic: {}", topicName);
            return null;
        }
        // Create new session
        UserQuizSession session = new UserQuizSession(chatId, topicName, userName, CommonUtils.shuffleMap(wordPairs));
        activeSessions.put(chatId, session);
        log.info("✅ Quiz session created with {} questions", session.getTotalQuestions());

        return session;
    }

    /**
     * Get active session for a user (read-only access)
     * Use this when you need to read session data without removing it
     *
     * @param chatId Telegram chat ID
     * @return UserQuizSession or null if no active session
     */
    public UserQuizSession getSession(Long chatId) {
        return activeSessions.get(chatId);
    }

    /**
     * Check if user has an active quiz session
     *
     * @param chatId Telegram chat ID
     * @return true if user has an active session
     */
    public boolean hasActiveSession(Long chatId) {
        return activeSessions.containsKey(chatId);
    }

    /**
     * Validate user's answer for current question
     *
     * @param chatId Telegram chat ID
     * @param userAnswer User's answer text
     * @return true if answer is correct, false otherwise
     */
    public boolean validateAnswer(Long chatId, String userAnswer) {
        UserQuizSession session = activeSessions.get(chatId);

        if (session == null || !session.hasMoreQuestions()) {
            return false;
        }

        QuizQuestion currentQuestion = session.getCurrentQuestion();
        String correctAnswer = currentQuestion.russianTranslation();

        // Normalize answers before comparison (trim and lowercase)
        boolean isCorrectAnswer = normalizeText(userAnswer).equals(normalizeText(correctAnswer));

        if (isCorrectAnswer) {
            session.incrementCorrectAnswers();
            log.info("✅ Correct answer from chatId: {}", chatId);
        } else {
            log.info("❌ Incorrect answer from chatId: {}. Expected: {}, Got: {}",
                    chatId, correctAnswer, userAnswer);
        }

        return isCorrectAnswer;
    }

    /**
     * Move to next question in the quiz
     *
     * @param chatId Telegram chat ID
     */
    public void moveToNextQuestion(Long chatId) {
        UserQuizSession session = activeSessions.get(chatId);
        if (session != null) {
            session.moveToNextQuestion();
        }
    }

    /**
     * Remove session from active sessions (cleanup)
     * Returns the removed session for final data access if needed
     *
     * @param chatId Telegram chat ID
     * @return Removed UserQuizSession or null if no session found
     */
    public UserQuizSession removeSession(Long chatId) {
        UserQuizSession removed = activeSessions.remove(chatId);
        if (removed != null) {
            log.info("🏁 Removed quiz session for chatId: {} - Topic: '{}', Final Score: {}/{}",
                    chatId, removed.getTopicName(),
                    removed.getCorrectAnswers(), removed.getTotalQuestions());
        } else {
            log.warn("⚠️ No session found to remove for chatId: {}", chatId);
        }
        return removed;
    }

    /**
     * End quiz session (removes from active sessions)
     *
     * @deprecated Use getSession() followed by removeSession() for clearer intent
     * This method is kept for backward compatibility
     * @param chatId Telegram chat ID
     * @return Removed UserQuizSession or null if no session found
     */
    @Deprecated
    public UserQuizSession endQuiz(Long chatId) {
        return removeSession(chatId);
    }

    /**
     * Get all available topics
     *
     * @return List of topic names
     */
    public List<String> getAllTopics() {
        return clientService.getAllTopics();
    }

    /**
     * Normalize text for comparison (trim, lowercase, remove extra spaces)
     *
     * @param text Text to normalize
     * @return Normalized text
     */
    private static String normalizeText(String text) {
        return StringUtils.isEmpty(text)
                ? ""
                : text.trim().toLowerCase().replace("\\s+", " ");
    }

}
