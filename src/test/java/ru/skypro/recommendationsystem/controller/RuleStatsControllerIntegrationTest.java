package ru.skypro.recommendationsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.service.DynamicRuleService;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RuleStatsControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DynamicRuleService dynamicRuleService;

    @Autowired
    private RuleStatsService ruleStatsService;

    @BeforeEach
    void setUp() {
        dynamicRuleService.getAllRules().forEach(r ->
                dynamicRuleService.deleteRule(r.getId())
        );
    }

    @Test
    void getStats_NoRules_Returns200WithEmptyList() {
        ResponseEntity<Map> response = restTemplate.getForEntity(statsUrl(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("stats")).isInstanceOf(List.class);
        List<?> stats = (List<?>) response.getBody().get("stats");
        assertThat(stats).isEmpty();
    }

    @Test
    void getStats_WithRules_Returns200WithStats() {
        DynamicRule rule1 = createAndSaveRule("Rule1");
        DynamicRule rule2 = createAndSaveRule("Rule2");

        ruleStatsService.incrementCount(rule1.getId());
        ruleStatsService.incrementCount(rule1.getId());
        ruleStatsService.incrementCount(rule1.getId());

        ResponseEntity<Map> response = restTemplate.getForEntity(statsUrl(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<Map<String, Object>> stats = (List<Map<String, Object>>) response.getBody().get("stats");
        assertThat(stats).hasSize(2);

        Optional<Map<String, Object>> rule1Stats = stats.stream()
                .filter(s -> s.get("rule_id").equals(rule1.getId().toString()))
                .findFirst();
        assertThat(rule1Stats).isPresent();
        assertThat(rule1Stats.get().get("count")).isEqualTo(3);

        Optional<Map<String, Object>> rule2Stats = stats.stream()
                .filter(s -> s.get("rule_id").equals(rule2.getId().toString()))
                .findFirst();
        assertThat(rule2Stats).isPresent();
        assertThat(rule2Stats.get().get("count")).isEqualTo(0);
    }

    @Test
    void getStats_AfterDeleteRule_Returns200WithoutDeletedRule() {
        DynamicRule rule = createAndSaveRule("ToDelete");
        ruleStatsService.incrementCount(rule.getId());

        ResponseEntity<Map> beforeDelete = restTemplate.getForEntity(statsUrl(), Map.class);
        List<Map<String, Object>> statsBefore = (List<Map<String, Object>>) beforeDelete.getBody().get("stats");
        assertThat(statsBefore).hasSize(1);

        dynamicRuleService.deleteRule(rule.getId());

        ResponseEntity<Map> afterDelete = restTemplate.getForEntity(statsUrl(), Map.class);
        List<Map<String, Object>> statsAfter = (List<Map<String, Object>>) afterDelete.getBody().get("stats");
        assertThat(statsAfter).isEmpty();
    }

    private DynamicRule createAndSaveRule(String name) {
        DynamicRule rule = new DynamicRule();
        rule.setProductName(name);
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("Text for " + name);
        rule.setQueries(new ArrayList<>());
        return dynamicRuleService.createRule(rule);
    }

    private String statsUrl() {
        return "http://localhost:" + port + "/rule/stats";
    }
}
