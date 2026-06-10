package ru.skypro.recommendationsystem.service.RuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.service.ProductCheckerService;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;


//Простой кредит
//Пользователь не использует продукты с типом CREDIT.
//Сумма пополнений по всем продуктам типа DEBIT больше, чем сумма трат по всем продуктам типа DEBIT.
//Сумма трат по всем продуктам типа DEBIT больше, чем 100 000 ₽.

@Component
public class UsualCreditRuleImpl implements RecommendationRuleSet {

    private final ProductCheckerService productChecker;

    public UsualCreditRuleImpl(ProductCheckerService productChecker) {
        this.productChecker = productChecker;
    }


    @Override
    public Optional<RecommendationDTO> checkRecommendation(UUID userId) {
        // Проверяем отсутствие CREDIT продуктов
        if (productChecker.hasProductType(userId, "CREDIT")) {
            return Optional.empty();
        }

        // Получаем суммы по DEBIT продуктам
        Double debitDeposits = productChecker.getTotalDepositsByProductType(userId, "DEBIT");
        Double debitWithdrawals = productChecker.getTotalWithdrawalsByProductType(userId, "DEBIT");

        // Проверяем DEBIT deposits > DEBIT withdrawals
        if (debitDeposits == null || debitWithdrawals == null || debitDeposits <= debitWithdrawals) {
            return Optional.empty();
        }

        // Проверяем DEBIT withdrawals > 100000
        if (debitWithdrawals <= 100000) {
            return Optional.empty();
        }

        return Optional.of(new RecommendationDTO(
                UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f"),
                "Usual Credit",
                "Откройте мир выгодных кредитов с нами!" +
                        "Ищете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.\n" +
                        "Почему выбирают нас:" +
                        "Быстрое рассмотрение заявки. Мы ценим ваше время, поэтому процесс рассмотрения заявки занимает всего несколько часов.\n" +
                        "Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.\n" +
                        "Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: покупку недвижимости, автомобиля, образование, лечение и многое другое.\n" +
                        "Не упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!"
        ));
    }
}

