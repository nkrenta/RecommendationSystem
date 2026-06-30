package ru.skypro.recommendationsystem.service.dynamicRuleSet;

import org.springframework.stereotype.Component;
import ru.skypro.recommendationsystem.DTO.RecommendationDTO;
import ru.skypro.recommendationsystem.entity.DynamicRule;
import ru.skypro.recommendationsystem.entity.RuleQuery;
import ru.skypro.recommendationsystem.repository.DynamicRuleRepository;
import ru.skypro.recommendationsystem.repository.RecommendationsRepository;
import ru.skypro.recommendationsystem.service.RecommendationRuleSet;
import ru.skypro.recommendationsystem.service.RuleStatsService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация движка правил рекомендаций на основе динамических правил ({@link DynamicRule}).
 * <p>
 * Этот компонент является центральным звеном логики системы: он загружает правила,
 * проверяет их условия против данных пользователя (используя кэшированные метрики)
 * и формирует список рекомендаций.
 * </p>
 *
 * <h3>Порядок выполнения операций в {@link #checkRecommendation(UUID)}:</h3>
 * <ol>
 *     <li><b>Загрузка данных:</b> Получает все активные правила со связанными условиями
 *         через {@link DynamicRuleRepository#findAllWithQueries()}.</li>
 *
 *     <li><b>Фильтрация (Проверка условий):</b> Для каждого правила проверяет все его условия
 *         ({@link RuleQuery}). Если все условия истинны — правило считается сработавшим.</li>
 *
 *     <li><b>Учет статистики:</b> Если правило сработало, немедленно увеличивает счетчик
 *         срабатываний через {@link RuleStatsService#incrementCount(UUID)}.
 *         <strong>Важно:</strong> Статистика увеличивается даже если пользователь в итоге
 *         не увидит рекомендацию (например, из-за лимита выдачи), так как правило <i>"сработало"</i>
 *         логически.</li>
 *
 *     <li><b>Маппинг в DTO:</b> Преобразует сработавшие правила в объекты {@link RecommendationDTO}.</li>
 * </ol>
 *
 * <h3>Критические архитектурные замечания:</h3>
 * <dl>
 *     <dt>Передача аргументов через List&lt;String&gt;</dt>
 *         <dd>В текущей реализации аргументы условия передаются как {@code List<String>}.
 *             Это делает код хрупким: порядок аргументов важен, типы не проверяются компилятором.
 *             В будущем рекомендуется заменить это на строго типизированные DTO для условий.</dd>
 *
 *     <dt>Обработка отрицания (Negation)</dt>
 *         <dd>Логика отрицания реализуется через XOR с флагом {@code query.isNegate()}.
 *             Формула: {@code result = (проверка_условия) ^ negate}.
 *             Пример: Если условие "сумма > 100" ложно (false), а флаг negate=true,
 *             то результат будет true (true ^ true = false? Нет! Смотри реализацию).
 *             <b>Внимание:</b> Реализация в коде использует XOR. Убедитесь, что логика
 *             соответствует бизнес-требованиям. Обычно отрицание делают через NOT.</dd>
 * </dl>
 *
 * @see DynamicRule
 * @see RuleQuery
 * @see RecommendationDTO
 */
@Component
public class DynamicRuleSet implements RecommendationRuleSet {
    private final DynamicRuleRepository dynamicRuleRepository;
    private final RecommendationsRepository recommendationsRepository;
    private final RuleStatsService ruleStatsService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param dynamicRuleRepository     репозиторий для загрузки правил и их условий.
     * @param recommendationsRepository репозиторий для получения агрегированных метрик пользователя
     *                                  (суммы, количества транзакций) с использованием кэширования.
     * @param ruleStatsService          сервис для управления статистикой срабатывания правил.
     */
    public DynamicRuleSet(DynamicRuleRepository dynamicRuleRepository,
                          RecommendationsRepository recommendationsRepository,
                          RuleStatsService ruleStatsService) {
        this.dynamicRuleRepository = dynamicRuleRepository;
        this.recommendationsRepository = recommendationsRepository;
        this.ruleStatsService = ruleStatsService;
    }

    /**
     * Основной метод генерации рекомендаций для пользователя.
     * <p>
     * Возвращает список рекомендаций, сформированный на основе сработавших динамических правил.
     * </p>
     *
     * <h4>Важные нюансы реализации:</h4>
     * <ul>
     *     <li>Использует Stream API для обработки списка правил. Это читаемо, но при очень
     *         большом количестве правил (тысячи) может быть менее производительно, чем цикл for.</li>
     *     <li>Побочные эффекты (обновление статистики) происходят внутри операции фильтрации
     *         {@code .filter(...)}. Это допустимо в данном контексте, так как операция выполняется
     *         в рамках одной транзакции сервиса, но считается нефункциональным стилем программирования.</li>
     * </ul>
     *
     * @param userId идентификатор пользователя, для которого формируются рекомендации
     * @return список объектов {@link RecommendationDTO}, соответствующих сработавшим правилам
     */
    @Override
    public List<RecommendationDTO> checkRecommendation(UUID userId) {
        return dynamicRuleRepository.findAllWithQueries().stream()
                .filter(rule -> {
                    boolean matches = checkRuleForUser(userId, rule);
                    if (matches) {
                        ruleStatsService.incrementCount(rule.getId());
                    }
                    return matches;
                })
                .map(rule -> new RecommendationDTO(
                        rule.getProductId(),
                        rule.getProductName(),
                        rule.getProductText()))
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, выполняются ли все условия ({@link RuleQuery}) для конкретного правила.
     * <p>
     * Правило считается сработавшим только если <b>все</b> его условия истинны.
     * Используется операция {@code allMatch}, которая прекращает проверку при первом ложном результате.
     * </p>
     *
     * @param userId идентификатор пользователя
     * @param rule   проверяемое правило
     * @return {@code true}, если все условия правила выполнены, иначе {@code false}
     */
    private boolean checkRuleForUser(UUID userId, DynamicRule rule) {
        return rule.getQueries().stream()
                .allMatch(query -> checkCondition(userId, query));
    }

    /**
     * Диспетчер проверки условий. Определяет тип условия и вызывает соответствующую логику.
     * <p>
     * Поддерживаемые типы условий (значения поля {@code query}):
     * </p>
     * <table>
     *     <thead><tr><th>Тип</th><th>Описание</th></tr></thead>
     *     <tbody>
     *         <tr><td>USER_OF</td><td>Проверяет наличие транзакций по типу продукта.</td></tr>
     *         <tr><td>ACTIVE_USER_OF</td><td>Проверяет, что количество транзакций >= 5.</td></tr>
     *         <tr><td>TRANSACTION_SUM_COMPARE</td><td>Сравнивает сумму депозитов/снятий с порогом.</td></tr>
     *         <tr><td>TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW</td><td>Сравнивает суммы депозитов и снятий между собой.</td></tr>
     *     </tbody>
     * </table>
     *
     * <p>
     * Результат проверки инвертируется, если установлен флаг {@code query.isNegate()},
     * используя операцию XOR (исключающее ИЛИ).
     * Формула: {@code finalResult = (conditionResult) ^ (negateFlag)}.
     * </p>
     *
     * @param userId идентификатор пользователя
     * @param query  условие правила
     * @return итоговый результат проверки условия (с учетом отрицания)
     */
    private boolean checkCondition(UUID userId, RuleQuery query) {
        return switch (query.getQuery()) {
            case "USER_OF" -> recommendationsRepository.hasProductType(userId, query.getArguments().get(0));
            case "ACTIVE_USER_OF" ->
                    recommendationsRepository.getTransactionCount(userId, query.getArguments().get(0)) >= 5;
            case "TRANSACTION_SUM_COMPARE" -> checkTransactionSum(userId, query);
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> checkDepositWithdrawCompare(userId, query);
            default -> throw new IllegalArgumentException("Unknown query type: " + query.getQuery());
            // Применяем отрицание, если флаг установлен
        } ^ query.isNegate();
    }

    /**
     * Проверка условия типа "TRANSACTION_SUM_COMPARE".
     * <p>
     * Аргументы ожидаются в строгом порядке (через {@code List<String>}):
     * <ol>
     *     <li>{@code productType}: Тип продукта.</li>
     *     <li>{@code transactionType}: "DEPOSIT" или "WITHDRAW".</li>
     *     <li>{@code operator}: Оператор сравнения (">", "<", "=", ">=", "<=").</li>
     *     <li>{@code thresholdValue}: Пороговое значение (число в виде строки).</li>
     * </ol>
     *
     * <strong>Предупреждение:</strong> Использование списка строк для передачи аргументов
     * является источником ошибок. Порядок и типы данных не контролируются компилятором.
     * В будущем следует заменить на DTO (например, {@code SumCompareArgs}).
     * </p>
     *
     * @param userId идентификатор пользователя
     * @param query  объект условия
     * @return результат сравнения суммы с пороговым значением
     */
    private boolean checkTransactionSum(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        double sum = args.get(1).equals("DEPOSIT")
                ? recommendationsRepository.getTotalDepositsByProductType(userId, args.get(0))
                : recommendationsRepository.getTotalWithdrawalsByProductType(userId, args.get(0));

        double compareValue = Double.parseDouble(args.get(3));

        return switch (args.get(2)) {
            case ">" -> sum > compareValue;
            case "<" -> sum < compareValue;
            case "=" -> Math.abs(sum - compareValue) < 0.001;
            case ">=" -> sum >= compareValue;
            case "<=" -> sum <= compareValue;
            default -> throw new IllegalArgumentException("Unknown operator: " + args.get(2));
        };
    }

    /**
     * Проверка условия типа "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW".
     * <p>
     * Сравнивает суммы депозитов и снятий по одному типу продукта.
     * </p>
     *
     * <h4>Формат аргументов:</h4>
     * <ol>
     *     <li>{@code productType}: Тип продукта.</li>
     *     <li>{@code operator}: Оператор сравнения между депозитом и снятием.</li>
     * </ol>
     *
     * @param userId идентификатор пользователя
     * @param query  объект условия
     * @return результат сравнения сумм
     */
    private boolean checkDepositWithdrawCompare(UUID userId, RuleQuery query) {
        List<String> args = query.getArguments();
        double depositSum = recommendationsRepository.getTotalDepositsByProductType(userId, args.get(0));
        double withdrawSum = recommendationsRepository.getTotalWithdrawalsByProductType(userId, args.get(0));

        return switch (args.get(1)) {
            case ">" -> depositSum > withdrawSum;
            case "<" -> depositSum < withdrawSum;
            case "=" -> Math.abs(depositSum - withdrawSum) < 0.001;
            case ">=" -> depositSum >= withdrawSum;
            case "<=" -> depositSum <= withdrawSum;
            default -> throw new IllegalArgumentException("Unknown operator: " + args.get(1));
        };
    }
}

