package ru.skypro.recommendationsystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @io.swagger.v3.oas.annotations.info.Info(title = "Star Bank Recommendation API"
        , version = "1.0.0",
        description = "API for Star Bank Recommendation for Credit Products"))
public class RecommendationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationSystemApplication.class, args);
    }

}
