package ru.skypro.recommendationsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skypro.recommendationsystem.entity.RuleStats;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы со статистикой срабатывания правил ({@link RuleStats}).
 * <p>
 * Расширяет стандартный {@code JpaRepository}, предоставляя базовые операции CRUD
 * для сущности {@code RuleStats} с ключом {@code UUID}.
 * </p>
 *
 * <h3>Бизнес-ориентированный доступ к данным:</h3>
 * <p>
 * В отличие от стандартных методов JPA, которые оперируют внутренним идентификатором
 * записи ({@code id}), методы этого репозитория ориентированы на <b>бизнес-ключ</b> —
 * поле {@code ruleId}. Это соответствует тому, как статистика используется в бизнес-логике:
 * сервис всегда знает ID правила, но не обязательно знает ID записи статистики.
 * </p>
 *
 * <h4>Ключевые сценарии использования:</h4>
 * <dl>
 *     <dt>Поиск статистики</dt>
 *         <dd>{@link #findByRuleId(UUID)} используется при инкременте счетчика.
 *             Сервис сначала пытается найти запись. Если её нет (например, правило только что создано),
 *             создается новая запись со счетчиком 0, а затем увеличивается.</dd>
 *
 *     <dt>Очистка статистики</dt>
 *         <dd>{@link #deleteByRuleId(UUID)} применяется при удалении правила или сбросе статистики.
 *             Позволяет удалить данные статистики, не зная её внутреннего технического ID.</dd>
 * </dl>
 *
 * <h3>Важные архитектурные нюансы:</h3>
 * <ul>
 *     <li><b>Уникальность связи:</b> Поле {@code ruleId} в таблице {@code rule_stats} имеет
 *         ограничение {@code UNIQUE}. Это гарантирует, что для одного правила существует ровно
 *         одна запись статистики. Метод {@code findByRuleId} всегда вернет 0 или 1 результат.</li>
 *
 *     <li><b>Каскадное удаление (Recommendation):</b> Хотя этот репозиторий предоставляет явный метод
 *         удаления, в идеальной архитектуре удаление статистики должно происходить автоматически
 *         через внешний ключ в БД с опцией {@code ON DELETE CASCADE} при удалении самого правила
 *         ({@link ru.skypro.recommendationsystem.entity.DynamicRule}). Явный метод здесь нужен
 *         для сценариев ручного сброса статистики без удаления правила.</li>
 * </ul>
 *
 * @see RuleStats
 */
@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStats, UUID> {
    Optional<RuleStats> findByDynamicRuleId(UUID ruleId);

    void deleteByDynamicRuleId(UUID ruleId);

    @Modifying
    @Query("UPDATE RuleStats rs SET rs.count = rs.count + 1 WHERE rs.dynamicRule.id = :ruleId")
    int incrementCount(@Param("ruleId") UUID ruleId);
}
