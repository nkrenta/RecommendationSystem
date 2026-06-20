package ru.skypro.recommendationsystem.service.dynamicRuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleQuery;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DynamicRuleSet implements RecommendationRuleSet {
    private final DynamicRuleRepository dynamicRuleRepository;
    private final RecommendationsRepository recommendationsRepository;

    public DynamicRuleSet(DynamicRuleRepository dynamicRuleRepository,
                          RecommendationsRepository recommendationsRepository) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public Optional<RecommendationDTO> checkRecommendation(UUID userId) {
        return dynamicRuleRepository.findAll().stream()
                .map(rule -> checkRuleForUser(userId, rule))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    Optional<RecommendationDTO> checkRuleForUser(UUID userId, DynamicRule rule) {
        boolean allConditionsMet = rule.getQueries().stream()
                .allMatch(query -> checkCondition(userId, query));

        return allConditionsMet
                ? Optional.of(new RecommendationDTO(
                rule.getProductId(),
                rule.getProductName(),
                rule.getProductText()))
                : Optional.empty();
    }

    private boolean checkCondition(UUID userId, RuleQuery query) {
        return switch (query.getQuery()) {
            case "USER_OF" ->
                    recommendationsRepository.hasProductType(userId, query.getArguments().get(0));
            case "ACTIVE_USER_OF" ->
                    recommendationsRepository.getTransactionCount(userId, query.getArguments().get(0)) >= 5;
            case "TRANSACTION_SUM_COMPARE" ->
                    checkTransactionSum(userId, query);
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" ->
                    checkDepositWithdrawCompare(userId, query);
            default -> throw new IllegalArgumentException("Unknown query type: " + query.getQuery());
        } ^ query.isNegate();
    }

    private boolean checkTransactionSum(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        double sum = args.get(1).equals("DEPOSIT")
                ? recommendationsRepository.getTotalDepositsByProductType(userId, args.get(0))
                : recommendationsRepository.getTotalWithdrawalsByProductType(userId, args.get(0));

        double compareValue = Double.parseDouble(args.get(3));

        return switch (args.get(2)) {
            case ">" -> sum > compareValue;
            case "<" -> sum < compareValue;
            case "=" -> Math.abs(sum - compareValue) < 0.001;
            case ">=" -> sum >= compareValue;
            case "<=" -> sum <= compareValue;
            default -> throw new IllegalArgumentException("Unknown operator: " + args.get(2));
        };
    }

    private boolean checkDepositWithdrawCompare(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        double depositSum = recommendationsRepository.getTotalDepositsByProductType(userId, args.get(0));
        double withdrawSum = recommendationsRepository.getTotalWithdrawalsByProductType(userId, args.get(0));

        return switch (args.get(1)) {
            case ">" -> depositSum > withdrawSum;
            case "<" -> depositSum < withdrawSum;
            case "=" -> Math.abs(depositSum - withdrawSum) < 0.001;
            case ">=" -> depositSum >= withdrawSum;
            case "<=" -> depositSum <= withdrawSum;
            default -> throw new IllegalArgumentException("Unknown operator: " + args.get(1));
        };
    }
}

