package ru.skypro.recommendationsystem.service;


import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Сервис-оркестратор для формирования финального списка рекомендаций пользователю.
 * <p>
 * Этот класс реализует паттерн <b>Composite</b> или <b>Chain of Responsibility (упрощенный)</b>.
 * Он агрегирует результаты от всех зарегистрированных в контексте Spring реализаций
 * {@link RecommendationRuleSet} (движков правил) и возвращает единый унифицированный ответ.
 * </p>
 *
 * <h3>Ключевые особенности архитектуры:</h3>
 * <dl>
 *     <dt>Полиморфизм источников данных</dt>
 *         <dd>Сервис не зависит от конкретной реализации правил. Он работает со списком интерфейсов.
 *             Это позволяет легко добавлять новые типы рекомендаций (например, подключить ML-модель
 *             как еще один бин {@code RecommendationRuleSet}), не меняя код сервиса.</dd>
 *
 *     <dt>Механизм внедрения зависимостей (List Injection)</dt>
 *         <dd>В конструктор передается {@code List<RecommendationRuleSet>}. Spring автоматически
 *             собирает все бины этого типа из контекста. Порядок выполнения зависит от порядка
 *             регистрации бинов (или может быть явно задан через {@code @Order}).</dd>
 *
 *     <dt>Стратегия объединения результатов</dt>
 *         <dd>Текущая реализация использует простое объединение (concatenation) списков через
 *             {@code flatMap}. Все рекомендации от всех движков попадают в один список.
 *             <strong>Важно:</strong> Ответственность за уникальность и сортировку лежит либо
 *             на отдельных реализациях {@code RecommendationRuleSet}, либо на этом сервисе
 *             (в будущих версиях).</dd>
 * </dl>
 *
 * @see RecommendationRuleSet
 * @see RecommendationDTO
 * @see RecommendationResponse
 */
@org.springframework.stereotype.Service
public class RecommendationService {

    /**
     * Список всех доступных движков генерации рекомендаций.
     * <p>
     * Spring автоматически внедряет сюда все бины, реализующие интерфейс {@code RecommendationRuleSet}.
     * </p>
     */
    private final List<RecommendationRuleSet> recommendationRules;

    /**
     * Конструктор с внедрением коллекции зависимостей.
     *
     * @param recommendationRules список всех реализаций {@link RecommendationRuleSet},
     *                            доступных в контексте приложения.
     */
    public RecommendationService(List<RecommendationRuleSet> recommendationRules) {
        this.recommendationRules = recommendationRules;
    }

    /**
     * Формирует полный список рекомендаций для пользователя, объединяя результаты
     * всех подключенных движков правил.
     * <p>
     * Алгоритм работы:
     * </p>
     * <ol>
     *     <li>Проходит по всем зарегистрированным {@code RecommendationRuleSet}.</li>
     *     <li>Вызывает метод {@code checkRecommendation(userId)} у каждого движка.</li>
     *     <li>Объединяет все полученные списки рекомендаций в один общий список.</li>
     *     <li>Создает и возвращает объект {@link RecommendationResponse}, содержащий
     *         идентификатор пользователя и итоговый список рекомендаций.</li>
     * </ol>
     *
     * <h4>Важные нюансы реализации:</h4>
     * <ul>
     *     <li><b>Отсутствие дедупликации:</b> Если два разных движка вернут одну и ту же рекомендацию
     *         (один и тот же {@code productId}), она появится в списке дважды.
     *         В будущем здесь можно добавить логику удаления дубликатов (например, по {@code productId}).</li>
     *
     *     <li><b>Порядок следования:</b> Рекомендации идут в порядке выполнения движков.
     *         Если важна приоритезация (например, динамические правила важнее ML-рекомендаций),
     *         нужно либо отсортировать финальный список, либо использовать аннотацию {@code @Order}
     *         на реализациях {@code RecommendationRuleSet}.</li>
     *
     *     <li><b>Обработка пустых результатов:</b> Если какой-то движок вернет пустой список,
     *         это не повлияет на работу сервиса (поток просто не добавит элементов).</li>
     *
     *     <li><b>Null-безопасность:</b> Предполагается, что ни один из внедренных бинов не равен null,
     *         а также что сами движки никогда не возвращают null вместо пустого списка
     *         (согласно контракту {@link RecommendationRuleSet}).</li>
     * </ul>
     *
     * @param userId идентификатор пользователя, для которого запрашиваются рекомендации
     * @return объект {@link RecommendationResponse} с идентификатором пользователя и списком рекомендаций
     */
    public RecommendationResponse getRecommendationsForUser(UUID userId) {
        List<RecommendationDTO> recommendations = recommendationRules.stream()
                .flatMap(rule -> rule.checkRecommendation(userId).stream())
                .collect(Collectors.toList());

        return new RecommendationResponse(userId, recommendations);
    }
}




