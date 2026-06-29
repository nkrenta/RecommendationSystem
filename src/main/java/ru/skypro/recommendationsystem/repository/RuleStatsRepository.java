package ru.skypro.recommendationsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skypro.recommendationsystem.entity.RuleStats;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStats, UUID> {
    Optional<RuleStats> findByDynamicRuleId(UUID ruleId);

    void deleteByDynamicRuleId(UUID ruleId);

    @Modifying
    @Query("UPDATE RuleStats rs SET rs.count = rs.count + 1 WHERE rs.dynamicRule.id = :ruleId")
    int incrementCount(@Param("ruleId") UUID ruleId);
}
