package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA-сущность, представляющая динамическое правило рекомендательной системы.
 * <p>
 * Хранится в таблице {@code dynamic_rules} и описывает логику предложения конкретного продукта
 * пользователю на основе набора условий (запросов), хранящихся в связанной сущности {@link RuleQuery}.
 * </p>
 *
 * <h3>Бизнес-смысл полей:</h3>
 * <ul>
 *     <li>{@code productName} — отображаемое название продукта (например, "Вклад под 15%").</li>
 *     <li>{@code productId} — внутренний идентификатор продукта в каталоге товаров (справочнике).
 *         Позволяет отделить бизнес-логику правил от конкретных продуктов.</li>
 *     <li>{@code productText} — текст рекомендации, который увидит пользователь. Может отличаться
 *         от названия продукта и содержать призыв к действию или условия.</li>
 * </ul>
 *
 * <h3>Особенности реализации JPA:</h3>
 * <dl>
 *     <dt>Связь с условиями ({@link RuleQuery})</dt>
 *         <dd>Реализована через {@code @OneToMany} с параметром {@code orphanRemoval = true}.
 *             Это означает, что при удалении правила из базы данных все связанные с ним условия
 *             также будут автоматически удалены. Инициализация списка в конструкторе предотвращает
 *             появление {@code null}.</dd>
 *
 *     <dt>Управление состоянием "новый/существующий"</dt>
 *         <dd>Класс реализует интерфейс {@link Persistable<UUID>}. Поле {@code isNew} и методы
 *             {@code markNotNew()} (помеченные {@code @PostPersist} и {@code @PostLoad}) позволяют
 *             Spring Data JPA корректно определять, нужно ли выполнять операцию {@code INSERT} или {@code UPDATE},
 *             даже если у сущности есть явный {@code @Id} (UUID), который может быть задан вручную.</dd>
 * </dl>
 *
 * @see RuleQuery
 * @see Persistable
 */
@Entity
@Table(name = "dynamic_rules")
public class DynamicRule implements Persistable<UUID> {

    /**
     * Уникальный идентификатор правила.
     * <p>
     * Генерируется на уровне приложения (UUID v4) или передается извне.
     * Не является автоинкрементным полем БД.
     * </p>
     */
    @Id
    private UUID id;

    /**
     * Флаг, определяющий, является ли сущность новой (не сохраненной в БД).
     * <p>
     * Используется Spring Data JPA для стратегии сохранения. По умолчанию {@code true}.
     * Изменяется автоматически методами {@code markNotNew()}.
     * </p>
     */
    @Transient
    private boolean isNew = true;

    /**
     * Отображаемое название продукта, связанного с правилом.
     * <p>
     * Обязательное поле ({@code nullable = false}). Используется для UI и аналитики.
     * </p>
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Идентификатор продукта в каталоге (справочнике продуктов).
     * <p>
     * Обязательное поле. Позволяет менять правила без изменения структуры каталога продуктов
     * и наоборот.
     * </p>
     */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /**
     * Текст рекомендации для пользователя.
     * <p>
     * Обязательное поле. Содержит финальное сообщение, которое будет показано в интерфейсе.
     * Может содержать форматирование или переменные подстановки.
     * </p>
     */
    @Column(name = "product_text", nullable = false)
    private String productText;

    /**
     * Список условий (запросов), определяющих, когда правило должно сработать.
     * <p>
     * Связь "Один ко многим" с сущностью {@link RuleQuery}.
     * <ul>
     *     <li>{@code cascade = CascadeType.ALL}: Все операции (сохранение, обновление, удаление)
     *         над правилом каскадно применяются к условиям.</li>
     *     <li>{@code orphanRemoval = true}: Если условие удаляется из списка {@code queries},
     *         оно будет удалено из БД.</li>
     *     <li>{@code fetch = FetchType.LAZY}: Загрузка списка происходит только при явном обращении
     *         к геттеру, что экономит ресурсы БД.</li>
     * </ul>
     * Инициализирован пустым списком для предотвращения {@code NullPointerException}.
     * </p>
     */
    @OneToMany(mappedBy = "dynamicRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RuleQuery> queries = new ArrayList<>(); // Инициализируй сразу, чтобы не было null

    /**
     * Реализация метода интерфейса {@link Persistable}.
     * Возвращает текущее состояние сущности.
     *
     * @return {@code true}, если сущность еще не сохранена в БД, иначе {@code false}
     */
    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Метод жизненного цикла JPA.
     * <p>
     * Вызывается автоматически Hibernate:
     * <ul>
     *     <li>После сохранения объекта в БД ({@code @PostPersist}).</li>
     *     <li>При загрузке объекта из БД ({@code @PostLoad}).</li>
     * </ul>
     * Устанавливает флаг {@code isNew = false}, сигнализируя Spring Data, что сущность
     * уже существует в базе данных.
     * </p>
     *
     * <strong>Важно:</strong> Метод должен быть package-private или public, не иметь аргументов
     * и не выбрасывать проверяемые исключения.
     */
    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductText() {
        return productText;
    }

    public void setProductText(String productText) {
        this.productText = productText;
    }

    public List<RuleQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<RuleQuery> queries) {
        this.queries = queries;
    }
}
