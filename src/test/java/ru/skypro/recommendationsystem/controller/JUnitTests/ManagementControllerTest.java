package ru.skypro.recommendationsystem.controller.JUnitTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.recommendationsystem.controller.ManagementController;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagementController.class)
@ActiveProfiles("management-test")
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void getInfo_Returns200WithAppInfo() throws Exception {
        when(buildProperties.getName()).thenReturn("recommendation-system");
        when(buildProperties.getVersion()).thenReturn("0.0.1-SNAPSHOT");

        mockMvc.perform(get("/management/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("recommendation-system"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"));

        verify(buildProperties).getName();
        verify(buildProperties).getVersion();
    }

    @Test
    void clearCaches_Returns200AndClearsAllCaches() throws Exception {
        Cache cache1 = mock(Cache.class);
        Cache cache2 = mock(Cache.class);
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cache1", "cache2"));
        when(cacheManager.getCache("cache1")).thenReturn(cache1);
        when(cacheManager.getCache("cache2")).thenReturn(cache2);

        mockMvc.perform(post("/management/clear-caches"))
                .andExpect(status().isOk());

        verify(cacheManager).getCache("cache1");
        verify(cacheManager).getCache("cache2");
        verify(cache1).clear();
        verify(cache2).clear();
    }

    @Test
    void clearCaches_EmptyCacheNames_Returns200() throws Exception {
        when(cacheManager.getCacheNames()).thenReturn(Set.of());

        mockMvc.perform(post("/management/clear-caches"))
                .andExpect(status().isOk());

        verify(cacheManager).getCacheNames();
        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    void clearCaches_NullCacheBean_SkipsClear() throws Exception {
        when(cacheManager.getCacheNames()).thenReturn(Set.of("missingCache"));
        when(cacheManager.getCache("missingCache")).thenReturn(null);

        mockMvc.perform(post("/management/clear-caches"))
                .andExpect(status().isOk());

        verify(cacheManager).getCache("missingCache");
    }
}
