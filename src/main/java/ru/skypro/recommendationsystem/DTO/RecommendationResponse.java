package ru.skypro.recommendationsystem.DTO;

import java.util.List;
import java.util.UUID;

/**
 * Корневой DTO-объект ответа для эндпоинта получения рекомендаций.
 * <p>
 * Инкапсулирует результат работы рекомендательной системы для конкретного пользователя.
 * Возвращается контроллером {@link ru.skypro.recommendationsystem.controller.RecommendationController}
 * в ответ на запрос {@code GET /recommendation/{userId}}.
 * </p>
 *
 * <h3>Структура данных:</h3>
 * Объект содержит два основных элемента:
 * <ul>
 *     <li>{@code userId} — идентификатор пользователя, для которого были сформированы рекомендации.
 *         <strong>Важно:</strong> Наличие этого поля в ответе позволяет фронтенду и системам аналитики
 *         однозначно сопоставить полученный список рекомендаций с конкретным пользователем,
 *         даже если список пуст или содержит ошибки.</li>
 *
 *     <li>{@code recommendations} — список объектов {@link RecommendationDTO}, представляющих
 *         конкретные предложения (продукты, акции, услуги), релевантные пользователю.</li>
 * </ul>
 *
 * <h3>Бизнес-смысл и сценарии использования:</h3>
 * <dl>
 *     <dt>Телеметрия и логирование</dt>
 *         <dd>Поле {@code userId} используется для логирования запросов и построения сквозной трассировки (trace ID),
 *             связывая запрос API с внутренними логами сервиса и событиями в Kafka/ClickHouse.</dd>
 *
 *     <dt>Обработка пустого списка</dt>
 *         <dd>Если подходящих рекомендаций не найдено, поле {@code recommendations} будет содержать
 *             пустой список ({@code []}), а не {@code null}. Это упрощает обработку на фронтенде
 *             (не нужно проверять на null) и соответствует лучшим практикам REST API.</dd>
 *
 *     <dt>Валидация целостности данных</dt>
 *         <dd>Клиентское приложение может проверить, что полученный список рекомендаций действительно
 *             принадлежит запрошенному пользователю (сравнение входящего path-параметра и поля {@code userId}).</dd>
 * </dl>
 *
 * @see RecommendationDTO
 * @see ru.skypro.recommendationsystem.controller.RecommendationController
 */
public class RecommendationResponse {

    /**
     * Уникальный идентификатор пользователя, запросившего рекомендации.
     * <p>
     * Дублирует параметр запроса {@code {userId}} из URL для обеспечения целостности данных
     * и удобства логирования на стороне клиента и сервера.
     * </p>
     */
    private UUID userId;

    /**
     * Список персонализированных рекомендаций для пользователя.
     * <p>
     * Содержит объекты {@link RecommendationDTO}. Гарантированно не равен {@code null},
     * но может быть пустым, если система не нашла релевантных предложений.
     * </p>
     */
    private List<RecommendationDTO> recommendations;

    /**
     * Конструктор для инициализации объекта с данными.
     *
     * @param userId идентификатор пользователя
     * @param recommendations список рекомендаций
     */
    public RecommendationResponse(UUID userId, List<RecommendationDTO> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<RecommendationDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationDTO> recommendations) {
        this.recommendations = recommendations;
    }
}
