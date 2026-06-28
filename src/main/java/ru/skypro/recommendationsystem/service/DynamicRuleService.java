package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {
    private final DynamicRuleRepository repository;
    private final RuleStatsService ruleStatsService;

    public DynamicRuleService(DynamicRuleRepository repository, RuleStatsService ruleStatsService) {
        this.repository = repository;
        this.ruleStatsService = ruleStatsService;
    }

    @Transactional
    public DynamicRule createRule(DynamicRule rule) {
        rule.setId(UUID.randomUUID());
        if (rule.getQueries() != null) {
            rule.getQueries().forEach(q -> q.setDynamicRule(rule));
        }
        return repository.save(rule);
    }

    public List<DynamicRule> getAllRules() {
        return repository.findAllWithQueries();
    }

    @Transactional
    public boolean deleteRule(UUID id) {
        if (!repository.existsById(id)) {
            return false;
        }
        ruleStatsService.deleteByRuleId(id);
        repository.deleteById(id);
        return true;
    }
}

