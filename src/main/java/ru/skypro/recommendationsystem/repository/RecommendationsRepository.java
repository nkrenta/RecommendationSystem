package ru.skypro.recommendationsystem.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий для получения агрегированных данных о транзакциях пользователя.
 * <p>
 * В отличие от стандартных JPA-репозиториев, этот класс использует {@link JdbcTemplate}
 * для выполнения оптимизированных SQL-запросов с агрегацией (SUM, COUNT).
 * </p>
 *
 * <h3>Ключевые особенности реализации:</h3>
 * <dl>
 *     <dt>Гибридный доступ к данным</dt>
 *         <dd>Используется прямой SQL через {@code JdbcTemplate}, так как запросы содержат
 *             JOIN с таблицей продуктов и специфические агрегации. Это часто работает быстрее
 *             и проще в поддержке, чем сложные JPQL-запросы с проекциями.</dd>
 *
 *     <dt>Кэширование результатов (Caching)</dt>
 *         <dd>Каждый метод оборачивает запрос в логику кэширования через {@link CacheManager}.
 *             Это критически важно для рекомендательной системы, где одни и те же метрики
 *             (сумма депозитов, наличие продукта) запрашиваются многократно при расчете
 *             разных правил для одного пользователя.</dd>
 *
 *     <dt>Стратегия ключей кэша</dt>
 *         <dd>Ключи формируются динамически на основе {@code userId} и {@code productType}.
 *             Префиксы (например, "deposit:", "withdraw:") позволяют изолировать разные типы
 *             метрик и упрощают мониторинг или ручную инвалидацию кэша при необходимости.</dd>
 * </dl>
 *
 * <h3>Зависимости:</h3>
 * <ul>
 *     <li>{@link JdbcTemplate}: Настроен специально для этого репозитория через квалификатор
 *         {@code "recommendationsJdbcTemplate"}. Это позволяет использовать отдельный пул соединений
 *         или настройки таймаутов для тяжелых аналитических запросов.</li>
 *     <li>{@link CacheManager}: Управляет кэшем. Предполагается, что в конфигурации приложения
 *         настроены кэши с разумным временем жизни (TTL), так как финансовые данные могут меняться.</li>
 * </ul>
 *
 * @see org.springframework.cache.annotation.Cacheable
 */
@Repository
public class RecommendationsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param jdbcTemplate экземпляр {@code JdbcTemplate}, настроенный с квалификатором
     *                     {@code "recommendationsJdbcTemplate"} для изолированной работы
     *                     с транзакциями и продуктами.
     * @param cacheManager менеджер кэша, предоставляющий доступ к именованным кэшам.
     */
    public RecommendationsRepository(
            @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate,
            CacheManager cacheManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManager = cacheManager;
    }

    /**
     * Проверяет, совершал ли пользователь транзакции по продукту определенного типа.
     * <p>
     * Возвращает {@code true}, если найдена хотя бы одна транзакция, связывающая
     * пользователя с продуктом заданного типа.
     * </p>
     *
     * <h4>Кэширование:</h4>
     * <ul>
     *     <li><b>Кэш:</b> {@code userOfCache}</li>
     *     <li><b>Ключ:</b> {@code "{userId}:{productType}"}</li>
     *     <li><b>Срок жизни:</b> Зависит от конфигурации кэша. Для булевых флагов допустимо
     *         более длительное время хранения, если бизнес-логика позволяет небольшую задержку
     *         в обновлении статуса.</li>
     * </ul>
     *
     * @param userId      идентификатор пользователя
     * @param productType тип продукта (например, "CREDIT_CARD", "DEPOSIT")
     * @return {@code true}, если транзакции найдены, иначе {@code false}
     */
    public boolean hasProductType(UUID userId, String productType) {
        Cache cache = cacheManager.getCache("userOfCache");
        String key = userId + ":" + productType;
        return cache.get(key, () -> jdbcTemplate.queryForObject(
                "SELECT COUNT(*)>0 FROM transactions t " +
                        "JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ?",
                Boolean.class,
                userId,
                productType
        ));
    }

    /**
     * Получает общую сумму депозитов пользователя по продукту определенного типа.
     * <p>
     * Агрегирует поле {@code amount} из таблицы транзакций, фильтруя по типу транзакции
     * "DEPOSIT" и типу продукта. Использует {@code COALESCE(..., 0)} для возврата 0,
     * если транзакций не найдено (вместо {@code null}).
     * </p>
     *
     * <h4>Кэширование:</h4>
     * <ul>
     *     <li><b>Кэш:</b> {@code sumCache}</li>
     *     <li><b>Ключ:</b> {@code "deposit:{userId}:{productType}"}</li>
     * </ul>
     *
     * @param userId      идентификатор пользователя
     * @param productType тип продукта
     * @return сумма депозитов (Double), 0 если транзакций нет
     */
    public Double getTotalDepositsByProductType(UUID userId, String productType) {
        Cache cache = cacheManager.getCache("sumCache");
        String key = "deposit:" + userId + ":" + productType;
        return cache.get(key, () -> jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
                        "JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ? AND t.type = 'DEPOSIT'",
                Double.class,
                userId,
                productType
        ));
    }

    /**
     * Получает общую сумму снятий (withdrawals) пользователя по продукту определенного типа.
     * <p>
     * Аналогично методу {@link #getTotalDepositsByProductType}, но фильтрует транзакции
     * по типу "WITHDRAW".
     * </p>
     *
     * <h4>Кэширование:</h4>
     * <ul>
     *     <li><b>Кэш:</b> {@code sumCache} (используется тот же кэш, но с уникальным ключом)</li>
     *     <li><b>Ключ:</b> {@code "withdraw:{userId}:{productType}"}</li>
     * </ul>
     *
     * @param userId      идентификатор пользователя
     * @param productType тип продукта
     * @return сумма снятий (Double), 0 если транзакций нет
     */
    public Double getTotalWithdrawalsByProductType(UUID userId, String productType) {
        Cache cache = cacheManager.getCache("sumCache");
        String key = "withdraw:" + userId + ":" + productType;
        return cache.get(key, () -> jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
                        "JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ? AND t.type = 'WITHDRAW'",
                Double.class,
                userId,
                productType
        ));
    }

    /**
     * Подсчитывает количество транзакций пользователя по продукту определенного типа.
     * <p>
     * Возвращает целое число. Если транзакций нет, результат будет 0.
     * </p>
     *
     * <h4>Кэширование:</h4>
     * <ul>
     *     <li><b>Кэш:</b> {@code activeUserCache}</li>
     *     <li><b>Ключ:</b> {@code "{userId}:{productType}"}</li>
     * </ul>
     *
     * @param userId      идентификатор пользователя
     * @param productType тип продукта
     * @return количество транзакций (int)
     */
    public int getTransactionCount(UUID userId, String productType) {
        Cache cache = cacheManager.getCache("activeUserCache");
        String key = userId + ":" + productType;
        return cache.get(key, () -> jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions t " +
                        "JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ?",
                Integer.class,
                userId,
                productType
        ));
    }
}
