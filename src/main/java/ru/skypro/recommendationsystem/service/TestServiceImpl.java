package ru.skypro.recommendationsystem.service;

import org.springframework.stereotype.Service;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;

import java.util.UUID;

@Service
public class TestServiceImpl implements TestService {

    private final RecommendationsRepository repository;

    public TestServiceImpl(RecommendationsRepository repository) {
        this.repository = repository;
    }


    @Override
    public Integer test(UUID userId) {
        return repository.getRandomTransactionAmount(userId);
    }
}
