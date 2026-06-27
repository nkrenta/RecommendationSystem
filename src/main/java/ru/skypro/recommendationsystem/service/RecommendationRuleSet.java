package ru.skypro.recommendationsystem.service;

import ru.skypro.recommendationsystem.DTO.RecommendationDTO;

import java.util.List;
import java.util.UUID;

public interface RecommendationRuleSet {
    List<RecommendationDTO> checkRecommendation(UUID userId);
}
