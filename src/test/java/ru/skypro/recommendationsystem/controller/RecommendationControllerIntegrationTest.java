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
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DynamicRuleService dynamicRuleService;

    @BeforeEach
    void cleanDynamicRules() {
        dynamicRuleService.getAllRules().forEach(r ->
                dynamicRuleService.deleteRule(r.getId())
        );
    }

    @Test
    void getRecommendationsIsPresent() {
        UUID userId = UUID.fromString("9d2df4a9-0085-4838-b8af-d8b46659cb62");
        ResponseEntity<RecommendationResponse> response = restTemplate.getForEntity(url(), RecommendationResponse.class, userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getRecommendations()).isNotEmpty();
        assertThat(response.getBody().getRecommendations()).anyMatch(dto -> dto.getName().equals("Top Saving"));
        assertThat(response.getBody().getRecommendations()).anyMatch(dto -> dto.getName().equals("Usual Credit"));
        assertThat(response.getBody().getRecommendations()).anyMatch(dto -> dto.getName().equals("INVEST500"));
    }

    @Test
    void getRecommendationIsEmpty() {
        UUID userId = UUID.fromString("e809075f-1752-411a-8e0c-de3bae23e1b9");
        ResponseEntity<RecommendationResponse> response = restTemplate.getForEntity(url(), RecommendationResponse.class, userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getRecommendations()).isEmpty();
    }

    @Test
    void getRecommendationInvalidUserId() {
        String invalidUserId = "invalid-uuid";
        ResponseEntity<String> response = restTemplate.getForEntity(url(), String.class, invalidUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Bad Request");
    }

    private String url() {
        return "http://localhost:" + port + "/recommendation/{userId}";
    }
}