package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.DTO.RuleStatsDTO;
import ru.skypro.recommendationsystem.DTO.RuleStatsResponse;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.service.DynamicRuleService;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST-контроллер для получения агрегированной статистики по динамическим правилам.
 * <p>
 * Предоставляет эндпоинт {@code GET /rule/stats}, который формирует сводный отчет,
 * объединяющий метаданные правил (из {@link DynamicRuleService}) и их количественную
 * статистику (из {@link RuleStatsService}).
 * </p>
 *
 * <h3>Бизнес-ценность:</h3>
 * <ul>
 *     <li><b>Мониторинг эффективности:</b> Позволяет увидеть, сколько раз каждое правило
 *         было применено к пользователям (поле {@code count}).</li>
 *
 *     <li><b>Выявление аномалий:</b> Помогает найти правила, которые никогда не срабатывали
 *         (count = 0), чтобы удалить их или скорректировать условия.</li>
 *
 *     <li><b>Поддержка принятия решений:</b> Данные используются для дашбордов аналитиков
 *         и менеджеров продукта для оценки качества рекомендательной системы.</li>
 * </ul>
 *
 * <h3>Алгоритм работы эндпоинта:</h3>
 * Метод выполняет <b>in-memory JOIN</b> двух наборов данных:
 * <ol>
 *     <li>Загружает все активные правила через {@link DynamicRuleService#getAllRules()}.</li>
 *     <li>Загружает всю накопленную статистику через {@link RuleStatsService#getAllStats()}.</li>
 *     <li>Строит хэш-таблицу ({@code Map<UUID, RuleStats>}) для быстрого поиска статистики по ID правила.</li>
 *     <li>Проходит по списку правил, подтягивает соответствующую статистику (или 0, если её нет)
 *         и формирует итоговый список для ответа.</li>
 * </ol>
 *
 * <p>
 * Такой подход выбран для гибкости: сервисы могут хранить данные в разных таблицах или даже
 * разных источниках, а контроллер собирает единую картину для клиента.
 * </p>
 *
 * @see DynamicRule
 * @see RuleStats
 * @see DynamicRuleService
 * @see RuleStatsService
 */
@RestController
@RequestMapping("/rule")
public class RuleStatsController {

    private final RuleStatsService ruleStatsService;
    private final DynamicRuleService dynamicRuleService;

    /**
     * Конструктор для внедрения зависимостей сервисных слоев.
     *
     * @param ruleStatsService   сервис, предоставляющий количественную статистику по правилам
     * @param dynamicRuleService сервис, предоставляющий список самих правил и их метаданные
     */
    public RuleStatsController(RuleStatsService ruleStatsService, DynamicRuleService dynamicRuleService) {
        this.ruleStatsService = ruleStatsService;
        this.dynamicRuleService = dynamicRuleService;
    }

    /**
     * Получает сводную статистику по всем динамическим правилам.
     * <p>
     * Возвращает JSON-объект со структурой:
     * {@code
     * {
     * "stats": [
     * {
     * "rule_id": "550e8400-e29b-41d4-a716-446655440000",
     * "count": 150
     * },
     * ...
     * ]
     * }
     * }
     * </p>
     *
     * <h4>Логика формирования данных:</h4>
     * <ul>
     *     <li>Если для правила существует запись в статистике, возвращается актуальное значение {@code count}.</li>
     *     <li>Если статистика отсутствует (правило новое или еще не применялось), возвращается {@code 0}.</li>
     *     <li>Порядок элементов в списке соответствует порядку правил, возвращаемому сервисом правил.</li>
     * </ul>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>200 OK</dt>
     *         <dd>Статистика успешно сформирована. Даже если правил или статистики нет,
     *             возвращается валидный JSON с пустым массивом.</dd>
     *     <dt>500 Internal Server Error</dt>
     *         <dd>В случае сбоя при получении данных из сервисов. Обработка исключений
     *             должна быть реализована в глобальном обработчике ошибок (@ControllerAdvice).</dd>
     * </dl>
     *
     * @return {@link ResponseEntity} с картой, содержащей ключ "stats" и список объектов статистики
     */
    @GetMapping("/stats")
    public ResponseEntity<RuleStatsResponse> getStats() {
        List<DynamicRule> allRules = dynamicRuleService.getAllRules();
        Map<UUID, RuleStats> statsMap = ruleStatsService.getAllStats().stream()
                .collect(Collectors.toMap(s -> s.getDynamicRule().getId(), s -> s));

        List<RuleStatsDTO> statsList = allRules.stream()
                .map(rule -> {
                    RuleStats stats = statsMap.get(rule.getId());
                    return new RuleStatsDTO(rule.getId().toString(), stats != null ? stats.getCount() : 0);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new RuleStatsResponse(statsList));
    }
}
