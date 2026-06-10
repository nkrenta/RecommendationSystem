package ru.skypro.recommendationsystem.service.RuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;


import java.util.Optional;
import java.util.UUID;

//INVEST 500
//Пользователь использует как минимум один продукт с типом DEBIT.
//Пользователь не использует продукты с типом INVEST.
//Сумма пополнений продуктов с типом SAVING больше 1000 ₽.

@Component
public class Invest500RuleImpl implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;


    public Invest500RuleImpl(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }


    @Override
    public Optional<RecommendationDTO> checkRecommendation(UUID userId) {
        // Проверяем наличие DEBIT продукта
        if (!recommendationsRepository.hasProductType(userId, "DEBIT")) {
            return Optional.empty();
        }

        // Проверяем отсутствие INVEST продуктов
        if (recommendationsRepository.hasProductType(userId, "INVEST")) {
            return Optional.empty();
        }

        // Проверяем суммарные пополнения SAVING > 1000
        Double savingDeposits = recommendationsRepository.getTotalDepositsByProductType(userId, "SAVING");
        if (savingDeposits == null || savingDeposits <= 1000) {
            return Optional.empty();
        }

        return Optional.of(new RecommendationDTO(
                UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                "INVEST500",
                "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! Воспользуйтесь налоговыми льготами и начните инвестировать с умом. " +
                        "Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. " +
                        "Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. " +
                        "Откройте ИИС сегодня и станьте ближе к финансовой независимости!"
        ));
    }
}

