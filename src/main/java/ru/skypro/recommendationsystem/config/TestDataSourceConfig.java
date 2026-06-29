package ru.skypro.recommendationsystem.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация источников данных для тестовых профилей приложения.
 * <p>
 * Активируется только при запуске в профилях {@code test} или {@code it}
 * ({@code @Profile({"test", "it"})}).
 * </p>
 *
 * <h3>Ключевые особенности реализации:</h3>
 * <ul>
 *     <li><b>In-Memory база данных:</b> Используется H2 в оперативной памяти ({@code jdbc:h2:mem:testdb}).
 *         Данные существуют только в течение жизненного цикла JVM и исчезают после завершения тестов.</li>
 *
 *     <li><b>Совместимость с PostgreSQL:</b> Параметр {@code MODE=PostgreSQL} эмулирует поведение Postgres.
 *         Это позволяет использовать в тестах тот же SQL-код и типы данных, что и в продакшене,
 *         избегая ошибок несоответствия диалектов.</li>
 *
 *     <li><b>Автоматическое управление схемой:</b> Режим {@code create-drop} автоматически создает
 *         все таблицы перед запуском тестов и полностью удаляет их после завершения.
 *         <strong>Миграции Liquibase не применяются</strong> — схема строится на основе JPA-аннотаций сущностей.</li>
 *
 *     <li><b>Логирование SQL:</b> Включено логирование всех генерируемых SQL-запросов
 *         ({@code hibernate.show_sql = true}) для упрощения отладки тестов.</li>
 * </ul>
 *
 * <p>
 * В этом профиле один источник данных ({@code recommendationsDataSource}) используется как для JdbcTemplate,
 * так и для JPA, что упрощает конфигурацию и гарантирует согласованность данных между разными типами запросов.
 * </p>
 *
 * @see DataSource
 * @see LocalContainerEntityManagerFactoryBean
 */
@Configuration
@Profile({"test", "it"})
@EnableJpaRepositories(
        basePackages = "ru.skypro.recommendationsystem.repository",
        entityManagerFactoryRef = "secondEntityManagerFactory",
        transactionManagerRef = "secondTransactionManager"
)
public class TestDataSourceConfig {

    /**
     * Создает источник данных на базе H2 для тестового окружения.
     * <p>
     * Настраивается как in-memory база с эмуляцией диалекта PostgreSQL.
     * Параметры {@code DB_CLOSE_DELAY=-1} и {@code DB_CLOSE_ON_EXIT=FALSE}
     * предотвращают преждевременное закрытие базы данных сборщиком мусора или при выходе JVM,
     * обеспечивая стабильную работу в рамках тестового контекста Spring.
     * </p>
     *
     * @return настроенный экземпляр {@link HikariDataSource} для H2
     */
    @Primary
    @Bean(name = "recommendationsDataSource")
    public DataSource recommendationsDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
        dataSource.setDriverClassName("org.h2.Driver");
        return dataSource;
    }

    /**
     * Бин {@link JdbcTemplate}, привязанный к тестовому источнику данных.
     * <p>
     * Используется в интеграционных тестах для прямой проверки данных в БД
     * (например, утверждения количества записей или проверки значений полей).
     * </p>
     *
     * @param dataSource источник данных {@code recommendationsDataSource}
     * @return экземпляр {@link JdbcTemplate}
     */
    @Bean(name = "recommendationsJdbcTemplate")
    public JdbcTemplate recommendationsJdbcTemplate(@Qualifier("recommendationsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Фабрика EntityManager для работы с тестовой базой данных.
     * <p>
     * Использует режим {@code create-drop}, который:
     * <ol>
     *     <li>Создает все таблицы при старте контекста на основе JPA-сущностей.</li>
     *     <li>Полностью удаляет схему при остановке контекста.</li>
     * </ol>
     * </p>
     * <p>
     * Явно указан диалект {@code H2Dialect}, несмотря на эмуляцию Postgres,
     * чтобы Hibernate корректно генерировал SQL для H2.
     * Включено отображение SQL-запросов в логах для удобства отладки.
     * </p>
     *
     * @param dataSource источник данных {@code recommendationsDataSource}
     * @return настроенный {@link LocalContainerEntityManagerFactoryBean}
     */
    @Primary
    @Bean(name = "secondEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondEntityManagerFactory(
            @Qualifier("recommendationsDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.skypro.recommendationsystem.entity");
        em.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "true");

        em.setJpaPropertyMap(properties);
        return em;
    }

    /**
     * Менеджер транзакций для тестового окружения.
     * <p>
     * Управляет транзакционным контекстом для операций JPA в рамках тестовой базы данных.
     * Связан с фабрикой {@code secondEntityManagerFactory}.
     * </p>
     *
     * @param entityManagerFactory фабрика EntityManager
     * @return экземпляр {@link JpaTransactionManager}
     */
    @Bean(name = "secondTransactionManager")
    public JpaTransactionManager secondTransactionManager(
            @Qualifier("secondEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}