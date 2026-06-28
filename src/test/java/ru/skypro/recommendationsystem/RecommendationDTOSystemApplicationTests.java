package ru.skypro.recommendationsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // <-- Эта аннотация говорит Spring: "Бери настройки из application-test.yml"
class RecommendationDTOSystemApplicationTests {

    private final DynamicRuleService dynamicRuleService;

    // Конструктор: Spring сам передаст сюда нужный сервис
    @Autowired
    public RecommendationDTOSystemApplicationTests(DynamicRuleService dynamicRuleService) {
        this.dynamicRuleService = dynamicRuleService;
    }

    @Test
    void testCreateAndFindRule() {
        // Подготовка данных
        DynamicRule rule = new DynamicRule();
        rule.setProductName("Test Rule Product Name");
        rule.setProductId(UUID.randomUUID());
        rule.setProductText("Test text for product");
        rule.setQueries(new ArrayList<>()); // Используй new ArrayList, Collections.emptyList() иногда вызывает проблемы при изменении

        // Выполнение действия
        DynamicRule savedRule = dynamicRuleService.createRule(rule);

        // Проверки
        assertNotNull(savedRule); // Используй JUnit assertions вместо ключевого слова assert
        assertNotNull(savedRule.getId());

        var allRules = dynamicRuleService.getAllRules();
        assertFalse(allRules.isEmpty(), "Список правил не должен быть пустым");

        // ПРАВИЛЬНАЯ ПРОВЕРКА: ищем по ID
        boolean found = allRules.stream()
                .anyMatch(r -> r.getId().equals(savedRule.getId()));

        assertTrue(found, "Правило с ID " + savedRule.getId() + " должно быть найдено в списке");

        System.out.println("✅ Тест успешно пройден! Правило создано: " + savedRule.getProductName());
    }
}
