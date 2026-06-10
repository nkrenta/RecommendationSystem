package ru.skypro.recommendationsystem.service;

import ru.skypro.recommendationsystem.DTO.RecommendationDTO;

import java.util.Optional;
import java.util.UUID;


public interface RecommendationRuleSet {
    Optional<RecommendationDTO> checkRecommendation (UUID userId);
}
