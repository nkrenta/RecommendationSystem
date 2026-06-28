package ru.skypro.recommendationsystem.controller.JUnitTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.recommendationsystem.controller.DynamicRuleController;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DynamicRuleController.class)
class DynamicRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamicRuleService dynamicRuleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRule_ValidInput_Returns200AndCallsService() throws Exception {
        DynamicRule rule = buildRule("Product", UUID.randomUUID(), "Description");
        DynamicRule saved = buildRule("Product", UUID.randomUUID(), "Description");
        saved.setId(UUID.randomUUID());

        when(dynamicRuleService.createRule(any(DynamicRule.class))).thenReturn(saved);

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Product"))
                .andExpect(jsonPath("$.id").value(saved.getId().toString()));

        ArgumentCaptor<DynamicRule> captor = ArgumentCaptor.forClass(DynamicRule.class);
        verify(dynamicRuleService).createRule(captor.capture());
        assertThat(captor.getValue().getProductName()).isEqualTo("Product");
        assertThat(captor.getValue().getProductId()).isNotNull();
    }

    @Test
    void createRule_NullProductName_Returns400AndDoesNotCallService() throws Exception {
        DynamicRule rule = buildRule(null, UUID.randomUUID(), "Text");

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isBadRequest());

        verify(dynamicRuleService, never()).createRule(any());
    }

    @Test
    void createRule_NullProductId_Returns400AndDoesNotCallService() throws Exception {
        DynamicRule rule = buildRule("Name", null, "Text");

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isBadRequest());

        verify(dynamicRuleService, never()).createRule(any());
    }

    @Test
    void createRule_NullProductText_Returns400AndDoesNotCallService() throws Exception {
        DynamicRule rule = buildRule("Name", UUID.randomUUID(), null);

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isBadRequest());

        verify(dynamicRuleService, never()).createRule(any());
    }

    @Test
    void createRule_NullBody_Returns400AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verify(dynamicRuleService, never()).createRule(any());
    }

    @Test
    void getAllRules_Returns200AndCallsServiceOnce() throws Exception {
        List<DynamicRule> rules = List.of(
                buildRule("P1", UUID.randomUUID(), "T1"),
                buildRule("P2", UUID.randomUUID(), "T2")
        );
        when(dynamicRuleService.getAllRules()).thenReturn(rules);

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(dynamicRuleService, times(1)).getAllRules();
    }

    @Test
    void getAllRules_EmptyList_Returns200WithEmptyData() throws Exception {
        when(dynamicRuleService.getAllRules()).thenReturn(List.of());

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(dynamicRuleService).getAllRules();
    }

    @Test
    void deleteRule_ExistingId_Returns204AndCallsService() throws Exception {
        UUID id = UUID.randomUUID();
        when(dynamicRuleService.deleteRule(id)).thenReturn(true);

        mockMvc.perform(delete("/rule/" + id))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(dynamicRuleService).deleteRule(captor.capture());
        assertThat(captor.getValue()).isEqualTo(id);
    }

    @Test
    void deleteRule_NonExistingId_Returns404AndCallsService() throws Exception {
        UUID id = UUID.randomUUID();
        when(dynamicRuleService.deleteRule(id)).thenReturn(false);

        mockMvc.perform(delete("/rule/" + id))
                .andExpect(status().isNotFound());

        verify(dynamicRuleService).deleteRule(id);
    }

    private DynamicRule buildRule(String name, UUID productId, String text) {
        DynamicRule rule = new DynamicRule();
        rule.setProductName(name);
        rule.setProductId(productId);
        rule.setProductText(text);
        rule.setQueries(new ArrayList<>());
        return rule;
    }
}
