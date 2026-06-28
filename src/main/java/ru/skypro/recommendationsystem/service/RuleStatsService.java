package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.repository.RuleStatsRepository;

import java.util.List;
import java.util.UUID;

@Service
public class RuleStatsService {
    private final RuleStatsRepository ruleStatsRepository;

    public RuleStatsService(RuleStatsRepository ruleStatsRepository) {
        this.ruleStatsRepository = ruleStatsRepository;
    }

    @Transactional
    public void incrementCount(UUID ruleId) {
        RuleStats stats = ruleStatsRepository.findByRuleId(ruleId)
                .orElse(new RuleStats(ruleId, 0));
        stats.setCount(stats.getCount() + 1);
        ruleStatsRepository.save(stats);
    }

    @Transactional
    public void deleteByRuleId(UUID ruleId) {
        ruleStatsRepository.deleteByRuleId(ruleId);
    }

    public List<RuleStats> getAllStats() {
        return ruleStatsRepository.findAll();
    }
}
