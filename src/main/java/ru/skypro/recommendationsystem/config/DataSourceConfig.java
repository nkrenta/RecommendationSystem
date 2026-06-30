package ru.skypro.recommendationsystem.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
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
 * Конфигурация источников данных (Data Sources) для приложения Star Bank Recommendation System.
 * <p>
 * Реализует архитектуру с поддержкой <b>двух независимых баз данных</b>:
 * </p>
 *
 * <h3>Источники данных:</h3>
 * <ul>
 *     <li><b>recommendationsDataSource (H2)</b> — встроенная легковесная БД.
 *         <ul>
 *             <li>Режим: <code>readOnly = true</code>.</li>
 *             <li>Назначение: хранение транзакционной истории для расчета рекомендаций (чтение данных).</li>
 *             <li>Тип доступа: через {@link JdbcTemplate} (прямой SQL) для максимальной производительности выборки.</li>
 *         </ul>
 *     </li>
 *     <li><b>secondDataSource (PostgreSQL)</b> — основная реляционная БД.
 *         <ul>
 *             <li>Статус: {@code @Primary} бин для Spring.</li>
 *             <li>Назначение: хранение правил, пользователей, конфигураций и метаданных.</li>
 *             <li>Тип доступа: через JPA/Hibernate ({@link LocalContainerEntityManagerFactoryBean}).</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h3>Важные особенности:</h3>
 * <ul>
 *     <li>Компонент активен только в продуктивных профилях ({@code @Profile("!test")}).</li>
 *     <li>Миграции Liquibase применяются <b>только</b> к PostgreSQL базе ({@code secondDataSource}).
 *         H2 база считается временным хранилищем данных для расчетов и не требует миграций схемы.</li>
 *     <li>Транзакции управляются исключительно для PostgreSQL источника ({@code secondTransactionManager}).
 *         Операции чтения из H2 не требуют транзакционного контекста.</li>
 * </ul>
 *
 * @see DataSource
 * @see JdbcTemplate
 * @see LocalContainerEntityManagerFactoryBean
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "ru.skypro.recommendationsystem.repository",
        entityManagerFactoryRef = "secondEntityManagerFactory",
        transactionManagerRef = "secondTransactionManager"
)

@Profile("!test")
public class DataSourceConfig {

    /**
     * Создает источник данных для чтения транзакционной истории.
     * <p>
     * Использует встроенную базу данных H2 в файловом режиме.
     * Настроен в режиме только для чтения ({@code readOnly = true}) для защиты данных
     * и оптимизации производительности при массовых выборках.
     * </p>
     *
     * @return настроенный экземпляр {@link HikariDataSource} для H2
     */
    @Bean(name = "recommendationsDataSource")
    public DataSource recommendationsDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:file:./db/transaction");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setReadOnly(true);
        return dataSource;
    }

    /**
     * Основной источник данных для работы с бизнес-сущностями.
     * <p>
     * Подключается к PostgreSQL. Помечен аннотацией {@link Primary}, чтобы стать
     * источником данных по умолчанию для всего контекста Spring, если явно не указано иное.
     * </p>
     *
     * @return настроенный экземпляр {@link HikariDataSource} для PostgreSQL
     */
    @Primary
    @Bean(name = "secondDataSource")
    public DataSource secondDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/rules_db");
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        return dataSource;
    }

    /**
     * Бин {@link JdbcTemplate}, привязанный к источнику данных H2.
     * <p>
     * Используется в сервисах для выполнения прямых SQL-запросов к таблице транзакций.
     * Прямой доступ через JDBC выбран здесь вместо JPA для упрощения агрегаций
     * (SUM, COUNT) и лучшей читаемости запросов к историческим данным.
     * </p>
     *
     * @param dataSource источник данных {@code recommendationsDataSource}
     * @return экземпляр {@link JdbcTemplate}
     */
    @Bean(name = "recommendationsJdbcTemplate")
    public JdbcTemplate recommendationsJdbcTemplate(
            @Qualifier("recommendationsDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Фабрика EntityManager для работы с PostgreSQL базой данных.
     * <p>
     * Сканирует пакет сущностей ({@code ru.skypro.recommendationsystem.entity})
     * и настраивает адаптер Hibernate.
     * </p>
     * <p>
     * Режим работы Hibernate установлен в {@code validate}. Это означает, что
     * при старте приложения Hibernate проверит соответствие сущностей и таблиц в БД,
     * но <b>не будет</b> пытаться изменять структуру БД. Управление схемой полностью
     * делегировано инструменту Liquibase.
     * </p>
     *
     * @param dataSource основной источник данных ({@code secondDataSource})
     * @return настроенный {@link LocalContainerEntityManagerFactoryBean}
     */
    @Bean(name = "secondEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondEntityManagerFactory(
            @Qualifier("secondDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.skypro.recommendationsystem.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        // Set JPA properties for PostgreSQL
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        em.setJpaPropertyMap(properties);

        return em;
    }

    /**
     * Менеджер транзакций для PostgreSQL базы данных.
     * <p>
     * Управляет транзакционным контекстом для операций записи (INSERT, UPDATE, DELETE)
     * в основной базе данных. Связан с фабрикой {@code secondEntityManagerFactory}.
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

    /**
     * Бин Liquibase для автоматического применения миграций базы данных.
     * <p>
     * Привязан <b>исключительно</b> к источнику данных PostgreSQL ({@code secondDataSource}),
     * так как управление схемой основной БД осуществляется через миграции.
     * </p>
     * <p>
     * Для базы H2 миграции не применяются, поскольку она используется как временное хранилище
     * данных для расчетов, а не как источник истины для структуры приложения.
     * </p>
     *
     * @param secondDataSource источник данных PostgreSQL
     * @return настроенные экземпляр {@link SpringLiquibase}
     */
    @Bean
    public SpringLiquibase liquibaseForPostgres(@Qualifier("secondDataSource") DataSource secondDataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(secondDataSource); // <--- Привязываем к Postgres (второй источник)
        liquibase.setChangeLog("classpath:liquibase/changelog-master.yaml");
        liquibase.setDefaultSchema("public");
        return liquibase;
    }
}