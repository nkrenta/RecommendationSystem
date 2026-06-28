package ru.skypro.recommendationsystem.controller.IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.skypro.recommendationsystem.bot.RecommendationBot;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class ManagementControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private RecommendationBot recommendationBot;

    @MockitoBean
    private BuildProperties buildProperties;

    @BeforeEach
    void setUp() {
        when(buildProperties.getName()).thenReturn("recommendation-system");
        when(buildProperties.getVersion()).thenReturn("0.0.1-SNAPSHOT");
    }

    @Test
    void getInfo_Returns200() {
        ResponseEntity<Map> response = restTemplate.getForEntity(infoUrl(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("name");
        assertThat(response.getBody()).containsKey("version");
    }

    @Test
    void getInfo_NameIsNotBlank() {
        ResponseEntity<Map> response = restTemplate.getForEntity(infoUrl(), Map.class);

        String name = (String) response.getBody().get("name");
        assertThat(name).isNotBlank();
    }

    @Test
    void clearCaches_Returns200() {
        ResponseEntity<Void> response = restTemplate.postForEntity(clearCachesUrl(), null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getInfo_ReturnsJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(infoUrl(), HttpMethod.GET, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
    }

    private String infoUrl() {
        return "http://localhost:" + port + "/management/info";
    }

    private String clearCachesUrl() {
        return "http://localhost:" + port + "/management/clear-caches";
    }
}
