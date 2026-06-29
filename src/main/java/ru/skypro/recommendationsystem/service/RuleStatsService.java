package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.DynamicRule;
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
        int updated = ruleStatsRepository.incrementCount(ruleId);
        if (updated == 0) {
            RuleStats stats = new RuleStats();
            DynamicRule rule = new DynamicRule();
            rule.setId(ruleId);
            stats.setDynamicRule(rule);
            stats.setCount(1);
            ruleStatsRepository.save(stats);
        }
    }

    @Transactional
    public void deleteByRuleId(UUID ruleId) {
        ruleStatsRepository.deleteByDynamicRuleId(ruleId);
    }

    public List<RuleStats> getAllStats() {
        return ruleStatsRepository.findAll();
    }
}
