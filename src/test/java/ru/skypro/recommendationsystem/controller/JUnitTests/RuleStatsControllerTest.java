package ru.skypro.recommendationsystem.controller.JUnitTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.recommendationsystem.controller.RuleStatsController;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleStats;
import ru.skypro.recommendationsystem.service.DynamicRuleService;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RuleStatsController.class)
class RuleStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamicRuleService dynamicRuleService;

    @MockitoBean
    private RuleStatsService ruleStatsService;

    @Test
    void getStats_NoRules_Returns200EmptyListAndCallsBothServices() throws Exception {
        when(dynamicRuleService.getAllRules()).thenReturn(List.of());
        when(ruleStatsService.getAllStats()).thenReturn(List.of());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats").isArray())
                .andExpect(jsonPath("$.stats.length()").value(0));

        verify(dynamicRuleService).getAllRules();
        verify(ruleStatsService).getAllStats();
    }

    @Test
    void getStats_WithRulesAndStats_Returns200() throws Exception {
        UUID rule1Id = UUID.randomUUID();
        UUID rule2Id = UUID.randomUUID();

        DynamicRule rule1 = buildRule(rule1Id, "Rule1");
        DynamicRule rule2 = buildRule(rule2Id, "Rule2");
        when(dynamicRuleService.getAllRules()).thenReturn(List.of(rule1, rule2));

        RuleStats stats1 = new RuleStats(rule1Id, 5);
        when(ruleStatsService.getAllStats()).thenReturn(List.of(stats1));

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(2))
                .andExpect(jsonPath("$.stats[0].rule_id").value(rule1Id.toString()))
                .andExpect(jsonPath("$.stats[0].count").value(5))
                .andExpect(jsonPath("$.stats[1].rule_id").value(rule2Id.toString()))
                .andExpect(jsonPath("$.stats[1].count").value(0));

        verify(dynamicRuleService).getAllRules();
        verify(ruleStatsService).getAllStats();
    }

    @Test
    void getStats_AllRulesHaveZeroCount() throws Exception {
        UUID ruleId = UUID.randomUUID();
        DynamicRule rule = buildRule(ruleId, "Rule");
        when(dynamicRuleService.getAllRules()).thenReturn(List.of(rule));
        when(ruleStatsService.getAllStats()).thenReturn(List.of());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(1))
                .andExpect(jsonPath("$.stats[0].count").value(0));

        verify(ruleStatsService).getAllStats();
    }

    @Test
    void getStats_OnlyStatsWithoutMatchingRules_AreIgnored() throws Exception {
        UUID ruleId = UUID.randomUUID();
        DynamicRule rule = buildRule(ruleId, "Rule");
        when(dynamicRuleService.getAllRules()).thenReturn(List.of(rule));

        UUID orphanRuleId = UUID.randomUUID();
        RuleStats orphanStats = new RuleStats(orphanRuleId, 10);
        when(ruleStatsService.getAllStats()).thenReturn(List.of(orphanStats));

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(1))
                .andExpect(jsonPath("$.stats[0].count").value(0));

        verify(dynamicRuleService).getAllRules();
        verify(ruleStatsService).getAllStats();
    }

    private DynamicRule buildRule(UUID id, String name) {
        DynamicRule rule = new DynamicRule();
        rule.setId(id);
        rule.setProductName(name);
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("Text");
        rule.setQueries(new ArrayList<>());
        return rule;
    }
}
