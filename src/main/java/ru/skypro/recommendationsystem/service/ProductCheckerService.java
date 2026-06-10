package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;

import java.util.UUID;


@Service
public class ProductCheckerService {
    private final RecommendationsRepository recommendationsRepository;

    public ProductCheckerService(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    public boolean hasProductType(UUID userId, String productType) {
        return recommendationsRepository.hasProductType(userId, productType);
    }

    public Double getTotalDepositsByProductType(UUID userId, String productType) {
        return recommendationsRepository.getTotalDepositsByProductType(userId, productType);
    }

    public Double getTotalWithdrawalsByProductType(UUID userId, String productType) {
        return recommendationsRepository.getTotalWithdrawalsByProductType(userId, productType);
    }

}
