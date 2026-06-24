package ru.skypro.recommendationsystem.service.RuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


//Простой кредит
//Пользователь не использует продукты с типом CREDIT.
//Сумма пополнений по всем продуктам типа DEBIT больше, чем сумма трат по всем продуктам типа DEBIT.
//Сумма трат по всем продуктам типа DEBIT больше, чем 100 000 ₽.

@Component
public class UsualCreditRuleImpl implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;

    public UsualCreditRuleImpl(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public List<RecommendationDTO> checkRecommendation(UUID userId) {
        // Проверяем отсутствие CREDIT продуктов
        if (recommendationsRepository.hasProductType(userId, "CREDIT")) {
            return Collections.emptyList();
        }

        // Получаем суммы по DEBIT продуктам
        Double debitDeposits = recommendationsRepository.getTotalDepositsByProductType(userId, "DEBIT");
        Double debitWithdrawals = recommendationsRepository.getTotalWithdrawalsByProductType(userId, "DEBIT");

        // Проверка на null важна, чтобы избежать NPE при сравнении
        if (debitDeposits == null || debitWithdrawals == null) {
            return Collections.emptyList();
        }

        // Проверяем DEBIT deposits > DEBIT withdrawals
        if (debitDeposits <= debitWithdrawals) {
            return Collections.emptyList();
        }

        // Проверяем DEBIT withdrawals > 100000
        if (debitWithdrawals <= 100000) {
            return Collections.emptyList();
        }

        RecommendationDTO dto = new RecommendationDTO(
                UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f"),
                "Usual Credit",
                "Откройте мир выгодных кредитов с нами!\n" +
                        "Ищете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.\n" +
                        "Почему выбирают нас:\n" +
                        "Быстрое рассмотрение заявки. Мы ценим ваше время, поэтому процесс рассмотрения заявки занимает всего несколько часов.\n" +
                        "Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.\n" +
                        "Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: покупку недвижимости, автомобиля, образование, лечение и многое другое.\n" +
                        "Не упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!"
        );

        return Collections.singletonList(dto);
    }
}

