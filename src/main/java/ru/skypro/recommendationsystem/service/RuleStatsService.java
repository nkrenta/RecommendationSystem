package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.repository.RuleStatsRepository;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления статистикой срабатывания динамических правил рекомендаций.
 * <p>
 * Отвечает за:
 * </p>
 * <ul>
 *     <li>Увеличение счетчика срабатываний правила при его успешном применении.</li>
 *     <li>Удаление статистики при удалении самого правила.</li>
 *     <li>Получение сводной статистики для аналитики и мониторинга.</li>
 * </ul>
 *
 * <h3>Критическое предупреждение о конкурентном доступе (Race Condition)</h3>
 * <p>
 * Текущая реализация метода {@link #incrementCount(UUID)} использует паттерн "Read-Modify-Save":
 * </p>
 * <ol>
 *     <li>Прочитать текущую запись из БД.</li>
 *     <li>Увеличить значение в памяти.</li>
 *     <li>Сохранить обновленную запись.</li>
 * </ol>
 * <p>
 * При высокой нагрузке (множество одновременных запросов) это может привести к потере инкрементов.
 * Пример сценария гонки данных:
 * </p>
 * <table>
 *     <thead><tr><th>Поток A</th><th>Поток B</th></tr></thead>
 *     <tbody>
 *         <tr><td>Читает count = 10</td><td></td></tr>
 *         <tr><td></td><td>Читает count = 10</td></tr>
 *         <tr><td>Вычисляет 10 + 1 = 11</td><td>Вычисляет 10 + 1 = 11</td></tr>
 *         <tr><td>Сохраняет 11</td><td>Сохраняет 11 (перезаписывает значение A)</td></tr>
 *     </tbody>
 * </table>
 * <p>
 * В результате счетчик станет 11 вместо ожидаемых 12.
 * </p>
 *
 * <h4>Рекомендуемое решение (Production Ready):</h4>
 * <p>
 * Для продакшена необходимо заменить логику на атомарную операцию на уровне БД:
 * <code>UPDATE rule_stats SET count = count + 1 WHERE rule_id = ?</code>.
 * Это можно реализовать через нативный запрос в репозитории или метод вида
 * {@code ruleStatsRepository.incrementCount(ruleId)}.
 * </p>
 *
 * @see RuleStats
 * @see RuleStatsRepository
 */
@Service
public class RuleStatsService {
    private final RuleStatsRepository ruleStatsRepository;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param ruleStatsRepository репозиторий для доступа к данным статистики правил.
     */
    public RuleStatsService(RuleStatsRepository ruleStatsRepository) {
        this.ruleStatsRepository = ruleStatsRepository;
    }

    /**
     * Увеличивает счетчик срабатываний для указанного правила.
     * <p>
     * Алгоритм работы текущей реализации:
     * </p>
     * <ol>
     *     <li>Пытается найти запись статистики по ID правила.</li>
     *     <li>Если запись не найдена, создает новую с начальным значением 0.</li>
     *     <li>Инкрементирует значение счетчика на 1.</li>
     *     <li>Сохраняет обновленную сущность в базу данных.</li>
     * </ol>
     *
     * <strong>ВНИМАНИЕ:</strong> Данная реализация <b>не является потокобезопасной</b> при высокой нагрузке.
     * См. описание класса для деталей проблемы Race Condition.
     * </p>
     *
     * @param ruleId идентификатор правила, для которого необходимо увеличить счетчик
     */
    @Transactional
    public void incrementCount(UUID ruleId) {
        RuleStats stats = ruleStatsRepository.findByRuleId(ruleId)
                .orElse(new RuleStats(ruleId, 0));
        stats.setCount(stats.getCount() + 1);
        ruleStatsRepository.save(stats);
    }

    /**
     * Удаляет статистику для правила с указанным идентификатором.
     * <p>
     * Вызывается перед удалением самого правила в {@link DynamicRuleService}, чтобы
     * гарантировать целостность данных и отсутствие "висячих" записей в таблице статистики.
     * Операция выполняется в рамках транзакции, поэтому либо удалится и статистика, и правило,
     * либо ничего не произойдет при ошибке.
     * </p>
     *
     * @param ruleId идентификатор правила, статистику которого нужно удалить
     */
    @Transactional
    public void deleteByRuleId(UUID ruleId) {
        ruleStatsRepository.deleteByRuleId(ruleId);
    }

    /**
     * Получает полную статистику по всем активным правилам.
     * <p>
     * Используется для административных панелей, дашбордов аналитики и мониторинга
     * эффективности правил рекомендаций.
     * </p>
     *
     * <h4>Производительность:</h4>
     * <p>
     * Метод загружает <b>все</b> записи из таблицы статистики. При росте объема данных
     * (миллионы строк) этот метод может стать узким местом.
     * В будущем рекомендуется добавить пагинацию или фильтрацию (например, топ-100 самых
     * эффективных правил).
     * </p>
     *
     * @return список всех записей статистики правил
     */
    public List<RuleStats> getAllStats() {
        return ruleStatsRepository.findAll();
    }
}
