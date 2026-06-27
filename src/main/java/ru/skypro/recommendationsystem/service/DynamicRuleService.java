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

    // Оставляем только реально используемую зависимость
    public DynamicRuleService(DynamicRuleRepository repository) {
        this.repository = repository;
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

    public void deleteRule(UUID id) {
        repository.deleteById(id);
    }
}

