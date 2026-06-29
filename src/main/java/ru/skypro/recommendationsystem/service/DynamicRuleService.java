package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления жизненным циклом динамических правил рекомендаций ({@link DynamicRule}).
 * <p>
 * Предоставляет CRUD-операции высокого уровня, гарантирующие целостность данных:
 * </p>
 * <ul>
 *     <li>При создании правила автоматически устанавливает связь с его условиями ({@code RuleQuery}).</li>
 *     <li>При удалении правила сначала очищает связанную статистику ({@code RuleStats}),
 *         чтобы избежать рассинхронизации метрик.</li>
 * </ul>
 *
 * <h3>Ключевые особенности реализации:</h3>
 * <dl>
 *     <dt>Ручное управление ID</dt>
 *         <dd>В методе {@link #createRule(DynamicRule)} ID генерируется вручную через
 *             {@code UUID.randomUUID()}. Это сделано намеренно, чтобы гарантировать, что
 *             ID будет установлен <b>до</b> сохранения сущности. Это критически важно для
 *             корректной установки обратной связи {@code RuleQuery.dynamicRule}.</dd>
 *
 *     <dt>Каскадные операции через сервис</dt>
 *         <dd>Хотя JPA поддерживает каскадное удаление ({@code CascadeType.REMOVE}), здесь
 *             используется явный вызов {@link RuleStatsService#deleteByRuleId(UUID)} перед
 *             удалением правила. Это дает возможность логировать факт сброса статистики,
 *             выполнять дополнительную очистку или реализовать мягкое удаление (soft delete)
 *             в будущем.</dd>
 * </dl>
 *
 * @see DynamicRule
 * @see RuleStatsService
 */
@Service
public class DynamicRuleService {
    private final DynamicRuleRepository repository;
    private final RuleStatsService ruleStatsService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param repository       репозиторий для работы с сущностью {@code DynamicRule}.
     * @param ruleStatsService сервис для управления статистикой правил.
     */
    public DynamicRuleService(DynamicRuleRepository repository, RuleStatsService ruleStatsService) {
        this.repository = repository;
        this.ruleStatsService = ruleStatsService;
    }

    /**
     * Создает новое динамическое правило.
     * <p>
     * Этот метод выполняет следующие шаги:
     * </p>
     * <ol>
     *     <li>Генерирует новый {@code UUID} для правила.</li>
     *     <li>Если у правила есть условия ({@code queries}), устанавливает ссылку
     *         {@code dynamicRule} в каждом из них, указывая на создаваемое правило.</li>
     *     <li>Сохраняет правило в базу данных. Благодаря каскадному сохранению
     *         ({@code CascadeType.ALL} на стороне сущности), условия также будут сохранены.</li>
     * </ol>
     *
     * <strong>Важно:</strong> Ожидается, что в сущности {@code DynamicRule} поле
     * {@code queries} аннотировано как {@code @OneToMany(cascade = CascadeType.ALL, ...)},
     * иначе условия не будут сохранены автоматически.
     * </p>
     *
     * @param rule объект правила, который необходимо создать. Поле {@code id} может быть null.
     * @return сохраненный объект {@code DynamicRule} с установленным ID и всеми связанными данными
     * @throws IllegalArgumentException если передан {@code null}
     */
    @Transactional
    public DynamicRule createRule(DynamicRule rule) {
        // Генерируем ID вручную ДО сохранения, чтобы иметь возможность установить связь
        rule.setId(UUID.randomUUID());
        if (rule.getQueries() != null) {
            rule.getQueries().forEach(q -> q.setDynamicRule(rule));
        }
        return repository.save(rule);
    }

    /**
     * Получает список всех активных правил вместе с их условиями.
     * <p>
     * Использует специализированный метод репозитория {@link DynamicRuleRepository#findAllWithQueries()},
     * который выполняет запрос с {@code JOIN FETCH}. Это предотвращает проблему N+1 запросов
     * и гарантирует, что коллекция {@code queries} будет инициализирована сразу.
     * </p>
     *
     * <h4>Производительность:</h4>
     * <p>
     * Загрузка всех правил сразу может быть затратной при большом количестве записей.
     * Для продакшена рекомендуется добавить пагинацию или фильтрацию (например, только активные правила).
     * </p>
     *
     * @return список всех правил со всеми связанными условиями
     */
    public List<DynamicRule> getAllRules() {
        return repository.findAllWithQueries();
    }

    /**
     * Удаляет правило по его идентификатору.
     * <p>
     * Операция выполняется в строгой последовательности для обеспечения целостности данных:
     * </p>
     * <ol>
     *     <li>Проверяет существование правила. Если его нет — возвращает {@code false}.</li>
     *     <li><b>Очищает статистику:</b> Вызывает {@link RuleStatsService#deleteByRuleId(UUID)},
     *         удаляя счетчики срабатываний для этого правила. Это предотвращает ситуацию, когда
     *         правило удалено, а статистика от него осталась висеть в базе.</li>
     *     <li><b>Удаляет правило:</b> Вызывает {@code repository.deleteById(id)}.</li>
     * </ol>
     *
     * <p>
     * Весь процесс обернут в транзакцию ({@code @Transactional}), поэтому если на каком-то этапе
     * произойдет ошибка, все изменения будут отменены (rollback).
     * </p>
     *
     * @param id идентификатор правила, подлежащего удалению
     * @return {@code true}, если правило было найдено и успешно удалено; {@code false}, если правило не найдено
     */
    @Transactional
    public boolean deleteRule(UUID id) {
        if (!repository.existsById(id)) {
            return false;
        }
        // Сначала удаляем статистику, чтобы не потерять целостность данных
        ruleStatsService.deleteByRuleId(id);
        repository.deleteById(id);
        return true;
    }
}

