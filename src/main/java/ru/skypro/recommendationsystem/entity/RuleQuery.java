package ru.skypro.recommendationsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

/**
 * JPA-сущность, описывающая отдельное условие (запрос) внутри динамического правила.
 * <p>
 * Представляет собой атомарное условие, которое может быть проверено над данными пользователя
 * (транзакциями, профилем). Хранится в таблице {@code rule_queries}.
 * </p>
 *
 * <h3>Бизнес-логика работы условия:</h3>
 * <p>
 * Условие состоит из трех частей:
 * </p>
 * <ol>
 *     <li><b>Тип запроса ({@code query})</b>: Определяет вид проверки. Например:
 *         <ul>
 *             <li>{@code "TRANSACTION_AMOUNT_GT"} — сумма транзакции больше значения.</li>
 *             <li>{@code "PRODUCT_CATEGORY_EQ"} — категория продукта равна значению.</li>
 *             <li>{@code "USER_TENURE_MONTHS_LT"} — стаж пользователя меньше значения.</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Аргументы ({@code arguments})</b>: Список строковых значений, необходимых для проверки.
 *         Например, для {@code "TRANSACTION_AMOUNT_GT"} аргументом будет строка {@code "5000"}.
 *         Использование {@code List<String>} позволяет передавать несколько значений для операторов IN/OR.
 *     </li>
 *
 *     <li><b>Инверсия ({@code negate})</b>: Флаг, инвертирующий результат проверки.
 *         Если условие {@code "isPremiumUser"} истинно, но {@code negate = true},
 *         то для срабатывания правила пользователь должен <b>не</b> быть премиум-клиентом.
 *     </li>
 * </ol>
 *
 * <h3>Особенности реализации JPA:</h3>
 * <dl>
 *     <dt>Хранение аргументов ({@code ElementCollection})</dt>
 *         <dd>Аргументы хранятся в отдельной таблице {@code rule_query_arguments} со связью
 *             по внешнему ключу {@code query_id}. Это позволяет иметь переменное количество
 *             аргументов для одного условия без создания лишней сущности-обертки.</dd>
 *
 *     <dt>Связь с правилом ({@code ManyToOne})</dt>
 *         <dd>Обратная сторона связи {@code @OneToMany} из {@link DynamicRule}.
 *             Помечена как {@code @JsonIgnore}, чтобы избежать циклической сериализации
 *             при преобразовании в JSON (так как правило уже содержит список запросов).</dd>
 *
 *     <dt>Генерация ID</dt>
 *         <dd>Идентификатор генерируется автоматически на уровне БД через стратегию
 *             {@code GenerationType.UUID}.</dd>
 * </dl>
 *
 * @see DynamicRule
 */
@Entity
@Table(name = "rule_queries")
public class RuleQuery {

    /**
     * Уникальный идентификатор условия.
     * <p>
     * Генерируется базой данных автоматически. Не подлежит изменению.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Тип условия (операция проверки).
     * <p>
     * Содержит код операции (например, {@code "AMOUNT_GREATER_THAN"}).
     * Значение должно соответствовать enum или конфигурации движка правил,
     * который будет интерпретировать эту строку во время выполнения.
     * </p>
     */
    @Column(name = "query_type", nullable = false)
    private String query;

    /**
     * Список аргументов для выполнения условия.
     * <p>
     * Хранится в отдельной таблице через {@code @ElementCollection}.
     * Загрузка происходит eagerly ({@code FetchType.EAGER}), так как аргументы
     * необходимы для немедленной валидации правила и редко бывают большими.
     * </p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rule_query_arguments", joinColumns = @JoinColumn(name = "query_id"))
    @Column(name = "argument")
    private List<String> arguments;

    /**
     * Флаг инверсии условия.
     * <p>
     * Если {@code true}, результат проверки условия инвертируется.
     * Пример: условие "возраст > 18" при {@code negate=true} превращается в "возраст <= 18".
     * Это позволяет описывать исключения и негативные сценарии без дублирования логики.
     * </p>
     */
    @Column(name = "negate", nullable = false)
    private boolean negate;

    /**
     * Ссылка на родительское правило.
     * <p>
     * Обратная сторона связи "Один ко многим" с {@link DynamicRule}.
     * Помечена аннотацией {@code @JsonIgnore} для предотвращения бесконечной рекурсии
     * при сериализации объекта в JSON.
     * </p>
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private DynamicRule dynamicRule;

    public DynamicRule getDynamicRule() {
        return dynamicRule;
    }

    public void setDynamicRule(DynamicRule dynamicRule) {
        this.dynamicRule = dynamicRule;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }
}
