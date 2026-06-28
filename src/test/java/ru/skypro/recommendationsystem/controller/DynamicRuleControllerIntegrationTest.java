package ru.skypro.recommendationsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleQuery;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DynamicRuleControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DynamicRuleService dynamicRuleService;

    private UUID createdRuleId;

    @BeforeEach
    void setUp() {
        DynamicRule rule = new DynamicRule();
        rule.setProductName("TestProduct");
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("Test product description");
        RuleQuery query = new RuleQuery();
        query.setQuery("USER_OF");
        query.setArguments(List.of("DEBIT"));
        query.setNegate(false);
        rule.setQueries(new ArrayList<>(List.of(query)));

        DynamicRule saved = dynamicRuleService.createRule(rule);
        createdRuleId = saved.getId();
    }

    @Test
    void createRule_ValidInput_Returns200() {
        DynamicRule rule = new DynamicRule();
        rule.setProductName("NewProduct");
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("New product");
        RuleQuery query = new RuleQuery();
        query.setQuery("USER_OF");
        query.setArguments(List.of("SAVING"));
        query.setNegate(false);
        rule.setQueries(new ArrayList<>(List.of(query)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DynamicRule> request = new HttpEntity<>(rule, headers);

        ResponseEntity<DynamicRule> response = restTemplate.postForEntity(url(), request, DynamicRule.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getProductName()).isEqualTo("NewProduct");
    }

    @Test
    void createRule_MissingProductName_Returns400() {
        DynamicRule rule = new DynamicRule();
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("Missing name");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DynamicRule> request = new HttpEntity<>(rule, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createRule_MissingProductId_Returns400() {
        DynamicRule rule = new DynamicRule();
        rule.setProductName("NoIdProduct");
        rule.setProductText("Missing productId");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DynamicRule> request = new HttpEntity<>(rule, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createRule_MissingProductText_Returns400() {
        DynamicRule rule = new DynamicRule();
        rule.setProductName("NoTextProduct");
        rule.setProductId(UUID.randomUUID());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DynamicRule> request = new HttpEntity<>(rule, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url(), request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllRules_Returns200WithList() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("data")).isInstanceOf(List.class);
        List<?> rules = (List<?>) response.getBody().get("data");
        assertThat(rules).isNotEmpty();
    }

    @Test
    void deleteRule_ExistingId_Returns204() {
        ResponseEntity<Void> response = restTemplate.exchange(
                url() + "/" + createdRuleId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteRule_NonExistingId_Returns404() {
        UUID fakeId = UUID.randomUUID();

        ResponseEntity<Void> response = restTemplate.exchange(
                url() + "/" + fakeId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String url() {
        return "http://localhost:" + port + "/rule";
    }
}
