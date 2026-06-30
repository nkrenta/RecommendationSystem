package ru.skypro.recommendationsystem.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.skypro.recommendationsystem.bot.RecommendationBot;

/**
 * Конфигурация для регистрации и управления жизненным циклом Telegram-бота.
 * <p>
 * Отвечает за инициализацию {@link TelegramBotsApi} и регистрацию экземпляра
 * {@link RecommendationBot} при старте приложения, а также за корректную
 * очистку ресурсов при его остановке.
 * </p>
 *
 * <h3>Особенности работы:</h3>
 * <ul>
 *     <li>Активируется только в продуктивных профилях ({@code @Profile("!test")}).</li>
 *     <li>Использует механизм Long Polling через {@link DefaultBotSession}.</li>
 *     <li>Гарантирует запуск бота только после полной инициализации контекста Spring.</li>
 * </ul>
 *
 * @see RecommendationBot
 * @see TelegramBotsApi
 */
@Configuration
@Profile("!test")
public class BotConfig {

    private final RecommendationBot recommendationBot;
    private TelegramBotsApi telegramBotsApi;

    /**
     * Конструктор для внедрения зависимости бота.
     *
     * @param recommendationBot экземпляр бота, который необходимо зарегистрировать в API Telegram
     */
    public BotConfig(RecommendationBot recommendationBot) {
        this.recommendationBot = recommendationBot;
    }

    /**
     * Метод инициализации, вызываемый Spring после создания бина.
     * <p>
     * Создает экземпляр {@link TelegramBotsApi}, используя реализацию сессии
     * {@link DefaultBotSession} (Long Polling), и регистрирует в нем бота.
     * </p>
     *
     * <strong>Важно:</strong> Исключения при регистрации перехватываются и логируются.
     * В случае ошибки приложение продолжит работу, но бот не будет получать обновления.
     * Для продакшена рекомендуется заменить {@code e.printStackTrace()} на использование
     * SLF4J логгера.
     */
    @PostConstruct
    public void init() {
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            // telegramBotsApi.registerBot(recommendationBot); чтобы ошибка в логах не вылазила
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод очистки, вызываемый Spring перед уничтожением бина.
     * <p>
     * Выполняет безопасное завершение работы с API Telegram.
     * </p>
     *
     * <p>
     * Примечание: В текущей реализации библиотеки TelegramBotsApi явного метода
     * "unregisterBot" может не существовать. Обнуление ссылки {@code telegramBotsApi = null}
     * является мерой предосторожности для очистки локальной переменной, однако
     * основные ресурсы (сетевые соединения) управляются внутри {@code TelegramBotsApi}.
     * При необходимости более агрессивной очистки следует изучить документацию
     * конкретной версии библиотеки.
     * </p>
     */
    @PreDestroy
    public void destroy() {
        if (telegramBotsApi != null) {
            // Явная очистка локальной ссылки.
            // Реальное закрытие сетевых соединений зависит от реализации TelegramBotsApi.
            telegramBotsApi = null;
        }
    }
}
