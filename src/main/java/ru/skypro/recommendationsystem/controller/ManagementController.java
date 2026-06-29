package ru.skypro.recommendationsystem.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Административный REST-контроллер для управления состоянием приложения.
 * <p>
 * Предоставляет эндпоинты для операционной поддержки сервиса рекомендаций:
 * </p>
 *
 * <h3>Доступные операции:</h3>
 * <ul>
 *     <li><b>POST /management/clear-caches</b> — принудительная очистка всех активных кэшей.
 *         Используется при обновлении правил рекомендаций или исправлении данных в БД,
 *         чтобы инвалидировать устаревшие значения в памяти.</li>
 *
 *     <li><b>GET /management/info</b> — получение метаданных сборки приложения.
 *         Возвращает имя артефакта и версию, указанные в файле <code>build-info.properties</code>.
 *         Необходимо для трассировки ошибок и подтверждения развернутой версии на сервере.</li>
 * </ul>
 *
 * <h3>Ограничения и особенности:</h3>
 * <ul>
 *     <li>Компонент активируется <b>только в продуктивных профилях</b> ({@code @Profile("!test")}).
 *         В тестовых окружениях эти эндпоинты недоступны, чтобы исключить случайное влияние
 *         на изолированные тесты.</li>
 *
 *     <li>Эндпоинт очистки кэша не имеет встроенной аутентификации в данном классе.
 *         <strong>Доступ к нему должен быть ограничен на уровне шлюза (API Gateway),
 *         Nginx или через Spring Security в реальном проекте.</strong></li>
 * </ul>
 *
 * @see CacheManager
 * @see BuildProperties
 */
@RestController
@RequestMapping("/management")
@Profile("!test")
public class ManagementController {

    private final CacheManager cacheManager;
    private final BuildProperties buildProperties;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param cacheManager    менеджер кэшей, используемый для очистки данных в памяти
     * @param buildProperties свойства сборки приложения (имя, версия), сгенерированные Maven/Gradle
     */
    public ManagementController(CacheManager cacheManager, BuildProperties buildProperties) {
        this.cacheManager = cacheManager;
        this.buildProperties = buildProperties;
    }

    /**
     * Принудительно очищает все кэши, зарегистрированные в приложении.
     * <p>
     * Проходит по всем именам кэшей, полученным из {@link CacheManager},
     * и вызывает метод {@code clear()} для каждого из них.
     * </p>
     *
     * <h4>Сценарии использования:</h4>
     * <ul>
     *     <li>После изменения правил рекомендаций через другой API или БД.</li>
     *     <li>При обнаружении рассинхронизации данных между БД и кэшем.</li>
     *     <li>В рамках процедур деплоя или отладки.</li>
     * </ul>
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>200 OK</dt>
     *         <dd>Кэши успешно очищены. Ответ не содержит тела.</dd>
     * </dl>
     *
     * @return {@link ResponseEntity} со статусом 200 и пустым телом
     */
    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает информацию о версии развернутого приложения.
     * <p>
     * Данные берутся из автоматически сгенерированного файла <code>build-info.properties</code>,
     * который создается плагинами Maven или Gradle во время сборки проекта.
     * </p>
     *
     * <h4>Формат ответа:</h4>
     * {@code
     * {
     * "name": "recommendation-system",
     * "version": "1.0.0-SNAPSHOT"
     * }
     * }
     *
     * <h4>Коды ответов:</h4>
     * <dl>
     *     <dt>200 OK</dt>
     *         <dd>Метаданные сборки успешно получены.</dd>
     * </dl>
     *
     * @return {@link ResponseEntity}, содержащий карту с полями "name" и "version"
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "name", buildProperties.getName(),
                "version", buildProperties.getVersion()
        ));
    }
}
