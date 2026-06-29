package ru.skypro.recommendationsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.skypro.recommendationsystem.entity.DynamicRule;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link ru.skypro.recommendationsystem.entity.DynamicRule}.
 * <p>
 * Расширяет стандартный {@code JpaRepository}, предоставляя базовые операции CRUD
 * (сохранение, поиск по ID, удаление и т.д.) для сущности {@code DynamicRule} с ключом {@code UUID}.
 * </p>
 *
 * <h3>Ключевая особенность: Загрузка связанных данных</h3>
 * <p>
 * Основной кастомный метод {@link #findAllWithQueries()} решает проблему <b>N+1 запросов</b>
 * и предотвращает ошибку {@code LazyInitializationException}.
 * </p>
 *
 * <h4>Почему нужен JOIN FETCH?</h4>
 * <p>
 * В сущности {@code DynamicRule} коллекция {@code queries} помечена как
 * {@code @OneToMany(fetch = FetchType.LAZY)}. Это значит, что по умолчанию:
 * </p>
 * <ol>
 *     <li>Hibernate выполнит 1 запрос для получения списка правил.</li>
 *     <li>При обращении к {@code rule.getQueries()} в цикле — еще N запросов (по одному на каждое правило).</li>
 * </ol>
 * <p>
 * Метод {@code findAllWithQueries()} использует конструкцию {@code JOIN FETCH}, которая заставляет
 * базу данных вернуть все данные в одном запросе, объединяя таблицы {@code dynamic_rules} и {@code rule_queries}.
 * </p>
 *
 * <h4>Важное предупреждение о производительности:</h4>
 * <dl>
 *     <dt>Риск декартова произведения</dt>
 *         <dd>Если у одного правила будет очень много условий ({@code RuleQuery}), результирующий набор
 *             строк может сильно раздуться (каждая строка правила дублируется для каждого условия).
 *             Для больших объемов данных этот метод следует использовать с осторожностью или ограничивать
 *             выборку (например, через {@code LIMIT} или пагинацию).</dd>
 *
 *     <dt>Отсутствие фильтрации</dt>
 *         <dd>Этот метод возвращает <b>все</b> активные и неактивные правила со всеми условиями.
 *             В продакшене часто требуется фильтровать по статусу (active/inactive) или версии правила.</dd>
 * </dl>
 *
 * @see ru.skypro.recommendationsystem.entity.DynamicRule
 * @see ru.skypro.recommendationsystem.service.DynamicRuleService
 */
@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {

    /**
     * Возвращает список всех динамических правил, принудительно загружая связанные условия ({@code queries}).
     * <p>
     * Использует JPQL-конструкцию {@code LEFT JOIN FETCH} для выполнения загрузки в один запрос к БД.
     * Это необходимо, так как связь {@code DynamicRule.queries} настроена как {@code LAZY}.
     * </p>
     *
     * <strong>Важно:</strong> Не используйте этот метод для выборки огромных объемов данных без пагинации.
     * При большом количестве дочерних записей ({@code RuleQuery}) объем передаваемых данных может стать
     * критическим из-за дублирования полей родительского объекта в каждой строке результата.
     * </strong>
     *
     * @return список правил со всеми связанными условиями
     */
    @Query("SELECT d FROM DynamicRule d LEFT JOIN FETCH d.queries")
    List<DynamicRule> findAllWithQueries();
}