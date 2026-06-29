package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST-контроллер для управления динамическими правилами рекомендаций.
 * <p>
 * Предоставляет CRUD-интерфейс для работы с сущностью {@link DynamicRule},
 * которая определяет логику формирования персональных предложений для клиентов.
 * </p>
 *
 * <h3>Поддерживаемые операции:</h3>
 * <ul>
 *     <li><b>POST /rule</b> — создание нового правила.</li>
 *     <li><b>GET /rule</b> — получение списка всех активных правил.</li>
 *     <li><b>DELETE /rule/{id}</b> — удаление правила по уникальному идентификатору.</li>
 * </ul>
 *
 * <h3>Правила валидации при создании:</h3>
 * Для успешного создания правила объект {@code DynamicRule} должен содержать следующие обязательные поля:
 * <ul>
 *     <li>{@code productName} — название продукта.</li>
 *     <li>{@code productId} — внутренний идентификатор продукта.</li>
 *     <li>{@code productText} — текст рекомендации, отображаемый пользователю.</li>
 * </ul>
 * В случае отсутствия любого из этих полей сервер вернет ответ со статусом {@code 400 Bad Request}.
 * </p>
 *
 * @see DynamicRule
 * @see DynamicRuleService
 */
@RestController
@RequestMapping("/rule")
public class DynamicRuleController {
    private final DynamicRuleService dynamicRuleService;

    /**
     * Конструктор для внедрения зависимости сервиса бизнес-логики.
     *
     * @param dynamicRuleService сервис, отвечающий за хранение и обработку правил
     */
    public DynamicRuleController(DynamicRuleService dynamicRuleService) {
        this.dynamicRuleService = dynamicRuleService;
    }

    /**
     * Создает новое динамическое правило.
     * <p>
     * Принимает JSON-объект правила в теле запроса. Выполняет базовую валидацию
     * обязательных полей перед передачей в сервисный слой.
     * </p>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>201 Created</dt>
     *         <dd>Правило успешно создано. В теле ответа возвращается объект созданного правила.</dd>
     *     <dt>400 Bad Request</dt>
     *         <dd>Объект правила невалиден: отсутствуют обязательные поля ({@code productName}, {@code productId}, {@code productText}) или сам объект null.</dd>
     * </dl>
     *
     * @param rule объект правила, переданный в формате JSON в теле запроса
     * @return {@link ResponseEntity} с созданным правилом или ошибкой валидации
     */
    @PostMapping
    public ResponseEntity<DynamicRule> createRule(@RequestBody DynamicRule rule) {
        if (rule == null || rule.getProductName() == null || rule.getProductId() == null || rule.getProductText() == null) {
            return ResponseEntity.badRequest().build();
        }
        DynamicRule createdRule = dynamicRuleService.createRule(rule);
        return ResponseEntity.ok(createdRule);
    }

    /**
     * Получает список всех динамических правил.
     * <p>
     * Возвращает данные в обертке {@code Map<String, List<DynamicRule>>} с ключом {@code "data"}.
     * Такая структура выбрана для единообразия ответов API и возможности расширения метаданными
     * (например, пагинацией или счетчиком total) в будущем без изменения контракта клиента.
     * </p>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>200 OK</dt>
     *         <dd>Список правил успешно получен. Даже если список пуст, возвращается пустой массив внутри объекта.</dd>
     * </dl>
     *
     * @return {@link ResponseEntity}, содержащий карту с ключом "data" и списком правил
     */
    @GetMapping
    public ResponseEntity<Map<String, List<DynamicRule>>> getAllRules() {
        List<DynamicRule> rules = dynamicRuleService.getAllRules();
        return ResponseEntity.ok(Map.of("data", rules));
    }

    /**
     * Удаляет динамическое правило по его уникальному идентификатору.
     * <p>
     * Идентификатор правила передается как часть URL-пути.
     * </p>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>204 No Content</dt>
     *         <dd>Правило найдено и успешно удалено. Тело ответа отсутствует.</dd>
     *     <dt>404 Not Found</dt>
     *         <dd>Правило с указанным идентификатором не найдено в базе данных.</dd>
     * </dl>
     *
     * @param id уникальный идентификатор ({@link UUID}) удаляемого правила
     * @return {@link ResponseEntity} со статусом 204 при успехе или 404 при отсутствии записи
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        boolean deleted = dynamicRuleService.deleteRule(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

