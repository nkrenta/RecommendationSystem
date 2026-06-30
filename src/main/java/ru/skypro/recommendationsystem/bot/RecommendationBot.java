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

/**
 * Telegram-бот для предоставления персонализированных рекомендаций клиентам банка.
 * <p>
 * Реализует базовый интерфейс взаимодействия с Telegram API через механизм Long Polling.
 * Обрабатывает пользовательские команды и формирует ответы на основе данных из базы.
 * </p>
 *
 * <h3>Поддерживаемые команды:</h3>
 * <ul>
 *     <li><code>/start</code> — отображает справочную информацию о доступных командах.</li>
 *     <li><code>/recommend &lt;username&gt;</code> — запрашивает список рекомендаций для указанного пользователя.</li>
 * </ul>
 *
 * <p>
 * Компонент активируется только в продуктивных профилях приложения ({@code @Profile("!test")}),
 * исключая его запуск в тестовых окружениях для предотвращения спама и утечки тестовых данных.
 * </p>
 *
 * @see TelegramLongPollingBot
 * @see RecommendationService
 * @see UserRepository
 */
@Component
@Profile("!test")
public class RecommendationBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(RecommendationBot.class);

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @Value("${bot.name:RecommendationBot}")
    private String botUsername;

    /**
     * Создает экземпляр бота с внедрением зависимостей.
     * <p>
     * Инициализирует родительский класс {@link TelegramLongPollingBot} токеном доступа,
     * а также сохраняет ссылки на сервисы для бизнес-логики и работы с данными.
     * </p>
     *
     * @param botToken              токен авторизации бота в Telegram (внедряется из конфигурации)
     * @param recommendationService сервис для получения списка рекомендаций
     * @param userRepository        репозиторий для поиска пользователей по username
     */
    public RecommendationBot(
            @Value("${bot.token}") String botToken,
            RecommendationService recommendationService,
            UserRepository userRepository) {
        super(botToken);
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    /**
     * Возвращает имя пользователя (username) бота в Telegram.
     * <p>
     * Значение берется из конфигурации приложения (свойство {@code bot.name}),
     * с резервным значением по умолчанию "RecommendationBot".
     * </p>
     *
     * @return имя бота в Telegram
     */

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Обработчик входящих обновлений (сообщений) от Telegram API.
     * <p>
     * Маршрутизирует входящие текстовые сообщения:
     * <ul>
     *     <li>Команда <code>/start</code> вызывает отправку справки.</li>
     *     <li>Команда вида <code>/recommend ...</code> инициирует поиск рекомендаций.</li>
     *     <li>Любые другие сообщения обрабатываются как запрос справки.</li>
     * </ul>
     * Сообщения без текста игнорируются.
     * </p>
     *
     * @param update объект обновления, полученный от сервера Telegram
     */
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

    /**
     * Отправляет пользователю сообщение со справочной информацией о командах бота.
     *
     * @param chatId идентификатор чата, куда нужно отправить сообщение
     */
    private void sendHelpMessage(Long chatId) {
        String helpText = "Добро пожаловать! Я бот рекомендаций.\n\n" +
                "Доступные команды:\n" +
                "/recommend <username> - получить рекомендации для пользователя\n\n" +
                "Пример: /recommend ivanov";
        sendMessage(chatId, helpText);
    }

    /**
     * Обрабатывает команду <code>/recommend</code>, извлекая username и запрашивая рекомендации.
     * <p>
     * Выполняет валидацию аргумента, поиск пользователя в БД и вызов бизнес-логики.
     * Формирует и отправляет итоговое сообщение со списком продуктов.
     * </p>
     *
     * @param chatId      идентификатор чата
     * @param messageText полный текст сообщения пользователя
     */
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

    /**
     * Отправляет текстовое сообщение в указанный чат через Telegram API.
     * <p>
     * Подготавливает объект {@link SendMessage}, включает поддержку HTML-разметки
     * и выполняет запрос к API.
     * </p>
     * <strong>Важно:</strong> Исключения {@link TelegramApiException} перехватываются и логируются,
     * но не пробрасываются выше, чтобы не прерывать обработку потока обновлений.
     *
     * @param chatId идентификатор чата (Long)
     * @param text   текст сообщения
     */
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
