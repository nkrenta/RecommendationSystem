package ru.skypro.recommendationsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.recommendationsystem.entity.RuleStats;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStats, UUID> {
    Optional<RuleStats> findByRuleId(UUID ruleId);

    void deleteByRuleId(UUID ruleId);
}
