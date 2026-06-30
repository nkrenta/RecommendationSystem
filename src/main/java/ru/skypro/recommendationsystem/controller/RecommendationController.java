package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;
import ru.skypro.recommendationsystem.service.RecommendationService;

import java.util.UUID;

/**
 * REST-контроллер для получения персонализированных рекомендаций.
 * <p>
 * Предоставляет единственный эндпоинт для формирования списка рекомендаций
 * для конкретного пользователя на основе динамических правил и истории транзакций.
 * </p>
 *
 * <h3>Бизнес-логика:</h3>
 * При запросе система выполняет следующие шаги (детали скрыты в {@link RecommendationService}):
 * <ol>
 *     <li>Загружает профиль и историю транзакций пользователя (из H2 БД через {@link org.springframework.jdbc.core.JdbcTemplate}).</li>
 *     <li>Применяет набор динамических правил ({@link ru.skypro.recommendationsystem.entity.DynamicRule}),
 *         хранящихся в PostgreSQL, с учетом кэшированных данных.</li>
 *     <li>Формирует итоговый список релевантных продуктов и возвращает его в формате DTO.</li>
 * </ol>
 *
 * <h3>Формат запроса:</h3>
 * {@code GET /recommendation/{userId}}
 * <p>
 * Где {@code userId} — уникальный идентификатор пользователя в формате UUID.
 * </p>
 *
 * @see RecommendationService
 * @see RecommendationResponse
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Конструктор для внедрения зависимости сервисного слоя.
     *
     * @param recommendationService сервис, отвечающий за расчет и формирование рекомендаций
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Получает персонализированные рекомендации для указанного пользователя.
     * <p>
     * Принимает UUID пользователя как часть URL-пути. Делегирует всю сложную логику
     * расчета (применение правил, агрегацию данных) сервису {@link RecommendationService}.
     * Контроллер отвечает только за преобразование результата в HTTP-ответ.
     * </p>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>200 OK</dt>
     *         <dd>Рекомендации успешно сформированы. В теле ответа объект {@link RecommendationResponse},
     *             который может содержать пустой список, если подходящих предложений нет.</dd>
     *     <dt>400 Bad Request</dt>
     *         <dd>(Теоретически) Если переданный UUID имеет неверный формат.
     *             В текущей реализации Spring автоматически вернет 400, если строка не является валидным UUID.</dd>
     *     <dt>500 Internal Server Error</dt>
     *         <dd>В случае сбоя бизнес-логики (ошибки БД, правил и т.д.). Обработка исключений
     *             должна быть реализована в глобальном обработчике ошибок (@ControllerAdvice).</dd>
     * </dl>
     *
     * @param userId уникальный идентификатор пользователя ({@link UUID}), для которого запрашиваются рекомендации
     * @return {@link ResponseEntity} с объектом {@link RecommendationResponse}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable UUID userId) {
        RecommendationResponse response = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(response);
    }
}
// Данные для проверки:
//cd515076-5d8a-44be-930e-8d4fcb79f42d  - invest 500
//d4a4d619-9a0c-4fc5-b0cb-76c49409546b - Top Saving
//1f9b149c-6577-448a-bc94-16bea229b71a - простой кредит
