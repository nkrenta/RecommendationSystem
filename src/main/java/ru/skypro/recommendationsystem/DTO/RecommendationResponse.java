package ru.skypro.recommendationsystem.DTO;

import java.util.List;
import java.util.UUID;

public  class RecommendationResponse {
    private UUID userId;
    private List<RecommendationDTO> recommendations;

    public RecommendationResponse(UUID userId, List<RecommendationDTO> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }


    public UUID getUserId() {
        return userId; }

    public void setUserId(UUID userId) {
        this.userId = userId; }

    public List<RecommendationDTO> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationDTO> recommendations) {
        this.recommendations = recommendations;
    }
}
