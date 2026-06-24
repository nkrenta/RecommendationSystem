package ru.skypro.recommendationsystem.service;


import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class RecommendationService {
    private final List<RecommendationRuleSet> recommendationRules;

    public RecommendationService(List<RecommendationRuleSet> recommendationRules) {
        this.recommendationRules = recommendationRules;
    }

    public RecommendationResponse getRecommendationsForUser(UUID userId) {
        List<RecommendationDTO> recommendations = recommendationRules.stream()
                .flatMap(rule -> rule.checkRecommendation(userId).stream())
                .collect(Collectors.toList());

        return new RecommendationResponse(userId, recommendations);
    }
}




