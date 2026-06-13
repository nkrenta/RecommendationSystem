package ru.skypro.recommendationsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.recommendationsystem.DTO.RecommendationResponse;
import ru.skypro.recommendationsystem.service.ProductCheckerService;
import ru.skypro.recommendationsystem.service.RecommendationService;

import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

  private final ProductCheckerService productCheckerService;
  private final RecommendationService recommendationService;

    public RecommendationController(ProductCheckerService productCheckerService, RecommendationService recommendationService) {
        this.productCheckerService = productCheckerService;
        this.recommendationService = recommendationService;
    }


    //проверка наличия продукта у пользователя
    @GetMapping("/checkUserHasProductType/{userId}/{productType}")
    public ResponseEntity<Boolean> hasProductType(@PathVariable UUID userId,@PathVariable String productType) {
         boolean result = productCheckerService.hasProductType(userId,productType);
         return ResponseEntity.ok(result);
    }

    //Получить сумму пополнения по типу продукта
    @GetMapping("/getTotalDepositsByProductType/{userId}/{productType}")
    public ResponseEntity<Double> getTotalDepositsByProductType (@PathVariable UUID userId,@PathVariable String productType){
        Double result = productCheckerService.getTotalDepositsByProductType(userId,productType);
        return ResponseEntity.ok(result);
    }

    //Получить сумму снятия по типу продукта
    @GetMapping("/getTotalWithdrawalsByProductType/{userId}/{productType}")
    public ResponseEntity<Double> getTotalWithdrawalsByProductType (@PathVariable UUID userId,@PathVariable String productType){
        Double result = productCheckerService.getTotalWithdrawalsByProductType(userId,productType);
        return ResponseEntity.ok(result);
    }

    //Получить рекомендации на основе условий
    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable UUID userId) {
        RecommendationResponse response = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(response);
    }
}

