package ru.skypro.recommendationsystem.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.entity.User;
import ru.skypro.recommendationsystem.repository.UserRepository;
import ru.skypro.recommendationsystem.service.RecommendationService;

import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
public class RecommendationBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(RecommendationBot.class);

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @Value("${bot.name:RecommendationBot}")
    private String botUsername;

    public RecommendationBot(
            @Value("${bot.token}") String botToken,
            RecommendationService recommendationService,
            UserRepository userRepository) {
        super(botToken);
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();

        if (messageText.equals("/start")) {
            sendHelpMessage(chatId);
        } else if (messageText.startsWith("/recommend")) {
            handleRecommendCommand(chatId, messageText);
        } else {
            sendHelpMessage(chatId);
        }
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = "Добро пожаловать! Я бот рекомендаций.\n\n" +
                "Доступные команды:\n" +
                "/recommend <username> - получить рекомендации для пользователя\n\n" +
                "Пример: /recommend ivanov";
        sendMessage(chatId, helpText);
    }

    private void handleRecommendCommand(Long chatId, String messageText) {
        String[] parts = messageText.split("\\s+", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            sendMessage(chatId, "Использование: /recommend <username>");
            return;
        }

        String username = parts[1].trim();
        List<User> users = userRepository.findByUsername(username);

        if (users.isEmpty() || users.size() > 1) {
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        User user = users.get(0);
        UUID userId = user.getId();

        List<RecommendationDTO> recommendations = recommendationService
                .getRecommendationsForUser(userId)
                .getRecommendations();

        if (recommendations.isEmpty()) {
            sendMessage(chatId, "Здравствуйте " + user.getFirstName() + " " + user.getLastName() + "!\n\n" +
                    "Новых рекомендаций для вас нет.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Здравствуйте ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("!\n\n");
        sb.append("Новые продукты для вас:\n\n");

        for (int i = 0; i < recommendations.size(); i++) {
            RecommendationDTO dto = recommendations.get(i);
            sb.append(i + 1).append(". ").append(dto.getName()).append("\n");
            sb.append("   ").append(dto.getText()).append("\n\n");
        }

        sendMessage(chatId, sb.toString());
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableHtml(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }
}
