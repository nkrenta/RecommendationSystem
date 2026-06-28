package ru.skypro.recommendationsystem.controller.JUnitTests;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;
import ru.skypro.recommendationsystem.controller.RecommendationController;
import ru.skypro.recommendationsystem.service.RecommendationService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void getRecommendations_ValidUserId_Returns200AndCallsService() throws Exception {
        UUID userId = UUID.randomUUID();
        List<RecommendationDTO> recommendations = List.of(
                new RecommendationDTO(UUID.randomUUID(), "Product1", "Text1"),
                new RecommendationDTO(UUID.randomUUID(), "Product2", "Text2")
        );
        RecommendationResponse response = new RecommendationResponse(userId, recommendations);
        when(recommendationService.getRecommendationsForUser(userId)).thenReturn(response);

        mockMvc.perform(get("/recommendation/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.recommendations.length()").value(2))
                .andExpect(jsonPath("$.recommendations[0].name").value("Product1"))
                .andExpect(jsonPath("$.recommendations[1].name").value("Product2"));

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(recommendationService).getRecommendationsForUser(captor.capture());
        assertThat(captor.getValue()).isEqualTo(userId);
    }

    @Test
    void getRecommendations_EmptyResult_Returns200AndCallsService() throws Exception {
        UUID userId = UUID.randomUUID();
        RecommendationResponse response = new RecommendationResponse(userId, Collections.emptyList());
        when(recommendationService.getRecommendationsForUser(userId)).thenReturn(response);

        mockMvc.perform(get("/recommendation/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(0));

        verify(recommendationService).getRecommendationsForUser(userId);
    }

    @Test
    void getRecommendations_InvalidUserId_Returns400AndDoesNotCallService() throws Exception {
        mockMvc.perform(get("/recommendation/invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(recommendationService, never()).getRecommendationsForUser(any());
    }
}
