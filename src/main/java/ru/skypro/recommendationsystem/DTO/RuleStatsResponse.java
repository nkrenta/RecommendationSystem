package ru.skypro.recommendationsystem.DTO;

import java.util.List;

public class RuleStatsResponse {
    private List<RuleStatsDTO> stats;

    public RuleStatsResponse() {
    }

    public RuleStatsResponse(List<RuleStatsDTO> stats) {
        this.stats = stats;
    }

    public List<RuleStatsDTO> getStats() {
        return stats;
    }

    public void setStats(List<RuleStatsDTO> stats) {
        this.stats = stats;
    }
}
