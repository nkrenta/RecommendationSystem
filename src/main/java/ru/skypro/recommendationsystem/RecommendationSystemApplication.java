package ru.skypro.recommendationsystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение Star Bank Recommendation System.
 * <p>
 * Инициализирует контекст Spring Boot, регистрирует все бины (контроллеры, сервисы, репозитории)
 * и запускает встроенный сервер Tomcat.
 * </p>
 *
 * @author SkyPro Team
 * @see SpringApplication
 */
@SpringBootApplication
@OpenAPIDefinition(info = @io.swagger.v3.oas.annotations.info.Info(title = "Star Bank Recommendation API"
        , version = "1.0.0",
        description = "API for Star Bank Recommendation for Credit Products"))
public class RecommendationSystemApplication {

    /**
     * Основной метод запуска приложения.
     *
     * @param args аргументы командной строки (обычно не используются в Spring Boot)
     */
    public static void main(String[] args) {
        SpringApplication.run(RecommendationSystemApplication.class, args);
    }
}
