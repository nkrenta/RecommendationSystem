package ru.skypro.recommendationsystem.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.service.DynamicRuleService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class DynamicRuleController {
    private final DynamicRuleService dynamicRuleService;

    @Autowired
    public DynamicRuleController(DynamicRuleService dynamicRuleService) {
        this.dynamicRuleService = dynamicRuleService;
    }

    @PostMapping
    public ResponseEntity<DynamicRule> createRule(@RequestBody DynamicRule rule) {
        DynamicRule createdRule = dynamicRuleService.createRule(rule);
        return ResponseEntity.ok(createdRule);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<DynamicRule>>> getAllRules() {
        List<DynamicRule> rules = dynamicRuleService.getAllRules();
        return ResponseEntity.ok(Map.of("data", rules));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        dynamicRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

}

