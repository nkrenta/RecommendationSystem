package ru.skypro.recommendationsystem.service;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;


import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {
    private final DynamicRuleRepository repository;


    public DynamicRuleService(DynamicRuleRepository repository, RecommendationsRepository recommendationsRepository, @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.repository = repository;
    }

    //создание динамического правила
    @Transactional
    public DynamicRule createRule(DynamicRule rule) {
        rule.setId(UUID.randomUUID());
        return repository.save(rule);
    }

    //получение всех динамических правил
    public List<DynamicRule> getAllRules() {
        return repository.findAllWithQueries();
    }

    //удаление динамического правила
    public void deleteRule(UUID id) {
        repository.deleteById(id);
    }


}

