package ru.skypro.recommendationsystem.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RecommendationsDataSourceConfiguration {
    @Bean(name = "recommendationsDataSource")
    public DataSource recommendationsDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:file:./db/transaction");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setReadOnly(true);
        return dataSource;
    }

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

    @Bean(name = "recommendationsJdbcTemplate")
    public JdbcTemplate recommendationsJdbcTemplate(
            @Qualifier("recommendationsDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }

}