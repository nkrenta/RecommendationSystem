package ru.skypro.recommendationsystem.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleQuery;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DynamicRuleService {
    private final DynamicRuleRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final Cache<CacheKey, Boolean> userOfCache;
    private final Cache<CacheKey, Boolean> activeUserCache;
    private final Cache<CacheKey, Double> sumCache;

    public DynamicRuleService(DynamicRuleRepository repository,@Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.userOfCache = Caffeine.newBuilder().build();
        this.activeUserCache = Caffeine.newBuilder().build();
        this.sumCache = Caffeine.newBuilder().build();
    }

    //создание динамического правила
    public DynamicRule createRule(DynamicRule rule) {
        return repository.save(rule);
    }

    //получение всех динамических правил
    public List<DynamicRule> getAllRules() {
        return repository.findAll();
    }

    //удаление динамического правила
    public void deleteRule(UUID id) {
        repository.deleteById(id);
    }

    public Optional<RecommendationDTO> checkDynamicRule(UUID userId, DynamicRule rule) {
        for (RuleQuery query : rule.getQueries()) {
            if (!checkRuleQuery(userId, query)) {
                return Optional.empty();
            }
        }
        return Optional.of(new RecommendationDTO(
                rule.getProductId(),
                rule.getProductName(),
                rule.getProductText()
        ));
    }


    private boolean checkRuleQuery(UUID userId, RuleQuery query) {
        switch (query.getQuery()) {
            case "USER_OF": return checkUserOf(userId, query);
            case "ACTIVE_USER_OF": return checkActiveUserOf(userId, query);
            case "TRANSACTION_SUM_COMPARE": return checkTransactionSumCompare(userId, query);
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW": return checkDepositWithdrawCompare(userId, query);
            default: throw new IllegalArgumentException("Unknown query type: " + query.getQuery());
        }
    }

    private boolean checkUserOf(UUID userId, RuleQuery query) {
        String productType = query.getArguments().get(0);
        CacheKey key = new CacheKey(userId, "USER_OF", List.of(productType));
        Boolean result = userOfCache.get(key, k -> jdbcTemplate.queryForObject(
                "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ?",
                Boolean.class,
                userId.toString(),
                productType.toUpperCase()));
        return (result != null && result) ^ query.isNegate();
    }

    private boolean checkActiveUserOf(UUID userId, RuleQuery query) {
        String productType = query.getArguments().get(0);
        CacheKey key = new CacheKey(userId, "ACTIVE_USER_OF", List.of(productType));
        Boolean result = activeUserCache.get(key, k -> {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM transactions t JOIN products p ON t.product_id = p.id " +
                            "WHERE t.user_id = ? AND p.type = ?",
                    Integer.class,
                    userId.toString(),
                    productType.toUpperCase());
            return count != null && count >= 5;
        });
        return result ^ query.isNegate();
    }

    private boolean checkTransactionSumCompare(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        String productType = args.get(0);
        String transactionType = args.get(1);
        String operator = args.get(2);
        double compareValue = Double.parseDouble(args.get(3));

        CacheKey key = new CacheKey(userId, "TRANSACTION_SUM_COMPARE", args);
        Double sum = sumCache.get(key, k -> jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ? AND t.type = ?",
                Double.class,
                userId.toString(),
                productType.toUpperCase(),
                transactionType.toUpperCase()));

        if (sum == null) return false;

        boolean result;
        switch (operator) {
            case ">": result = sum > compareValue; break;
            case "<": result = sum < compareValue; break;
            case "=": result = Math.abs(sum - compareValue) < 0.001; break;
            case ">=": result = sum >= compareValue; break;
            case "<=": result = sum <= compareValue; break;
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }

        return result ^ query.isNegate();
    }

    private boolean checkDepositWithdrawCompare(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        String productType = args.get(0);
        String operator = args.get(1);

        CacheKey depositKey = new CacheKey(userId, "DEPOSIT_SUM", List.of(productType));
        Double depositSum = sumCache.get(depositKey, k -> jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ? AND t.type = 'DEPOSIT'",
                Double.class,
                userId.toString(),
                productType.toUpperCase()));

        CacheKey withdrawKey = new CacheKey(userId, "WITHDRAW_SUM", List.of(productType));
        Double withdrawSum = sumCache.get(withdrawKey, k -> jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id " +
                        "WHERE t.user_id = ? AND p.type = ? AND t.type = 'WITHDRAW'",
                Double.class,
                userId.toString(),
                productType.toUpperCase()));

        if (depositSum == null || withdrawSum == null) return false;

        boolean result;
        switch (operator) {
            case ">": result = depositSum > withdrawSum; break;
            case "<": result = depositSum < withdrawSum; break;
            case "=": result = Math.abs(depositSum - withdrawSum) < 0.001; break;
            case ">=": result = depositSum >= withdrawSum; break;
            case "<=": result = depositSum <= withdrawSum; break;
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }

        return result ^ query.isNegate();
    }

    private record CacheKey(UUID userId, String queryType, List<String> arguments) {}
}
