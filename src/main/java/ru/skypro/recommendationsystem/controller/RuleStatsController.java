package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.DTO.RuleStatsDTO;
import ru.skypro.recommendationsystem.DTO.RuleStatsResponse;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.service.DynamicRuleService;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.*;
import java.util.stream.Collectors;

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
    public ResponseEntity<RuleStatsResponse> getStats() {
        List<DynamicRule> allRules = dynamicRuleService.getAllRules();
        Map<UUID, RuleStats> statsMap = ruleStatsService.getAllStats().stream()
                .collect(Collectors.toMap(s -> s.getDynamicRule().getId(), s -> s));

        List<RuleStatsDTO> statsList = allRules.stream()
                .map(rule -> {
                    RuleStats stats = statsMap.get(rule.getId());
                    return new RuleStatsDTO(rule.getId().toString(), stats != null ? stats.getCount() : 0);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new RuleStatsResponse(statsList));
    }
}
