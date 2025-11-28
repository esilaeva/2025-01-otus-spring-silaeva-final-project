package ru.otus.hw.service;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.QuizQuestion;
import ru.otus.hw.model.UserQuizSession;
import ru.otus.hw.util.CommonUtils;
import ru.otus.hw.util.QuizMessages;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramBot telegramBot;

    private final GrpcDicClientService grpcClientService;

    private final QuizService quizService;


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(this::processUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception.response() != null) {
                log.error("❌ Telegram API error: {} - {}", exception.response().errorCode(),
                        exception.response().description());
            } else {
                log.error("❌ Network error: {}", exception.getMessage());
            }
        });
        log.info("✅ Telegram bot started and listening for updates");
    }

    @PreDestroy
    public void shutdown() {
        telegramBot.removeGetUpdatesListener();
        log.info("✅ Telegram bot stopped");
    }


    private void processUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            processTextMessage(update);
        } else if (update.callbackQuery() != null) {
            processCallbackQuery(update);
        }
    }

    private void processCallbackQuery(Update update) {

        CallbackQuery callbackQuery = update.callbackQuery();
        Long chatId = callbackQuery.message().chat().id();
        String callbackData = callbackQuery.data();
        String userName = callbackQuery.from().firstName();

        log.warn("✅ Received callback query from chat ID: {} with data: {}", chatId, callbackData);

        // Answer the callback query (removes loading state from button)
        telegramBot.execute(new AnswerCallbackQuery(callbackQuery.id()));

        // Handle topic selection
        if (callbackData.startsWith("topic:")) {
            String selectedTopic = callbackData.substring(6); // Remove "topic:" prefix
            handleTopicSelection(chatId, userName, selectedTopic);
        }
    }

    private void handleTopicSelection(Long chatId, String userName, String topicName) {
        log.info("🎯 User {} selected topic: {}", userName, topicName);

        UserQuizSession session = quizService.startQuiz(chatId, userName, topicName);

        if (session == null) {
            sendMessageAsync(chatId, QuizMessages.noWordsAvailableMessage(), ParseMode.Markdown);
            return;
        }

        var startMessage = QuizMessages.startMessage(session);

        sendMessageAsync(chatId, startMessage, ParseMode.Markdown);
        // Send first question
        sendQuestion(session);
    }

    private void sendQuestion(UserQuizSession session) {
        QuizQuestion question = session.getCurrentQuestion();
        if (question == null) {
            return;
        }

        var questionMessage = QuizMessages.questionMessage(session, question);

        sendMessageAsync(session.getChatId(), questionMessage, ParseMode.Markdown);
    }

    private void processTextMessage(Update update) {
        Message message = update.message();
        Long chatId = message.chat().id();
        String text = message.text();

        log.debug("✅ Received message from chat ID: {} with text: {}", chatId, text);

        // Check if user is in an active quiz session and not sending a command
        if (quizService.hasActiveSession(chatId) && !text.startsWith("/")) {
            handleQuizAnswer(chatId, text);
            return;
        }

        // Handle commands
        switch (text.toLowerCase()) {
            case "/start" -> handleStartCommand(chatId, message.from().firstName());
            case "/help" -> handleHelpCommand(chatId);
            case "/status" -> handleStatusCommand(chatId);
            case "/stop" -> handleStopCommand(chatId);
            default -> handleUnknownCommand(chatId, text);
        }
    }

    private void handleStopCommand(Long chatId) {
        if (!quizService.hasActiveSession(chatId)) {
            sendMessageAsync(chatId, "*No active quiz to stop*. Type /start to begin.", ParseMode.Markdown);
            return;
        }
        UserQuizSession session = quizService.getSession(chatId);
        if (session == null) {
            sendMessageAsync(chatId, "*No active quiz to stop*. Type /start to begin.", ParseMode.Markdown);
            return;
        }
        String message = QuizMessages.stopMessage(session.getCurrentQuestionIndex(), session.getTotalQuestions());

        sendMessageAsync(chatId, message, ParseMode.Markdown);
        quizService.removeSession(chatId);
        log.info("✅ Quiz manually stopped and cleaned up for chatId: {}", chatId);
    }

    private void handleQuizAnswer(Long chatId, String userAnswer) {
        UserQuizSession session = quizService.getSession(chatId);
        if (session == null) {
            return;
        }
        QuizQuestion currentQuestion = session.getCurrentQuestion();
        boolean isCorrect = quizService.validateAnswer(chatId, userAnswer);
        String feedback = isCorrect
                ? QuizMessages.correctAnswerFeedback(
                        currentQuestion, session.getCorrectAnswers(),
                session.getCurrentQuestionIndex() + 1)
                : QuizMessages.incorrectAnswerFeedback(
                        currentQuestion, userAnswer, session.getCorrectAnswers(),
                session.getCurrentQuestionIndex() + 1);
        sendMessageAsync(chatId, feedback, ParseMode.Markdown);
        quizService.moveToNextQuestion(chatId);
        if (!session.hasMoreQuestions()) {
            endQuizSession(chatId);
        } else {
            sendQuestion(session);
        }
    }

    private void endQuizSession(Long chatId) {
        // Step 1: Get session data (don't remove yet)
        UserQuizSession session = quizService.getSession(chatId);
        if (session == null) {
            log.warn("⚠️ No active session found for chatId: {} during quiz completion", chatId);
            return;
        }
        // Step 2: Calculate and prepare final message using session data
        String emoji = session.getScore() >= 80 ? "🎉" : session.getScore() >= 60 ? "👍" : "💪";

        String finalMessage = QuizMessages.finalMessage(session);

        // Step 3: Send the message
        sendMessageAsync(chatId, finalMessage, ParseMode.Markdown);

        // Step 4: Clean up (remove session)
        quizService.removeSession(chatId);
        log.info("✅ Quiz completed and cleaned up for chatId: {}", chatId);
    }

    private void handleHelpCommand(Long chatId) {
        var helpMessage = QuizMessages.helpMessage();

        sendMessageAsync(chatId, helpMessage, ParseMode.Markdown);
    }

    private void handleStartCommand(Long chatId, String userName) {
        log.info("🎯 User {} requested to start quiz", userName);
        if (quizService.hasActiveSession(chatId)) {
            sendMessageAsync(chatId,
                    "⚠️ *You already have an active quiz!* Type /stop to end it, or continue answering.",
                    ParseMode.Markdown);
            return;
        }
        // Get available topics
        List<String> topics = quizService.getAllTopics();
        if (topics.isEmpty()) {
            sendMessageAsync(chatId,
                    "❌ *No topics available at the moment*. Please try again later.", ParseMode.Markdown);
            return;
        }
        // Create inline keyboard with topic buttons
        InlineKeyboardMarkup keyboard = createTopicKeyboard(CommonUtils.sortingStringList(topics));
        var welcomeMessage = QuizMessages.welcomeMessage(userName);
        sendMessageWithKeyboard(chatId, welcomeMessage, keyboard);
    }

    private void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage request = new SendMessage(chatId, text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);

        telegramBot.execute(request, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage request, SendResponse response) {
                if (!response.isOk()) {
                    log.error("❌ Failed to send message: {} - {}",
                            response.errorCode(), response.description());
                }
            }

            @Override
            public void onFailure(SendMessage request, IOException e) {
                log.error("❌ Error sending message to {}: {}", chatId, e.getMessage(), e);
            }
        });
    }

    private InlineKeyboardMarkup createTopicKeyboard(List<String> topics) {
        List<InlineKeyboardButton[]> rows = new ArrayList<>();

        // Create a button for each topic
        for (String topic : topics) {
            InlineKeyboardButton button = new InlineKeyboardButton(topic)
                    .callbackData("topic:" + topic);
            rows.add(new InlineKeyboardButton[]{button});
        }
        return new InlineKeyboardMarkup(rows.toArray(new InlineKeyboardButton[0][]));
    }

    private void handleStatusCommand(long chatId) {
        var status = QuizMessages.statusMessage(System.getProperty("java.version"),
                getUpTime(), (int) grpcClientService.getAllTopicsNumber());

        sendMessageAsync(chatId, status, ParseMode.Markdown);
    }

    private void handleUnknownCommand(long chatId, String text) {
        var response = QuizMessages.unknownCommandMessage(text);
        sendMessageAsync(chatId, response, ParseMode.Markdown);

    }

    private void sendMessageAsync(long chatId, String text, ParseMode parseMode) {

        SendMessage request = new SendMessage(chatId, text);
        if (parseMode != null) {
            request.parseMode(parseMode);
        }

        telegramBot.execute(request, new Callback<SendMessage, SendResponse>() {

            @Override
            public void onResponse(SendMessage request, SendResponse response) {
                if (!response.isOk()) {
                    log.error("❌ Failed to send message: {} - {}", response.errorCode(), response.description());
                }
            }

            @Override
            public void onFailure(SendMessage request, IOException e) {
                log.error("❌ Error sending message to {}: {}", chatId, e.getMessage(), e);
            }
        });
    }

    private static String getUpTime() {
        long uptime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        long sec = uptime / 1000;
        long min = sec / 60;
        long hours = min / 60;
        return String.format("%02d:%02d:%02d", hours, min % 60, sec % 60);
    }
}