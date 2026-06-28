package ru.skypro.recommendationsystem.service.RuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

//Top Saving
//Пользователь использует как минимум один продукт с типом DEBIT.
//Сумма пополнений по всем продуктам типа DEBIT больше или равна 50 000 ₽ ИЛИ Сумма пополнений по всем продуктам типа SAVING больше или равна 50 000 ₽.
//Сумма пополнений по всем продуктам типа DEBIT больше, чем сумма трат по всем продуктам типа DEBIT.
@Component
public class TopSavingRuleImpl implements RecommendationRuleSet {

    private final RecommendationsRepository recommendationsRepository;

    public TopSavingRuleImpl(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public List<RecommendationDTO> checkRecommendation(UUID userId) {
        // Проверяем наличие DEBIT продукта
        if (!recommendationsRepository.hasProductType(userId, "DEBIT")) {
            return Collections.emptyList();
        }

        // Получаем суммы пополнений
        Double debitDeposits = recommendationsRepository.getTotalDepositsByProductType(userId, "DEBIT");
        Double savingDeposits = recommendationsRepository.getTotalDepositsByProductType(userId, "SAVING");

        // Проверяем условие: DEBIT >= 50000 или SAVING >= 50000
        // Если значение null, считаем его равным 0 для логики сравнения (или можно оставить как есть, тогда условие не выполнится)
        boolean debitCondition = (debitDeposits != null && debitDeposits >= 50000);
        boolean savingCondition = (savingDeposits != null && savingDeposits >= 50000);

        if (!debitCondition && !savingCondition) {
            return Collections.emptyList();
        }

        // Проверяем DEBIT deposits > DEBIT withdrawals
        Double debitWithdrawals = recommendationsRepository.getTotalWithdrawalsByProductType(userId, "DEBIT");

        if (debitDeposits == null || debitWithdrawals == null || debitDeposits <= debitWithdrawals) {
            return Collections.emptyList();
        }

        RecommendationDTO dto = new RecommendationDTO(
                UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                "Top Saving",
                "Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, который поможет вам легко и удобно накапливать деньги на важные цели. Больше никаких забытых чеков и потерянных квитанций — всё под контролем!\n" +
                        "Преимущества «Копилки»:\n" +
                        "Накопление средств на конкретные цели. Установите лимит и срок накопления, и банк будет автоматически переводить определенную сумму на ваш счет.\n" +
                        "Прозрачность и контроль. Отслеживайте свои доходы и расходы, контролируйте процесс накопления и корректируйте стратегию при необходимости.\n" +
                        "Безопасность и надежность. Ваши средства находятся под защитой банка, а доступ к ним возможен только через мобильное приложение или интернет-банкинг.\n" +
                        "Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!"
        );

        return Collections.singletonList(dto);
    }
}

