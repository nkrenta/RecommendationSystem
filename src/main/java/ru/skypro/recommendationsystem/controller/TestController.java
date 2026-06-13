package ru.skypro.recommendationsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.service.TestService;
import ru.skypro.recommendationsystem.service.TestServiceImpl;

import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService service;

    public TestController(TestServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index() {
        return "Recommendation Service";
    }

    @GetMapping("/recommendation/{userId}")
    public Integer recommendation(@PathVariable UUID userId) {
        return service.test(userId);
    }
}
