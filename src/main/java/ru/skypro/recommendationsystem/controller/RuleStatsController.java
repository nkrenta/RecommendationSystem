package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.service.DynamicRuleService;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.*;

@RestController
@RequestMapping("/rule")
public class RuleStatsController {

    private final RuleStatsService ruleStatsService;
    private final DynamicRuleService dynamicRuleService;

    public RuleStatsController(RuleStatsService ruleStatsService, DynamicRuleService dynamicRuleService) {
        this.ruleStatsService = ruleStatsService;
        this.dynamicRuleService = dynamicRuleService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getStats() {
        List<DynamicRule> allRules = dynamicRuleService.getAllRules();
        Map<UUID, RuleStats> statsMap = new HashMap<>();

        for (RuleStats stats : ruleStatsService.getAllStats()) {
            statsMap.put(stats.getRuleId(), stats);
        }

        List<Map<String, Object>> statsList = new ArrayList<>();
        for (DynamicRule rule : allRules) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rule_id", rule.getId().toString());
            RuleStats stats = statsMap.get(rule.getId());
            entry.put("count", stats != null ? stats.getCount() : 0);
            statsList.add(entry);
        }

        return ResponseEntity.ok(Map.of("stats", statsList));
    }
}
