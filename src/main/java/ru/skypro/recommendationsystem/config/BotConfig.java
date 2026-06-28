package ru.skypro.recommendationsystem.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.skypro.recommendationsystem.bot.RecommendationBot;

@Configuration
@Profile("!test")
public class BotConfig {

    private final RecommendationBot recommendationBot;
    private TelegramBotsApi telegramBotsApi;

    public BotConfig(RecommendationBot recommendationBot) {
        this.recommendationBot = recommendationBot;
    }

    @PostConstruct
    public void init() {
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(recommendationBot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        if (telegramBotsApi != null) {
            telegramBotsApi = null;
        }
    }
}
