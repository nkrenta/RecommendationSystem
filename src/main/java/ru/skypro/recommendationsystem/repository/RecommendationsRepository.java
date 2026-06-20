package ru.skypro.recommendationsystem.repository;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RecommendationsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;

    public RecommendationsRepository(
            @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate,
            CacheManager cacheManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManager = cacheManager;
    }

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
