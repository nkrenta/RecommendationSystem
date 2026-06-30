package ru.skypro.recommendationsystem.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация кэширования для приложения Star Bank Recommendation System.
 * <p>
 * Настраивает простой in-memory кэш на основе {@link ConcurrentMapCacheManager}.
 * Предназначен для ускорения работы бизнес-логики, связанной с проверкой правил рекомендаций.
 * </p>
 *
 * <h3>Настроенные области кэширования (Cache Names):</h3>
 * <ul>
 *     <li><b>userOfCache</b> — хранит результаты проверки наличия у пользователя конкретного типа продукта.
 *         Используется правилом {@code USER_OF}.</li>
 *
 *     <li><b>activeUserCache</b> — кэширует статус активности пользователя (количество транзакций).
 *         Используется правилом {@code ACTIVE_USER_OF} для проверки порога в 5 операций.</li>
 *
 *     <li><b>sumCache</b> — содержит агрегированные суммы транзакций пользователя.
 *         Используется правилами сравнения сумм ({@code TRANSACTION_SUM_COMPARE} и др.).</li>
 * </ul>
 *
 * <p>
 * <strong>Важно:</strong> Данная реализация использует память JVM ({@code ConcurrentMap}).
 * Кэш не сохраняется на диск и очищается при перезапуске приложения.
 * Подходит для тестирования и небольших нагрузок. Для продакшена рекомендуется заменить
 * на Redis или другой распределенный кэш.
 * </p>
 *
 * @see ConcurrentMapCacheManager
 * @see CacheManager
 */
@Configuration
public class CacheConfig {

    /**
     * Создает и регистрирует бин {@link CacheManager}.
     * <p>
     * Инициализирует менеджер кэша с заранее определенными именами областей (cache names),
     * которые будут использоваться аннотациями {@code @Cacheable}, {@code @CachePut}
     * и {@code @CacheEvict} в сервисах приложения.
     * </p>
     *
     * @return экземпляр {@link CacheManager}, настроенный на работу с кэшами:
     * "userOfCache", "activeUserCache", "sumCache"
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("userOfCache", "activeUserCache", "sumCache");
    }
}
